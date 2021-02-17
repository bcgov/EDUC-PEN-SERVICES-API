package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationIssueSeverityCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Pen request batch validation issue severity code repository.
 */
@Repository
public interface PenRequestBatchValidationIssueSeverityCodeRepository extends CrudRepository<PenRequestBatchValidationIssueSeverityCodeEntity, String> {
    /**
     * Find all list.
     *
     * @return the list
     */
    @Override
    List<PenRequestBatchValidationIssueSeverityCodeEntity> findAll();
}
