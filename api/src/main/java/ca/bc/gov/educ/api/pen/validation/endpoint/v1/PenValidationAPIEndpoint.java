package ca.bc.gov.educ.api.pen.validation.endpoint.v1;

import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * The interface Pen validation api endpoint.
 */
@RequestMapping("/api/v1/pen-validation")
@OpenAPIDefinition(info = @Info(title = "API for Pen Registry data Validation.", description = "This API is responsible for data validation of student requests .", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"VALIDATE_STUDENT_REQUEST_PEN"})})
public interface PenValidationAPIEndpoint {

  /**
   * Create pen request batch list.
   *
   * @param validationPayload the validation payload
   * @return the list
   */
  @PostMapping("/student-request")
  @PreAuthorize("#oauth2.hasAnyScope('VALIDATE_STUDENT_REQUEST_PEN')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Transactional
  @Tag(name = "Endpoint to validate student request.", description = "Endpoint to validate student request.")
  @Schema(name = "PenRequestStudentValidationIssue", implementation = PenRequestStudentValidationIssue.class)
  List<PenRequestStudentValidationIssue> createPenRequestBatch(@Validated @RequestBody PenRequestStudentValidationPayload validationPayload);
}
