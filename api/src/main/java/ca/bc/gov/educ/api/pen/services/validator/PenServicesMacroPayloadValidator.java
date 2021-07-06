package ca.bc.gov.educ.api.pen.services.validator;

import ca.bc.gov.educ.api.pen.services.service.PenServicesMacroService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenServicesMacro;
import lombok.Getter;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Component
public class PenServicesMacroPayloadValidator {
  public static final String MACRO_TYPE_CODE = "macroTypeCode";
  @Getter(PRIVATE)
  private final PenServicesMacroService penServicesMacroService;

  public PenServicesMacroPayloadValidator(PenServicesMacroService penServicesMacroService) {
    this.penServicesMacroService = penServicesMacroService;
  }

  public List<FieldError> validatePayload(PenServicesMacro penServicesMacro, boolean isCreateOperation) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && penServicesMacro.getMacroId() != null) {
      apiValidationErrors.add(createFieldError("macroId", penServicesMacro.getMacroId(), "macroId should be null for post operation."));
    }
    validateMacroTypeCode(penServicesMacro.getMacroTypeCode(), apiValidationErrors);
    return apiValidationErrors;
  }

  private void validateMacroTypeCode(String macroTypeCode, List<FieldError> apiValidationErrors) {
    val result = getPenServicesMacroService().getMacroTypeCode(macroTypeCode);
    if (result.isPresent()) {
      val entity = result.get();
      if (entity.getEffectiveDate().isAfter(LocalDate.now())) {
        apiValidationErrors.add(createFieldError(MACRO_TYPE_CODE, macroTypeCode, "macroTypeCode is not yet effective."));
      } else if (entity.getExpiryDate().isBefore(LocalDate.now())) {
        apiValidationErrors.add(createFieldError(MACRO_TYPE_CODE, macroTypeCode, "macroTypeCode is expired."));
      }
    } else {
      apiValidationErrors.add(createFieldError(MACRO_TYPE_CODE, macroTypeCode, "macroTypeCode Invalid."));
    }
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError(PenServicesMacro.class.getName(), fieldName, rejectedValue, false, null, null, message);
  }
}
