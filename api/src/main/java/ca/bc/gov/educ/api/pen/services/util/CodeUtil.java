package ca.bc.gov.educ.api.pen.services.util;

import ca.bc.gov.educ.api.pen.services.constants.GenderCodes;
import ca.bc.gov.educ.api.pen.services.constants.SexCodes;

/**
 * The code util.
 */
public final class CodeUtil {

  /**
   * Instantiates a new code util.
   */
  private CodeUtil() {
  }

  /**
   * Get the sex code according to the gender code
   *
   * @param genderCode the gender code
   * @return the sex code
   */
  public static String getSexCodeFromGenderCode(final String genderCode) {
    if (GenderCodes.X.getCode().equals(genderCode)) {
      return SexCodes.U.getCode();
    }
    return genderCode;
  }
}
