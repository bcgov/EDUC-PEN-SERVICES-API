package ca.bc.gov.educ.api.pen.services.messaging;

import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
}
