package ca.bc.gov.educ.api.pen.services.constants;

import lombok.Getter;

/**
 * The enum Pen request  student validation issue type code.
 */
public enum PenRequestStudentValidationIssueTypeCode {
  /**
   * One char name pen request  student validation issue type code.
   */
  ONE_CHAR_NAME("1CHARNAME"),
  /**
   * Apostrophe pen request  student validation issue type code.
   */
  APOSTROPHE("APOSTROPHE"),
  /**
   * Blank field pen request  student validation issue type code.
   */
  BLANK_FIELD("BLANKFIELD"),
  /**
   * Blank in name pen request  student validation issue type code.
   */
  BLANK_IN_NAME("BLANKINNAME"),
  /**
   * Check digit pen request  student validation issue type code.
   */
  CHECK_DIGIT("CHKDIG"),
  /**
   * Dob invalid pen request  student validation issue type code.
   */
  DOB_INVALID("DOB_INVALID"),
  /**
   * Dob past pen request  student validation issue type code.
   */
  DOB_PAST("DOB_PAST"),
  /**
   * Dob future pen request  student validation issue type code.
   */
  DOB_FUTURE("DOB_FUTURE"),
  /**
   * Dob too young pen request student validation issue type code.
   */
  DOB_TOO_YOUNG("DOBTOOYOUNG"),
  /**
   * Embedded mid pen request  student validation issue type code.
   */
  EMBEDDED_MID("EMBEDDEDMID"),
  /**
   * Gender err pen request  student validation issue type code.
   */
  GENDER_ERR("GENDER_ERR"),
  /**
   * Pc err pen request  student validation issue type code.
   */
  PC_ERR("PC_ERR"),
  /**
   * Grade cd err pen request  student validation issue type code.
   */
  GRADE_CD_ERR("GRADECD_ERR"),
  /**
   * Inv chars pen request  student validation issue type code.
   */
  INV_CHARS("INVCHARS"),
  /**
   * Repeated chars pen request  student validation issue type code.
   */
  REPEATED_CHARS("REPEATCHARS"),
  /**
   * Inv prefix pen request  student validation issue type code.
   */
  INV_PREFIX("INVPREFIX"),
  /**
   * Old 4 grade pen request  student validation issue type code.
   */
  OLD4GRADE("OLD4GRADE"),
  /**
   * On block list pen request  student validation issue type code.
   */
  ON_BLOCK_LIST("ONBLOCKLIST"),
  /**
   * Repeat mid pen request  student validation issue type code.
   */
  REPEAT_MID("REPEATMID"),
  /**
   * Schar prefix pen request  student validation issue type code.
   */
  SCHAR_PREFIX("SCHARPREFIX"),
  /**
   * Young 4 grade pen request  student validation issue type code.
   */
  YOUNG4GRADE("YOUNG4GRADE"),
  /**
   * Begin invalid pen request  student validation issue type code.
   */
  BEGIN_INVALID("BEGININVALID"),
  /**
   * Invalid name pen request  student validation issue type code.
   */
  BLOCKED_NAME("BLOCKEDNAME"),
  /**
   * Same name pen request student validation issue type code.
   */
  SAME_NAME("SAMENAME"),
  /**
   * Has a number in the name
   */
  NUMBER_NAME("NUMBERNAME");

  /**
   * The Code.
   */
  @Getter
  private final String code;

  /**
   * Instantiates a new Pen request student validation issue type code.
   *
   * @param code the code
   */
  PenRequestStudentValidationIssueTypeCode(final String code) {
    this.code = code;
  }
}
