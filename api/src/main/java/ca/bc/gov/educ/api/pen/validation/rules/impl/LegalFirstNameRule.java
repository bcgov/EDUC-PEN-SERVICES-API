package ca.bc.gov.educ.api.pen.validation.rules.impl;

import ca.bc.gov.educ.api.pen.validation.rules.BaseRule;
import ca.bc.gov.educ.api.pen.validation.service.PENNameTextService;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationFieldCode.LEGAL_FIRST;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueTypeCode.*;


/**
 * The type Legal last name rule.
 */
@Slf4j
public class LegalFirstNameRule extends BaseRule {

  private final PENNameTextService penNameTextService;

  public LegalFirstNameRule(final PENNameTextService penNameTextService) {
    this.penNameTextService = penNameTextService;
  }

  /**
   * Validate the Last Name.
   *
   * @param validationPayload the validation payload
   * @return the list
   */
  @Override
  public List<PenRequestStudentValidationIssue> validate(PenRequestStudentValidationPayload validationPayload) {
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    var legalFirstName = validationPayload.getLegalFirstName();
    if (StringUtils.isBlank(legalFirstName)) {
      results.add(createValidationEntity(WARNING, BLANK_FIELD, LEGAL_FIRST));
    } else if (legalFirstName.trim().equals("'")) {
      results.add(createValidationEntity(WARNING, APOSTROPHE, LEGAL_FIRST));
    } else {
      defaultValidationForNameFields(results, legalFirstName, LEGAL_FIRST);
    }
    //PreReq: Skip this check if any of these issues has been reported for the current field: V2, V3, V4, V5, V6, V7, V8
    // to achieve above we do an empty check here and proceed only if there were no validation error till now, for this field. V9 check.
    if (results.isEmpty()) {
      checkFieldValueExactMatchWithInvalidText(results, legalFirstName, LEGAL_FIRST, validationPayload.getIsInteractive(), penNameTextService.getPenNameTexts());
    }
    if (results.isEmpty() && legalFirstName.trim().length() == 1) { // if we dont have any validation
      results.add(createValidationEntity(WARNING, ONE_CHAR_NAME, LEGAL_FIRST));
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    return results;
  }


}