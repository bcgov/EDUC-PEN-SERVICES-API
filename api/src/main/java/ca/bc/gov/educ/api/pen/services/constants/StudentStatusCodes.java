package ca.bc.gov.educ.api.pen.services.constants;

import lombok.Getter;

/**
 * The enum Student status codes.
 */
public enum StudentStatusCodes {
  /**
   * Active
   */
  ACTIVE("A"),
  /**
   * Merged
   */
  MERGE("M");

  /**
   * The Code.
   */
  @Getter
  private final String code;

  /**
   * Instantiates a new student status code.
   *
   * @param code the code
   */
  StudentStatusCodes(final String code) {
    this.code = code;
  }
}
