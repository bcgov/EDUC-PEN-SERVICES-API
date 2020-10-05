package ca.bc.gov.educ.api.pen.validation.rules.impl;

import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class BirthDateRuleTest {
  private BirthDateRule birthDateRule;

  @Before
  public void setup() {
    birthDateRule = new BirthDateRule();
  }

  @Test
  @Parameters({
      "null, 1",
      ", 1",
      "2000-01-01, 1",
      "18990101, 1",
      "50000101, 1",
      "20000101, 0"
  })
  public void testValidate_givenDifferentDOB_shouldReturnResults(String dob, int expectedErrors) {
    if ("null".equals(dob)) {
      dob = null;
    }
    PenRequestStudentValidationPayload payload = PenRequestStudentValidationPayload.builder().transactionID(UUID.randomUUID().toString()).dob(dob).build();
    var result = birthDateRule.validate(payload);
    assertThat(result).size().isEqualTo(expectedErrors);
  }
}
