package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rest.RestUtils;
import ca.bc.gov.educ.api.pen.services.struct.v1.GenderCode;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * The type Gender rule test.
 */
@RunWith(JUnitParamsRunner.class)
public class GenderRuleTest {

  private static GenderRule rule;
  private static List<GenderCode> genderCodes;

  /**
   * The constant restUtils.
   */
  @Mock
  static RestUtils restUtils;


  /**
   * Sets .
   *
   * @throws IOException the io exception
   */
  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    rule = new GenderRule(restUtils);
    if (genderCodes == null) {
      final File file = new File(
          Objects.requireNonNull(getClass().getClassLoader().getResource("gender_codes.json")).getFile()
      );
      genderCodes = new ObjectMapper().readValue(file, new TypeReference<>() {
      });
    }
  }

  /**
   * Test validate given different gender codes should return results.
   *
   * @param gender         the gender
   * @param expectedErrors the expected errors
   */
  @Test
  @Parameters({
      "null, 1",
      ", 1",
      "     , 1",
      "A, 1",
      "a, 1",
      "L, 1",
      "M, 0",
      "X, 0",
      "U, 0",
      "F, 0",
      "B, 1",
      "D, 1",
      "E, 1",
      "G, 1",
      "H, 1",
      "I, 1",
      "K, 1",
      "O, 1",
      "P, 1",
      "Q, 1",
      "R, 1",
      "S, 1",
      "T, 1",
      "V, 1",
      "W, 1",
      "Y, 1",
      "Z, 1",
  })
  public void testValidate_givenDifferentGenderCodes_shouldReturnResults(String gender, int expectedErrors) {
    if ("null".equals(gender)) {
      gender = null;
    }
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    PenRequestStudentValidationPayload payload = PenRequestStudentValidationPayload.builder().transactionID(UUID.randomUUID().toString()).genderCode(gender).build();
    var result = rule.validate(payload);
    assertThat(result).size().isEqualTo(expectedErrors);
  }
}

