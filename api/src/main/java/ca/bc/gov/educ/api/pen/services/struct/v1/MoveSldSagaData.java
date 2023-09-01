package ca.bc.gov.educ.api.pen.services.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * The type Move sld saga data.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoveSldSagaData {
  /**
   * the PEN which is used to search the sld records to move.
   */
  @NotNull(message = "pen can not be null.")
  String pen;

  /**
   * the distNo which is used to search the sld records to move.
   */
  @NotNull(message = "distNo can not be null.")
  String distNo;

  /**
   * the schlNo which is used to search the sld records to move.
   */
  @NotNull(message = "schlNo can not be null.")
  String schlNo;

  /**
   * the reportDate which is used to search the sld records to move.
   */
  @NotNull(message = "reportDate can not be null.")
  Long reportDate;

  /**
   * the studentId which is used to search the sld records to move.
   */
  @NotNull(message = "studentId can not be null.")
  String studentId;

}
