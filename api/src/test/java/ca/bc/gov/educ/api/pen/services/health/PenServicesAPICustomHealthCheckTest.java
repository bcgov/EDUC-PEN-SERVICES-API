package ca.bc.gov.educ.api.pen.services.health;

import ca.bc.gov.educ.api.pen.services.PenServicesApiResourceApplication;
import ca.bc.gov.educ.api.pen.services.support.TestRedisConfiguration;
import io.nats.client.Connection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {TestRedisConfiguration.class, PenServicesApiResourceApplication.class})
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class PenServicesAPICustomHealthCheckTest {

  @Autowired
  Connection natsConnection;

  @Autowired
  private PenServicesAPICustomHealthCheck penServicesAPICustomHealthCheck;

  @Test
  public void testGetHealth_givenClosedNatsConnection_shouldReturnStatusDown() {
    when(natsConnection.getStatus()).thenReturn(Connection.Status.CLOSED);
    assertThat(penServicesAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(penServicesAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testGetHealth_givenOpenNatsConnection_shouldReturnStatusUp() {
    when(natsConnection.getStatus()).thenReturn(Connection.Status.CONNECTED);
    assertThat(penServicesAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(penServicesAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.UP);
  }


  @Test
  public void testHealth_givenClosedNatsConnection_shouldReturnStatusDown() {
    when(natsConnection.getStatus()).thenReturn(Connection.Status.CLOSED);
    assertThat(penServicesAPICustomHealthCheck.health()).isNotNull();
    assertThat(penServicesAPICustomHealthCheck.health().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testHealth_givenOpenNatsConnection_shouldReturnStatusUp() {
    when(natsConnection.getStatus()).thenReturn(Connection.Status.CONNECTED);
    assertThat(penServicesAPICustomHealthCheck.health()).isNotNull();
    assertThat(penServicesAPICustomHealthCheck.health().getStatus()).isEqualTo(Status.UP);
  }
}
