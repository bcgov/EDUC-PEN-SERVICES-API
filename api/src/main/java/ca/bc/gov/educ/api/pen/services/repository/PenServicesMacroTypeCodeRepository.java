package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.PenServicesMacroTypeCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PenServicesMacroTypeCodeRepository extends JpaRepository<PenServicesMacroTypeCodeEntity, String> {
}
