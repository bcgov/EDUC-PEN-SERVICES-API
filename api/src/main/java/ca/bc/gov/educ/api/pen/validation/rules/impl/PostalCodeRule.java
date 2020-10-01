package ca.bc.gov.educ.api.pen.validation.rules.impl;

import ca.bc.gov.educ.api.pen.validation.rules.BaseRule;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationFieldCode.POSTAL_CODE;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueTypeCode.PC_ERR;

/**
 * The type Postal code rule.
 */
@Slf4j
public class PostalCodeRule extends BaseRule {

  private static final Pattern pattern = Pattern.compile("^([A-Z]\\d[A-Z]\\d[A-Z]\\d|)$");

  /**
   * Validates the student record for the given rule.
   *
   * @param validationPayload the validation payload
   * @return the validation result as a list.
   */
  @Override
  public List<PenRequestStudentValidationIssue> validate(PenRequestStudentValidationPayload validationPayload) {
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    String postalCode = validationPayload.getPostalCode();
    if (StringUtils.isNotBlank(postalCode) && !pattern.matcher(postalCode).matches()) {
      results.add(createValidationEntity(WARNING, PC_ERR, POSTAL_CODE));
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    return results;
  }
}
