package ca.bc.gov.educ.api.pen.services.messaging.jetstream;

import ca.bc.gov.educ.api.pen.services.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.services.service.events.JetStreamEventHandlerService;
import ca.bc.gov.educ.api.pen.services.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.Message;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.PEN_SERVICES_EVENTS_TOPIC;


/**
 * The type Subscriber.
 */
@Component
@DependsOn("publisher")
@Slf4j
public class Subscriber {
  /**
   * The Stan event handler service.
   */
  private final JetStreamEventHandlerService jetStreamEventHandlerService;
  private final Connection natsConnection;

  /**
   * Instantiates a new Subscriber.
   *
   * @param natsConnection               the nats connection
   * @param jetStreamEventHandlerService the stan event handler service
   */
  @Autowired
  public Subscriber(final Connection natsConnection, final JetStreamEventHandlerService jetStreamEventHandlerService) {
    this.jetStreamEventHandlerService = jetStreamEventHandlerService;
    this.natsConnection = natsConnection;
  }


  /**
   * This subscription will makes sure the messages are required to acknowledge manually to jet stream..
   * Subscribe.
   *
   * @throws IOException           the io exception
   * @throws JetStreamApiException the jet stream api exception
   */
  @PostConstruct
  public void subscribe() throws IOException, JetStreamApiException {
    val qName = "PEN-SERVICES-EVENTS-TOPIC-PEN-SERVICES-API";
    val autoAck = false;
    final PushSubscribeOptions options = PushSubscribeOptions.builder().stream(ApplicationProperties.STREAM_NAME)
        .durable("PEN-SERVICES-API-PEN-SERVICES-EVENTS-TOPIC-DURABLE")
        .configuration(ConsumerConfiguration.builder().deliverPolicy(DeliverPolicy.New).build()).build();
    this.natsConnection.jetStream().subscribe(PEN_SERVICES_EVENTS_TOPIC.toString(), qName, this.natsConnection.createDispatcher(), this::onPenServicesEventsTopic,
        autoAck, options);
  }

  /**
   * This method will process the event message pushed into the PEN_SERVICES_EVENTS_TOPIC.
   * this will get the message and update the event status to mark that the event reached the message broker.
   * On message message handler.
   *
   * @param message the string representation of {@link ChoreographedEvent} if it not type of event then it will throw exception and will be ignored.
   */
  public void onPenServicesEventsTopic(final Message message) {
    log.debug("Received message :: {}", message);
    try {
      final String eventString = new String(message.getData());
      final ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
      log.info("received event :: {} ", event);
      this.jetStreamEventHandlerService.updateEventStatus(event);
      message.ack();
    } catch (final Exception ex) {
      log.error("Exception ", ex);
    }
  }
}
