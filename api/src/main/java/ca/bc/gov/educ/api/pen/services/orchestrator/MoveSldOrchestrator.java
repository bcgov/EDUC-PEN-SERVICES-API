package ca.bc.gov.educ.api.pen.services.orchestrator;

import ca.bc.gov.educ.api.pen.services.mapper.v1.MoveSldSagaDataMapper;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.model.SagaEventStates;
import ca.bc.gov.educ.api.pen.services.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import static ca.bc.gov.educ.api.pen.services.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.services.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.PEN_SERVICES_MOVE_SLD_SAGA;
import static ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.*;

/**
 * The type Move sld orchestrator
 */
@Component
@Slf4j
public class MoveSldOrchestrator extends BaseOrchestrator<MoveSldSagaData> {

  /**
   * The constant MOVE_SLD_SAGA_DATA_MAPPER.
   */
  protected static final MoveSldSagaDataMapper MOVE_SLD_SAGA_DATA_MAPPER = MoveSldSagaDataMapper.mapper;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService      the saga service
   * @param messagePublisher the message publisher
   */
  public MoveSldOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher) {
    super(sagaService, messagePublisher, MoveSldSagaData.class, PEN_SERVICES_MOVE_SLD_SAGA.toString(), PEN_SERVICES_MOVE_SLD_SAGA_TOPIC.toString());
  }

  /**
   * Populate steps to execute map.
   */
  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
        .begin(UPDATE_SLD_STUDENT, this::updateSldStudent)
        .step(UPDATE_SLD_STUDENT, SLD_STUDENT_UPDATED, UPDATE_SLD_STUDENT_PROGRAMS, this::updateSldStudentPrograms)
        .end(UPDATE_SLD_STUDENT_PROGRAMS, SLD_STUDENT_PROGRAM_UPDATED);
  }

  /**
   * Update the pen of sld student record
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param moveSldSagaData              the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void updateSldStudent(final Event event, final Saga saga, final MoveSldSagaData moveSldSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(UPDATE_SLD_STUDENT.toString()); // set current event as saga state.

    final var sldUpdateSingleStudentEvent = MOVE_SLD_SAGA_DATA_MAPPER.toSldUpdateSingleStudentEvent(moveSldSagaData);
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(UPDATE_SLD_STUDENT)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(sldUpdateSingleStudentEvent))
      .build();
    this.postMessageToTopic(SLD_API_TOPIC.toString(), nextEvent);
    log.info("message sent to SLD_API_TOPIC for UPDATE_SLD_STUDENT Event.");
  }

  /**
   * Update the pen of sld student program records
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param moveSldSagaData              the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void updateSldStudentPrograms(final Event event, final Saga saga, final MoveSldSagaData moveSldSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(UPDATE_SLD_STUDENT_PROGRAMS.toString()); // set current event as saga state.

    final var sldUpdateStudentProgramsEvent = MOVE_SLD_SAGA_DATA_MAPPER.toSldUpdateStudentProgramsEvent(moveSldSagaData);
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(UPDATE_SLD_STUDENT_PROGRAMS)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(sldUpdateStudentProgramsEvent))
      .build();
    this.postMessageToTopic(SLD_API_TOPIC.toString(), nextEvent);
    log.info("message sent to SLD_API_TOPIC for UPDATE_SLD_STUDENT_PROGRAMS Event.");
  }

}
