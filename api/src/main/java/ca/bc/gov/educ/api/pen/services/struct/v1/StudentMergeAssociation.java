package ca.bc.gov.educ.api.pen.services.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * The type Student merge association.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentMergeAssociation implements Serializable {
  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;
  /**
   * The Merge student id.
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
