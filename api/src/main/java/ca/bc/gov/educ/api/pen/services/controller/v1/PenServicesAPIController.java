package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.endpoint.v1.PenServicesAPIEndpoint;
import ca.bc.gov.educ.api.pen.services.mapper.v1.PenServicesMapper;
import ca.bc.gov.educ.api.pen.services.service.PenRequestBatchStudentValidationIssueCodesService;
import ca.bc.gov.educ.api.pen.services.service.PenRequestStudentRecordValidationService;
import ca.bc.gov.educ.api.pen.services.service.PenService;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * The type Pen validation api controller.
 */
@RestController
@Slf4j
public class PenServicesAPIController implements PenServicesAPIEndpoint {
  /**
   * The constant mapper.
   */
  private static final PenServicesMapper mapper = PenServicesMapper.mapper;
  /**
   * The Service.
   */
  private final PenRequestStudentRecordValidationService service;
  /**
   * The Pen service.
   */
  private final PenService penService;
  /**
   * The Prb validation issue codes service.
   */
  private final PenRequestBatchStudentValidationIssueCodesService prbValidationIssueCodesService;

  /**
   * Instantiates a new Pen validation api controller.
   *
   * @param service                        the service
   * @param penService                     the pen service
   * @param prbValidationIssueCodesService the pen request batch validation issue codes service
   */
  @Autowired
  public PenServicesAPIController(final PenRequestStudentRecordValidationService service, final PenService penService, final PenRequestBatchStudentValidationIssueCodesService prbValidationIssueCodesService) {
    this.service = service;
    this.penService = penService;
    this.prbValidationIssueCodesService = prbValidationIssueCodesService;
  }

  @Override
  public List<PenRequestStudentValidationIssue> validateStudentData(final PenRequestStudentValidationPayload validationPayload) {
    return this.service.validateStudentRecord(validationPayload);
  }

  @Override
  public CompletableFuture<String> getNextPenNumber(final UUID transactionID) {
    return CompletableFuture.completedFuture(this.penService.getNextPenNumber(transactionID.toString()));
  }

  @Override
  public List<PenRequestBatchStudentValidationFieldCode> getPrbStudentValidationIssueFieldCodes() {
    return this.prbValidationIssueCodesService.getAllPrbValidationFieldCodes().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public List<PenRequestBatchStudentValidationIssueSeverityCode> getPrbStudentValidationIssueSeverityCodes() {
    return this.prbValidationIssueCodesService.getAllPrbValidationIssueSeverityCodes().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public List<PenRequestBatchStudentValidationIssueTypeCode> getPrbStudentValidationIssueTypeCodes() {
    return this.prbValidationIssueCodesService.getAllPrbValidationIssueTypeCodes().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

}
