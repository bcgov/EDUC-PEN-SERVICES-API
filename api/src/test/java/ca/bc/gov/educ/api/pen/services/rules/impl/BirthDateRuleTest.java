package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rest.RestUtils;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import ca.bc.gov.educ.api.pen.services.struct.v1.School;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The type Birth date rule test.
 */
@RunWith(JUnitParamsRunner.class)
public class BirthDateRuleTest {
  @Mock
  static RestUtils restUtils;
  private static BirthDateRule birthDateRule;
  private static Optional<School> schoolOptional = Optional.empty();
  private static Optional<School> schoolOptional1 = Optional.empty();
  private static Optional<School> schoolOptional2 = Optional.empty();
  private static Optional<School> schoolOptional3 = Optional.empty();

  /**
   * Sets .
   */
  @Before
  public void setup() {
    MockitoAnnotations.openMocks(this);
    birthDateRule = new BirthDateRule(restUtils);
    schoolOptional = Optional.of(School.builder().schoolName("testSchool").schoolCategoryCode("02").build());
    schoolOptional1 = Optional.of(School.builder().schoolName("testSchool").schoolCategoryCode("09").build());
    schoolOptional2 = Optional.of(School.builder().schoolName("testSchool").schoolCategoryCode("10").build());
    schoolOptional3 = Optional.of(School.builder().schoolName("testSchool").schoolCategoryCode("11").build());
  }

  /**
   * Test validate given different dob should return results.
   *
   * @param dob            the dob
   * @param expectedErrors the expected errors
   */
  @Test
  @Parameters({
    "null, 1",
    ", 1",
    "2000-01-01, 1",
    "18990101, 1",
    "50000101, 1",
    "20000101, 0",
    "TESTDOBTOOYOUNG1, 1",
    "TESTDOBTOOYOUNG2, 0",
    "TESTDOBTOOYOUNG3, 1"
  })
  public void testValidate_givenDifferentDOB_shouldReturnResults(String dob, final int expectedErrors) {
    if ("null".equals(dob)) {
      dob = null;
    }
    if ("TESTDOBTOOYOUNG1".equals(dob)) {
      dob = LocalDate.now().minusYears(2).getYear() + "0101";
      when(restUtils.getSchoolByMincode(any())).thenReturn(schoolOptional1);
    } else if ("TESTDOBTOOYOUNG2".equals(dob)) {
      dob = LocalDate.now().minusYears(4).getYear() + "0101";
      when(restUtils.getSchoolByMincode(any())).thenReturn(schoolOptional2);
    } else if ("TESTDOBTOOYOUNG3".equals(dob)) {
      dob = LocalDate.now().minusYears(4).getYear() + "0101";
      when(restUtils.getSchoolByMincode(any())).thenReturn(schoolOptional3);
    } else {
      when(restUtils.getSchoolByMincode(any())).thenReturn(schoolOptional);
    }

    final PenRequestStudentValidationPayload payload = PenRequestStudentValidationPayload.builder().transactionID(UUID.randomUUID().toString()).dob(dob).build();
    final var result = birthDateRule.validate(payload);
    assertThat(result).size().isEqualTo(expectedErrors);
  }
}
