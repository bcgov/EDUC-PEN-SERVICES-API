package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationIssueTypeCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Pen request batch validation issue type code repository.
 */
@Repository
public interface PenRequestBatchValidationIssueTypeCodeRepository extends CrudRepository<PenRequestBatchValidationIssueTypeCodeEntity, String> {
    /**
     * Find all list.
     *
     * @return the list
     */
    List<PenRequestBatchValidationIssueTypeCodeEntity> findAll();
}
