package ca.bc.gov.educ.api.pen.services.constants;

import lombok.Getter;

public enum StudentStatusCodes {
  /**
   * Active
   */
  Active("A"),
  /**
   * Merged
   */
  Merge("M");

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
  StudentStatusCodes(String code) {
    this.code = code;
  }
}
