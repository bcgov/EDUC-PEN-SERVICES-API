package ca.bc.gov.educ.api.pen.services.endpoint.v1;

import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeCompleteSagaData;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static ca.bc.gov.educ.api.pen.services.constants.v1.URL.PEN_SERVICES;

@RequestMapping(PEN_SERVICES)
public interface PenServicesSagaEndpoint {

  @PostMapping("/student-merge-complete-saga")
  @PreAuthorize("hasAuthority('SCOPE_STUDENT_MERGE_COMPLETE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK.")})
  ResponseEntity<String> completeStudentMerge(@Validated @RequestBody StudentMergeCompleteSagaData studentMergeCompleteSagaData);

}
