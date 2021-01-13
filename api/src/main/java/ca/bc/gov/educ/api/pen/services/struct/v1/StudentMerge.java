package ca.bc.gov.educ.api.pen.services.struct.v1;

import ca.bc.gov.educ.api.pen.services.struct.BaseRequest;
import ca.bc.gov.educ.api.pen.services.struct.Student;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentMerge extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  String studentMergeID;
  @NotNull(message = "Student ID can not be null.")
  String studentID;
  @NotNull(message = "Merge Student ID can not be null.")
  String mergeStudentID;
  @NotNull(message = "Student Merge Direction Code can not be null.")
  String studentMergeDirectionCode;
  @NotNull(message = "Student Merge Source Code can not be null.")
  String studentMergeSourceCode;
}