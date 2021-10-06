package ca.bc.gov.educ.api.pen.services.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * The type Student merge stats.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentMergeStats implements Serializable {
  Map<String, Long> numberOfMergesInLastTwelveMonth;
}
