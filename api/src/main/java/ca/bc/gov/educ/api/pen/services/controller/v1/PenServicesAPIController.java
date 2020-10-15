package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.endpoint.v1.PenServicesAPIEndpoint;
import ca.bc.gov.educ.api.pen.services.service.PenRequestStudentRecordValidationService;
import ca.bc.gov.educ.api.pen.services.service.PenService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The type Pen validation api controller.
 */
@RestController
@EnableResourceServer
@Slf4j
public class PenServicesAPIController implements PenServicesAPIEndpoint {
  private final PenRequestStudentRecordValidationService service;
  private final PenService penService;

  /**
   * Instantiates a new Pen validation api controller.
   *
   * @param service    the service
   * @param penService the pen service
   */
  @Autowired
  public PenServicesAPIController(PenRequestStudentRecordValidationService service, PenService penService) {
    this.service = service;
    this.penService = penService;
  }

  @Override
  public List<PenRequestStudentValidationIssue> validateStudentData(PenRequestStudentValidationPayload validationPayload) {
    return service.validateStudentRecord(validationPayload);
  }

  @Override
  public CompletableFuture<String> getNextPenNumber(UUID transactionID) {
    return CompletableFuture.completedFuture(penService.getNextPenNumber(transactionID.toString()));
  }
}
