package ca.bc.gov.educ.api.pen.services.orchestrator;

import ca.bc.gov.educ.api.pen.services.constants.StudentHistoryActivityCodes;
import ca.bc.gov.educ.api.pen.services.constants.StudentMergeDirectionCodes;
import ca.bc.gov.educ.api.pen.services.constants.StudentMergeSourceCodes;
import ca.bc.gov.educ.api.pen.services.constants.StudentStatusCodes;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.pen.services.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.services.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.STUDENT_MERGE_COMPLETE_SAGA;
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
  public StudentMergeCompleteOrchestrator(SagaService sagaService, MessagePublisher messagePublisher) {
    super(sagaService, messagePublisher, StudentMergeCompleteSagaData.class, STUDENT_MERGE_COMPLETE_SAGA.toString(), MERGE_STUDENTS_SAGA_TOPIC.toString());
  }

  /**
   * Populate steps to execute map.
   */
  @Override
  public void populateStepsToExecuteMap() {
    stepBuilder()
            .begin(GET_STUDENT, this::getStudentByMergedToPen)
            .step(GET_STUDENT, STUDENT_FOUND, this::isStepForMergedToPEN, UPDATE_STUDENT, this::updateMergedToStudent)
            .step(UPDATE_STUDENT, STUDENT_UPDATED, this::isStepForMergedToPEN, CREATE_MERGE, this::createMerge)
            .step(CREATE_MERGE, MERGE_CREATED, GET_STUDENT, this::getStudentByMergedFromPen)
            .step(GET_STUDENT, STUDENT_FOUND, this::isStepForMergedFromPEN, UPDATE_STUDENT, this::updateMergedFromStudent)
            .step(UPDATE_STUDENT, STUDENT_UPDATED, this::isStepForMergedFromPEN, GET_STUDENT_HISTORY, this::readAuditHistory)
            .step(GET_STUDENT_HISTORY, STUDENT_HISTORY_FOUND, CREATE_STUDENT_HISTORY, this::addAuditHistory)
            .step(CREATE_STUDENT_HISTORY, STUDENT_HISTORY_CREATED, GET_POSSIBLE_MATCH, this::readPossibleMatches)
            .step(GET_POSSIBLE_MATCH, POSSIBLE_MATCH_FOUND, DELETE_POSSIBLE_MATCH, this::deletePossibleMatches)
            .step(GET_POSSIBLE_MATCH, POSSIBLE_MATCH_NOT_FOUND, MARK_SAGA_COMPLETE, this::markSagaComplete)
            .step(DELETE_POSSIBLE_MATCH, POSSIBLE_MATCH_DELETED, MARK_SAGA_COMPLETE, this::markSagaComplete);
  }

  protected void getStudentByMergedToPen(Event event, Saga saga, StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws IOException, InterruptedException, TimeoutException {
    SagaEventStates eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(GET_STUDENT.toString()); // set current event as saga state.
    studentMergeCompleteSagaData.setRequestStudentID(studentMergeCompleteSagaData.getStudentID());
    saga.setPayload(JsonUtil.getJsonStringFromObject(studentMergeCompleteSagaData));
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(GET_STUDENT)
            .replyTo(getTopicToSubscribe())
            .eventPayload(studentMergeCompleteSagaData.getMergedToPen())
            .build();
    postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for GET_STUDENT Event to get MergedToPen.");
  }

  protected void getStudentByMergedFromPen(Event event, Saga saga, StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws IOException, InterruptedException, TimeoutException {
    SagaEventStates eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(GET_STUDENT.toString()); // set current event as saga state.
    studentMergeCompleteSagaData.setRequestStudentID(studentMergeCompleteSagaData.getMergeStudentID());
    saga.setPayload(JsonUtil.getJsonStringFromObject(studentMergeCompleteSagaData));
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(GET_STUDENT)
            .replyTo(getTopicToSubscribe())
            .eventPayload(studentMergeCompleteSagaData.getMergedFromPen())
            .build();
    postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for GET_STUDENT Event to get MergedFromPen.");
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * mincode
   * Local ID
   * Student Grade Code
   * Postal Code
   *
   * @param event                             the event
   * @param saga                              the saga
   * @param studentMergeCompleteSagaData      the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void updateMergedToStudent(Event event, Saga saga, StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws JsonProcessingException {
    SagaEventStates eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(UPDATE_STUDENT.toString()); // set current event as saga state.

    StudentSagaData studentDataFromEventResponse = JsonUtil.getJsonObjectFromString(StudentSagaData.class, event.getEventPayload());
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
    studentDataFromEventResponse.setUpdateUser(studentMergeCompleteSagaData.getUpdateUser());
    studentDataFromEventResponse.setHistoryActivityCode(studentMergeCompleteSagaData.getHistoryActivityCode());

    processStudentUpdate(saga, studentMergeCompleteSagaData, eventStates, studentDataFromEventResponse);
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                             the event
   * @param saga                              the saga
   * @param studentMergeCompleteSagaData      the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void updateMergedFromStudent(Event event, Saga saga, StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws JsonProcessingException {
    SagaEventStates eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(UPDATE_STUDENT.toString()); // set current event as saga state.

    StudentSagaData studentDataFromEventResponse = JsonUtil.getJsonObjectFromString(StudentSagaData.class, event.getEventPayload());
    studentDataFromEventResponse.setStatusCode(StudentStatusCodes.Merge.getCode());
    studentDataFromEventResponse.setTrueStudentID(studentMergeCompleteSagaData.getStudentID().toString());
    studentDataFromEventResponse.setLegalFirstName(studentMergeCompleteSagaData.getLegalFirstName());
    studentDataFromEventResponse.setUpdateUser(studentMergeCompleteSagaData.getUpdateUser());
    studentDataFromEventResponse.setHistoryActivityCode(studentMergeCompleteSagaData.getHistoryActivityCode());

    processStudentUpdate(saga, studentMergeCompleteSagaData, eventStates, studentDataFromEventResponse);
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                             the event
   * @param saga                              the saga
   * @param studentMergeCompleteSagaData      the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void createMerge(Event event, Saga saga, StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws JsonProcessingException {
    SagaEventStates eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(CREATE_MERGE.toString()); // set current event as saga state.

    StudentMerge studentMerge = STUDENT_MERGE_COMPLETE_SAGA_DATA_MAPPER.toStudentMerge(studentMergeCompleteSagaData);
    studentMerge.setStudentMergeDirectionCode(StudentMergeDirectionCodes.FROM.getCode());
    studentMerge.setStudentMergeSourceCode(StudentMergeSourceCodes.MI.getCode());
    studentMergeCompleteSagaData.setRequestStudentID(studentMergeCompleteSagaData.getMergeStudentID());
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(CREATE_MERGE)
            .replyTo(getTopicToSubscribe())
            .eventPayload(JsonUtil.getJsonStringFromObject(studentMerge))
            .build();
    postMessageToTopic(PEN_SERVICES_API_TOPIC.toString(), nextEvent);
    log.info("message sent to PEN_SERVICES_API_TOPIC for CREATE_MERGE Event.");
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                             the event
   * @param saga                              the saga
   * @param studentMergeCompleteSagaData      the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void readAuditHistory(Event event, Saga saga, StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws JsonProcessingException {
    SagaEventStates eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(GET_STUDENT_HISTORY.toString()); // set current event as saga state.
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(GET_STUDENT_HISTORY)
            .replyTo(getTopicToSubscribe())
            .eventPayload(studentMergeCompleteSagaData.getMergeStudentID().toString())
            .build();
    postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for GET_STUDENT_HISTORY Event.");
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                             the event
   * @param saga                              the saga
   * @param studentMergeCompleteSagaData              the student merge saga data
   * @throws JsonProcessingException          the json processing exception
   */
  protected void addAuditHistory(Event event, Saga saga, StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws JsonProcessingException {
    SagaEventStates eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(CREATE_STUDENT_HISTORY.toString()); // set current event as saga state.

    ObjectMapper objectMapper = new ObjectMapper();
    JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, StudentHistory.class);
    List<StudentHistory> historyList = objectMapper.readValue(event.getEventPayload(), type);
    historyList.stream().forEach(h -> {
      h.setStudentHistoryID(null);
      h.setStudentID(studentMergeCompleteSagaData.getStudentID().toString());
    });
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(CREATE_STUDENT_HISTORY)
            .replyTo(getTopicToSubscribe())
            .eventPayload(JsonUtil.getJsonStringFromObject(historyList))
            .build();
    postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for CREATE_STUDENT_HISTORY Event.");
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                             the event
   * @param saga                              the saga
   * @param studentMergeCompleteSagaData              the student merge saga data
   * @throws JsonProcessingException          the json processing exception
   */
  protected void readPossibleMatches(Event event, Saga saga, StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws JsonProcessingException {
    SagaEventStates eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(GET_POSSIBLE_MATCH.toString()); // set current event as saga state.
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(GET_POSSIBLE_MATCH)
            .replyTo(getTopicToSubscribe())
            .eventPayload(studentMergeCompleteSagaData.getMergeStudentID().toString())
            .build();
    postMessageToTopic(PEN_MATCH_API_TOPIC.toString(), nextEvent);
    log.info("message sent to PEN_MATCH_API_TOPIC for GET_POSSIBLE_MATCH Event.");
  }

  /**
   * the following attributes on the student merge record get updated based on the incoming Merge request
   * status
   *
   * @param event                             the event
   * @param saga                              the saga
   * @param studentMergeCompleteSagaData      the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void deletePossibleMatches(Event event, Saga saga, StudentMergeCompleteSagaData studentMergeCompleteSagaData) throws JsonProcessingException {
    SagaEventStates eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(DELETE_POSSIBLE_MATCH.toString()); // set current event as saga state.

    ObjectMapper objectMapper = new ObjectMapper();
    JavaType type = objectMapper.getTypeFactory().
            constructCollectionType(List.class, PossibleMatch.class);
    List<PossibleMatch> possibleMatches = objectMapper.readValue(event.getEventPayload(), type);
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(DELETE_POSSIBLE_MATCH)
            .replyTo(getTopicToSubscribe())
            .eventPayload(JsonUtil.getJsonStringFromObject(possibleMatches))
            .build();
    postMessageToTopic(PEN_MATCH_API_TOPIC.toString(), nextEvent);
    log.info("message sent to PEN_MATCH_API_TOPIC for DELETE_POSSIBLE_MATCH Event.");
  }

  private void processStudentUpdate(Saga saga, StudentMergeCompleteSagaData studentMergeCompleteSagaData, SagaEventStates eventStates, StudentSagaData studentUpdate) throws JsonProcessingException {
    studentUpdate.setHistoryActivityCode(StudentHistoryActivityCodes.MERGE.getCode());
    studentMergeCompleteSagaData.setRequestStudentID(UUID.fromString(studentUpdate.getStudentID()));
    saga.setPayload(JsonUtil.getJsonStringFromObject(studentMergeCompleteSagaData));
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(UPDATE_STUDENT)
            .replyTo(getTopicToSubscribe())
            .eventPayload(JsonUtil.getJsonStringFromObject(studentUpdate))
            .build();
    postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for UPDATE_STUDENT Event.");
  }

  private boolean isStepForMergedToPEN(StudentMergeCompleteSagaData studentMergeCompleteSagaData) {
    return studentMergeCompleteSagaData.getRequestStudentID().equals(studentMergeCompleteSagaData.getStudentID());
  }

  private boolean isStepForMergedFromPEN(StudentMergeCompleteSagaData studentMergeCompleteSagaData) {
    return studentMergeCompleteSagaData.getRequestStudentID().equals(studentMergeCompleteSagaData.getMergeStudentID());
  }
}
