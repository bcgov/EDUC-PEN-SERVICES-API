package ca.bc.gov.educ.api.pen.services.endpoint.v1;

import ca.bc.gov.educ.api.pen.services.struct.v1.Saga;
import ca.bc.gov.educ.api.pen.services.struct.v1.SplitPenSagaData;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentDemergeCompleteSagaData;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeCompleteSagaData;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.v1.URL.PEN_SERVICES;

/**
 * The interface Pen services saga endpoint.
 */
@RequestMapping(PEN_SERVICES)
public interface PenServicesSagaEndpoint {

  /**
   * Read saga response entity.
   *
   * @param sagaID the saga id
   * @return the response entity
   */
  @GetMapping("/saga/{sagaID}")
  @PreAuthorize("hasAuthority('SCOPE_PEN_SERVICES_READ_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK."), @ApiResponse(responseCode = "404", description = "Not Found.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to retrieve saga by its ID (GUID).", description = "Endpoint to retrieve saga by its ID (GUID).")
  ResponseEntity<Saga> readSaga(@PathVariable UUID sagaID);

  /**
   * Complete student merge response entity.
   *
   * @param studentMergeCompleteSagaData the student merge complete saga data
   * @return the response entity
   */
  @PostMapping("/student-merge-complete-saga")
  @PreAuthorize("hasAuthority('SCOPE_STUDENT_MERGE_COMPLETE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK.")})
  ResponseEntity<String> completeStudentMerge(@Validated @RequestBody StudentMergeCompleteSagaData studentMergeCompleteSagaData);

  /**
   * Complete student demerge response entity.
   *
   * @param studentDemergeCompleteSagaData the student demerge complete saga data
   * @return the response entity
   */
  @PostMapping("/student-demerge-complete-saga")
  @PreAuthorize("hasAuthority('SCOPE_STUDENT_DEMERGE_COMPLETE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK.")})
  ResponseEntity<String> completeStudentDemerge(@Validated @RequestBody StudentDemergeCompleteSagaData studentDemergeCompleteSagaData);

  /**
   * Complete split pen response entity.
   *
   * @param splitPenSagaData the split pen saga data
   * @return the response entity
   */
  @PostMapping("/split-pen-saga")
  @PreAuthorize("hasAuthority('SCOPE_STUDENT_SPLIT_PEN_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK.")})
  ResponseEntity<String> splitPen(@Validated @RequestBody SplitPenSagaData splitPenSagaData);
}
