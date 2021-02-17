package ca.bc.gov.educ.api.pen.services.constants;

import lombok.Getter;

/**
 * The enum Sex codes.
 */
@Getter
public enum SexCodes {
  /**
   * Male
   */
  M("M"),
  /**
   * Female
   */
  F("F"),
  /**
   * Intersex
   */
  I("I"),
  /**
   * Unknown.
   */
  U("U");

  /**
   * The Code.
   */
  private final String code;

  /**
   * Instantiates a new sex code.
   *
   * @param code the code
   */
  SexCodes(final String code) {
    this.code = code;
  }
}
