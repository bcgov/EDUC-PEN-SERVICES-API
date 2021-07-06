package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.endpoint.v1.PenServicesMacroEndpoint;
import ca.bc.gov.educ.api.pen.services.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.pen.services.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.pen.services.exception.errors.ApiError;
import ca.bc.gov.educ.api.pen.services.mapper.v1.PenServicesMacroMapper;
import ca.bc.gov.educ.api.pen.services.service.PenServicesMacroService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenServicesMacro;
import ca.bc.gov.educ.api.pen.services.util.RequestUtil;
import ca.bc.gov.educ.api.pen.services.validator.PenServicesMacroPayloadValidator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@Slf4j
public class PenServicesMacroController implements PenServicesMacroEndpoint {

  private static final PenServicesMacroMapper mapper = PenServicesMacroMapper.mapper;
  @Getter(PRIVATE)
  private final PenServicesMacroService penServicesMacroService;
  @Getter(PRIVATE)
  private final PenServicesMacroPayloadValidator penServicesMacroPayloadValidator;

  @Autowired
  public PenServicesMacroController(PenServicesMacroService penServicesMacroService, PenServicesMacroPayloadValidator penServicesMacroPayloadValidator) {
    this.penServicesMacroService = penServicesMacroService;
    this.penServicesMacroPayloadValidator = penServicesMacroPayloadValidator;
  }

  @Override
  public List<PenServicesMacro> findPenServicesMacros(String macroTypeCode) {
    if (StringUtils.isNotBlank(macroTypeCode)) {
      return getPenServicesMacroService().findMacrosByMacroTypeCode(macroTypeCode).stream().map(mapper::toStructure).collect(Collectors.toList());
    }
    return getPenServicesMacroService().findAllMacros().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public PenServicesMacro findPenServicesMacroById(UUID macroId) {
    val result = getPenServicesMacroService().getMacro(macroId);
    if (result.isPresent()) {
      return mapper.toStructure(result.get());
    }
    throw new EntityNotFoundException(PenServicesMacro.class, "macroId", macroId.toString());
  }

  @Override
  public PenServicesMacro createPenServicesMacro(PenServicesMacro penServicesMacro) {
    validatePayload(penServicesMacro, true);
    RequestUtil.setAuditColumnsForCreate(penServicesMacro);
    return mapper.toStructure(getPenServicesMacroService().createMacro(mapper.toModel(penServicesMacro)));
  }

  @Override
  public PenServicesMacro updatePenServicesMacro(UUID macroId, PenServicesMacro penServicesMacro) {
    validatePayload(penServicesMacro, false);
    RequestUtil.setAuditColumnsForUpdate(penServicesMacro);
    return mapper.toStructure(getPenServicesMacroService().updateMacro(macroId, mapper.toModel(penServicesMacro)));
  }

  private void validatePayload(PenServicesMacro penRequestMacro, boolean isCreateOperation) {
    val validationResult = getPenServicesMacroPayloadValidator().validatePayload(penRequestMacro, isCreateOperation);
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
  }
}
