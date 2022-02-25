package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rules.BaseRule;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.POSTAL_CODE;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.INV_CHARS;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.PC_ERR;

/**
 * The type Postal code rule.
 */
@Slf4j
public class PostalCodeRule extends BaseRule {

  /**
   * The constant pattern.
   */
  private static final Pattern pattern = Pattern.compile("^([A-Z]\\d[A-Z]\\d[A-Z]\\d|)$");
  /**
   * The constant MINCODE_STARTS_WITH_102.
   */
  public static final String MINCODE_STARTS_WITH_102 = "102";

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
    final String postalCode = validationPayload.getPostalCode();
    final String mincode = validationPayload.getMincode();
    if (this.fieldContainsNonAsciiCharacter(postalCode)) {
      results.add(this.createValidationEntity(ERROR, INV_CHARS, POSTAL_CODE));
    } else if (StringUtils.isNotBlank(postalCode) && !pattern.matcher(postalCode).matches() && !mincode.startsWith(MINCODE_STARTS_WITH_102)) {
      results.add(this.createValidationEntity(WARNING, PC_ERR, POSTAL_CODE));
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds", validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }
}