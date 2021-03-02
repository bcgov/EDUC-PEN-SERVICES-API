package ca.bc.gov.educ.api.pen.services.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

/**
 * The type Student demerge complete saga data.
 */
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentDemergeCompleteSagaData extends BaseStudentSagaData {

  /**
   * MergedFromPen.
   */
  @NotNull(message = "mergedFromPen can not be null.")
  String mergedFromPen;

  /**
   * MergedFromStudent: student id.
   */
  @NotNull(message = "MergedFromStudent ID can not be null.")
  String mergedFromStudentID;

  /**
   * MergeToPen: TruePEN.
   */
  @NotNull(message = "MergedToPen can not be null.")
  String mergedToPen;

  /**
   * MergedFromStudent: student id.
   */
  @NotNull(message = "MergedToStudent ID can not be null.")
  String mergedToStudentID;

}
