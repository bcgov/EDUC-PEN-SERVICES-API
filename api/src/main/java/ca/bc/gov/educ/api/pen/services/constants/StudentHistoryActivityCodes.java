package ca.bc.gov.educ.api.pen.services.constants;

import lombok.Getter;

/**
 * The enum Student history activity codes.
 */
public enum StudentHistoryActivityCodes {
  /**
   * Record was merged
   */
  MERGE("MERGE"),
  /**
   * Record was demerged
   */
  DEMERGE("DEMERGE"),
  /**
   * Record was split
   */
  SPLITNEW("SPLITNEW");

  /**
   * The Code.
   */
  @Getter
  private final String code;

  /**
   * Instantiates a new student history activity code.
   *
   * @param code the code
   */
  StudentHistoryActivityCodes(final String code) {
    this.code = code;
  }
}
