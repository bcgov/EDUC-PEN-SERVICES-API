package ca.bc.gov.educ.api.pen.services.constants;

import lombok.Getter;

/**
 * The enum Student demog codes.
 */
public enum StudentDemogCodes {
  /**
   * Accepted
   */
  ACCEPTED("A"),
  /**
   * Confirmed
   */
  CONFIRMED("C");

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
  StudentDemogCodes(final String code) {
    this.code = code;
  }
}
