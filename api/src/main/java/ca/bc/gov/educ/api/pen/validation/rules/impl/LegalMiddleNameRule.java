package ca.bc.gov.educ.api.pen.validation.rules.impl;

import ca.bc.gov.educ.api.pen.validation.rules.BaseRule;
import ca.bc.gov.educ.api.pen.validation.service.PENNameTextService;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationFieldCode.*;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueTypeCode.EMBEDDED_MID;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueTypeCode.REPEAT_MID;


/**
 * The type Legal middle name rule.
 */
@Slf4j
public class LegalMiddleNameRule extends BaseRule {

  private final PENNameTextService penNameTextService;

  /**
   * Instantiates a new Legal middle name rule.
   *
   * @param penNameTextService the pen name text service
   */
  public LegalMiddleNameRule(final PENNameTextService penNameTextService) {
    this.penNameTextService = penNameTextService;
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
    var legalMiddleName = validationPayload.getLegalMiddleNames();
    if (StringUtils.isNotBlank(legalMiddleName)) {
      legalMiddleName = legalMiddleName.trim();
      defaultValidationForNameFields(results, legalMiddleName, LEGAL_MID);
    }
    if (results.isEmpty() && StringUtils.isNotBlank(legalMiddleName)) {
      checkFieldValueExactMatchWithInvalidText(results, legalMiddleName, LEGAL_MID, validationPayload.getIsInteractive(), penNameTextService.getPenNameTexts());
    }
    if (results.isEmpty() && StringUtils.isNotBlank(legalMiddleName)
        && legalFirstNameHasNoErrors(validationPayload) && legalLastNameHasNoErrors(validationPayload)
        && (legalMiddleName.equals(validationPayload.getLegalFirstName()) || legalMiddleName.equals(validationPayload.getLegalLastName()))) {
      results.add(createValidationEntity(WARNING, REPEAT_MID, LEGAL_MID));
    }
    if (results.isEmpty() && StringUtils.isNotBlank(legalMiddleName)
        && legalFirstNameHasNoErrors(validationPayload)
        && StringUtils.isNotBlank(validationPayload.getLegalFirstName())
        && validationPayload.getLegalFirstName().contains(legalMiddleName)) {
      results.add(createValidationEntity(WARNING, EMBEDDED_MID, LEGAL_MID));
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds",validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }

  private boolean legalLastNameHasNoErrors(PenRequestStudentValidationPayload validationPayload) {
    var result = validationPayload.getIssueList().stream().filter(element -> element.getPenRequestBatchValidationFieldCode().equals(LEGAL_LAST.getCode())).collect(Collectors.toList());
    return result.isEmpty();
  }

  private boolean legalFirstNameHasNoErrors(PenRequestStudentValidationPayload validationPayload) {
    var result = validationPayload.getIssueList().stream().filter(element -> element.getPenRequestBatchValidationFieldCode().equals(LEGAL_FIRST.getCode())).collect(Collectors.toList());
    return result.isEmpty();
  }
}
