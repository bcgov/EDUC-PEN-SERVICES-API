package ca.bc.gov.educ.api.pen.services.endpoint.v1;

import ca.bc.gov.educ.api.pen.services.struct.v1.PenServicesMacro;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.v1.URL.*;
import static org.springframework.http.HttpStatus.CREATED;

@RequestMapping(PEN_SERVICES+PEN_SERVICES_MACRO)
public interface PenServicesMacroEndpoint {

  @GetMapping
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_SERVICES_MACRO')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<PenServicesMacro> findPenServicesMacros(@RequestParam(value = "macroTypeCode", required = false) String macroTypeCode);

  @GetMapping(MACRO_ID)
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_SERVICES_MACRO')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  PenServicesMacro findPenServicesMacroById(@PathVariable UUID macroId);

  @PostMapping
  @PreAuthorize("hasAuthority('SCOPE_WRITE_PEN_SERVICES_MACRO')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED")})
  @ResponseStatus(CREATED)
  PenServicesMacro createPenServicesMacro(@Validated @RequestBody PenServicesMacro penServicesMacro);

  @PutMapping(MACRO_ID)
  @PreAuthorize("hasAuthority('SCOPE_WRITE_PEN_SERVICES_MACRO')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  PenServicesMacro updatePenServicesMacro(@PathVariable UUID macroId, @Validated @RequestBody PenServicesMacro penServicesMacro);
}
