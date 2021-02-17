package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeSourceCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Student merge source code table repository.
 */
@Repository
public interface StudentMergeSourceCodeTableRepository extends CrudRepository<StudentMergeSourceCodeEntity, Long> {
  @Override
  List<StudentMergeSourceCodeEntity> findAll();
}
