package ca.bc.gov.educ.api.pen.validation.controller.v1;

import ca.bc.gov.educ.api.pen.validation.endpoint.v1.PenValidationAPIEndpoint;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Pen validation api controller.
 */
@RestController
@EnableResourceServer
@Slf4j
public class PenValidationAPIController implements PenValidationAPIEndpoint {
  @Override
  public List<PenRequestStudentValidationIssue> createPenRequestBatch(PenRequestStudentValidationPayload validationPayload) {
    return new ArrayList<>();
  }
}
