package ca.bc.gov.educ.api.pen.services.orchestrator;

import ca.bc.gov.educ.api.pen.services.constants.*;
import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMapper;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.model.SagaEventStates;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static ca.bc.gov.educ.api.pen.services.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.services.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.PEN_SERVICES_SPLIT_PEN_SAGA;
import static ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.*;

/**
 * The type Split pen orchestrator
 */
@Component
@Slf4j
public class SplitPenOrchestrator extends BaseUserActionsOrchestrator<SplitPenSagaData> {

  /**
   * The constant studentMapper.
   */
  protected static final StudentMapper studentMapper = StudentMapper.mapper;

  /**
   * The Ob mapper.
   */
  private final ObjectMapper obMapper = new ObjectMapper();

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService      the saga service
   * @param messagePublisher the message publisher
   */
  public SplitPenOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher) {
    super(sagaService, messagePublisher, SplitPenSagaData.class, PEN_SERVICES_SPLIT_PEN_SAGA.toString(), PEN_SERVICES_SPLIT_PEN_SAGA_TOPIC.toString());
  }

  /**
   * Populate steps to execute map.
   */
  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
        .begin(UPDATE_STUDENT, this::updateOriginalStudent)
        .step(UPDATE_STUDENT, STUDENT_UPDATED, GET_NEXT_PEN_NUMBER, this::getNextPenNumber)
        .step(GET_NEXT_PEN_NUMBER, NEXT_PEN_NUMBER_RETRIEVED, CREATE_STUDENT, this::createStudent)
        .step(CREATE_STUDENT, STUDENT_ALREADY_EXIST, ADD_POSSIBLE_MATCH, this::addPossibleMatchesToStudent)
        .or()
        .step(CREATE_STUDENT, STUDENT_CREATED, ADD_POSSIBLE_MATCH, this::addPossibleMatchesToStudent)
        .step(ADD_POSSIBLE_MATCH, POSSIBLE_MATCH_ADDED, MARK_SAGA_COMPLETE, this::markSplitPenSagaComplete);
  }

  /**
   * Update the original student record
   *
   * @param event             the event
   * @param saga              the saga
   * @param splitPenSagaData  the split pen saga data
   * @throws JsonProcessingException the json processing exception
   */
  public void updateOriginalStudent(final Event event, final Saga saga, final SplitPenSagaData splitPenSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());

    var studentUpdate = studentMapper.toStudent(splitPenSagaData);
    studentUpdate.setHistoryActivityCode(StudentHistoryActivityCodes.SPLITNEW.getCode());

    this.processStudentUpdate(saga, splitPenSagaData, eventStates, studentUpdate);
  }

  /**
   * Get the next PEN number.
   *
   * @param event             the event
   * @param saga              the saga
   * @param splitPenSagaData  the split pen saga data
   */
  public void getNextPenNumber(final Event event, final Saga saga, final SplitPenSagaData splitPenSagaData) {
    var eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(GET_NEXT_PEN_NUMBER.toString());
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    var transactionID = saga.getSagaId().toString();
    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(GET_NEXT_PEN_NUMBER)
      .replyTo(getTopicToSubscribe())
      .eventPayload(transactionID)
      .build();
    postMessageToTopic(PEN_SERVICES_API_TOPIC.toString(), nextEvent);
    log.info("message sent to PEN_SERVICES_API_TOPIC for GET_NEXT_PEN_NUMBER Event. :: {}", saga.getSagaId());
  }

  /**
   * Create student record.
   *
   * @param event             the event
   * @param saga              the saga
   * @param splitPenSagaData  the split pen saga data
   * @throws JsonProcessingException the json processing exception
   */
  public void createStudent(final Event event, final Saga saga, final SplitPenSagaData splitPenSagaData) throws JsonProcessingException {
    var pen = event.getEventPayload();
    var student = splitPenSagaData.getNewStudent();
    student.setPen(pen);
    student.setDemogCode(StudentDemogCodes.ACCEPTED.getCode());
    student.setStatusCode(StudentStatusCodes.ACTIVE.getCode());
    student.setHistoryActivityCode(StudentHistoryActivityCodes.SPLITNEW.getCode());
    student.setCreateUser(splitPenSagaData.getCreateUser());
    student.setUpdateUser(splitPenSagaData.getUpdateUser());

    saga.setSagaState(CREATE_STUDENT.toString());
    saga.setPayload(JsonUtil.getJsonStringFromObject(splitPenSagaData));
    var eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(CREATE_STUDENT)
      .replyTo(getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(student))
      .build();
    postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for CREATE_STUDENT Event. :: {}", saga.getSagaId());
  }

  /**
   * Add possible matches to student record.
   *
   * @param event             the event
   * @param saga              the saga
   * @param splitPenSagaData  the split pen saga data
   * @throws JsonProcessingException the json processing exception
   */
  public void addPossibleMatchesToStudent(final Event event, final Saga saga, final SplitPenSagaData splitPenSagaData) throws JsonProcessingException {
    final String studentID;
    // if PEN number is already existing then student-api will return the student-id
    // this scenario might occur during replay when message could not reach batch api from student-api and batch api retried the same flow.
    if (event.getEventOutcome() == STUDENT_ALREADY_EXIST) {
      studentID = event.getEventPayload();
    } else {
      var student = JsonUtil.getJsonObjectFromString(StudentSagaData.class, event.getEventPayload());
      studentID = student.getStudentID();
    }

    var eventStates = createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(ADD_POSSIBLE_MATCH.toString()); // set current event as saga state.
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    var possibleMatches = List.of(PossibleMatch.builder()
        .createUser(splitPenSagaData.getCreateUser())
        .updateUser(splitPenSagaData.getUpdateUser())
        .studentID(studentID)
        .matchedStudentID(splitPenSagaData.getStudentID())
        .matchReasonCode(MatchReasonCodes.SPLIT)
        .build());

    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(ADD_POSSIBLE_MATCH)
      .replyTo(getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(possibleMatches))
      .build();
    postMessageToTopic(PEN_MATCH_API_TOPIC.toString(), nextEvent);
    log.info("message sent to PEN_MATCH_API_TOPIC for ADD_POSSIBLE_MATCH Event.");
  }

  /**
   * Mark the process as complete
   *
   * @param event             the event
   * @param saga              the saga
   * @param splitPenSagaData  the split pen saga data
   * @throws JsonProcessingException the json processing exception
   */
  public void markSplitPenSagaComplete(final Event event, final Saga saga, final SplitPenSagaData splitPenSagaData) throws JsonProcessingException {
    var studentID = "";

    JavaType type = obMapper.getTypeFactory().
      constructCollectionType(List.class, PossibleMatch.class);

    List<PossibleMatch> possibleMatches = obMapper.readValue(event.getEventPayload(), type);
    if(possibleMatches.size() > 0) {
      studentID = possibleMatches.get(0).getStudentID();
    }

    markSagaComplete(event, saga, splitPenSagaData, studentID);
  }
}
