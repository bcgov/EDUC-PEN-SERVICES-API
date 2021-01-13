package ca.bc.gov.educ.api.pen.services.endpoint.v1;

import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeSourceCode;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static ca.bc.gov.educ.api.pen.services.constants.v1.URL.*;
import static org.springframework.http.HttpStatus.CREATED;

@RequestMapping(PEN_SERVICES)
public interface StudentMergeEndpoint {
  @GetMapping("/{studentID}" + MERGES)
  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_MERGE')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  List<StudentMerge> findStudentMerges(@PathVariable String studentID, @Param("mergeDirection") String mergeDirection);

  @PostMapping("/{studentID}" + MERGES)
  @PreAuthorize("hasAuthority('SCOPE_WRITE_STUDENT_MERGE')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  StudentMerge createStudentMerge(@PathVariable String studentID, @Validated @RequestBody StudentMerge studentMerge);

  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_MERGE_CODES')")
  @GetMapping(MERGE_SOURCE_CODES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<StudentMergeSourceCode> getStudentMergeSourceCodes();
}
