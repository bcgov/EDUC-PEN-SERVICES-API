package ca.bc.gov.educ.api.pen.services.messaging.stan;

import ca.bc.gov.educ.api.pen.services.constants.EventOutcome;
import ca.bc.gov.educ.api.pen.services.constants.EventType;
import ca.bc.gov.educ.api.pen.services.model.ServicesEvent;
import ca.bc.gov.educ.api.pen.services.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.services.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.PEN_SERVICES_EVENTS_TOPIC;


/**
 * The type Publisher.
 */
@Component
@Slf4j
public class Publisher extends PubSub implements Closeable {
  /**
   * The Connection factory.
   */
  private final StreamingConnectionFactory connectionFactory;
  /**
   * The Connection.
   */
  private StreamingConnection connection;

  /**
   * Instantiates a new Publisher.
   *
   * @param applicationProperties the application properties
   * @param natsConnection        the nats connection
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   */
  @Autowired
  public Publisher(final ApplicationProperties applicationProperties, final Connection natsConnection) throws IOException, InterruptedException {
    final Options options = new Options.Builder()
        .clusterId(applicationProperties.getStanCluster())
        .connectionLostHandler(this::connectionLostHandler)
        .natsConn(natsConnection)
        .maxPingsOut(30)
        .pingInterval(Duration.ofSeconds(2))
        .clientId("pen-services-api-publisher" + UUID.randomUUID().toString()).build();
    this.connectionFactory = new StreamingConnectionFactory(options);
    this.connection = this.connectionFactory.createConnection();
  }

  /**
   * Connection lost handler.
   *
   * @param streamingConnection the streaming connection
   * @param exception           the exception
   */
  private void connectionLostHandler(final StreamingConnection streamingConnection, final Exception exception) {
    this.connection = super.connectionLostHandler(this.connectionFactory);
  }


  /**
   * Dispatch choreography event.
   *
   * @param event the event
   */
  public void dispatchChoreographyEvent(final ServicesEvent event) {
    if (event != null && event.getEventId() != null) {
      final ChoreographedEvent choreographedEvent = new ChoreographedEvent();
      choreographedEvent.setEventType(EventType.valueOf(event.getEventType()));
      choreographedEvent.setEventOutcome(EventOutcome.valueOf(event.getEventOutcome()));
      choreographedEvent.setEventPayload(event.getEventPayload());
      choreographedEvent.setEventID(event.getEventId().toString());
      choreographedEvent.setCreateUser(event.getCreateUser());
      choreographedEvent.setUpdateUser(event.getUpdateUser());
      try {
        log.info("Broadcasting event :: {}", choreographedEvent);
        this.connection.publish(PEN_SERVICES_EVENTS_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(choreographedEvent));
      } catch (final IOException | TimeoutException e) {
        log.error("exception while broadcasting message to STAN", e);
      } catch (final InterruptedException e) {
        log.error("exception while broadcasting message to STAN", e);
        Thread.currentThread().interrupt();
      }
    }
  }


  /**
   * Closes this stream and releases any system resources associated
   * with it. If the stream is already closed then invoking this
   * method has no effect.
   *
   * <p> As noted in {@link AutoCloseable#close()}, cases where the
   * close may fail require careful attention. It is strongly advised
   * to relinquish the underlying resources and to internally
   * <em>mark</em> the {@code Closeable} as closed, prior to throwing
   * the {@code IOException}.
   */
  @Override
  public void close() {
    super.close(this.connection);
  }
}
