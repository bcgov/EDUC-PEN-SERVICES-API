package ca.bc.gov.educ.api.pen.services.service.events;

import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.pen.services.model.ServicesEvent;
import ca.bc.gov.educ.api.pen.services.struct.v1.Event;
import io.nats.client.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
public class EventHandlerDelegatorService {
  /**
   * The constant PAYLOAD_LOG.
   */
  public static final String PAYLOAD_LOG = "Payload is :: {}";
  /**
   * The Event handler service.
   */
  @Getter
  private final EventHandlerService eventHandlerService;

  /**
   * The Message publisher.
   */
  @Getter(PRIVATE)
  private final MessagePublisher messagePublisher;
  /**
   * The Publisher.
   */
  private final Publisher publisher; // Jet Stream publisher for choreography

  /**
   * Instantiates a new Event handler delegator service.
   *
   * @param eventHandlerService the event handler service
   * @param messagePublisher    the message publisher
   * @param publisher           the publisher
   */
  @Autowired
  public EventHandlerDelegatorService(final EventHandlerService eventHandlerService, final MessagePublisher messagePublisher, final Publisher publisher) {
    this.eventHandlerService = eventHandlerService;
    this.messagePublisher = messagePublisher;
    this.publisher = publisher;
  }

  /**
   * Handle event.
   *
   * @param event   the event
   * @param message the message
   */
  @Async("subscriberExecutor")
  public void handleEvent(final Event event, final Message message) {
    final boolean isSynchronous = message.getReplyTo() != null;
    final byte[] response;
    final Pair<byte[], Optional<ServicesEvent>> pairedResult;
    try {
      switch (event.getEventType()) {
        case VALIDATE_STUDENT_DEMOGRAPHICS:
          log.debug("received validate student demographics event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = this.getEventHandlerService().handleValidateStudentDemogDataEvent(event);
          this.publishToNATS(event, message, isSynchronous, response);
          break;
        case GET_NEXT_PEN_NUMBER:
          log.debug("received get next pen number event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = this.getEventHandlerService().handleGetNextPenNumberEvent(event);
          this.publishToNATS(event, message, isSynchronous, response);
          break;
        case CREATE_MERGE:
          log.debug("received create merge data :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          pairedResult = this.getEventHandlerService().handleCreateMergeEvent(event);
          this.publishToNATS(event, message, isSynchronous, pairedResult.getLeft());
          pairedResult.getRight().ifPresent(this::publishToJetStream);
          break;
        case DELETE_MERGE:
          log.debug("received delete merge data :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          pairedResult = this.getEventHandlerService().handleDeleteMergeEvent(event);
          this.publishToNATS(event, message, isSynchronous, pairedResult.getLeft());
          pairedResult.getRight().ifPresent(this::publishToJetStream);
          break;
        case GET_MERGES:
          log.debug("received get merge data :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = this.getEventHandlerService().handleGetMergeEvent(event);
          this.publishToNATS(event, message, isSynchronous, response);
          break;
        case GET_MERGES_IN_DATE_RANGE:
          log.debug("received get merge in date range data :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = this.getEventHandlerService().handleGetMergeInDateRangeEvent(event);
          this.publishToNATS(event, message, isSynchronous, response);
          break;
        default:
          log.info("silently ignoring other event :: {}", event);
          break;
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }

  /**
   * Publish to nats.
   *
   * @param event         the event
   * @param message       the message
   * @param isSynchronous the is synchronous
   * @param response      the response
   */
  private void publishToNATS(final Event event, final Message message, final boolean isSynchronous, final byte[] response) {
    if (isSynchronous) { // this is for synchronous request/reply pattern.
      this.getMessagePublisher().dispatchMessage(message.getReplyTo(), response);
    } else { // this is for async.
      this.getMessagePublisher().dispatchMessage(event.getReplyTo(), response);
    }
  }

  /**
   * Publish to stan.
   *
   * @param event the event
   */
  private void publishToJetStream(@NonNull final ServicesEvent event) {
    this.publisher.dispatchChoreographyEvent(event);
  }

}
