package ca.bc.gov.educ.api.pen.services.validator;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeDirectionCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeSourceCodeEntity;
import ca.bc.gov.educ.api.pen.services.service.StudentMergeService;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The type Student merge payload validator.
 */
@Component
public class StudentMergePayloadValidator extends BasePayloadValidator {

  /**
   * The constant MERGE_DIRECTION_CODE.
   */
  public static final String MERGE_DIRECTION_CODE = "studentMergeDirectionCode";
  /**
   * The constant MERGE_SOURCE_CODE.
   */
  public static final String MERGE_SOURCE_CODE = "studentMergeSourceCode";
  /**
   * The constant STUDENT_ID.
   */
  public static final String STUDENT_ID = "studentID";
  /**
   * The constant MERGE_STUDENT_ID.
   */
  public static final String MERGE_STUDENT_ID = "mergeStudentID";
  /**
   * The Student merge service.
   */
  @Getter(AccessLevel.PRIVATE)
  private final StudentMergeService studentMergeService;


  /**
   * Instantiates a new Student merge payload validator.
   *
   * @param studentMergeService the student merge service
   */
  @Autowired
  public StudentMergePayloadValidator(final StudentMergeService studentMergeService) {
    this.studentMergeService = studentMergeService;
  }

  /**
   * Validate payload list.
   *
   * @param studentID          the student id
   * @param studentMerge       the student merge
   * @param isCreateOperation  the is create operation
   * @param studentMergeEntity the student merge entity
   * @return the list
   */
  public List<FieldError> validatePayload(final String studentID, final StudentMerge studentMerge, final boolean isCreateOperation, final StudentMergeEntity studentMergeEntity) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && studentMerge.getStudentMergeID() != null) {
      apiValidationErrors.add(this.createFieldError("studentMergeID", studentMerge.getStudentMergeID(), "studentMergeID should be null for post operation."));
    }
    this.validateMergeDirectionCode(studentMerge, apiValidationErrors);
    this.validateMergeSourceCode(studentMerge, apiValidationErrors);
    return apiValidationErrors;
  }

  /**
   * Validate merge direction code.
   *
   * @param studentMerge        the student merge
   * @param apiValidationErrors the api validation errors
   */
  protected void validateMergeDirectionCode(final StudentMerge studentMerge, final List<FieldError> apiValidationErrors) {
    if (studentMerge.getStudentMergeDirectionCode() != null) {
      final Optional<StudentMergeDirectionCodeEntity> mergeDirectionCodeEntity = this.studentMergeService.findStudentMergeDirectionCode(studentMerge.getStudentMergeDirectionCode());
      this.validateMergeDirectionCodeAgainstDB(studentMerge.getStudentMergeDirectionCode(), apiValidationErrors, mergeDirectionCodeEntity);
    }
  }


  /**
   * Validate merge source code.
   *
   * @param studentMerge        the student merge
   * @param apiValidationErrors the api validation errors
   */
  protected void validateMergeSourceCode(final StudentMerge studentMerge, final List<FieldError> apiValidationErrors) {
    if (studentMerge.getStudentMergeSourceCode() != null) {
      final Optional<StudentMergeSourceCodeEntity> mergeSourceCodeEntity = this.studentMergeService.findStudentMergeSourceCode(studentMerge.getStudentMergeSourceCode());
      this.validateMergeSourceCodeAgainstDB(studentMerge.getStudentMergeSourceCode(), apiValidationErrors, mergeSourceCodeEntity);
    }
  }

  /**
   * Create field error field error.
   *
   * @param fieldName     the field name
   * @param rejectedValue the rejected value
   * @param message       the message
   * @return the field error
   */
  private FieldError createFieldError(final String fieldName, final Object rejectedValue, final String message) {
    return new FieldError(STUDENT_MERGE, fieldName, rejectedValue, false, null, null, message);
  }

}
