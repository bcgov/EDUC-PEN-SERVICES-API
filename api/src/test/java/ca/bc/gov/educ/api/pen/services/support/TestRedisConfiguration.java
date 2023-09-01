package ca.bc.gov.educ.api.pen.services.support;

import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@TestConfiguration
public class TestRedisConfiguration {

  private final RedisServer redisServer;

  public TestRedisConfiguration() {
    this.redisServer = RedisServer.builder().port(6370).build();
  }

  @PostConstruct
  public void postConstruct() {
    this.redisServer.start();
  }

  @PreDestroy
  public void preDestroy() {
    this.redisServer.stop();
  }
}
