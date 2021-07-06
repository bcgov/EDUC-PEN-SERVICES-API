package ca.bc.gov.educ.api.pen.services.service;

import ca.bc.gov.educ.api.pen.services.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.pen.services.model.PenServicesMacroEntity;
import ca.bc.gov.educ.api.pen.services.model.PenServicesMacroTypeCodeEntity;
import ca.bc.gov.educ.api.pen.services.repository.PenServicesMacroRepository;
import ca.bc.gov.educ.api.pen.services.repository.PenServicesMacroTypeCodeRepository;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Service
public class PenServicesMacroService {
  @Getter(PRIVATE)
  private final PenServicesMacroRepository penServicesMacroRepository;
  @Getter(PRIVATE)
  private final PenServicesMacroTypeCodeRepository penServicesMacroTypeCodeRepository;

  @Autowired
  public PenServicesMacroService(PenServicesMacroRepository penServicesMacroRepository, PenServicesMacroTypeCodeRepository penServicesMacroTypeCodeRepository) {
    this.penServicesMacroRepository = penServicesMacroRepository;
    this.penServicesMacroTypeCodeRepository = penServicesMacroTypeCodeRepository;
  }

  public Optional<PenServicesMacroTypeCodeEntity> getMacroTypeCode(String code) {
    return getPenServicesMacroTypeCodeRepository().findById(code);
  }

  public List<PenServicesMacroEntity> findAllMacros() {
    return getPenServicesMacroRepository().findAll();
  }

  public Optional<PenServicesMacroEntity> getMacro(UUID macroId) {
    return getPenServicesMacroRepository().findById(macroId);
  }

  public List<PenServicesMacroEntity> findMacrosByMacroTypeCode(String macroTypeCode) {
    return getPenServicesMacroRepository().findAllByMacroTypeCode(macroTypeCode);
  }

  public PenServicesMacroEntity createMacro(PenServicesMacroEntity entity) {
    return getPenServicesMacroRepository().save(entity);
  }

  public PenServicesMacroEntity updateMacro(UUID macroId, PenServicesMacroEntity entity) {
    val result = getPenServicesMacroRepository().findById(macroId);
    if (result.isPresent()) {
      return getPenServicesMacroRepository().save(entity);
    } else {
      throw new EntityNotFoundException(entity.getClass(),"macroId", macroId.toString());
    }
  }
}
