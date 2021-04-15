package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rules.BaseLastNameFirstNameRule;
import ca.bc.gov.educ.api.pen.services.service.PENNameTextService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.LEGAL_FIRST;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.*;


/**
 * The type Legal last name rule.
 */
@Slf4j
public class LegalFirstNameRule extends BaseLastNameFirstNameRule {

  /**
   * The Pen name text service.
   */
  private final PENNameTextService penNameTextService;

  /**
   * Instantiates a new Legal first name rule.
   *
   * @param penNameTextService the pen name text service
   */
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
  public List<PenRequestStudentValidationIssue> validate(final PenRequestStudentValidationPayload validationPayload) {
    final var stopwatch = Stopwatch.createStarted();
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    final var legalFirstName = validationPayload.getLegalFirstName();
    if (StringUtils.isBlank(legalFirstName)) {
      results.add(this.createValidationEntity(WARNING, BLANK_FIELD, LEGAL_FIRST));
    } else if (legalFirstName.trim().equals("'")) {
      results.add(this.createValidationEntity(WARNING, APOSTROPHE, LEGAL_FIRST));
    } else {
      this.defaultValidationForNameFields(results, legalFirstName, LEGAL_FIRST);
    }
    //PreReq: Skip this check if any of these issues has been reported for the current field: V2, V3, V4, V5, V6, V7, V8
    // to achieve above we do an empty check here and proceed only if there were no validation error till now, for this field. V9 check.
    if (results.isEmpty()) {
      this.checkFieldValueExactMatchWithInvalidText(results, legalFirstName, LEGAL_FIRST, validationPayload.getIsInteractive(), this.penNameTextService.getPenNameTexts());
    }
    if (results.isEmpty() && legalFirstName.trim().length() == 1) { // if we dont have any validation
      results.add(this.createValidationEntity(WARNING, ONE_CHAR_NAME, LEGAL_FIRST));
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds", validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }


}
