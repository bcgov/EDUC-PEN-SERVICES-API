package ca.bc.gov.educ.api.pen.services.struct.v1;

import ca.bc.gov.educ.api.pen.services.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PenServicesMacro extends BaseRequest {
  private String macroId;

  @NotNull(message = "macroCode cannot be null")
  @Size(max = 10)
  private String macroCode;

  @NotNull(message = "macroTypeCode cannot be null")
  @Size(max = 10)
  private String macroTypeCode;

  @NotNull(message = "macroText cannot be null")
  @Size(max = 4000)
  private String macroText;
}
