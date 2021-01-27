package ca.bc.gov.educ.api.pen.services.messaging;

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
public class MessageSubscriber extends MessagePubSub {

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

  @Autowired
  public MessageSubscriber(final Connection con, EventHandlerDelegatorService eventHandlerDelegatorService, final List<EventHandler> eventHandlers) {
    this.eventHandlerDelegatorService = eventHandlerDelegatorService;
    super.connection = con;
    eventHandlers.forEach(handler -> {
      handlerMap.put(handler.getTopicToSubscribe(), handler);
      subscribeForSAGA(handler.getTopicToSubscribe(), handler);
    });
  }

  /**
   * Subscribe the topic on messages for SAGA
   *
   * @param topic         the topic name
   * @param eventHandler  the orchestrator
   */
  private void subscribeForSAGA(String topic, EventHandler eventHandler) {
    if (!handlerMap.containsKey(topic)) {
      handlerMap.put(topic, eventHandler);
    }
    String queue = topic.replace("_", "-");
    var dispatcher = connection.createDispatcher(onMessageForSAGA(eventHandler));
    dispatcher.subscribe(topic, queue);
  }

  /**
   * On message, event handler for SAGA
   * 
   * @param eventHandler  the orchestrator
   * @return the message handler
   */
  private MessageHandler onMessageForSAGA(EventHandler eventHandler) {
    return (Message message) -> {
      if (message != null) {
        log.info("Message received subject :: {},  replyTo :: {}, subscriptionID :: {}", message.getSubject(), message.getReplyTo(), message.getSID());
        try {
          var eventString = new String(message.getData());
          var event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
          eventHandler.handleEvent(event);
        } catch (final Exception e) {
          log.error("Exception ", e);
        }
      }
    };
  }

  /**
   * Subscribe the topic on messages for API
   */
  @PostConstruct
  public void subscribe() {
    String queue = PEN_SERVICES_API_TOPIC.toString().replace("_", "-");
    var dispatcher = connection.createDispatcher(onMessage());
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
          var eventString = new String(message.getData());
          var event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
          eventHandlerDelegatorService.handleEvent(event, message);
          log.debug("Event is :: {}", event);
        } catch (final Exception e) {
          log.error("Exception ", e);
        }
      }
    };
  }
}
