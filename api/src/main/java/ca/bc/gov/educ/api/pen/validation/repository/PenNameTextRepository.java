package ca.bc.gov.educ.api.pen.validation.repository;

import ca.bc.gov.educ.api.pen.validation.model.PENNameText;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Pen name text repository.
 */
@Repository
public interface PenNameTextRepository extends CrudRepository<PENNameText, Integer> {
  List<PENNameText> findAll();
}
