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
   * The Event handler delegator service.
   */
  @Getter(PRIVATE)
  private final EventHandlerDelegatorService eventHandlerDelegatorService;

  @Getter(PRIVATE)
  private final Map<String, EventHandler> handlerMap = new HashMap<>();

  @Autowired
  public MessageSubscriber(final Connection con, EventHandlerDelegatorService eventHandlerDelegatorService, final List<EventHandler> eventHandlers) {
    this.eventHandlerDelegatorService = eventHandlerDelegatorService;
    super.connection = con;
    eventHandlers.forEach(handler -> {
      handlerMap.put(handler.getTopicToSubscribe(), handler);
      subscribe(handler.getTopicToSubscribe(), handler);
    });
  }

  private void subscribe(String topic, EventHandler eventHandler) {
    if (!handlerMap.containsKey(topic)) {
      handlerMap.put(topic, eventHandler);
    }
    String queue = topic.replace("_", "-");
    var dispatcher = connection.createDispatcher(onSagaMessage(eventHandler));
    dispatcher.subscribe(topic, queue);
  }

  /**
   * This subscription will makes sure the messages are required to acknowledge manually to STAN.
   * Subscribe.
   */
  @PostConstruct
  public void subscribeForAPI() {
    String queue = PEN_SERVICES_API_TOPIC.toString().replace("_", "-");
    var dispatcher = connection.createDispatcher(onMessage());
    dispatcher.subscribe(PEN_SERVICES_API_TOPIC.toString(), queue);
  }

  /**
   * On message message handler.
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

  /**
   * On message message handler.
   *
   * @return the message handler
   */
  public MessageHandler onSagaMessage(EventHandler eventHandler) {
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


}
