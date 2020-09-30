package ca.bc.gov.educ.api.pen.validation.rules.impl;

import ca.bc.gov.educ.api.pen.validation.rules.BaseRule;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestBatchStudentValidationFieldCode.LEGAL_LAST;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestBatchStudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestBatchStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestBatchStudentValidationIssueTypeCode.BLANK_FIELD;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestBatchStudentValidationIssueTypeCode.ONE_CHAR_NAME;


/**
 * The type Legal last name rule.
 */
@Slf4j
public class LegalLastNameRule extends BaseRule {
  /**
   * Validate the Last Name.
   *
   * @param validationPayload the validation payload
   * @return the list
   */
  @Override
  public List<PenRequestStudentValidationIssue> validate(PenRequestStudentValidationPayload validationPayload) {
    final List<PenRequestStudentValidationIssue> results = new ArrayList<>();
    var legalLastName = validationPayload.getLegalLastName();
    if (StringUtils.isBlank(legalLastName)) {
      results.add(createValidationEntity(ERROR, BLANK_FIELD, LEGAL_LAST));
    } else {
      defaultValidationForNameFields(results, legalLastName, LEGAL_LAST);
    }
    //PreReq: Skip this check if any of these issues has been reported for the current field: V2, V3, V4, V5, V6, V7, V8
    // to achieve above we do an empty check here and proceed only if there were no validation error till now, for this field.
    if (results.isEmpty()) {
      checkFieldValueExactMatchWithInvalidText(results, legalLastName, LEGAL_LAST, validationPayload.getIsInteractive());
    }
    if (results.isEmpty() && legalLastName.trim().length() == 1) {
      results.add(createValidationEntity(WARNING, ONE_CHAR_NAME, LEGAL_LAST));
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    return results;
  }


}
