package ca.bc.gov.educ.api.pen.services.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;

/**
 * The type Student merge complete saga data.
 */
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentMergeCompleteSagaData extends BaseStudentSagaData {

  /**
   * MergeToPen: TruePEN.
   */
  @NotNull(message = "MergedToPen can not be null.")
  String mergedToPen;

  /**
   * MergedFromPen.
   */
  @NotNull(message = "mergedFromPen can not be null.")
  String mergedFromPen;

  /**
   * The merge student id.
   */
  @NotNull(message = "Merge Student ID can not be null.")
  String mergeStudentID;

  /**
   * The Student merge direction code.
   */
  @NotNull(message = "Student Merge Direction Code can not be null.")
  String studentMergeDirectionCode;

  /**
   * The Student merge source code.
   */
  @NotNull(message = "Student Merge Source Code can not be null.")
  String studentMergeSourceCode;
}
