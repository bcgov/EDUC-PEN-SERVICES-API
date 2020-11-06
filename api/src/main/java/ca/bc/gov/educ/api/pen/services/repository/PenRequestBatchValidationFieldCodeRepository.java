package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationFieldCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Pen request batch validation field code repository.
 */
@Repository
public interface PenRequestBatchValidationFieldCodeRepository extends CrudRepository<PenRequestBatchValidationFieldCodeEntity, String> {
    /**
     * Find all list.
     *
     * @return the list
     */
    List<PenRequestBatchValidationFieldCodeEntity> findAll();
}
