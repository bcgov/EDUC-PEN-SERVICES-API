package ca.bc.gov.educ.api.pen.services.service.events;

import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.struct.v1.Event;
import io.nats.client.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
  public static final String PAYLOAD_LOG = "Payload is :: ";
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
   * Instantiates a new Event handler delegator service.
   *
   * @param eventHandlerService the event handler service
   * @param messagePublisher    the message publisher
   */
  @Autowired
  public EventHandlerDelegatorService(EventHandlerService eventHandlerService, MessagePublisher messagePublisher) {
    this.eventHandlerService = eventHandlerService;
    this.messagePublisher = messagePublisher;
  }

  /**
   * Handle event.
   *
   * @param event   the event
   * @param message the message
   */
  @Async("subscriberExecutor")
  public void handleEvent(Event event, Message message) {
    boolean isSynchronous = message.getReplyTo() != null;
    byte[] response;
    try {
      switch (event.getEventType()) {
        case VALIDATE_STUDENT_DEMOGRAPHICS:
          log.info("received validate student demographics event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = getEventHandlerService().handleValidateStudentDemogDataEvent(event);
          if (isSynchronous) { // this is for synchronous request/reply pattern.
            getMessagePublisher().dispatchMessage(message.getReplyTo(), response);
          } else { // this is for async.
            getMessagePublisher().dispatchMessage(event.getReplyTo(), response);
          }
          break;
        case GET_NEXT_PEN_NUMBER:
          log.info("received get next pen number event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = getEventHandlerService().handleGetNextPenNumberEvent(event);
          if (isSynchronous) { // this is for synchronous request/reply pattern.
            getMessagePublisher().dispatchMessage(message.getReplyTo(), response);
          } else { // this is for async.
            getMessagePublisher().dispatchMessage(event.getReplyTo(), response);
          }
          break;
        case CREATE_MERGE:
          log.info("received create merge data :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = getEventHandlerService().handleCreateMergeEvent(event);
          if (isSynchronous) { // this is for synchronous request/reply pattern.
            getMessagePublisher().dispatchMessage(message.getReplyTo(), response);
          } else { // this is for async.
            getMessagePublisher().dispatchMessage(event.getReplyTo(), response);
          }
          break;
        case DELETE_MERGE:
          log.info("received delete merge data :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = getEventHandlerService().handleDeleteMergeEvent(event);
          if (isSynchronous) { // this is for synchronous request/reply pattern.
            getMessagePublisher().dispatchMessage(message.getReplyTo(), response);
          } else { // this is for async.
            getMessagePublisher().dispatchMessage(event.getReplyTo(), response);
          }
          break;
        default:
          log.info("silently ignoring other event :: {}", event);
          break;
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }
}
