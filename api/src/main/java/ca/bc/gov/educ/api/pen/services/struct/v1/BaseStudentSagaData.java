package ca.bc.gov.educ.api.pen.services.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * The base pen request batch saga data.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseStudentSagaData {
  /**
   * The student id.
   */
  @NotNull(message = "studentID cannot be null")
  UUID studentID;

  /**
   * The mincode.
   */
  String mincode;
  /**
   * The Local id.
   */
  String localID;
  /**
   * The Legal first name.
   */
  String legalFirstName;
  /**
   * The Legal middle names.
   */
  String legalMiddleNames;
  /**
   * The Legal last name.
   */
  String legalLastName;
  /**
   * The Usual first name.
   */
  String usualFirstName;
  /**
   * The Usual middle names.
   */
  String usualMiddleNames;
  /**
   * The Usual last name.
   */
  String usualLastName;
  /**
   * The Dob.
   */
  String dob;
  /**
   * The Gender code.
   */
  String genderCode;
  /**
   * The Grade code.
   */
  String gradeCode;
  /**
   * The Postal code.
   */
  String postalCode;
  /**
   * The Demog code.
   */
  String demogCode;
  /**
   * The Status code.
   */
  String statusCode;
  /**
   * The Memo.
   */
  String memo;
  /**
   * The Create user.
   */
  String createUser;
  /**
   * The Update user.
   */
  String updateUser;

  /**
   * The History activity code.
   */
  @NotNull(message = "historyActivityCode can not be null.")
  String historyActivityCode;

}
