package ca.bc.gov.educ.api.pen.validation.service;

import ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueSeverityCode;
import ca.bc.gov.educ.api.pen.validation.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.validation.struct.v1.Event;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import ca.bc.gov.educ.api.pen.validation.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static ca.bc.gov.educ.api.pen.validation.constants.EventOutcome.*;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
public class EventHandlerService {

  @Getter(PRIVATE)
  private final PenRequestStudentRecordValidationService validationService;
  @Getter(PRIVATE)
  private final MessagePublisher messagePublisher;

  /**
   * Instantiates a new Event handler service.
   *
   * @param validationService the validation service
   * @param messagePublisher  the message publisher
   */
  @Autowired
  public EventHandlerService(PenRequestStudentRecordValidationService validationService, MessagePublisher messagePublisher) {
    this.validationService = validationService;
    this.messagePublisher = messagePublisher;
  }

  /**
   * Handle validate student demog data event.
   *
   * @param event the event
   */
  @Async("subscriberExecutor")
  public void handleValidateStudentDemogDataEvent(Event event) {
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

}
