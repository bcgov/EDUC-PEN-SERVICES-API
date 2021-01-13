package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeDirectionCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentMergeDirectionCodeTableRepository extends CrudRepository<StudentMergeDirectionCodeEntity, Long> {
    List<StudentMergeDirectionCodeEntity> findAll();
}
