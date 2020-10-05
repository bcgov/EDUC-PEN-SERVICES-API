package ca.bc.gov.educ.api.pen.validation.rules.impl;

import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class SubmittedPENRuleTest {
  private SubmittedPENRule rule;


  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    rule = new SubmittedPENRule();
  }

  @Test
  @Parameters({
      "null, 0",
      ", 0",
      "123456789, 1",
      "123455789, 1",
      "120164447, 0",
      "123, 1",
      "1234, 1",
      "12354, 1",
      "123456, 1",
      "1234567, 1",
      "12345678,1",
      "1234567890454, 1",
      "INVALIDPEN, 1"
  })
  public void testValidate_givenDifferentPEN_shouldReturnResults(String submittedPEN, int expectedErrors) {
    if ("null".equals(submittedPEN)) {
      submittedPEN = null;
    }
    PenRequestStudentValidationPayload payload = PenRequestStudentValidationPayload.builder().isInteractive(false).transactionID(UUID.randomUUID().toString()).submittedPen(submittedPEN).build();
    var result = rule.validate(payload);
    assertThat(result).size().isEqualTo(expectedErrors);
  }
}