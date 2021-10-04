package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.endpoint.v1.StudentMergeEndpoint;
import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMergeMapper;
import ca.bc.gov.educ.api.pen.services.service.StudentMergeService;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeSourceCode;
import ca.bc.gov.educ.api.pen.services.validator.StudentMergePayloadValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Student Merge controller
 *
 * @author Mingwei
 */
@RestController
@Slf4j
public class StudentMergeController implements StudentMergeEndpoint {
  /**
   * The constant mapper.
   */
  private static final StudentMergeMapper mapper = StudentMergeMapper.mapper;
  /**
   * The Service.
   */
  @Getter(AccessLevel.PRIVATE)
  private final StudentMergeService service;
  /**
   * The Payload validator.
   */
  @Getter(AccessLevel.PRIVATE)
  private final StudentMergePayloadValidator payloadValidator;

  /**
   * Instantiates a new Student merge controller.
   *
   * @param studentMergeService the student merge service
   * @param payloadValidator    the payload validator
   */
  @Autowired
  StudentMergeController(final StudentMergeService studentMergeService, final StudentMergePayloadValidator payloadValidator) {
    this.service = studentMergeService;
    this.payloadValidator = payloadValidator;
  }

  @Override
  public List<StudentMerge> findStudentMerges(final String studentID, final String mergeDirection) {
    return this.getService().findStudentMerges(UUID.fromString(studentID), mergeDirection).stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public List<StudentMergeSourceCode> getStudentMergeSourceCodes() {
    return this.getService().getStudentMergeSourceCodesList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public List<StudentMerge> findStudentMergesBetweenDatesCreated(String createDateStart, String createDateEnd, String mergeDirectionCode) {
    return this.getService().findStudentMerges(LocalDateTime.parse(createDateStart), LocalDateTime.parse(createDateEnd), mergeDirectionCode).stream().map(mapper::toStructure).collect(Collectors.toList());
  }

}
