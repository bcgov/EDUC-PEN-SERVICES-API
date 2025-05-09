package ca.bc.gov.educ.api.pen.services.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class holds all application properties
 *
 * @author Marco Villeneuve
 */
@Component
@Getter
@Setter
public class ApplicationProperties {

  /**
   * The constant PEN_SERVICES_API.
   */
  public static final String PEN_SERVICES_API = "PEN-SERVICES-API";
  public static final String STREAM_NAME = "PEN_SERVICES_EVENTS";
  public static final String CORRELATION_ID = "correlationID";
  /**
   * The Client id.
   */
  @Value("${client.id}")
  private String clientID;
  /**
   * The Client secret.
   */
  @Value("${client.secret}")
  private String clientSecret;
  /**
   * The Token url.
   */
  @Value("${url.token}")
  private String tokenURL;
  /**
   * The Student api url.
   */
  @Value("${url.api.student}")
  private String studentApiURL;
  /**
   * The Server.
   */
  @Value("${nats.server}")
  private String server;
  /**
   * The Max reconnect.
   */
  @Value("${nats.maxReconnect}")
  private int maxReconnect;
  /**
   * The Connection name.
   */
  @Value("${nats.connectionName}")
  private String connectionName;

  @Value("${ramp.up.http}")
  private Boolean isHttpRampUp;

  @Value("${url.api.institute}")
  private String instituteApiURL;
}
