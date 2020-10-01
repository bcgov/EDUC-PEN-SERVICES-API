package ca.bc.gov.educ.api.pen.validation.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;

/**
 * The type Pen request batch student validation payload.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PenRequestStudentValidationPayload {

  /**
   * Gets issue list.
   *
   * @return the issue list
   */
  public List<PenRequestStudentValidationIssue> getIssueList() {
    if(this.issueList == null){
      this.issueList = new LinkedList<>();
    }
    return issueList;
  }

  /**
   * The Issue list.
   */

  private List<PenRequestStudentValidationIssue> issueList;
  /**
   * The Is interactive.
   */
  @NotNull
  Boolean isInteractive;

  /**
   * The Transaction id.
   */
  @NotNull
  String transactionID;
  /**
   * The Pen request batch student id.
   */
  String penRequestBatchStudentID;
  /**
   * The Pen request batch id.
   */
  String penRequestBatchID;
  /**
   * The Pen request batch student status code.
   */
  String penRequestBatchStudentStatusCode;
  /**
   * The Local id.
   */
  String localID;
  /**
   * The Submitted pen.
   */
  String submittedPen;
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
   * The Assigned pen.
   */
  String assignedPEN;
  /**
   * The Student id.
   */
  String studentID;

  /**
   * The Create user.
   */
  String createUser;

  /**
   * The Update user.
   */
  String updateUser;

  /**
   * The Record number.
   */
  Integer recordNumber;

  /**
   * The best match pen.
   */
  String bestMatchPEN;

  /**
   * The Min code from PenRequestBatch
   */
  String minCode;

  /**
   * The Submission number from PenRequestBatch
   */
  String submissionNumber;
}
