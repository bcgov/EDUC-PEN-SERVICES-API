package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeDirectionCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Student merge direction code table repository.
 */
@Repository
public interface StudentMergeDirectionCodeTableRepository extends CrudRepository<StudentMergeDirectionCodeEntity, Long> {
  @Override
  List<StudentMergeDirectionCodeEntity> findAll();
}
