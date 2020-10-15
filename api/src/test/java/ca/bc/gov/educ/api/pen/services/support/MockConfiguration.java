package ca.bc.gov.educ.api.pen.services.support;

import ca.bc.gov.educ.api.pen.services.config.RedissonSpringDataConfig;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.messaging.MessageSubscriber;
import ca.bc.gov.educ.api.pen.services.rest.RestUtils;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * The type Mock configuration.
 */
@Profile("test")
@Configuration
public class MockConfiguration {
  /**
   * Message publisher message publisher.
   *
   * @return the message publisher
   */
  @Bean
  @Primary
  public RestUtils restUtils() {
    return Mockito.mock(RestUtils.class);
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


  /**
   * Redisson spring data config redisson spring data config.
   *
   * @return the redisson spring data config
   */
  @Bean
  @Primary
  public RedissonSpringDataConfig redissonSpringDataConfig() {
    return Mockito.mock(RedissonSpringDataConfig.class);
  }


  /**
   * Redisson connection factory redisson connection factory.
   *
   * @return the redisson connection factory
   */
  @Bean
  @Primary
  public RedissonConnectionFactory redissonConnectionFactory() {
    return Mockito.mock(RedissonConnectionFactory.class);
  }

  /**
   * Redisson client redisson client.
   *
   * @return the redisson client
   */
  @Bean
  @Primary
  public RedissonClient redissonClient() {
    return Mockito.mock(RedissonClient.class);
  }
}
