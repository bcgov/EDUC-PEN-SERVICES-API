package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.endpoint.v1.StudentMergeEndpoint;
import ca.bc.gov.educ.api.pen.services.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.pen.services.exception.errors.ApiError;
import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMergeMapper;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeEntity;
import ca.bc.gov.educ.api.pen.services.service.StudentMergeService;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeSourceCode;
import ca.bc.gov.educ.api.pen.services.util.RequestUtil;
import ca.bc.gov.educ.api.pen.services.validator.StudentMergePayloadValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * Student Merge controller
 *
 * @author Mingwei
 */

@RestController
@Slf4j
public class StudentMergeController implements StudentMergeEndpoint {
  @Getter(AccessLevel.PRIVATE)
  private final StudentMergeService service;

  @Getter(AccessLevel.PRIVATE)
  private final StudentMergePayloadValidator payloadValidator;
  private static final StudentMergeMapper mapper = StudentMergeMapper.mapper;

  @Autowired
  StudentMergeController(final StudentMergeService studentMergeService, StudentMergePayloadValidator payloadValidator) {
    this.service = studentMergeService;
    this.payloadValidator = payloadValidator;
  }

  public List<StudentMerge> findStudentMerges(String studentID, String mergeDirection) {
    return getService().findStudentMerges(UUID.fromString(studentID), mergeDirection).stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  public StudentMerge createStudentMerge(String studentID, StudentMerge studentMerge) {
    RequestUtil.setAuditColumnsForCreate(studentMerge);
    StudentMergeEntity entity = mapper.toModel(studentMerge);
    validatePayload(studentID, studentMerge, true, entity);
    return mapper.toStructure(getService().createStudentMerge(entity));
  }

  public List<StudentMergeSourceCode> getStudentMergeSourceCodes() {
    return getService().getStudentMergeSourceCodesList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  private void validatePayload(String studentID, StudentMerge studentMerge, boolean isCreateOperation, StudentMergeEntity studentMergeEntity) {
    val validationResult = getPayloadValidator().validatePayload(studentID, studentMerge, isCreateOperation, studentMergeEntity);
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
  }

}
