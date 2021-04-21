package ca.bc.gov.educ.api.pen.services.messaging;

import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * The type Message publisher.
 */
@Component
@Slf4j
public class MessagePublisher {


  /**
   * The Connection.
   */
  private final Connection connection;

  /**
   * Instantiates a new Message publisher.
   *
   * @param con the con
   */
  @Autowired
  public MessagePublisher(final Connection con) {
    this.connection = con;
  }

  /**
   * Dispatch message.
   *
   * @param subject the subject
   * @param message the message
   */
  public void dispatchMessage(final String subject, final byte[] message) {
    this.connection.publish(subject, message);
  }

  public Optional<String> requestMessage(final String subject, final byte[] message) throws InterruptedException {
    log.info("requesting from NATS on topic :: {} with payload :: {}", subject, new String(message));
    val response = this.connection.request(subject, message, Duration.ofSeconds(30)).getData();
    if (response == null || response.length == 0) {
      return Optional.empty();
    }
    final String responseValue = new String(response);
    log.info("got response from NATS :: {}", responseValue);
    return Optional.of(responseValue);
  }

/*  @Scheduled(fixedRate = 10000)
  public void test() throws JsonProcessingException {
    val event = new NotificationEvent();
    event.setEventType(EventType.GET_STUDENT);
    event.setEventOutcome(EventOutcome.STUDENT_FOUND);
    event.setSagaStatus("INITIATED");
    event.setEventPayload("");
    this.dispatchMessage(PEN_SERVICES_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event));
  }*/
}
