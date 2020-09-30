package ca.bc.gov.educ.api.pen.validation.rules.impl;

import ca.bc.gov.educ.api.pen.validation.rules.BaseRule;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestBatchStudentValidationFieldCode.USUAL_MID;


/**
 * The type Usual middle name rule.
 */
@Slf4j
public class UsualMiddleNameRule extends BaseRule {
  /**
   * Validates the student record for the given rule.
   *
   * @param validationPayload the validation payload
   * @return the validation result as a list.
   */
  @Override
  public List<PenRequestStudentValidationIssue> validate(PenRequestStudentValidationPayload validationPayload) {
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    doValidate(validationPayload.getIsInteractive(), results, validationPayload.getUsualMiddleNames(), USUAL_MID);
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    return results;
  }
}
