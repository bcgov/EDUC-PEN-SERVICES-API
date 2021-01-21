package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.Saga;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Saga repository.
 */
@Repository
public interface SagaRepository extends CrudRepository<Saga, UUID> {
  /**
   * Find all by status in list.
   *
   * @param statuses the statuses
   * @return the list
   */
  List<Saga> findAllByStatusIn(List<String> statuses);

  /**
   * Find all list.
   *
   * @return the list
   */
  List<Saga> findAll();

  /**
   * Find by student id optional.
   *
   * @param studentID      the student id
   * @param sagaName       the saga name
   * @return the optional Saga
   */
  Optional<Saga> findByStudentIDAndSagaName(UUID studentID, String sagaName);

  /**
   * ind by pen request batch student id and status
   *
   * @param studentID       the student id
   * @param sagaName        the saga name
   * @param statuses        the statuses
   * @return the list
   */
  List<Saga> findAllByStudentIDAndSagaNameAndStatusIn(UUID studentID, String sagaName, List<String> statuses);

  List<Saga> findAllByCreateDateBefore(LocalDateTime createDate);
}
