package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rest.RestUtils;
import ca.bc.gov.educ.api.pen.services.rules.BaseRule;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.GENDER;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.GENDER_ERR;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Gender rule.
 */
@Slf4j
public class GenderRule extends BaseRule {

  /**
   * The Rest utils.
   */
  @Getter(PRIVATE)
  private final RestUtils restUtils;

  /**
   * Instantiates a new Gender rule.
   *
   * @param restUtils the rest utils
   */
  public GenderRule(final RestUtils restUtils) {
    this.restUtils = restUtils;
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
    final var genderCodes = this.restUtils.getGenderCodes();
    final String genderCode = validationPayload.getGenderCode();
    if (StringUtils.isBlank(genderCode)) {
      results.add(this.createValidationEntity(ERROR, GENDER_ERR, GENDER));
    } else {
      final String finalGenderCode = genderCode.trim();
      val isValidGenderCode = genderCodes.stream().anyMatch(gender -> LocalDateTime.now().isAfter(gender.getEffectiveDate())
        && LocalDateTime.now().isBefore(gender.getExpiryDate())
        && finalGenderCode.equalsIgnoreCase(gender.getGenderCode()));
      if (!isValidGenderCode) {
        results.add(this.createValidationEntity(ERROR, GENDER_ERR, GENDER));
      }
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds", validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }
}
