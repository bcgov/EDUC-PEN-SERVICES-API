package ca.bc.gov.educ.api.pen.services.constants;

import lombok.Getter;

/**
 * The enum Gender codes.
 */
@Getter
public enum GenderCodes {
  /**
   * Gender Male
   */
  M("M"),
  /**
   * Gender Female
   */
  F("F"),
  /**
   * Gender Diverse.
   */
  X("X"),
  /**
   * Gender Unknown
   */
  U("U");

  /**
   * The Code.
   */
  private final String code;

  /**
   * Instantiates a new gender code.
   *
   * @param code the code
   */
  GenderCodes(String code) {
    this.code = code;
  }
}
