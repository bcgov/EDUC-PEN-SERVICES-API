package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rules.BaseRule;
import ca.bc.gov.educ.api.pen.services.service.PENNameTextService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.LEGAL_LAST;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.BLANK_FIELD;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.ONE_CHAR_NAME;


/**
 * The type Legal last name rule.
 */
@Slf4j
public class LegalLastNameRule extends BaseRule {
  private final PENNameTextService penNameTextService;

  /**
   * Instantiates a new Legal last name rule.
   *
   * @param penNameTextService the pen name text service
   */
  public LegalLastNameRule(final PENNameTextService penNameTextService) {
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
    final List<PenRequestStudentValidationIssue> results = new ArrayList<>();
    final var legalLastName = validationPayload.getLegalLastName();
    if (StringUtils.isBlank(legalLastName)) {
      results.add(this.createValidationEntity(ERROR, BLANK_FIELD, LEGAL_LAST));
    } else {
      this.defaultValidationForNameFields(results, legalLastName, LEGAL_LAST);
    }
    //PreReq: Skip this check if any of these issues has been reported for the current field: V2, V3, V4, V5, V6, V7, V8
    // to achieve above we do an empty check here and proceed only if there were no validation error till now, for this field.
    if (results.isEmpty()) {
      this.checkFieldValueExactMatchWithInvalidText(results, legalLastName, LEGAL_LAST, validationPayload.getIsInteractive(), this.penNameTextService.getPenNameTexts());
    }
    if (results.isEmpty() && legalLastName.trim().length() == 1) {
      results.add(this.createValidationEntity(WARNING, ONE_CHAR_NAME, LEGAL_LAST));
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds", validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }


}
