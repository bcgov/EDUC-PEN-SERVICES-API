package ca.bc.gov.educ.api.pen.services.orchestrator;

import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMergeCompleteSagaDataMapper;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.v1.Event;
import ca.bc.gov.educ.api.pen.services.struct.v1.BaseStudentSagaData;
import lombok.extern.slf4j.Slf4j;

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
