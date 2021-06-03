package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rules.BaseRule;
import ca.bc.gov.educ.api.pen.services.service.PENNameTextService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.*;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.*;


/**
 * The type Legal middle name rule.
 */
@Slf4j
public class LegalMiddleNameRule extends BaseRule {

  /**
   * The Pen name text service.
   */
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
  public List<PenRequestStudentValidationIssue> validate(final PenRequestStudentValidationPayload validationPayload) {
    final var stopwatch = Stopwatch.createStarted();
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    var legalMiddleName = validationPayload.getLegalMiddleNames();
    if (StringUtils.isNotBlank(legalMiddleName)) {
      legalMiddleName = legalMiddleName.trim();
      if (StringUtils.equals("'", legalMiddleName)) {
        results.add(this.createValidationEntity(ERROR, APOSTROPHE, LEGAL_MID));
      } else {
        this.defaultValidationForNameFields(results, legalMiddleName, LEGAL_MID);
      }
      if (results.isEmpty()) {
        this.checkFieldValueExactMatchWithInvalidText(results, legalMiddleName, LEGAL_MID, validationPayload.getIsInteractive(), this.penNameTextService.getPenNameTexts());
      }
      if (results.isEmpty()
        && this.legalFirstNameHasNoErrors(validationPayload)
        && this.legalLastNameHasNoErrors(validationPayload)
        && (legalMiddleName.equals(validationPayload.getLegalFirstName()) || legalMiddleName.equals(validationPayload.getLegalLastName()))) {
        results.add(this.createValidationEntity(WARNING, REPEAT_MID, LEGAL_MID));
      }
      if (results.isEmpty()
        && this.legalFirstNameHasNoErrors(validationPayload)
        && StringUtils.isNotBlank(validationPayload.getLegalFirstName())
        && validationPayload.getLegalFirstName().contains(legalMiddleName)) {
        results.add(this.createValidationEntity(WARNING, EMBEDDED_MID, LEGAL_MID));
      }
    }

    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds", validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }

  /**
   * Legal last name has no errors boolean.
   *
   * @param validationPayload the validation payload
   * @return the boolean
   */
  private boolean legalLastNameHasNoErrors(final PenRequestStudentValidationPayload validationPayload) {
    final var result = validationPayload.getIssueList().stream().filter(element -> element.getPenRequestBatchValidationFieldCode().equals(LEGAL_LAST.getCode())).collect(Collectors.toList());
    return result.isEmpty();
  }

  /**
   * Legal first name has no errors boolean.
   *
   * @param validationPayload the validation payload
   * @return the boolean
   */
  private boolean legalFirstNameHasNoErrors(final PenRequestStudentValidationPayload validationPayload) {
    final var result = validationPayload.getIssueList().stream().filter(element -> element.getPenRequestBatchValidationFieldCode().equals(LEGAL_FIRST.getCode())).collect(Collectors.toList());
    return result.isEmpty();
  }
}
