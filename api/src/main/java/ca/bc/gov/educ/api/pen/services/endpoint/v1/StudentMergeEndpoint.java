package ca.bc.gov.educ.api.pen.services.endpoint.v1;

import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeSourceCode;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static ca.bc.gov.educ.api.pen.services.constants.v1.URL.*;

/**
 * The interface Student merge endpoint.
 */
@RequestMapping(PEN_SERVICES)
public interface StudentMergeEndpoint {
  /**
   * Find student merges list.
   *
   * @param studentID      the student id
   * @param mergeDirection the merge direction
   * @return the list
   */
  @GetMapping("/{studentID}" + MERGES)
  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_MERGE')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  List<StudentMerge> findStudentMerges(@PathVariable String studentID, @Param("mergeDirection") String mergeDirection);

  /**
   * Gets student merge source codes.
   *
   * @return the student merge source codes
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_MERGE_CODES')")
  @GetMapping(MERGE_SOURCE_CODES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<StudentMergeSourceCode> getStudentMergeSourceCodes();

  @Transactional(readOnly = true)
  @GetMapping(MERGES + BETWEEN_DATES_CREATED)
  @PreAuthorize("hasAuthority('SCOPE_READ_STUDENT_MERGE')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<StudentMerge> findStudentMergesBetweenDatesCreated(@RequestParam("createDateStart") String createDateStart, @RequestParam("createDateEnd") String createDateEnd, @RequestParam(value = "mergeDirectionCode", defaultValue = "TO") String mergeDirectionCode);

}
