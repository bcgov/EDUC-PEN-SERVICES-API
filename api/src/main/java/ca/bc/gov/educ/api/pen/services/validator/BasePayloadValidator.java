package ca.bc.gov.educ.api.pen.services.validator;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeDirectionCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeSourceCodeEntity;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ca.bc.gov.educ.api.pen.services.validator.StudentMergePayloadValidator.MERGE_DIRECTION_CODE;
import static ca.bc.gov.educ.api.pen.services.validator.StudentMergePayloadValidator.MERGE_SOURCE_CODE;

/**
 * The type Base payload validator.
 */
public abstract class BasePayloadValidator {

  /**
   * The constant STUDENT_TWIN.
   */
  public static final String STUDENT_TWIN = "studentTwin";
  /**
   * The constant STUDENT_MERGE.
   */
  public static final String STUDENT_MERGE = "studentMerge";

  /**
   * Validate merge direction code against db.
   *
   * @param mergeDirectionCode       the merge direction code
   * @param apiValidationErrors      the api validation errors
   * @param mergeDirectionCodeEntity the merge direction code entity
   */
  protected void validateMergeDirectionCodeAgainstDB(final String mergeDirectionCode, final List<FieldError> apiValidationErrors, final Optional<StudentMergeDirectionCodeEntity> mergeDirectionCodeEntity) {
    if (mergeDirectionCodeEntity.isEmpty()) {
      apiValidationErrors.add(this.createFieldError(STUDENT_MERGE, MERGE_DIRECTION_CODE, mergeDirectionCode, "Invalid Student Merge Direction Code."));
    } else if (mergeDirectionCodeEntity.get().getEffectiveDate() != null && mergeDirectionCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
      apiValidationErrors.add(this.createFieldError(STUDENT_MERGE, MERGE_DIRECTION_CODE, mergeDirectionCode, "Student Merge Direction Code provided is not yet effective."));
    } else if (mergeDirectionCodeEntity.get().getExpiryDate() != null && mergeDirectionCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
      apiValidationErrors.add(this.createFieldError(STUDENT_MERGE, MERGE_DIRECTION_CODE, mergeDirectionCode, "Student Merge Direction Code provided has expired."));
    }
  }

  /**
   * Validate merge source code against db.
   *
   * @param mergeSourceCode       the merge source code
   * @param apiValidationErrors   the api validation errors
   * @param mergeSourceCodeEntity the merge source code entity
   */
  protected void validateMergeSourceCodeAgainstDB(final String mergeSourceCode, final List<FieldError> apiValidationErrors, final Optional<StudentMergeSourceCodeEntity> mergeSourceCodeEntity) {
    if (mergeSourceCodeEntity.isEmpty()) {
      apiValidationErrors.add(this.createFieldError(STUDENT_MERGE, MERGE_SOURCE_CODE, mergeSourceCode, "Invalid Student Merge Source Code."));
    } else if (mergeSourceCodeEntity.get().getEffectiveDate() != null && mergeSourceCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
      apiValidationErrors.add(this.createFieldError(STUDENT_MERGE, MERGE_SOURCE_CODE, mergeSourceCode, "Student Merge Source Code provided is not yet effective."));
    } else if (mergeSourceCodeEntity.get().getExpiryDate() != null && mergeSourceCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
      apiValidationErrors.add(this.createFieldError(STUDENT_MERGE, MERGE_SOURCE_CODE, mergeSourceCode, "Student Merge Source Code provided has expired."));
    }
  }

  /**
   * Create field error field error.
   *
   * @param objectName    the object name
   * @param fieldName     the field name
   * @param rejectedValue the rejected value
   * @param message       the message
   * @return the field error
   */
  protected FieldError createFieldError(final String objectName, final String fieldName, final Object rejectedValue, final String message) {
    return new FieldError(objectName, fieldName, rejectedValue, false, null, null, message);
  }
}
