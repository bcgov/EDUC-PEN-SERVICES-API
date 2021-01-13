package ca.bc.gov.educ.api.pen.services.validator;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeDirectionCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeSourceCodeEntity;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ca.bc.gov.educ.api.pen.services.validator.StudentMergePayloadValidator.MERGE_DIRECTION_CODE;
import static ca.bc.gov.educ.api.pen.services.validator.StudentMergePayloadValidator.MERGE_SOURCE_CODE;

public abstract class BasePayloadValidator {

  public static final String STUDENT_TWIN = "studentTwin";
  public static final String STUDENT_MERGE = "studentMerge";

  protected void validateMergeDirectionCodeAgainstDB(String mergeDirectionCode, List<FieldError> apiValidationErrors, Optional<StudentMergeDirectionCodeEntity> mergeDirectionCodeEntity) {
    if (mergeDirectionCodeEntity.isEmpty()) {
      apiValidationErrors.add(createFieldError(STUDENT_MERGE, MERGE_DIRECTION_CODE, mergeDirectionCode, "Invalid Student Merge Direction Code."));
    } else if (mergeDirectionCodeEntity.get().getEffectiveDate() != null && mergeDirectionCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
      apiValidationErrors.add(createFieldError(STUDENT_MERGE, MERGE_DIRECTION_CODE, mergeDirectionCode, "Student Merge Direction Code provided is not yet effective."));
    } else if (mergeDirectionCodeEntity.get().getExpiryDate() != null && mergeDirectionCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
      apiValidationErrors.add(createFieldError(STUDENT_MERGE, MERGE_DIRECTION_CODE, mergeDirectionCode, "Student Merge Direction Code provided has expired."));
    }
  }

  protected void validateMergeSourceCodeAgainstDB(String mergeSourceCode , List<FieldError> apiValidationErrors, Optional<StudentMergeSourceCodeEntity> mergeSourceCodeEntity) {
    if (mergeSourceCodeEntity.isEmpty()) {
      apiValidationErrors.add(createFieldError(STUDENT_MERGE, MERGE_SOURCE_CODE, mergeSourceCode, "Invalid Student Merge Source Code."));
    } else if (mergeSourceCodeEntity.get().getEffectiveDate() != null && mergeSourceCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
      apiValidationErrors.add(createFieldError(STUDENT_MERGE, MERGE_SOURCE_CODE, mergeSourceCode, "Student Merge Source Code provided is not yet effective."));
    } else if (mergeSourceCodeEntity.get().getExpiryDate() != null && mergeSourceCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
      apiValidationErrors.add(createFieldError(STUDENT_MERGE, MERGE_SOURCE_CODE, mergeSourceCode, "Student Merge Source Code provided has expired."));
    }
  }

  protected FieldError createFieldError(String objectName, String fieldName, Object rejectedValue, String message) {
    return new FieldError(objectName, fieldName, rejectedValue, false, null, null, message);
  }
}
