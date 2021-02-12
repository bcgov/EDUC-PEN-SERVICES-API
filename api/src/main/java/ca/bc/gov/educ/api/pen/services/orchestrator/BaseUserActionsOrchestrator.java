package ca.bc.gov.educ.api.pen.services.orchestrator;

import ca.bc.gov.educ.api.pen.services.constants.StudentHistoryActivityCodes;
import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMergeCompleteSagaDataMapper;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.model.SagaEventStates;
import ca.bc.gov.educ.api.pen.services.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.v1.Event;
import ca.bc.gov.educ.api.pen.services.struct.v1.BaseStudentSagaData;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeCompleteSagaData;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentSagaData;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.EventType.GET_STUDENT;
import static ca.bc.gov.educ.api.pen.services.constants.EventType.UPDATE_STUDENT;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.STUDENT_API_TOPIC;

/**
 * The type Base user actions orchestrator.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class BaseUserActionsOrchestrator<T> extends BaseOrchestrator<T> {

  /**
   * The constant STUDENT_MERGE_COMPLETE_SAGA_DATA_MAPPER.
   */
  protected static final StudentMergeCompleteSagaDataMapper STUDENT_MERGE_COMPLETE_SAGA_DATA_MAPPER = StudentMergeCompleteSagaDataMapper.mapper;

  /**
   * Instantiates a new Base user actions orchestrator.
   *
   * @param sagaService      the saga service
   * @param messagePublisher the message publisher
   * @param clazz            the clazz
   * @param sagaName         the saga name
   * @param topicToSubscribe the topic to subscribe
   */
  protected BaseUserActionsOrchestrator(SagaService sagaService, MessagePublisher messagePublisher, Class<T> clazz, String sagaName, String topicToSubscribe) {
    super(sagaService, messagePublisher, clazz, sagaName, topicToSubscribe);
  }

  protected void processStudentRead(Saga saga, BaseStudentSagaData sagaData, SagaEventStates eventStates, String pen) throws JsonProcessingException {
    saga.setSagaState(GET_STUDENT.toString()); // set current event as saga state.
    saga.setPayload(JsonUtil.getJsonStringFromObject(sagaData));
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(GET_STUDENT)
            .replyTo(getTopicToSubscribe())
            .eventPayload(pen)
            .build();
    postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for GET_STUDENT Event.");
  }

  protected void processStudentUpdate(Saga saga, BaseStudentSagaData sagaData, SagaEventStates eventStates, StudentSagaData studentUpdate) throws JsonProcessingException {
    studentUpdate.setUpdateUser(sagaData.getUpdateUser());
    sagaData.setRequestStudentID(UUID.fromString(studentUpdate.getStudentID()));
    saga.setSagaState(UPDATE_STUDENT.toString()); // set current event as saga state.
    saga.setPayload(JsonUtil.getJsonStringFromObject(sagaData));
    getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(UPDATE_STUDENT)
            .replyTo(getTopicToSubscribe())
            .eventPayload(JsonUtil.getJsonStringFromObject(studentUpdate))
            .build();
    postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for UPDATE_STUDENT Event.");
  }

  /**
   *
   * @param event                 the event
   * @param saga                  the saga
   * @param baseStudentSagaData   the student update saga data
   */
  protected void logStudentNotFound(Event event, Saga saga, BaseStudentSagaData baseStudentSagaData) {
    log.error("Student record was not found. This should not happen. Please check the services api. :: {}", saga.getSagaId());
  }
}
