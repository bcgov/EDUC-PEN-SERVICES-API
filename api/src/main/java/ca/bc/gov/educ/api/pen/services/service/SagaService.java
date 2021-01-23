package ca.bc.gov.educ.api.pen.services.service;

import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.model.SagaEventStates;
import ca.bc.gov.educ.api.pen.services.repository.SagaEventRepository;
import ca.bc.gov.educ.api.pen.services.repository.SagaRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.EventType.INITIATED;
import static ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum.STARTED;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Saga service.
 */
@Service
@Slf4j
public class SagaService {
  /**
   * The Saga repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final SagaRepository sagaRepository;
  /**
   * The Saga event repository.
   */
  @Getter(PRIVATE)
  private final SagaEventRepository sagaEventRepository;

  /**
   * Instantiates a new Saga service.
   *
   * @param sagaRepository      the saga repository
   * @param sagaEventRepository the saga event repository
   */
  @Autowired
  public SagaService(final SagaRepository sagaRepository, SagaEventRepository sagaEventRepository) {
    this.sagaRepository = sagaRepository;
    this.sagaEventRepository = sagaEventRepository;
  }


  /**
   * Create saga record saga.
   *
   * @param saga the saga
   * @return the saga
   */
  public Saga createSagaRecord(Saga saga) {
    return getSagaRepository().save(saga);
  }

  /**
   * no need to do a get here as it is an attached entity
   * first find the child record, if exist do not add. this scenario may occur in replay process,
   * so dont remove this check. removing this check will lead to duplicate records in the child table.
   *
   * @param saga      the saga object.
   * @param sagaEventStates the saga event
   */
  @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(multiplier = 2, delay = 2000))
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateAttachedSagaWithEvents(Saga saga, SagaEventStates sagaEventStates) {
    saga.setUpdateDate(LocalDateTime.now());
    getSagaRepository().save(saga);
    val result = getSagaEventRepository()
        .findBySagaAndSagaEventOutcomeAndSagaEventStateAndSagaStepNumber(saga, sagaEventStates.getSagaEventOutcome(), sagaEventStates.getSagaEventState(), sagaEventStates.getSagaStepNumber() - 1); //check if the previous step was same and had same outcome, and it is due to replay.
    if (result.isEmpty()) {
      getSagaEventRepository().save(sagaEventStates);
    }
  }

  /**
   * Find saga by id optional.
   *
   * @param sagaId the saga id
   * @return the optional
   */
  public Optional<Saga> findSagaById(UUID sagaId) {
    return getSagaRepository().findById(sagaId);
  }

  /**
   * Find all saga states list.
   *
   * @param saga the saga
   * @return the list
   */
  public List<SagaEventStates> findAllSagaStates(Saga saga) {
    return getSagaEventRepository().findBySaga(saga);
  }


  /**
   * Update saga record.
   *
   * @param saga the saga
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public void updateSagaRecord(Saga saga) { // saga here MUST be an attached entity
    getSagaRepository().save(saga);
  }

  /**
   * Find by student id optional.
   *
   * @param studentID     the student id
   * @return the list
   */
  public Optional<Saga> findByStudentIDAndSagaName(UUID studentID, String sagaName){
    return getSagaRepository().findByStudentIDAndSagaName(studentID, sagaName);
  }

  public List<Saga> findAllByStudentIDAndStatusIn(UUID studentID, String sagaName, List<String> statuses){
    return getSagaRepository().findAllByStudentIDAndSagaNameAndStatusIn(studentID, sagaName, statuses);
  }

  @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(multiplier = 2, delay = 2000))
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateAttachedEntityDuringSagaProcess(Saga saga){
    getSagaRepository().save(saga);
  }

  /**
   * Create saga record in db saga.
   *
   * @param sagaName            the saga name
   * @param userName            the user name
   * @param payload             the payload
   * @param studentID           the student id
   * @return the saga
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Saga createSagaRecordInDB(String sagaName, String userName, String payload, UUID studentID) {
    var saga = Saga
      .builder()
      .payload(payload)
      .studentID(studentID)
      .sagaName(sagaName)
      .status(STARTED.toString())
      .sagaState(INITIATED.toString())
      .createDate(LocalDateTime.now())
      .createUser(userName)
      .updateUser(userName)
      .updateDate(LocalDateTime.now())
      .build();
    return createSagaRecord(saga);
  }
}
