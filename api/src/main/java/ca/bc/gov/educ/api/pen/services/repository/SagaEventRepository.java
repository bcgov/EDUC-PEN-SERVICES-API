package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.model.SagaEventStates;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Saga event repository.
 */
@Repository
public interface SagaEventRepository extends CrudRepository<SagaEventStates, UUID> {
  /**
   * Find by saga list.
   *
   * @param saga the saga
   * @return the list
   */
  List<SagaEventStates> findBySaga(Saga saga);

  /**
   * Find by saga and saga event outcome and saga event state and saga step number optional.
   *
   * @param saga         the saga
   * @param eventOutcome the event outcome
   * @param eventState   the event state
   * @param stepNumber   the step number
   * @return the optional
   */
  Optional<SagaEventStates> findBySagaAndSagaEventOutcomeAndSagaEventStateAndSagaStepNumber(Saga saga, String eventOutcome, String eventState, int stepNumber);

  @Transactional
  @Modifying
  @Query(value = "delete from PEN_SERVICES_SAGA_EVENT_STATES e where exists(select 1 from PEN_SERVICES_SAGA s where s.SAGA_ID = e.SAGA_ID and s.CREATE_DATE <= :createDate)", nativeQuery = true)
  void deleteBySaga_createDateBefore(LocalDateTime createDate);
}
