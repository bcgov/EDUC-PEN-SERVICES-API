package ca.bc.gov.educ.api.pen.services.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentMergeCompleteSagaData extends BaseStudentSagaData {

  /**
   * The merge student id.
   */
  @NotNull(message = "Merge Student ID can not be null.")
  UUID mergeStudentID;

  @NotNull(message = "Student Merge Direction Code can not be null.")
  String studentMergeDirectionCode;

  @NotNull(message = "Student Merge Source Code can not be null.")
  String studentMergeSourceCode;

  /**
   * The request student id to identify which student is processed in merge saga.
   */
  UUID requestStudentID;
}
