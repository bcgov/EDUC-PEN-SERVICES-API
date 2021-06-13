package ca.bc.gov.educ.api.pen.services.messaging;

import ca.bc.gov.educ.api.pen.services.helpers.LogHelper;
import ca.bc.gov.educ.api.pen.services.orchestrator.base.EventHandler;
import ca.bc.gov.educ.api.pen.services.service.events.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.pen.services.struct.v1.Event;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.PEN_SERVICES_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Message subscriber.
 */
@Component
@Slf4j
public class MessageSubscriber {

  /**
   * The Event Handlers as orchestrator for SAGA
   */
  @Getter(PRIVATE)
  private final Map<String, EventHandler> handlerMap = new HashMap<>();

  /**
   * The Event handler as delegator service for API
   */
  @Getter(PRIVATE)
  private final EventHandlerDelegatorService eventHandlerDelegatorService;
  /**
   * The Connection.
   */
  private final Connection connection;

  /**
   * Instantiates a new Message subscriber.
   *
   * @param con                          the con
   * @param eventHandlerDelegatorService the event handler delegator service
   * @param eventHandlers                the event handlers
   */
  @Autowired
  public MessageSubscriber(final Connection con, final EventHandlerDelegatorService eventHandlerDelegatorService, final List<EventHandler> eventHandlers) {
    this.eventHandlerDelegatorService = eventHandlerDelegatorService;
    this.connection = con;
    eventHandlers.forEach(handler -> {
      this.handlerMap.put(handler.getTopicToSubscribe(), handler);
      this.subscribeForSAGA(handler.getTopicToSubscribe(), handler);
    });
  }

  /**
   * On message, event handler for SAGA
   *
   * @param eventHandler the orchestrator
   * @return the message handler
   */
  private static MessageHandler onMessageForSAGA(final EventHandler eventHandler) {
    return (Message message) -> {
      if (message != null) {
        log.info("Message received subject :: {},  replyTo :: {}, subscriptionID :: {}", message.getSubject(), message.getReplyTo(), message.getSID());
        try {
          final var eventString = new String(message.getData());
          final var event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
          eventHandler.handleEvent(event);
        } catch (final Exception e) {
          log.error("Exception ", e);
        }
      }
    };
  }

  /**
   * Subscribe the topic on messages for SAGA
   *
   * @param topic        the topic name
   * @param eventHandler the orchestrator
   */
  private void subscribeForSAGA(final String topic, final EventHandler eventHandler) {
    this.handlerMap.computeIfAbsent(topic, k -> eventHandler);
    final String queue = topic.replace("_", "-");
    final var dispatcher = this.connection.createDispatcher(MessageSubscriber.onMessageForSAGA(eventHandler));
    dispatcher.subscribe(topic, queue);
  }

  /**
   * Subscribe the topic on messages for API
   */
  @PostConstruct
  public void subscribe() {
    final String queue = PEN_SERVICES_API_TOPIC.toString().replace("_", "-");
    final var dispatcher = this.connection.createDispatcher(this.onMessage());
    dispatcher.subscribe(PEN_SERVICES_API_TOPIC.toString(), queue);
  }

  /**
   * On message, event handler for API
   *
   * @return the message handler
   */
  private MessageHandler onMessage() {
    return (Message message) -> {
      if (message != null) {
        log.info("Message received is :: {} ", message);
        try {
          final var eventString = new String(message.getData());
          LogHelper.logMessagingEventDetails(eventString);
          final var event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
          this.eventHandlerDelegatorService.handleEvent(event, message);
          log.debug("Event is :: {}", event);
        } catch (final Exception e) {
          log.error("Exception ", e);
        }
      }
    };
  }
}
