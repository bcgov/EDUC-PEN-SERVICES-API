package ca.bc.gov.educ.api.pen.services.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;

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
   * The History activity code.
   */
  @NotNull(message = "historyActivityCode can not be null.")
  private String historyActivityCode;

  /**
   * The request student id to identify which student is processed in saga.
   */
  String requestStudentID;
}
