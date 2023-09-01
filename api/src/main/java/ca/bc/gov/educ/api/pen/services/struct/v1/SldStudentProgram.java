package ca.bc.gov.educ.api.pen.services.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type sld student program.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SldStudentProgram {
  private String studentId;

  private String distNo;

  private String schlNo;

  private Long reportDate;

  private String enrolledProgramCode;

  private String careerProgram;

  private String pen;

}
