package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rest.RestUtils;
import ca.bc.gov.educ.api.pen.services.rules.BaseRule;
import ca.bc.gov.educ.api.pen.services.struct.v1.GradeAgeRange;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.BIRTH_DATE;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.GRADE_CODE;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.*;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Gender rule.
 */
@Slf4j
public class GradeCodeRule extends BaseRule {

  /**
   * The Grade age range map.
   */
  private final Map<String, GradeAgeRange> gradeAgeRangeMap = new ConcurrentHashMap<>();
  /**
   * The Rest utils.
   */
  @Getter(PRIVATE)
  private final RestUtils restUtils;
  /**
   * The Current date.
   */
  private LocalDate currentDate;

  /**
   * Instantiates a new Gender rule.
   *
   * @param restUtils the rest utils
   */
  public GradeCodeRule(final RestUtils restUtils) {
    this.restUtils = restUtils;
  }


  /**
   * Init.
   * Grade Code: Age on Sept 30
   * <p>
   * HS: 4 - 19
   * KH: 4 - 7
   * KF: 4 - 7
   * 01: 5 - 8
   * 02: 6 - 9
   * 03: 7 - 10
   * 04: 8 - 11
   * 05: 9 - 12
   * 06: 10 - 13
   * 07: 11 - 14
   * EU: 4 and up
   * 08: 12 - 15
   * 09: 13 - 16
   * 10: 14- 18
   * 11: 15 - 19
   * 12: 16 - 21
   * <p>
   * EL: under 7
   * SU: 12 and up
   * GA: 19 and up
   */
  @PostConstruct
  public void init() {
    this.gradeAgeRangeMap.put("EL", GradeAgeRange.builder().lowerRange(0).upperRange(7).build()); // no lower range so assuming baby is just born.
    this.gradeAgeRangeMap.put("GA", GradeAgeRange.builder().lowerRange(19).upperRange(1000).build()); // no upper range assuming no human lives for 1000 years.
    this.gradeAgeRangeMap.put("SU", GradeAgeRange.builder().lowerRange(12).upperRange(1000).build()); // no upper range assuming no human lives for 1000 years.
    this.gradeAgeRangeMap.put("EU", GradeAgeRange.builder().lowerRange(4).upperRange(1000).build()); // no upper range assuming no human lives for 1000 years.
    this.gradeAgeRangeMap.put("HS", GradeAgeRange.builder().lowerRange(4).upperRange(19).build());
    this.gradeAgeRangeMap.put("KH", GradeAgeRange.builder().lowerRange(4).upperRange(7).build());
    this.gradeAgeRangeMap.put("KF", GradeAgeRange.builder().lowerRange(4).upperRange(7).build());
    this.gradeAgeRangeMap.put("01", GradeAgeRange.builder().lowerRange(5).upperRange(8).build());
    this.gradeAgeRangeMap.put("02", GradeAgeRange.builder().lowerRange(6).upperRange(9).build());
    this.gradeAgeRangeMap.put("03", GradeAgeRange.builder().lowerRange(7).upperRange(10).build());
    this.gradeAgeRangeMap.put("04", GradeAgeRange.builder().lowerRange(8).upperRange(11).build());
    this.gradeAgeRangeMap.put("05", GradeAgeRange.builder().lowerRange(9).upperRange(12).build());
    this.gradeAgeRangeMap.put("06", GradeAgeRange.builder().lowerRange(10).upperRange(13).build());
    this.gradeAgeRangeMap.put("07", GradeAgeRange.builder().lowerRange(11).upperRange(14).build());
    this.gradeAgeRangeMap.put("08", GradeAgeRange.builder().lowerRange(12).upperRange(15).build());
    this.gradeAgeRangeMap.put("09", GradeAgeRange.builder().lowerRange(13).upperRange(16).build());
    this.gradeAgeRangeMap.put("10", GradeAgeRange.builder().lowerRange(14).upperRange(18).build());
    this.gradeAgeRangeMap.put("11", GradeAgeRange.builder().lowerRange(15).upperRange(19).build());
    this.gradeAgeRangeMap.put("12", GradeAgeRange.builder().lowerRange(16).upperRange(21).build());
  }

  /**
   * Validates the student record for the given rule.
   *
   * @param validationPayload the validation payload
   * @return the validation result as a list.
   */
  @Override
  public List<PenRequestStudentValidationIssue> validate(final PenRequestStudentValidationPayload validationPayload) {
    final var stopwatch = Stopwatch.createStarted();
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    final var gradeCodes = this.restUtils.getGradeCodes();
    final String gradeCode = validationPayload.getGradeCode();
    if (StringUtils.isBlank(gradeCode)) {
      results.add(this.createValidationEntity(WARNING, GRADE_CD_ERR, GRADE_CODE));
    } else {
      final String finalGradeCode = gradeCode.trim();
      final long filteredCount = gradeCodes.stream().filter(gradeCode1 -> LocalDateTime.now().isAfter(gradeCode1.getEffectiveDate())
          && LocalDateTime.now().isBefore(gradeCode1.getExpiryDate())
          && finalGradeCode.equalsIgnoreCase(gradeCode1.getGradeCode())).count();
      if (filteredCount < 1) {
        results.add(this.createValidationEntity(WARNING, GRADE_CD_ERR, GRADE_CODE));
      }
    }
    if (results.isEmpty() && this.noDOBErrorReported(validationPayload.getIssueList())) {
      this.checkForYoungAndOld(results, gradeCode, validationPayload);
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds", validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }

  /**
   * <pre>
   *   V20
   * PreReq: Skip this check if V16 or V19 reported.
   *
   * Check:
   *
   * Calculate age of Student as of Sept 30th of the current school year:
   * If current month is June, July, Aug, or Sept: use the upcoming Sept 30.
   * If current month is Oct thru May, use the prior Sept 30.
   * Check that age of student is within the bounds of the Grade Code in the PEN Request, according to this table:
   * Grade Code: Age on Sept 30
   *
   * HS: 4 - 19
   * KH: 4 - 7
   * KF: 4 - 7
   * 01: 5 - 8
   * 02: 6 - 9
   * 03: 7 - 10
   * 04: 8 - 11
   * 05: 9 - 12
   * 06: 10 - 13
   * 07: 11 - 14
   * EU: 4 and up
   * 08: 12 - 15
   * 09: 13 - 16
   * 10: 14- 18
   * 11: 15 - 19
   * 12: 16 - 21
   *
   * EL: under 7
   * SU: 12 and up
   * GA: 19 and up
   *
   * 3. If age falls out side the range for the Grade Code, report a validation issue
   *
   * If age is lower than the grade code range YOUNG4GRADE;
   *
   * If age is higher than the grade code range OLD4GRADE
   *
   * WARNING	GRADECODE
   * </pre>
   *
   * @param results           the validation issue array
   * @param gradeCode         the grade code which needs to be validated
   * @param validationPayload the entire payload.
   */
  private void checkForYoungAndOld(final List<PenRequestStudentValidationIssue> results, final String gradeCode, final PenRequestStudentValidationPayload validationPayload) {
    final int diff = this.calculateAge(validationPayload.getDob());
    final var gradeAgeRange = Optional.ofNullable(this.gradeAgeRangeMap.get(gradeCode));
    if (gradeAgeRange.isPresent()) {
      if (diff < gradeAgeRange.get().getLowerRange()) {
        results.add(this.createValidationEntity(WARNING, YOUNG4GRADE, GRADE_CODE));
      }
      if (diff > gradeAgeRange.get().getUpperRange()) {
        results.add(this.createValidationEntity(WARNING, OLD4GRADE, GRADE_CODE));
      }
    }
  }

  /**
   * Calculate age int.
   *
   * @param dob the dob
   * @return the int
   */
  protected int calculateAge(final String dob) {
    final int schoolYear = this.getSchoolYear(this.getCurrentDate());
    final LocalDate schoolDate = LocalDate.parse(schoolYear + "-09-30");
    final var dobDate = LocalDate.parse(dob, DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));
    return Period.between(dobDate, schoolDate).getYears();
  }

  /**
   * Gets school year.
   *
   * @param localDate the local date
   * @return the school year
   */
  protected int getSchoolYear(final LocalDate localDate) {
    final var currentMonth = localDate.getMonth();
    var schoolYear = localDate.getYear();
    if (currentMonth.getValue() < 6) {
      schoolYear = schoolYear - 1;
    }
    log.debug("calculated year is :: {}", schoolYear);
    return schoolYear;
  }

  /**
   * No dob error reported boolean.
   *
   * @param issueList the issue list
   * @return the boolean
   */
  private boolean noDOBErrorReported(final List<PenRequestStudentValidationIssue> issueList) {
    final var result = issueList.stream().filter(element -> element.getPenRequestBatchValidationFieldCode().equals(BIRTH_DATE.getCode())).collect(Collectors.toList());
    return result.isEmpty();
  }

  /**
   * Gets current date.
   *
   * @return the current date
   */
  public LocalDate getCurrentDate() {
    if (this.currentDate == null) {
      this.currentDate = LocalDate.now();
    }
    return this.currentDate;
  }

  /**
   * Sets current date.
   *
   * @param currentDate the current date
   */
  public void setCurrentDate(final LocalDate currentDate) {
    this.currentDate = currentDate;
  }
}
