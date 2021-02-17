package ca.bc.gov.educ.api.pen.services.constants;

import lombok.Getter;

/**
 * The enum Student merge source codes.
 */
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
  StudentMergeSourceCodes(final String code) {
    this.code = code;
  }
}
