package ca.bc.gov.educ.api.pen.services.schedulers;

import ca.bc.gov.educ.api.pen.services.constants.EventOutcome;
import ca.bc.gov.educ.api.pen.services.constants.EventStatus;
import ca.bc.gov.educ.api.pen.services.constants.EventType;
import ca.bc.gov.educ.api.pen.services.messaging.NatsConnection;
import ca.bc.gov.educ.api.pen.services.messaging.stan.Publisher;
import ca.bc.gov.educ.api.pen.services.model.ServicesEvent;
import ca.bc.gov.educ.api.pen.services.repository.ServicesEventRepository;
import net.javacrumbs.shedlock.core.LockAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class STANEventSchedulerTest {

  @Autowired
  ServicesEventRepository eventRepository;

  @Autowired
  STANEventScheduler stanEventScheduler;

  @Autowired
  NatsConnection natsConnection;

  @Autowired
  Publisher publisher;

  @Before
  public void before() {
    LockAssert.TestHelper.makeAllAssertsPass(true);
  }

  @After
  public void tearDown() {
    this.eventRepository.deleteAll();
  }

  @Test
  public void testFindAndPublishStudentEventsToSTAN_givenNoRecordsInDB_shouldDoNothing() {
    final var invocations = mockingDetails(this.publisher).getInvocations().size();
    this.stanEventScheduler.findAndPublishStudentEventsToSTAN();
    verify(this.publisher, atMost(invocations)).dispatchChoreographyEvent(any());
  }

  @Test
  public void testFindAndPublishStudentEventsToSTAN_givenRecordsInDBButLessThan5Minutes_shouldDoNothing() {
    final var invocations = mockingDetails(this.publisher).getInvocations().size();
    this.eventRepository.save(this.createPlaceHolderEvent(1));
    this.stanEventScheduler.findAndPublishStudentEventsToSTAN();
    verify(this.publisher, atMost(invocations)).dispatchChoreographyEvent(any());
  }

  @Test
  public void testFindAndPublishStudentEventsToSTAN_givenRecordsInDBButGreaterThan5Minutes_shouldSendMessagesToSTAN() {
    final var invocations = mockingDetails(this.publisher).getInvocations().size();
    this.eventRepository.save(this.createPlaceHolderEvent(10));
    this.stanEventScheduler.findAndPublishStudentEventsToSTAN();
    verify(this.publisher, atLeast(invocations + 1)).dispatchChoreographyEvent(any());
  }

  private ServicesEvent createPlaceHolderEvent(final int subtractMinutes) {
    return ServicesEvent.builder().eventPayload("test_payload").eventId(UUID.randomUUID()).eventType(EventType.CREATE_MERGE.toString())
        .eventOutcome(EventOutcome.MERGE_CREATED.toString())
        .createDate(LocalDateTime.now().minusMinutes(subtractMinutes))
        .updateDate(LocalDateTime.now().minusMinutes(subtractMinutes))
        .eventStatus(EventStatus.DB_COMMITTED.toString())
        .createUser("TEST")
        .updateUser("TEST")
        .build();
  }
}
