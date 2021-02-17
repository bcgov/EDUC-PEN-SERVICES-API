package ca.bc.gov.educ.api.pen.services.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Student saga data.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentSagaData {
  /**
   * The Student id.
   */
  private String studentID;
  /**
   * The Pen.
   */
  private String pen;
  /**
   * The Legal first name.
   */
  private String legalFirstName;
  /**
   * The Legal middle names.
   */
  private String legalMiddleNames;
  /**
   * The Legal last name.
   */
  private String legalLastName;
  /**
   * The Dob.
   */
  private String dob;
  /**
   * The Sex code.
   */
  private String sexCode;
  /**
   * The Gender code.
   */
  private String genderCode;
  /**
   * The Usual first name.
   */
  private String usualFirstName;
  /**
   * The Usual middle names.
   */
  private String usualMiddleNames;
  /**
   * The Usual last name.
   */
  private String usualLastName;
  /**
   * The Email.
   */
  private String email;
  /**
   * The Deceased date.
   */
  private String deceasedDate;
  /**
   * The Create user.
   */
  private String createUser;
  /**
   * The Update user.
   */
  private String updateUser;
  /**
   * The Local id.
   */
  private String localID;
  /**
   * The Postal code.
   */
  private String postalCode;
  /**
   * The Grade code.
   */
  private String gradeCode;
  /**
   * The Mincode.
   */
  private String mincode;
  /**
   * The Email verified.
   */
  private String emailVerified;
  /**
   * The Grade year.
   */
  private String gradeYear;
  /**
   * The Demog code.
   */
  private String demogCode;
  /**
   * The Status code.
   */
  private String statusCode;
  /**
   * The Memo.
   */
  private String memo;
  /**
   * The History activity code.
   */
  private String historyActivityCode;
  /**
   * The True student id.
   */
  private String trueStudentID;
}
