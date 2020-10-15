package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rest.RestUtils;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * The type Grade code rule test.
 */
@RunWith(JUnitParamsRunner.class)
public class GradeCodeRuleTest {

  private GradeCodeRule gradeCodeRule;
  /**
   * The Rest utils.
   */
  @MockBean
  RestUtils restUtils;

  /**
   * Sets up.
   */
  @Before
  public void setUp() {
    gradeCodeRule = new GradeCodeRule(restUtils);
  }

  /**
   * Test get school year given different inputs should return calculated year.
   *
   * @param currentDate  the current date
   * @param expectedYear the expected year
   */
  @Test
  @Parameters({
      "20200601, 2020",
      "20200531, 2019",
      "20201031, 2020",
      "20201130, 2020",
      "20201231, 2020",
      "20200131, 2019",
      "20200229, 2019",
      "20200331, 2019",
      "20200430, 2019"
  })
  public void testGetSchoolYear_givenDifferentInputs_shouldReturnCalculatedYear(String currentDate, int expectedYear) {
    var date = LocalDate.parse(currentDate, DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));
    int schoolYear = gradeCodeRule.getSchoolYear(date);
    assertThat(schoolYear).isEqualTo(expectedYear);
  }

  /**
   * If current month is Oct thru May, use the prior Sept 30.
   * So if current date is in Feb of 2021, compare the student's birthdate to 2020-09-30 (the prior Sept 30). Find the difference in years between this date and the student's birthdate.
   * E.g. if student was born 2011-08-08, difference between birthdate and 2020-09-30 is 9 full years; the student's age as of 2021-09-30 was 9.
   *
   * @param currentDate the current date
   * @param dob         the dob
   * @param expectedAge the expected age
   */
  @Test
  @Parameters({
      "20210101, 20110808,9",
      "20210201, 20110808,9",
      "20210331, 20110808,9",
      "20210430, 20110808,9",
      "20210531, 20110808,9",
      "20210601, 20110808,10",
      "20210701, 20110808,10",
      "20210801, 20110808,10",
      "20210930, 20110808,10",
      "20211001, 20110808,10",
      "20211101, 20110808,10",
      "20211231, 20110808,10"
  })
  public void testCalculateAge_givenConditions_shouldReturn9Years(String currentDate, String dob, int expectedAge) {
    var date = LocalDate.parse(currentDate, DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));
    gradeCodeRule.setCurrentDate(date);
    int age = gradeCodeRule.calculateAge(dob);
    assertThat(age).isEqualTo(expectedAge);
  }

}