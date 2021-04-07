package ca.bc.gov.educ.api.pen.services.orchestrator;

import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMergeCompleteSagaDataMapper;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.model.SagaEventStates;
import ca.bc.gov.educ.api.pen.services.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.v1.BaseStudentSagaData;
import ca.bc.gov.educ.api.pen.services.struct.v1.Event;
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
  protected BaseUserActionsOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, final Class<T> clazz, final String sagaName, final String topicToSubscribe) {
    super(sagaService, messagePublisher, clazz, sagaName, topicToSubscribe);
  }

  /**
   * Process student read.
   *
   * @param saga        the saga
   * @param sagaData    the saga data
   * @param eventStates the event states
   * @param pen         the pen
   * @throws JsonProcessingException the json processing exception
   */
  protected void processStudentRead(final Saga saga, final BaseStudentSagaData sagaData, final SagaEventStates eventStates, final String pen) throws JsonProcessingException {
    saga.setSagaState(GET_STUDENT.toString()); // set current event as saga state.
    saga.setPayload(JsonUtil.getJsonStringFromObject(sagaData));
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    final Event nextEvent = this.buildGetStudentByPenEvent(saga.getSagaId(), pen);
    this.postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for GET_STUDENT Event.");
  }

  protected Event buildGetStudentByPenEvent(final UUID sagaID, final String pen) {
    return Event.builder().sagaId(sagaID)
        .eventType(GET_STUDENT)
        .replyTo(this.getTopicToSubscribe())
        .eventPayload(pen)
        .build();
  }

  /**
   * Process student update.
   *
   * @param saga          the saga
   * @param sagaData      the saga data
   * @param eventStates   the event states
   * @param studentUpdate the student update
   * @throws JsonProcessingException the json processing exception
   */
  protected void processStudentUpdate(final Saga saga, final BaseStudentSagaData sagaData, final SagaEventStates eventStates, final StudentSagaData studentUpdate) throws JsonProcessingException {
    studentUpdate.setUpdateUser(sagaData.getUpdateUser());
    sagaData.setRequestStudentID(studentUpdate.getStudentID());
    saga.setSagaState(UPDATE_STUDENT.toString()); // set current event as saga state.
    saga.setPayload(JsonUtil.getJsonStringFromObject(sagaData));
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
        .eventType(UPDATE_STUDENT)
        .replyTo(this.getTopicToSubscribe())
        .eventPayload(JsonUtil.getJsonStringFromObject(studentUpdate))
        .build();
    this.postMessageToTopic(STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to STUDENT_API_TOPIC for UPDATE_STUDENT Event.");
  }

}
