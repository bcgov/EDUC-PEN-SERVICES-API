package ca.bc.gov.educ.api.pen.services.struct.v1;

import ca.bc.gov.educ.api.pen.services.struct.Student;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Student history.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("java:S1948")
public class StudentHistory extends Student implements Serializable {
  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;
  /**
   * The Update date.
   */
  String updateDate;
  /**
   * The Student history id.
   */
  String studentHistoryID;
  /**
   * The History activity code.
   */
  String historyActivityCode;

}
