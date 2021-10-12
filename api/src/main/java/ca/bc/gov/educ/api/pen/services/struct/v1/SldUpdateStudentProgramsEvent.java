package ca.bc.gov.educ.api.pen.services.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type sld update dia student programs event.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SldUpdateStudentProgramsEvent {
  /**
   * the PEN which is used to search the sld student programs.
   */
  private String pen;
  /**
   * the distNo which is used to search the sld student programs.
   */
  private String distNo;
  /**
   * the schlNo which is used to search the sld student programs.
   */
  private String schlNo;
  /**
   * the reportDate which is used to search the sld student programs.
   */
  private Long reportDate;
  /**
   * the studentId which is used to search the sld student programs.
   */
  private String studentId;

  /**
   * the attributes of sld record to be updated.
   * Leave the attribute null if no update.
   */
  private SldStudentProgram sldStudentProgram;
}
