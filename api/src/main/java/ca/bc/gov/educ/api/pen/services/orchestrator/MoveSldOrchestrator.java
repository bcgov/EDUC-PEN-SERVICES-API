package ca.bc.gov.educ.api.pen.services.orchestrator;

import ca.bc.gov.educ.api.pen.services.mapper.v1.MoveMultipleSldSagaDataMapper;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.model.SagaEventStates;
import ca.bc.gov.educ.api.pen.services.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class MoveSldOrchestrator extends BaseOrchestrator<MoveMultipleSldSagaData> {

  /**
   * The constant MOVE_MULTIPLE_SLD_SAGA_DATA_MAPPER.
   */
  protected static final MoveMultipleSldSagaDataMapper MOVE_MULTIPLE_SLD_SAGA_DATA_MAPPER = MoveMultipleSldSagaDataMapper.mapper;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService      the saga service
   * @param messagePublisher the message publisher
   */
  public MoveSldOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher) {
    super(sagaService, messagePublisher, MoveMultipleSldSagaData.class, PEN_SERVICES_MOVE_SLD_SAGA.toString(), PEN_SERVICES_MOVE_SLD_SAGA_TOPIC.toString());
  }

  /**
   * Populate steps to execute map.
   */
  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
        .begin(UPDATE_SLD_STUDENTS_BY_IDS, this::updateSldStudents)
        .step(UPDATE_SLD_STUDENTS_BY_IDS, SLD_STUDENT_UPDATED, UPDATE_SLD_STUDENT_PROGRAMS_BY_DATA, this::updateSldStudentPrograms)
        .end(UPDATE_SLD_STUDENT_PROGRAMS_BY_DATA, SLD_STUDENT_PROGRAM_UPDATED);
  }

  /**
   * Update the pen of sld student records
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param moveSldSagaData              the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void updateSldStudents(final Event event, final Saga saga, final MoveMultipleSldSagaData moveSldSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(UPDATE_SLD_STUDENTS_BY_IDS.toString()); // set current event as saga state.

    final var sldUpdateStudentsEvent = MOVE_MULTIPLE_SLD_SAGA_DATA_MAPPER.toSldUpdateStudentsByIdsEvent(moveSldSagaData);
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(UPDATE_SLD_STUDENTS_BY_IDS)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(sldUpdateStudentsEvent))
      .build();
    this.postMessageToTopic(SLD_API_TOPIC.toString(), nextEvent);
    log.info("message sent to SLD_API_TOPIC for UPDATE_SLD_STUDENTS_BY_IDS Event.");
  }

  /**
   * Update the pen of sld student program records
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param moveSldSagaData              the student merge saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void updateSldStudentPrograms(final Event event, final Saga saga, final MoveMultipleSldSagaData moveSldSagaData) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(UPDATE_SLD_STUDENT_PROGRAMS_BY_DATA.toString()); // set current event as saga state.

    moveSldSagaData.getMoveSldSagaData().forEach(sldData -> {
      if (StringUtils.length(sldData.getPen()) >= 10) {
        sldData.setPen(StringUtils.substring(sldData.getPen(), 0, 9));  // remove suffix of the pen number to search sld program records
      }
    });
    final var sldUpdateStudentProgramsEvent = MOVE_MULTIPLE_SLD_SAGA_DATA_MAPPER.toSldUpdateStudentProgramsByDataEvent(moveSldSagaData);
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(UPDATE_SLD_STUDENT_PROGRAMS_BY_DATA)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(sldUpdateStudentProgramsEvent))
      .build();
    this.postMessageToTopic(SLD_API_TOPIC.toString(), nextEvent);
    log.info("message sent to SLD_API_TOPIC for UPDATE_SLD_STUDENT_PROGRAMS_BY_DATA Event.");
  }

}
