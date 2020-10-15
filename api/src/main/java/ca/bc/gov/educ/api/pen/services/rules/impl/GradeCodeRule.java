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

  private final Map<String, GradeAgeRange> gradeAgeRangeMap = new ConcurrentHashMap<>();

  private LocalDate currentDate;

  @Getter(PRIVATE)
  private final RestUtils restUtils;

  /**
   * Instantiates a new Gender rule.
   *
   * @param restUtils the rest utils
   */
  public GradeCodeRule(RestUtils restUtils) {
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
    gradeAgeRangeMap.put("EL", GradeAgeRange.builder().lowerRange(0).upperRange(7).build()); // no lower range so assuming baby is just born.
    gradeAgeRangeMap.put("GA", GradeAgeRange.builder().lowerRange(19).upperRange(1000).build()); // no upper range assuming no human lives for 1000 years.
    gradeAgeRangeMap.put("SU", GradeAgeRange.builder().lowerRange(12).upperRange(1000).build()); // no upper range assuming no human lives for 1000 years.
    gradeAgeRangeMap.put("EU", GradeAgeRange.builder().lowerRange(4).upperRange(1000).build()); // no upper range assuming no human lives for 1000 years.
    gradeAgeRangeMap.put("HS", GradeAgeRange.builder().lowerRange(4).upperRange(19).build());
    gradeAgeRangeMap.put("KH", GradeAgeRange.builder().lowerRange(4).upperRange(7).build());
    gradeAgeRangeMap.put("KF", GradeAgeRange.builder().lowerRange(4).upperRange(7).build());
    gradeAgeRangeMap.put("01", GradeAgeRange.builder().lowerRange(5).upperRange(8).build());
    gradeAgeRangeMap.put("02", GradeAgeRange.builder().lowerRange(6).upperRange(9).build());
    gradeAgeRangeMap.put("03", GradeAgeRange.builder().lowerRange(7).upperRange(10).build());
    gradeAgeRangeMap.put("04", GradeAgeRange.builder().lowerRange(8).upperRange(11).build());
    gradeAgeRangeMap.put("05", GradeAgeRange.builder().lowerRange(9).upperRange(12).build());
    gradeAgeRangeMap.put("06", GradeAgeRange.builder().lowerRange(10).upperRange(13).build());
    gradeAgeRangeMap.put("07", GradeAgeRange.builder().lowerRange(11).upperRange(14).build());
    gradeAgeRangeMap.put("08", GradeAgeRange.builder().lowerRange(12).upperRange(15).build());
    gradeAgeRangeMap.put("09", GradeAgeRange.builder().lowerRange(13).upperRange(16).build());
    gradeAgeRangeMap.put("10", GradeAgeRange.builder().lowerRange(14).upperRange(18).build());
    gradeAgeRangeMap.put("11", GradeAgeRange.builder().lowerRange(15).upperRange(19).build());
    gradeAgeRangeMap.put("12", GradeAgeRange.builder().lowerRange(16).upperRange(21).build());
  }

  /**
   * Validates the student record for the given rule.
   *
   * @param validationPayload the validation payload
   * @return the validation result as a list.
   */
  @Override
  public List<PenRequestStudentValidationIssue> validate(PenRequestStudentValidationPayload validationPayload) {
    var stopwatch = Stopwatch.createStarted();
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    var gradeCodes = restUtils.getGradeCodes();
    String gradeCode = validationPayload.getGradeCode();
    if (StringUtils.isBlank(gradeCode)) {
      results.add(createValidationEntity(ERROR, GRADE_CD_ERR, GRADE_CODE));
    } else {
      String finalGradeCode = gradeCode.trim();
      long filteredCount = gradeCodes.stream().filter(gradeCode1 -> LocalDateTime.now().isAfter(gradeCode1.getEffectiveDate())
          && LocalDateTime.now().isBefore(gradeCode1.getExpiryDate())
          && finalGradeCode.equalsIgnoreCase(gradeCode1.getGradeCode())).count();
      if (filteredCount < 1) {
        results.add(createValidationEntity(ERROR, GRADE_CD_ERR, GRADE_CODE));
      }
    }
    if (results.isEmpty() && noDOBErrorReported(validationPayload.getIssueList())) {
      checkForYoungAndOld(results, gradeCode, validationPayload);
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds",validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
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
  private void checkForYoungAndOld(List<PenRequestStudentValidationIssue> results, String gradeCode, PenRequestStudentValidationPayload validationPayload) {
    int diff = calculateAge(validationPayload.getDob());
    var gradeAgeRange = Optional.ofNullable(gradeAgeRangeMap.get(gradeCode));
    if (gradeAgeRange.isPresent()) {
      if (diff < gradeAgeRange.get().getLowerRange()) {
        results.add(createValidationEntity(WARNING, YOUNG4GRADE, GRADE_CODE));
      }
      if (diff > gradeAgeRange.get().getUpperRange()) {
        results.add(createValidationEntity(WARNING, OLD4GRADE, GRADE_CODE));
      }
    }
  }

  /**
   * Calculate age int.
   *
   * @param dob the dob
   * @return the int
   */
  protected int calculateAge(String dob) {
    int schoolYear = getSchoolYear(getCurrentDate());
    LocalDate schoolDate = LocalDate.parse(schoolYear + "-09-30");
    var dobDate = LocalDate.parse(dob, DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));
    return Period.between(dobDate, schoolDate).getYears();
  }

  /**
   * Gets school year.
   *
   * @param localDate the local date
   * @return the school year
   */
  protected int getSchoolYear(LocalDate localDate) {
    var currentMonth = localDate.getMonth();
    var schoolYear = localDate.getYear();
    if (currentMonth.getValue() < 6) {
      schoolYear = schoolYear - 1;
    }
    log.debug("calculated year is :: {}", schoolYear);
    return schoolYear;
  }

  private boolean noDOBErrorReported(List<PenRequestStudentValidationIssue> issueList) {
    var result = issueList.stream().filter(element -> element.getPenRequestBatchValidationFieldCode().equals(BIRTH_DATE.getCode())).collect(Collectors.toList());
    return result.isEmpty();
  }


  /**
   * Sets current date.
   *
   * @param currentDate the current date
   */
  public void setCurrentDate(LocalDate currentDate) {
    this.currentDate = currentDate;
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
}
