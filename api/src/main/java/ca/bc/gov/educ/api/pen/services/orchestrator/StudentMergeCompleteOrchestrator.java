package ca.bc.gov.educ.api.pen.services.orchestrator;

import ca.bc.gov.educ.api.pen.services.constants.*;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.model.SagaEventStates;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.Student;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
import ca.bc.gov.educ.api.pen.services.util.CodeUtil;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.pen.services.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.services.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.PEN_SERVICES_STUDENT_MERGE_COMPLETE_SAGA;
import static ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.*;

/**
 * The type Merge students orchestrator
 */
@Component
@Slf4j
public class StudentMergeCompleteOrchestrator extends BaseUserActionsOrchestrator<StudentMergeCompleteSagaData> {
  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService      the saga service
   * @param messagePublisher the message publisher
   */
  public StudentMergeCompleteOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher) {
    super(sagaService, messagePublisher, StudentMergeCompleteSagaData.class, PEN_SERVICES_STUDENT_MERGE_COMPLETE_SAGA.toString(), PEN_SERVICES_MERGE_STUDENTS_SAGA_TOPIC.toString());
  }

  /**
   * Populate steps to execute map.
   */
  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
        .begin(GET_STUDENT, this::getStudentByMergedToPen)
        .step(GET_STUDENT, STUDENT_FOUND, this::isStepForMergedToPEN, UPDATE_STUDENT, this::updateMergedToStudent)
        .step(UPDATE_STUDENT, STUDENT_UPDATED, this::isStepForMergedToPEN, CREATE_MERGE, this::createMerge)
        .step(CREATE_MERGE, MERGE_CREATED, GET_STUDENT_HISTORY, this::readAuditHistory)
        .step(GET_STUDENT_HISTORY, STUDENT_HISTORY_FOUND, CREATE_STUDENT_HISTORY, this::addAuditHistory)
        .step(CREATE_STUDENT_HISTORY, STUDENT_HISTORY_CREATED, GET_STUDENT, this::getStudentByMergedFromPen)
        .step(GET_STUDENT, STUDENT_FOUND, this::isStepForMergedFromPEN, UPDATE_STUDENT, this::updateMergedFromStudent)
        .step(UPDATE_STUDENT, STUDENT_UPDATED, this::isStepForMergedFromPEN, GET_POSSIBLE_MATCH, this::readPossibleMatches)
        .step(GET_POSSIBLE_MATCH, POSSIBLE_MATCH_FOUND, DELETE_POSSIBLE_MATCH, this::deletePossibleMatches)
        .step(GET_POSSIBLE_MATCH, POSSIBLE_MATCH_NOT_FOUND, MARK_SAGA_COMPLETE, this::markSagaComplete)
        .step(DELETE_POSSIBLE_MATCH, POSSIBLE_MATCH_DELETED, MARK_SAGA_COMPLETE, this::markSagaComplete);
  }

  /**
   * Gets student by merged to pen.
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param studentMergeCompleteSagaData the student merge complete saga data
   * @throws IOException the io exception
   */
  protected void getStudentByMergedToPen(final Event event, final Saga saga, final StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws IOException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    studentMergeCompleteSagaData.setRequestStudentID(studentMergeCompleteSagaData.getStudentID());

    this.processStudentRead(saga, studentMergeCompleteSagaData, eventStates, studentMergeCompleteSagaData.getMergedToPen());
  }

  /**
   * Gets student by merged from pen.
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param studentMergeCompleteSagaData the student merge complete saga data
   * @throws IOException the io exception
   */
  protected void getStudentByMergedFromPen(final Event event, final Saga saga, final StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws IOException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    studentMergeCompleteSagaData.setRequestStudentID(studentMergeCompleteSagaData.getMergeStudentID());

    this.processStudentRead(saga, studentMergeCompleteSagaData, eventStates, studentMergeCompleteSagaData.getMergedFromPen());
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * mincode
   * Local ID
   * Student Grade Code
   * Postal Code
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param studentMergeCompleteSagaData the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void updateMergedToStudent(final Event event, final Saga saga, final StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws JsonProcessingException, InterruptedException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    final StudentSagaData studentDataFromEventResponse = JsonUtil.getJsonObjectFromString(StudentSagaData.class, event.getEventPayload());
    val getMergedFromPenEvent = JsonUtil.getJsonString(this.buildGetStudentByPenEvent(saga.getSagaId(), studentMergeCompleteSagaData.getMergedFromPen()));
    if (getMergedFromPenEvent.isPresent()) {
      val mergedFromStudent = this.getMessagePublisher().requestMessage(STUDENT_API_TOPIC.toString(), getMergedFromPenEvent.get().getBytes());
      if (mergedFromStudent.isPresent()) {
        final Student mergedFromStudentFromStudentAPI = JsonUtil.getJsonObjectFromString(Student.class, mergedFromStudent.get());
        this.updateMergeToStudentDemographics(studentMergeCompleteSagaData, studentDataFromEventResponse, mergedFromStudentFromStudentAPI);
        this.processStudentUpdate(saga, studentMergeCompleteSagaData, eventStates, studentDataFromEventResponse);
      } else {
        log.error("Either NATS timed out or student from student api was returned null for PEN :: {}, saga ID :: {}", studentMergeCompleteSagaData.getMergedFromPen(), saga.getSagaId());
      }
    } else {
      log.error("This should not have happened, saga ID :: {}", saga.getSagaId());
    }

  }

  /**
   * -------------
   * 07 April 2021
   * As part of this https://gww.wiki.educ.gov.bc.ca/display/PEN/Manage+%27Confirmed%27+and+Merged+Students
   * If 'Merged From' PEN is a confirmed student and 'merged to' isn't, then resulting 'Merged to' or TRUE PEN adopts Confirmed status
   * -------------
   *
   * @param studentMergeCompleteSagaData    the saga payload.
   * @param studentDataFromEventResponse    the student from student-api get call
   * @param mergedFromStudentFromStudentAPI the merged From Student from Student-api get call.
   */
  private void updateMergeToStudentDemographics(final StudentMergeCompleteSagaData studentMergeCompleteSagaData, final StudentSagaData studentDataFromEventResponse, final Student mergedFromStudentFromStudentAPI) {
    studentDataFromEventResponse.setLegalFirstName(studentMergeCompleteSagaData.getLegalFirstName());
    studentDataFromEventResponse.setLegalLastName(studentMergeCompleteSagaData.getLegalLastName());
    studentDataFromEventResponse.setLegalMiddleNames(studentMergeCompleteSagaData.getLegalMiddleNames());
    studentDataFromEventResponse.setUsualFirstName(studentMergeCompleteSagaData.getUsualFirstName());
    studentDataFromEventResponse.setUsualLastName(studentMergeCompleteSagaData.getUsualLastName());
    studentDataFromEventResponse.setUsualMiddleNames(studentMergeCompleteSagaData.getUsualMiddleNames());
    studentDataFromEventResponse.setDob(studentMergeCompleteSagaData.getDob());
    studentDataFromEventResponse.setGenderCode(studentMergeCompleteSagaData.getGenderCode());
    studentDataFromEventResponse.setSexCode(CodeUtil.getSexCodeFromGenderCode(studentMergeCompleteSagaData.getGenderCode()));
    studentDataFromEventResponse.setMincode(studentMergeCompleteSagaData.getMincode());
    studentDataFromEventResponse.setLocalID(studentMergeCompleteSagaData.getLocalID());
    studentDataFromEventResponse.setPostalCode(studentMergeCompleteSagaData.getPostalCode());
    studentDataFromEventResponse.setMemo(studentMergeCompleteSagaData.getMemo());
    studentDataFromEventResponse.setHistoryActivityCode(StudentHistoryActivityCodes.MERGE.getCode());
    if (StudentDemogCodes.CONFIRMED.getCode().equalsIgnoreCase(mergedFromStudentFromStudentAPI.getDemogCode())) {
      studentDataFromEventResponse.setDemogCode(StudentDemogCodes.CONFIRMED.getCode());
    }
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param studentMergeCompleteSagaData the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void updateMergedFromStudent(final Event event, final Saga saga, final StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());

    final StudentSagaData studentDataFromEventResponse = JsonUtil.getJsonObjectFromString(StudentSagaData.class, event.getEventPayload());
    studentDataFromEventResponse.setStatusCode(StudentStatusCodes.MERGE.getCode());
    studentDataFromEventResponse.setTrueStudentID(studentMergeCompleteSagaData.getStudentID());
    studentDataFromEventResponse.setHistoryActivityCode(StudentHistoryActivityCodes.MERGE.getCode());

    this.processStudentUpdate(saga, studentMergeCompleteSagaData, eventStates, studentDataFromEventResponse);
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param studentMergeCompleteSagaData the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void createMerge(final Event event, final Saga saga, final StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(CREATE_MERGE.toString()); // set current event as saga state.

    final StudentMerge studentMerge = STUDENT_MERGE_COMPLETE_SAGA_DATA_MAPPER.toStudentMerge(studentMergeCompleteSagaData);
    studentMerge.setStudentMergeDirectionCode(StudentMergeDirectionCodes.FROM.getCode());
    studentMerge.setStudentMergeSourceCode(StudentMergeSourceCodes.MI.getCode());
    studentMerge.setCreateUser(studentMergeCompleteSagaData.getCreateUser());
    studentMerge.setUpdateUser(studentMergeCompleteSagaData.getUpdateUser());
    studentMergeCompleteSagaData.setRequestStudentID(studentMergeCompleteSagaData.getMergeStudentID());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(CREATE_MERGE)
        .replyTo(this.getTopicToSubscribe())
        .eventPayload(JsonUtil.getJsonStringFromObject(studentMerge))
        .build();
    this.postMessageToTopic(PEN_SERVICES_API_TOPIC.toString(), nextEvent);
    log.info("message sent to PEN_SERVICES_API_TOPIC for CREATE_MERGE Event.");
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param studentMergeCompleteSagaData the student merge saga data
   */
  protected void readAuditHistory(final Event event, final Saga saga, final StudentMergeCompleteSagaData studentMergeCompleteSagaData) {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(GET_STUDENT_HISTORY.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(GET_STUDENT_HISTORY)
        .replyTo(this.getTopicToSubscribe())
        .eventPayload(studentMergeCompleteSagaData.getMergeStudentID())
        .build();
    this.postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for GET_STUDENT_HISTORY Event.");
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   * --------
   * 07 April 2021
   * Method updated to include https://gww.jira.educ.gov.bc.ca/browse/PEN-1286
   * The history import needs to take the merge to student PEN when bringing over all the history
   * --------
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param studentMergeCompleteSagaData the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void addAuditHistory(final Event event, final Saga saga, final StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(CREATE_STUDENT_HISTORY.toString()); // set current event as saga state.

    final ObjectMapper objectMapper = new ObjectMapper();
    final JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, StudentHistory.class);
    final List<StudentHistory> historyList = objectMapper.readValue(event.getEventPayload(), type);
    historyList.forEach(h -> {
      h.setStudentHistoryID(null);
      h.setStudentID(studentMergeCompleteSagaData.getStudentID());
      h.setPen(studentMergeCompleteSagaData.getMergedToPen());
    });
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(CREATE_STUDENT_HISTORY)
        .replyTo(this.getTopicToSubscribe())
        .eventPayload(JsonUtil.getJsonStringFromObject(historyList))
        .build();
    this.postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for CREATE_STUDENT_HISTORY Event.");
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param studentMergeCompleteSagaData the student merge saga data
   */
  protected void readPossibleMatches(final Event event, final Saga saga, final StudentMergeCompleteSagaData studentMergeCompleteSagaData) {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(GET_POSSIBLE_MATCH.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(GET_POSSIBLE_MATCH)
        .replyTo(this.getTopicToSubscribe())
        .eventPayload(studentMergeCompleteSagaData.getStudentID())
        .build();
    this.postMessageToTopic(PEN_MATCH_API_TOPIC.toString(), nextEvent);
    log.info("message sent to PEN_MATCH_API_TOPIC for GET_POSSIBLE_MATCH Event.");
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param studentMergeCompleteSagaData the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void deletePossibleMatches(final Event event, final Saga saga, final StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(DELETE_POSSIBLE_MATCH.toString()); // set current event as saga state.

    final ObjectMapper objectMapper = new ObjectMapper();
    final JavaType type = objectMapper.getTypeFactory().
        constructCollectionType(List.class, PossibleMatch.class);
    final List<PossibleMatch> allPossibleMatches = objectMapper.readValue(event.getEventPayload(), type);
    final List<PossibleMatch> possibleMatches = allPossibleMatches.stream()
        .filter(item -> item.getMatchedStudentID().equals(studentMergeCompleteSagaData.getMergeStudentID()))
        .collect(Collectors.toList());

    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(DELETE_POSSIBLE_MATCH)
        .replyTo(this.getTopicToSubscribe())
        .eventPayload(JsonUtil.getJsonStringFromObject(possibleMatches))
        .build();
    this.postMessageToTopic(PEN_MATCH_API_TOPIC.toString(), nextEvent);
    log.info("message sent to PEN_MATCH_API_TOPIC for DELETE_POSSIBLE_MATCH Event.");
  }

  /**
   * Is step for merged to pen boolean.
   *
   * @param studentMergeCompleteSagaData the student merge complete saga data
   * @return the boolean
   */
  private boolean isStepForMergedToPEN(final StudentMergeCompleteSagaData studentMergeCompleteSagaData) {
    return studentMergeCompleteSagaData.getRequestStudentID().equals(studentMergeCompleteSagaData.getStudentID());
  }

  /**
   * Is step for merged from pen boolean.
   *
   * @param studentMergeCompleteSagaData the student merge complete saga data
   * @return the boolean
   */
  private boolean isStepForMergedFromPEN(final StudentMergeCompleteSagaData studentMergeCompleteSagaData) {
    return studentMergeCompleteSagaData.getRequestStudentID().equals(studentMergeCompleteSagaData.getMergeStudentID());
  }
}
