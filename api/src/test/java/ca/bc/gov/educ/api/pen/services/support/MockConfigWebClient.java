package ca.bc.gov.educ.api.pen.services.support;

import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.messaging.MessageSubscriber;
import ca.bc.gov.educ.api.pen.services.messaging.NatsConnection;
import ca.bc.gov.educ.api.pen.services.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.pen.services.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.pen.services.service.events.EventHandlerService;
import io.nats.client.Connection;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("testWebclient")
public class MockConfigWebClient {

  /**
   * Message publisher message publisher.
   *
   * @return the message publisher
   */
  @Bean
  @Primary
  public WebClient webClient() {
    return Mockito.mock(WebClient.class);
  }

  /**
   * Message publisher message publisher.
   *
   * @return the message publisher
   */
  @Bean
  @Primary
  public MessagePublisher messagePublisher() {
    return Mockito.mock(MessagePublisher.class);
  }

  /**
   * Message subscriber message subscriber.
   *
   * @return the message subscriber
   */
  @Bean
  @Primary
  public MessageSubscriber messageSubscriber() {
    return Mockito.mock(MessageSubscriber.class);
  }

  @Bean
  @Primary
  public Connection connection() {
    return Mockito.mock(Connection.class);
  }

  @Bean
  @Primary
  public NatsConnection natsConnection() {
    return Mockito.mock(NatsConnection.class);
  }

  @Bean
  @Primary
  public RedissonClient redisson() {
    return Mockito.mock(RedissonClient.class);
  }

  @Bean
  @Primary
  public Publisher publisher() {
    return Mockito.mock(Publisher.class);
  }

  @Bean
  @Primary
  public Subscriber subscriber() {
    return Mockito.mock(Subscriber.class);
  }


  @Bean
  @Primary
  public EventHandlerService eventHandlerService() {
    return Mockito.mock(EventHandlerService.class);
  }
}
