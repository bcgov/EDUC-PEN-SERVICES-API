package ca.bc.gov.educ.api.pen.validation.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Grade age range.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GradeAgeRange {
  /**
   * The Lower range.
   */
  Integer lowerRange;
  /**
   * The Upper range.
   */
  Integer upperRange;
}
