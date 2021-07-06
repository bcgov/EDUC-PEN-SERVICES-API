package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.PenServicesMacroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PenServicesMacroRepository extends JpaRepository<PenServicesMacroEntity, UUID> {

  List<PenServicesMacroEntity> findAllByMacroTypeCode(String macroTypeCode);
}
