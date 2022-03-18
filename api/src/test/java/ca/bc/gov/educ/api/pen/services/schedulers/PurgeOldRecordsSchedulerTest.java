package ca.bc.gov.educ.api.pen.services.schedulers;

import ca.bc.gov.educ.api.pen.services.PenServicesApiResourceApplication;
import ca.bc.gov.educ.api.pen.services.constants.EventStatus;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.model.SagaEventStates;
import ca.bc.gov.educ.api.pen.services.model.ServicesEvent;
import ca.bc.gov.educ.api.pen.services.repository.SagaEventRepository;
import ca.bc.gov.educ.api.pen.services.repository.SagaRepository;
import ca.bc.gov.educ.api.pen.services.repository.ServicesEventRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

import static ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum.COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PenServicesApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PurgeOldRecordsSchedulerTest {

  @Autowired
  SagaRepository repository;

  @Autowired
  SagaEventRepository sagaEventRepository;

  @Autowired
  ServicesEventRepository servicesEventRepository;

  @Autowired
  PurgeOldRecordsScheduler purgeOldRecordsScheduler;


  @Test
  public void testPurgeOldRecords_givenOldRecordsPresent_shouldBeDeleted() {
    final String penRequestBatchID = "7f000101-7151-1d84-8171-5187006c0000";
    final String getPenRequestBatchStudentID = "7f000101-7151-1d84-8171-5187006c0001";
    final var payload = " {\n" +
        "    \"createUser\": \"test\",\n" +
        "    \"updateUser\": \"test\",\n" +
        "    \"penRequestBatchID\": \"" + penRequestBatchID + "\",\n" +
        "    \"penRequestBatchStudentID\": \"" + getPenRequestBatchStudentID + "\",\n" +
        "    \"legalFirstName\": \"Jack\"\n" +
        "  }";
    final var saga_today = this.getSaga(payload, LocalDateTime.now());
    final var yesterday = LocalDateTime.now().minusDays(1);
    final var saga_yesterday = this.getSaga(payload, yesterday);

    this.repository.save(saga_today);
    this.sagaEventRepository.save(this.getSagaEvent(saga_today, payload));
    this.servicesEventRepository.save(this.getServicesEvent(saga_today, payload, LocalDateTime.now()));

    this.repository.save(saga_yesterday);
    this.sagaEventRepository.save(this.getSagaEvent(saga_yesterday, payload));
    this.servicesEventRepository.save(this.getServicesEvent(saga_yesterday, payload, yesterday));

    this.purgeOldRecordsScheduler.setSagaRecordStaleInDays(1);
    this.purgeOldRecordsScheduler.purgeOldRecords();
    final var sagas = this.repository.findAll();
    assertThat(sagas).hasSize(1);

    final var sagaEvents = this.sagaEventRepository.findAll();
    assertThat(sagaEvents).hasSize(1);

    final var servicesEvents = this.servicesEventRepository.findAll();
    assertThat(servicesEvents).hasSize(1);
  }


  private Saga getSaga(final String payload, final LocalDateTime createDateTime) {
    return Saga
        .builder()
        .payload(payload)
        .sagaName("PEN_SERVICES_STUDENT_DEMERGE_COMPLETE_SAGA")
        .status(COMPLETED.toString())
        .sagaState(COMPLETED.toString())
        .createDate(createDateTime)
        .createUser("PEN_SERVICES_API")
        .updateUser("PEN_SERVICES_API")
        .updateDate(createDateTime)
        .build();
  }

  private SagaEventStates getSagaEvent(final Saga saga, final String payload) {
    return SagaEventStates
        .builder()
        .sagaEventResponse(payload)
        .saga(saga)
        .sagaEventState("UPDATE_STUDENT")
        .sagaStepNumber(3)
        .sagaEventOutcome("STUDENT_UPDATED")
        .createDate(LocalDateTime.now())
        .createUser("PEN_SERVICES_API")
        .updateUser("PEN_SERVICES_API")
        .updateDate(LocalDateTime.now())
        .build();
  }

  private ServicesEvent getServicesEvent(final Saga saga, final String payload, final LocalDateTime createDateTime) {
    return ServicesEvent
      .builder()
      .eventPayloadBytes(payload.getBytes())
      .eventStatus(EventStatus.MESSAGE_PUBLISHED.toString())
      .eventType("UPDATE_STUDENT")
      .sagaId(saga.getSagaId())
      .eventOutcome("STUDENT_UPDATED")
      .replyChannel("TEST_CHANNEL")
      .createDate(createDateTime)
      .createUser("PEN_SERVICES_API")
      .updateUser("PEN_SERVICES_API")
      .updateDate(createDateTime)
      .build();
  }
}
