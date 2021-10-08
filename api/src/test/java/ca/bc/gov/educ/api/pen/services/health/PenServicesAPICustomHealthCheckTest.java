package ca.bc.gov.educ.api.pen.services.health;

import io.nats.client.Connection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.redisson.api.redisnode.RedisCluster;
import org.redisson.api.redisnode.RedisClusterMaster;
import org.redisson.api.redisnode.RedisClusterSlave;
import org.redisson.api.redisnode.RedisNodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest()
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class PenServicesAPICustomHealthCheckTest {

  @Autowired
  Connection natsConnection;

  @MockBean
  RedissonClient redissonClient;

  @Before
  public void setup() {
    Mockito.reset(redissonClient);
  }

  @Autowired
  private PenServicesAPICustomHealthCheck penServicesAPICustomHealthCheck;

  @Test
  public void testGetHealth_givenClosedNatsConnection_shouldReturnStatusDown() {
    when(natsConnection.getStatus()).thenReturn(Connection.Status.CLOSED);
    when(redissonClient.getRedisNodes(RedisNodes.CLUSTER)).thenReturn(null);
    assertThat(penServicesAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(penServicesAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testGetHealth_givenOpenNatsConnection_shouldReturnStatusUp() {
    when(natsConnection.getStatus()).thenReturn(Connection.Status.CONNECTED);
    when(redissonClient.getRedisNodes(RedisNodes.CLUSTER)).thenReturn(this.getRedisClusterMock(true));
    assertThat(penServicesAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(penServicesAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.UP);
  }


  @Test
  public void testHealth_givenClosedNatsConnection_shouldReturnStatusDown() {
    when(natsConnection.getStatus()).thenReturn(Connection.Status.CLOSED);
    when(redissonClient.getRedisNodes(RedisNodes.CLUSTER)).thenReturn(this.getRedisClusterMock(false));
    assertThat(penServicesAPICustomHealthCheck.health()).isNotNull();
    assertThat(penServicesAPICustomHealthCheck.health().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testHealth_givenOpenNatsConnection_shouldReturnStatusUp() {
    when(natsConnection.getStatus()).thenReturn(Connection.Status.CONNECTED);
    when(redissonClient.getRedisNodes(RedisNodes.CLUSTER)).thenReturn(this.getRedisClusterMock(true));
    assertThat(penServicesAPICustomHealthCheck.health()).isNotNull();
    assertThat(penServicesAPICustomHealthCheck.health().getStatus()).isEqualTo(Status.UP);
  }

  private RedisCluster getRedisClusterMock(boolean pingAll) {
    return new RedisCluster() {
      @Override
      public Collection<RedisClusterMaster> getMasters() {
        return null;
      }

      @Override
      public RedisClusterMaster getMaster(String address) {
        return null;
      }

      @Override
      public Collection<RedisClusterSlave> getSlaves() {
        return null;
      }

      @Override
      public RedisClusterSlave getSlave(String address) {
        return null;
      }

      @Override
      public boolean pingAll() {
        return pingAll;
      }

      @Override
      public boolean pingAll(long timeout, TimeUnit timeUnit) {
        return false;
      }
    };
  }
}
