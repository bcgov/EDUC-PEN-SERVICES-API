package ca.bc.gov.educ.api.pen.services.constants;

import lombok.Getter;

public enum StudentMergeSourceCodes {
  /**
   * Ministry Identified
   */
  MI("MI");

  /**
   * The Code.
   */
  @Getter
  private final String code;

  /**
   * Instantiates a new student merge source code.
   *
   * @param code the code
   */
  StudentMergeSourceCodes(String code) {
    this.code = code;
  }
}
