package ca.bc.gov.educ.api.pen.services.service;

import ca.bc.gov.educ.api.pen.services.rest.RestUtils;
import ca.bc.gov.educ.api.pen.services.support.TestRedisConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestRedisConfiguration.class})
@ActiveProfiles("testRedisson")
public class PenServiceTest {

  @MockBean
  RestUtils restUtils;

  @Autowired
  RedissonClient redissonClient;

  @Autowired
  PenService penService;

  String transactionID = UUID.randomUUID().toString();

  /**
   * need to delete the records to make it working in unit tests assertion, else the records will keep growing and assertions will fail.
   */
  @After
  public void after() {
    this.redissonClient.getBucket(transactionID).delete();
    this.redissonClient.getAtomicLong("NEXT_PEN_NUMBER").delete();
  }

  @Test
  public void testGetNextPenNumber__whenNewTransactionID_And_NoPenNumberInRedis_shouldCallAPI() throws JsonProcessingException {
    when(this.restUtils.getLatestPenNumberFromStudentAPI(transactionID)).thenReturn(120164446);
    final var penNumber = this.penService.getNextPenNumber(transactionID);
    assertThat(penNumber).startsWith(String.valueOf(120164447));
  }

  @Test
  public void testGetNextPenNumber__whenNewTransactionID_And_PenNumberInRedis_shouldNotCallAPI() throws JsonProcessingException {
    this.redissonClient.getAtomicLong("NEXT_PEN_NUMBER").set(120164447);
    final var penNumber = this.penService.getNextPenNumber(transactionID);
    assertThat(penNumber).startsWith(String.valueOf(120164448));
  }

  @Test
  public void testGetNextPenNumber__whenOldTransactionID_shouldReturnSamePen() throws JsonProcessingException {
    when(this.restUtils.getLatestPenNumberFromStudentAPI(transactionID)).thenReturn(120164446);
    final var penNumber1 = this.penService.getNextPenNumber(transactionID);
    assertThat(penNumber1).startsWith(String.valueOf(120164447));
    final var penNumber2 = this.penService.getNextPenNumber(transactionID);
    assertThat(penNumber2).startsWith(String.valueOf(120164447));
  }
}
