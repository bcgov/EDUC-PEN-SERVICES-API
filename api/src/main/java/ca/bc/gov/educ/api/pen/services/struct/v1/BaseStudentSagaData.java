package ca.bc.gov.educ.api.pen.services.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

/**
 * The base saga data.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BaseStudentSagaData extends BaseStudentData {
  /**
   * The Student id.
   */
  @NotNull(message = "studentID cannot be null")
  private String studentID;

  /**
   * The History activity code.
   */
  @NotNull(message = "historyActivityCode can not be null.")
  private String historyActivityCode;

  /**
   * The request student id to identify which student is processed in saga.
   */
  String requestStudentID;
}
