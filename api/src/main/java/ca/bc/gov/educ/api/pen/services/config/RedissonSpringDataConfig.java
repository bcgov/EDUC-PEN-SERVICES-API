package ca.bc.gov.educ.api.pen.services.config;

import ca.bc.gov.educ.api.pen.services.properties.ApplicationProperties;
import lombok.val;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * The type Redisson spring data config.
 */
@Configuration
@Profile("!testWebclient")
public class RedissonSpringDataConfig {

  /**
   * The Application properties.
   */
  private final ApplicationProperties applicationProperties;

  /**
   * Instantiates a new Redisson spring data config.
   *
   * @param applicationProperties the application properties
   */
  public RedissonSpringDataConfig(final ApplicationProperties applicationProperties) {
    this.applicationProperties = applicationProperties;
  }

  /**
   * Redisson connection factory redisson connection factory.
   *
   * @param redisson the redisson
   * @return the redisson connection factory
   */
  @Bean
  public RedissonConnectionFactory redissonConnectionFactory(final RedissonClient redisson) {
    return new RedissonConnectionFactory(redisson);
  }

  /**
   * Redisson redisson client.
   *
   * @return the redisson client
   */
  @Bean(destroyMethod = "shutdown")
  public RedissonClient redisson() {
    final RedissonClient redisson;
    val config = new Config();
    if ("local".equals(this.applicationProperties.getEnvironment())) {
      config.useSingleServer()
          .setAddress(this.applicationProperties.getRedisUrl());
    } else {
      config.useClusterServers()
          .setMasterConnectionMinimumIdleSize(2)
          .setSlaveConnectionMinimumIdleSize(2)
          .addNodeAddress(this.applicationProperties.getRedisUrl());
    }
    redisson = Redisson.create(config);
    redisson.getConfig().setCodec(StringCodec.INSTANCE);
    return redisson;
  }
}
