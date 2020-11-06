package ca.bc.gov.educ.api.pen.services.endpoint.v1;

import ca.bc.gov.educ.api.pen.services.struct.v1.*;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The interface Pen validation api endpoint.
 */
@RequestMapping("/api/v1/pen-services")
@OpenAPIDefinition(info = @Info(title = "API to provide different miscellaneous services for Pen Registry.", description = "This API is responsible for providing different miscellaneous services.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"VALIDATE_STUDENT_DEMOGRAPHICS"})})
public interface PenServicesAPIEndpoint {

  /**
   * Validate student data.
   *
   * @param validationPayload the validation payload
   * @return the list
   */
  @PostMapping("/validation/student-request")
  @PreAuthorize("#oauth2.hasScope('VALIDATE_STUDENT_DEMOGRAPHICS')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Transactional
  @Tag(name = "Endpoint to validate student request.", description = "Endpoint to validate student request.")
  @Schema(name = "PenRequestStudentValidationIssue", implementation = PenRequestStudentValidationIssue.class)
  List<PenRequestStudentValidationIssue> validateStudentData(@Validated @RequestBody PenRequestStudentValidationPayload validationPayload);

  @GetMapping("/next-pen-number")
  @PreAuthorize("#oauth2.hasScope('GET_NEXT_PEN_NUMBER')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  @Transactional
  @Tag(name = "Endpoint to generate a new PEN number and return the same.", description = "Endpoint to generate a new PEN number and return the same. The transaction ID is mandatory, so that for each unique transaction only one PEN is issued.")
  CompletableFuture<String> getNextPenNumber(@RequestParam("transactionID") UUID transactionID);

  /**
   * Gets list of pen request batch student validation field codes
   *
   * @return the list
   */
  @PreAuthorize("#oauth2.hasScope('READ_VALIDATION_CODES')")
  @GetMapping("/validation/issue-field-code")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<PenRequestBatchStudentValidationFieldCode> getPrbStudentValidationIssueFieldCodes();

  /**
   * Gets list of pen request batch student validation severity codes
   *
   * @return the list
   */
  @PreAuthorize("#oauth2.hasScope('READ_VALIDATION_CODES')")
  @GetMapping("/validation/issue-severity-code")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<PenRequestBatchStudentValidationIssueSeverityCode> getPrbStudentValidationIssueSeverityCodes();

  /**
   * Gets list of pen request batch student validation type codes
   *
   * @return the list
   */
  @PreAuthorize("#oauth2.hasScope('READ_VALIDATION_CODES')")
  @GetMapping("/validation/issue-type-code")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<PenRequestBatchStudentValidationIssueTypeCode> getPrbStudentValidationIssueTypeCodes();
}
