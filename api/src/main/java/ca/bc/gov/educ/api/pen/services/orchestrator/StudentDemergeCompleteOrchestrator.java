package ca.bc.gov.educ.api.pen.services.orchestrator;

import ca.bc.gov.educ.api.pen.services.constants.*;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.model.SagaEventStates;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
import ca.bc.gov.educ.api.pen.services.util.CodeUtil;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.pen.services.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.services.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.PEN_SERVICES_STUDENT_DEMERGE_COMPLETE_SAGA;
import static ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.*;

/**
 * The type Demerge students orchestrator
 */
@Component
@Slf4j
public class StudentDemergeCompleteOrchestrator extends BaseUserActionsOrchestrator<StudentDemergeCompleteSagaData> {
  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService      the saga service
   * @param messagePublisher the message publisher
   */
  public StudentDemergeCompleteOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher) {
    super(sagaService, messagePublisher, StudentDemergeCompleteSagaData.class, PEN_SERVICES_STUDENT_DEMERGE_COMPLETE_SAGA.toString(), PEN_SERVICES_DEMERGE_STUDENTS_SAGA_TOPIC.toString());
  }

  /**
   * Populate steps to execute map.
   */
  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
        .begin(GET_STUDENT_HISTORY, this::readAuditHistory)
        .step(GET_STUDENT_HISTORY, STUDENT_HISTORY_FOUND, GET_STUDENT, this::getStudentByMergedFromPen)
        .step(GET_STUDENT, STUDENT_FOUND, this::isStepForMergedFromPEN, UPDATE_STUDENT, this::updateMergedFromStudent)
        .step(UPDATE_STUDENT, STUDENT_UPDATED, this::isStepForMergedFromPEN, DELETE_MERGE, this::deleteMerge)
        .step(DELETE_MERGE, MERGE_DELETED, GET_STUDENT, this::getStudentByMergedToPen)
        .step(GET_STUDENT, STUDENT_FOUND, this::isStepForMergedToPEN, UPDATE_STUDENT, this::updateMergedToStudent)
        .step(UPDATE_STUDENT, STUDENT_UPDATED, this::isStepForMergedToPEN, ADD_POSSIBLE_MATCH, this::createPossibleMatches)
        .step(ADD_POSSIBLE_MATCH, POSSIBLE_MATCH_ADDED, MARK_SAGA_COMPLETE, this::markSagaComplete);
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param studentDemergeCompleteSagaData the student demerge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void readAuditHistory(final Event event, final Saga saga, final StudentDemergeCompleteSagaData studentDemergeCompleteSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(GET_STUDENT_HISTORY.toString()); // set current event as saga state.
    studentDemergeCompleteSagaData.setRequestStudentID(studentDemergeCompleteSagaData.getMergedFromStudentID());
    saga.setPayload(JsonUtil.getJsonStringFromObject(studentDemergeCompleteSagaData));
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(GET_STUDENT_HISTORY)
        .replyTo(this.getTopicToSubscribe())
        .eventPayload(studentDemergeCompleteSagaData.getMergedFromStudentID())
        .build();
    this.postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for GET_STUDENT_HISTORY Event.");
  }

  /**
   * Gets student by merged from pen.
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param studentDemergeCompleteSagaData the student demerge complete saga data
   * @throws IOException the io exception
   */
  protected void getStudentByMergedFromPen(final Event event, final Saga saga, final StudentDemergeCompleteSagaData studentDemergeCompleteSagaData) throws IOException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());

    // Retrieve history
    final StudentHistory studentHistory;
    final ObjectMapper objectMapper = new ObjectMapper();
    final JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, StudentHistory.class);
    final List<StudentHistory> historyList = objectMapper.readValue(event.getEventPayload(), type);

    // Find the demog from the last merge history
    final List<StudentHistory> merges = historyList.stream().filter(h -> (StringUtils.equals(h.getHistoryActivityCode(), StudentHistoryActivityCodes.MERGE.getCode()))
    ).sorted(Comparator.comparing(StudentHistory::getUpdateDate)).collect(Collectors.toList());
    if (!merges.isEmpty()) {
      studentHistory = merges.get(merges.size() - 1);
    } else {
      studentHistory = historyList.get(historyList.size() - 1);
    }

    // Populate the demog
    studentDemergeCompleteSagaData.setLegalLastName(studentHistory.getLegalLastName());
    studentDemergeCompleteSagaData.setLegalFirstName(studentHistory.getLegalFirstName());
    studentDemergeCompleteSagaData.setLegalMiddleNames(studentHistory.getLegalMiddleNames());
    studentDemergeCompleteSagaData.setUsualLastName(studentHistory.getUsualLastName());
    studentDemergeCompleteSagaData.setUsualFirstName(studentHistory.getUsualFirstName());
    studentDemergeCompleteSagaData.setUsualMiddleNames(studentHistory.getUsualMiddleNames());
    studentDemergeCompleteSagaData.setGenderCode(studentHistory.getGenderCode());
    studentDemergeCompleteSagaData.setDob(studentHistory.getDob());
    studentDemergeCompleteSagaData.setGradeCode(studentHistory.getGradeCode());
    studentDemergeCompleteSagaData.setMincode(studentHistory.getMincode());
    studentDemergeCompleteSagaData.setLocalID(studentHistory.getLocalID());
    studentDemergeCompleteSagaData.setPostalCode(studentHistory.getPostalCode());
    studentDemergeCompleteSagaData.setRequestStudentID(studentDemergeCompleteSagaData.getMergedFromStudentID());

    this.processStudentRead(saga, studentDemergeCompleteSagaData, eventStates, studentDemergeCompleteSagaData.getMergedFromPen());
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param studentDemergeCompleteSagaData the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void updateMergedFromStudent(final Event event, final Saga saga, final StudentDemergeCompleteSagaData studentDemergeCompleteSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());

    final StudentSagaData studentDataFromEventResponse = JsonUtil.getJsonObjectFromString(StudentSagaData.class, event.getEventPayload());
    // Populate student with the demog from the last merge history
    studentDataFromEventResponse.setLegalLastName(studentDemergeCompleteSagaData.getLegalLastName());
    studentDataFromEventResponse.setLegalFirstName(studentDemergeCompleteSagaData.getLegalFirstName());
    studentDataFromEventResponse.setLegalMiddleNames(studentDemergeCompleteSagaData.getLegalMiddleNames());
    studentDataFromEventResponse.setUsualLastName(studentDemergeCompleteSagaData.getUsualLastName());
    studentDataFromEventResponse.setUsualFirstName(studentDemergeCompleteSagaData.getUsualFirstName());
    studentDataFromEventResponse.setUsualMiddleNames(studentDemergeCompleteSagaData.getUsualMiddleNames());
    studentDataFromEventResponse.setDob(studentDemergeCompleteSagaData.getDob());
    studentDataFromEventResponse.setGenderCode(studentDemergeCompleteSagaData.getGenderCode());
    studentDataFromEventResponse.setGradeCode(studentDemergeCompleteSagaData.getGradeCode());
    studentDataFromEventResponse.setSexCode(CodeUtil.getSexCodeFromGenderCode(studentDemergeCompleteSagaData.getGenderCode()));
    studentDataFromEventResponse.setMincode(studentDemergeCompleteSagaData.getMincode());
    studentDataFromEventResponse.setLocalID(studentDemergeCompleteSagaData.getLocalID());
    studentDataFromEventResponse.setPostalCode(studentDemergeCompleteSagaData.getPostalCode());

    // Student update for demerge
    studentDataFromEventResponse.setStatusCode(StudentStatusCodes.ACTIVE.getCode());
    studentDataFromEventResponse.setTrueStudentID(null);
    studentDataFromEventResponse.setHistoryActivityCode(StudentHistoryActivityCodes.DEMERGE.getCode());

    this.processStudentUpdate(saga, studentDemergeCompleteSagaData, eventStates, studentDataFromEventResponse);
  }

  /**
   * Gets student by merged to pen.
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param studentDemergeCompleteSagaData the student demerge complete saga data
   * @throws IOException the io exception
   */
  protected void getStudentByMergedToPen(final Event event, final Saga saga, final StudentDemergeCompleteSagaData studentDemergeCompleteSagaData) throws IOException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    studentDemergeCompleteSagaData.setRequestStudentID(studentDemergeCompleteSagaData.getMergedToStudentID());

    this.processStudentRead(saga, studentDemergeCompleteSagaData, eventStates, studentDemergeCompleteSagaData.getMergedToPen());
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * mincode
   * Local ID
   * Student Grade Code
   * Postal Code
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param studentDemergeCompleteSagaData the student demerge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void updateMergedToStudent(final Event event, final Saga saga, final StudentDemergeCompleteSagaData studentDemergeCompleteSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());

    final StudentSagaData studentDataFromEventResponse = JsonUtil.getJsonObjectFromString(StudentSagaData.class, event.getEventPayload());
    studentDataFromEventResponse.setHistoryActivityCode(StudentHistoryActivityCodes.DEMERGE.getCode());

    this.processStudentUpdate(saga, studentDemergeCompleteSagaData, eventStates, studentDataFromEventResponse);
  }


  /**
   * the following attributes on the student merge record get deleted based on the incoming Merge request
   * status
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param studentDemergeCompleteSagaData the student demerge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void deleteMerge(final Event event, final Saga saga, final StudentDemergeCompleteSagaData studentDemergeCompleteSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(DELETE_MERGE.toString()); // set current event as saga state.

    final StudentMerge studentMerge = STUDENT_MERGE_COMPLETE_SAGA_DATA_MAPPER.toStudentMerge(studentDemergeCompleteSagaData);
    studentMerge.setStudentMergeDirectionCode(StudentMergeDirectionCodes.FROM.getCode());
    studentMerge.setStudentMergeSourceCode(StudentMergeSourceCodes.MI.getCode());
    studentDemergeCompleteSagaData.setRequestStudentID(studentDemergeCompleteSagaData.getMergedFromStudentID());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(DELETE_MERGE)
        .replyTo(this.getTopicToSubscribe())
        .eventPayload(JsonUtil.getJsonStringFromObject(studentMerge))
        .build();
    this.postMessageToTopic(PEN_SERVICES_API_TOPIC.toString(), nextEvent);
    log.info("message sent to PEN_SERVICES_API_TOPIC for DELETE_MERGE Event.");
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param studentDemergeCompleteSagaData the student demerge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void createPossibleMatches(final Event event, final Saga saga, final StudentDemergeCompleteSagaData studentDemergeCompleteSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(ADD_POSSIBLE_MATCH.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final PossibleMatch possibleMatch = new PossibleMatch();
    possibleMatch.setStudentID(studentDemergeCompleteSagaData.getMergedToStudentID());
    possibleMatch.setMatchedStudentID(studentDemergeCompleteSagaData.getMergedFromStudentID());
    possibleMatch.setMatchReasonCode(MatchReasonCodes.DEMERGE);
    possibleMatch.setCreateUser(studentDemergeCompleteSagaData.getCreateUser());
    possibleMatch.setUpdateUser(studentDemergeCompleteSagaData.getUpdateUser());

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(ADD_POSSIBLE_MATCH)
        .replyTo(this.getTopicToSubscribe())
        .eventPayload(JsonUtil.getJsonStringFromObject(Collections.singletonList(possibleMatch)))
        .build();
    this.postMessageToTopic(PEN_MATCH_API_TOPIC.toString(), nextEvent);
    log.info("message sent to PEN_MATCH_API_TOPIC for CREATE_POSSIBLE_MATCH Event.");
  }

  /**
   * Is step for merged to pen boolean.
   *
   * @param studentDemergeCompleteSagaData the student demerge complete saga data
   * @return the boolean
   */
  private boolean isStepForMergedToPEN(final StudentDemergeCompleteSagaData studentDemergeCompleteSagaData) {
    return studentDemergeCompleteSagaData.getRequestStudentID().equals(studentDemergeCompleteSagaData.getMergedToStudentID());
  }

  /**
   * Is step for merged from pen boolean.
   *
   * @param studentDemergeCompleteSagaData the student demerge complete saga data
   * @return the boolean
   */
  private boolean isStepForMergedFromPEN(final StudentDemergeCompleteSagaData studentDemergeCompleteSagaData) {
    return studentDemergeCompleteSagaData.getRequestStudentID().equals(studentDemergeCompleteSagaData.getMergedFromStudentID());
  }
}
