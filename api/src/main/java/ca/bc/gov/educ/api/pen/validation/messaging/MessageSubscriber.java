package ca.bc.gov.educ.api.pen.validation.messaging;

import ca.bc.gov.educ.api.pen.validation.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.validation.service.EventHandlerService;
import ca.bc.gov.educ.api.pen.validation.struct.v1.Event;
import ca.bc.gov.educ.api.pen.validation.util.JsonUtil;
import io.nats.streaming.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This listener uses durable queue groups of nats streaming client. A durable
 * queue group allows you to have all members leave but still maintain state.
 * When a member re-joins, it starts at the last position in that group. <b>DO
 * NOT call unsubscribe on the subscription.</b> please see the below for
 * details. Closing the Group The last member calling Unsubscribe will close
 * (that is destroy) the group. So if you want to maintain durability of the
 * group, <b>you should not be calling Unsubscribe.</b>
 * <p>
 * So unlike for non-durable queue subscribers, it is possible to maintain a
 * queue group with no member in the server. When a new member re-joins the
 * durable queue group, it will resume from where the group left of, actually
 * first receiving all unacknowledged messages that may have been left when the
 * last member previously left.
 */
@Component
@Slf4j
@SuppressWarnings("java:S2142")
public class MessageSubscriber extends MessagePubSub {


  private static final String PEN_VALIDATION_API_TOPIC = "PEN_VALIDATION_API_TOPIC";
  private final EventHandlerService eventHandlerService;

  /**
   * Instantiates a new Message subscriber.
   *
   * @param applicationProperties the application properties
   * @param eventHandlerService   the event handler service
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   */
  @Autowired
  public MessageSubscriber(final ApplicationProperties applicationProperties, EventHandlerService eventHandlerService) throws IOException, InterruptedException {
    this.eventHandlerService = eventHandlerService;
    var builder = new Options.Builder();
    builder.natsUrl(applicationProperties.getNatsUrl());
    builder.clusterId(applicationProperties.getNatsClusterId());
    builder.clientId("pen-validation-api-subscriber-" + UUID.randomUUID().toString());
    builder.connectionLostHandler(this::connectionLostHandler);
    connectionFactory = new StreamingConnectionFactory(builder.build());
    connection = connectionFactory.createConnection();
  }

  /**
   * This subscription will makes sure the messages are required to acknowledge manually to STAN.
   * Subscribe.
   */
  @PostConstruct
  public void subscribe() {


    String queue = PEN_VALIDATION_API_TOPIC.replace("_", "-");
    SubscriptionOptions options = new SubscriptionOptions.Builder().durableName(queue + "-consumer").build();// ":" is not allowed in durable name by NATS.
    try {
      connection.subscribe(PEN_VALIDATION_API_TOPIC, queue, onMessage(), options);
    } catch (IOException | InterruptedException | TimeoutException e) {
      throw new RuntimeException(e.getMessage());
    }
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
            eventHandlerService.handleValidateStudentDemogDataEvent(event);
          } catch (final Exception e) {
            log.error("Exception ", e);
          }
      }
    };
  }

  /**
   * This method will keep retrying for a connection.
   *
   * @param streamingConnection the streaming connection
   * @param e                   the e
   * @return the int
   */
  @Override
  protected int connectionLostHandler(StreamingConnection streamingConnection, Exception e) {
    int numOfRetries = 1;
    if (e != null) {
      numOfRetries = super.connectionLostHandler(streamingConnection, e);
      retrySubscription(numOfRetries);
    }
    return numOfRetries;
  }

  /**
   * Retry subscription.
   *
   * @param numOfRetries the num of retries
   */
  private void retrySubscription(int numOfRetries) {
    while (true) {
      try {
        log.trace("retrying subscription as connection was lost :: retrying ::" + numOfRetries++);
        this.subscribe();
        log.info("successfully resubscribed after {} attempts", numOfRetries);
        break;
      } catch (Exception exception) {
        log.error("exception occurred while retrying subscription", exception);
        try {
          double sleepTime = (2 * numOfRetries);
          TimeUnit.SECONDS.sleep((long) sleepTime);
        } catch (InterruptedException exc) {
          log.error("InterruptedException occurred while retrying subscription", exc);
        }
      }
    }
  }
}
