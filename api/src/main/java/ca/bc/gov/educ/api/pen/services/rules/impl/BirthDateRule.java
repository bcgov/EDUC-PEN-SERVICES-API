package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.exception.PenServicesAPIRuntimeException;
import ca.bc.gov.educ.api.pen.services.rest.RestUtils;
import ca.bc.gov.educ.api.pen.services.rules.BaseRule;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.BIRTH_DATE;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.*;

/**
 * The type Birth date rule.
 */
@Slf4j
public class BirthDateRule extends BaseRule {

  private final RestUtils restUtils;

  public BirthDateRule(final RestUtils restUtils) {
    this.restUtils = restUtils;
  }

  /**
   * Validates the student record for the given rule.
   * Note on pattern "uuuuMMdd" this is from  Java 8
   * Java 8 uses 'uuuu' for year, not 'yyyy'. In Java 8, ‘yyyy’ means “year of era” (BC or AD).
   *
   * @param validationPayload the validation payload
   * @return the validation result as a list.
   */
  @Override
  public List<PenRequestStudentValidationIssue> validate(final PenRequestStudentValidationPayload validationPayload) {
    final var stopwatch = Stopwatch.createStarted();
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    String birthDate = validationPayload.getDob();
    if (StringUtils.isBlank(birthDate)) {
      results.add(this.createValidationEntity(ERROR, DOB_INVALID, BIRTH_DATE));
    }
    LocalDate dobDate = null;
    if (results.isEmpty()) {
      birthDate = birthDate.trim();
      try {
        dobDate = LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));
        final var dobPast = LocalDate.parse("19000101", DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));
        if (dobDate.isAfter(LocalDate.now())) {
          results.add(this.createValidationEntity(ERROR, DOB_FUTURE, BIRTH_DATE));
        }
        if (dobDate.isBefore(dobPast)) {
          results.add(this.createValidationEntity(ERROR, DOB_PAST, BIRTH_DATE));
        }
      } catch (final Exception ex) {
        results.add(this.createValidationEntity(ERROR, DOB_INVALID, BIRTH_DATE));
      }
    }
    if (results.isEmpty() && StringUtils.isNotBlank(validationPayload.getMincode())) {
      this.validateDOBForOffshoreAndIndependentSchool(results, dobDate, validationPayload);
      this.validateDOBForPublicSchool(results, dobDate, validationPayload);
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds", validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }

  /**
   * Student is registered in a public school and is 5 years or younger as of Dec 31st.
   * <p>
   * Public, Yukon, PSI etc schools – Set to ‘Y’ if
   * <p>
   * SCHOOL_CATEGORY_CODE OF SCHOOL_MASTER NE "02" and & ; Not Independent
   * SCHOOL_CATEGORY_CODE OF SCHOOL_MASTER NE "09" and & ; Not Offshore
   * SCHOOL_CATEGORY_CODE OF SCHOOL_MASTER NE "10"       ; Not Early Learning
   *
   * @param results           the error/warning list
   * @param dob               the dob of student
   * @param validationPayload the payload that is being validated.
   */
  private void validateDOBForPublicSchool(final List<PenRequestStudentValidationIssue> results, final LocalDate dob, final PenRequestStudentValidationPayload validationPayload) {
    val schoolCategoryCode = this.getSchoolCategoryCode(validationPayload);
    if (!StringUtils.equals("02", schoolCategoryCode)
      && !StringUtils.equals("09", schoolCategoryCode)
      && !StringUtils.equals("10", schoolCategoryCode)
      && (Period.between(dob, this.getComparisonDate()).toTotalMonths() < 60)) {
      results.add(this.createValidationEntity(ERROR, DOB_TOO_YOUNG, BIRTH_DATE));
    }
  }

  /**
   * V21
   * Student is registered in an independent school and is 4 years or younger as of Dec 31st.
   * <p>
   * Independent or Offshore school– Set to ‘Y’ if
   * <p>
   * SCHOOL_CATEGORY_CODE OF SCHOOL_MASTER = "02" or ; Independent School
   * SCHOOL_CATEGORY_CODE OF SCHOOL_MASTER = "09"      ; Offshore School
   * BIRTHDATE	ERROR	ERROR	DOB TOO YOUNG
   *
   * @param results           the error/warning list
   * @param dob               the dob of student
   * @param validationPayload the payload that is being validated.
   */
  private void validateDOBForOffshoreAndIndependentSchool(final List<PenRequestStudentValidationIssue> results, final LocalDate dob, final PenRequestStudentValidationPayload validationPayload) {
    val schoolCategoryCode = this.getSchoolCategoryCode(validationPayload);
    if ((StringUtils.equals("02", schoolCategoryCode) || StringUtils.equals("09", schoolCategoryCode))
      && (Period.between(dob, this.getComparisonDate()).toTotalMonths() < 48)) {
      results.add(this.createValidationEntity(ERROR, DOB_TOO_YOUNG, BIRTH_DATE));
    }
  }

  private LocalDate getComparisonDate() {
    return LocalDate.parse(LocalDate.now().getYear() + "-12-31");// 1 day is added as the between excludes the date, please read the docs of Period.Between
  }

  private String getSchoolCategoryCode(final PenRequestStudentValidationPayload validationPayload) {
    return this.restUtils.getSchoolByMincode(validationPayload.getMincode()).orElseThrow(() -> new PenServicesAPIRuntimeException("School not present for " + validationPayload.getMincode() + ", this should not have happened :: " + validationPayload.getTransactionID())).getSchoolCategoryCode();
  }

}
