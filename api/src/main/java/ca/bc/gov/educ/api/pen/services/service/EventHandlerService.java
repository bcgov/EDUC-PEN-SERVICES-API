package ca.bc.gov.educ.api.pen.services.service;

import ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode;
import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMergeMapper;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeRepository;
import ca.bc.gov.educ.api.pen.services.struct.v1.Event;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import ca.bc.gov.educ.api.pen.services.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static ca.bc.gov.educ.api.pen.services.constants.EventOutcome.*;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
public class EventHandlerService {

  public static final String PAYLOAD_LOG = "payload is :: {}";
  public static final String EVENT_PAYLOAD = "event is :: {}";
  /**
   * The constant NO_RECORD_SAGA_ID_EVENT_TYPE.
   */
  public static final String NO_RECORD_SAGA_ID_EVENT_TYPE = "no record found for the saga id and event type combination, processing.";

  /**
   * The constant studentMapper.
   */
  @Getter(PRIVATE)
  private static final StudentMergeMapper studentMergeMapper = StudentMergeMapper.mapper;

  @Getter(PRIVATE)
  private final PenRequestStudentRecordValidationService validationService;

  @Getter(PRIVATE)
  private final PenService penService;

  @Getter(PRIVATE)
  private final StudentMergeRepository studentMergeRepository;

  @Getter(PRIVATE)
  private final MessagePublisher messagePublisher;

  /**
   * Instantiates a new Event handler service.
   *
   * @param validationService the validation service
   * @param messagePublisher  the message publisher
   */
  @Autowired
  public EventHandlerService(PenRequestStudentRecordValidationService validationService, MessagePublisher messagePublisher, PenService penService, StudentMergeRepository studentMergeRepository) {
    this.validationService = validationService;
    this.messagePublisher = messagePublisher;
    this.penService = penService;
    this.studentMergeRepository = studentMergeRepository;
  }

  /**
   * Handle events.
   *
   * @param event the event
   */
  @Async("subscriberExecutor")
  public void handleEvent(Event event) {
    try {
      switch (event.getEventType()) {
        case VALIDATE_STUDENT_DEMOGRAPHICS:
          log.info("received validate student demographics event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          handleValidateStudentDemogDataEvent(event);
          break;
        case GET_NEXT_PEN_NUMBER:
          log.info("received get next pen number event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          handleGetNextPenNumberEvent(event);
          break;
        case CREATE_MERGE:
          log.info("received create merge data :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          handleCreateMergeEvent(event);
          break;
        default:
          log.info("silently ignoring other events.");
          break;
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }

  /**
   * Handle validate student demog data event.
   *
   * @param event the event
   */
  private void handleValidateStudentDemogDataEvent(Event event) {
    try {
      var validationPayload = JsonUtil.getJsonObjectFromString(PenRequestStudentValidationPayload.class, event.getEventPayload());
      var result = getValidationService().validateStudentRecord(validationPayload);
      if (result.isEmpty()) {
        event.setEventOutcome(VALIDATION_SUCCESS_NO_ERROR_WARNING);
        event.setEventPayload(VALIDATION_SUCCESS_NO_ERROR_WARNING.toString());
      } else {
        var isError = result.stream().anyMatch(x -> x.getPenRequestBatchValidationIssueSeverityCode().equals(PenRequestStudentValidationIssueSeverityCode.ERROR.toString()));
        if (isError) {
          event.setEventOutcome(VALIDATION_SUCCESS_WITH_ERROR);
        } else {
          event.setEventOutcome(VALIDATION_SUCCESS_WITH_ONLY_WARNING);
        }
        event.setEventPayload(JsonUtil.getJsonStringFromObject(result));
      }
      if (log.isDebugEnabled()) {
        log.debug("responding back :: {}", event);
      }
      log.info("responding back with :: {} errors/warnings , event outcome :: {}, for transaction ID :: {}", result.size(), event.getEventOutcome().toString(), validationPayload.getTransactionID());
      getMessagePublisher().dispatchMessage(event.getReplyTo(), JsonUtil.getJsonStringFromObject(event).getBytes());
    } catch (JsonProcessingException e) {
      log.error("JsonProcessingException for saga ::, {} , :: {}", event.getSagaId(), e);
    }

  }

  /**
   * Handle get next PEN number event.
   *
   * @param event the event
   */
  private void handleGetNextPenNumberEvent(Event event) {
    try {
      var transactionID = event.getEventPayload();
      var nextPenNumber = getPenService().getNextPenNumber(transactionID);

      event.setEventOutcome(NEXT_PEN_NUMBER_RETRIEVED);
      event.setEventPayload(nextPenNumber);

      if (log.isDebugEnabled()) {
        log.debug("responding back :: {}", event);
      }
      log.info("responding back with :: event outcome :: {}, for transaction ID :: {}", event.getEventOutcome().toString(), transactionID);
      getMessagePublisher().dispatchMessage(event.getReplyTo(), JsonUtil.getJsonStringFromObject(event).getBytes());
    } catch (JsonProcessingException e) {
      log.error("JsonProcessingException for saga ::, {} , :: {}", event.getSagaId(), e);
    }

  }

  @Transactional(propagation = REQUIRES_NEW)
  protected void handleCreateMergeEvent(Event event) {
    try {
      log.trace(EVENT_PAYLOAD, event);
      List<StudentMerge> payload = new ArrayList<>();

      // MergedToStudent
      StudentMerge mergedToPEN = JsonUtil.getJsonObjectFromString(StudentMerge.class, event.getEventPayload());
      RequestUtil.setAuditColumnsForCreate(mergedToPEN);
      getStudentMergeRepository().save(studentMergeMapper.toModel(mergedToPEN));
      payload.add(mergedToPEN);

      // MergedFromStudent
      StudentMerge mergedFromPEN = StudentMerge.builder().studentID(mergedToPEN.getMergeStudentID()).mergeStudentID(mergedToPEN.getStudentID())
              .studentMergeDirectionCode("TO").studentMergeSourceCode(mergedToPEN.getStudentMergeSourceCode()).build();
      RequestUtil.setAuditColumnsForCreate(mergedFromPEN);
      getStudentMergeRepository().save(studentMergeMapper.toModel(mergedFromPEN));
      payload.add(mergedFromPEN);

      event.setEventOutcome(MERGE_CREATED);
      event.setEventPayload(JsonUtil.getJsonStringFromObject(payload));

      if (log.isDebugEnabled()) {
        log.debug("responding back :: {}", event);
      }
      log.info("responding back with :: event outcome :: {}, for mergedToStudent ID :: {}", event.getEventOutcome().toString(), mergedToPEN.getStudentID());
      getMessagePublisher().dispatchMessage(event.getReplyTo(), JsonUtil.getJsonStringFromObject(event).getBytes());
    } catch (JsonProcessingException e) {
      log.error("JsonProcessingException for saga ::, {} , :: {}", event.getSagaId(), e);
    }

  }
}
