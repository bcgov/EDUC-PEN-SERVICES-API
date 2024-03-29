package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The type Postal code rule test.
 */
@RunWith(JUnitParamsRunner.class)
public class PostalCodeRuleTest {

  private PostalCodeRule rule;


  /**
   * Sets .
   */
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    rule = new PostalCodeRule();
  }

  /**
   * Validate.
   *
   * @param postalCode     the postal code
   * @param expectedErrors the expected errors
   */
  @Test
  @Parameters({
      "null, 0",
      ", 0",
      "V8W2E1, 0",
      "V8R4N4, 0",
      "123546, 1",
      "123, 1",
      "1234, 1",
      "12354, 1",
      "123456, 1",
      "1234567, 1",
      "12345678, 1",
      "1234567891454, 1",
      "INVALIDPOSTAL, 1",
      "V8R4Æ4, 1"
  })
  public void validate(String postalCode, int expectedErrors) {
    if ("null".equals(postalCode)) {
      postalCode = null;
    }
    PenRequestStudentValidationPayload payload = PenRequestStudentValidationPayload.builder().isInteractive(false).transactionID(UUID.randomUUID().toString()).mincode("1234597").postalCode(postalCode).build();
    var result = rule.validate(payload);
    assertThat(result).size().isEqualTo(expectedErrors);
  }
}