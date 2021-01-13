package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeSourceCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentMergeSourceCodeTableRepository extends CrudRepository<StudentMergeSourceCodeEntity, Long> {
    List<StudentMergeSourceCodeEntity> findAll();
}
