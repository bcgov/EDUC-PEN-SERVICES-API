package ca.bc.gov.educ.api.pen.services.validator;

import ca.bc.gov.educ.api.pen.services.exception.EntityNotFoundException;
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
import java.util.UUID;

@Component
public class StudentMergePayloadValidator extends BasePayloadValidator{

  public static final String MERGE_DIRECTION_CODE = "studentMergeDirectionCode";
  public static final String MERGE_SOURCE_CODE = "studentMergeSourceCode";
  public static final String STUDENT_ID = "studentID";
  public static final String MERGE_STUDENT_ID = "mergeStudentID";
  @Getter(AccessLevel.PRIVATE)
  private final StudentMergeService studentMergeService;


  @Autowired
  public StudentMergePayloadValidator(final StudentMergeService studentMergeService) {
    this.studentMergeService = studentMergeService;
  }

  public List<FieldError> validatePayload(String studentID, StudentMerge studentMerge, boolean isCreateOperation, StudentMergeEntity studentMergeEntity) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && studentMerge.getStudentMergeID() != null) {
      apiValidationErrors.add(createFieldError("studentMergeID", studentMerge.getStudentMergeID(), "studentMergeID should be null for post operation."));
    }
    validateMergeDirectionCode(studentMerge, apiValidationErrors);
    validateMergeSourceCode(studentMerge, apiValidationErrors);
    return apiValidationErrors;
  }

  protected void validateMergeDirectionCode(StudentMerge studentMerge, List<FieldError> apiValidationErrors) {
	  if(studentMerge.getStudentMergeDirectionCode() != null) {
	    Optional<StudentMergeDirectionCodeEntity> mergeDirectionCodeEntity = studentMergeService.findStudentMergeDirectionCode(studentMerge.getStudentMergeDirectionCode());
      validateMergeDirectionCodeAgainstDB(studentMerge.getStudentMergeDirectionCode(), apiValidationErrors, mergeDirectionCodeEntity);
    }
  }


  protected void validateMergeSourceCode(StudentMerge studentMerge, List<FieldError> apiValidationErrors) {
	  if(studentMerge.getStudentMergeSourceCode() != null) {
	    Optional<StudentMergeSourceCodeEntity> mergeSourceCodeEntity = studentMergeService.findStudentMergeSourceCode(studentMerge.getStudentMergeSourceCode());
      validateMergeSourceCodeAgainstDB(studentMerge.getStudentMergeSourceCode(), apiValidationErrors, mergeSourceCodeEntity);
    }
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError(STUDENT_MERGE, fieldName, rejectedValue, false, null, null, message);
  }

}
