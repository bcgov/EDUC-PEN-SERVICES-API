package ca.bc.gov.educ.api.pen.validation.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GradeAgeRange {
  Integer lowerRange;
  Integer upperRange;
}
