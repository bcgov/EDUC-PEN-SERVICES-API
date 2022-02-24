package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rules.BaseLastNameFirstNameRule;
import ca.bc.gov.educ.api.pen.services.service.PENNameTextService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.LEGAL_FIRST;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.ERROR;
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
    val legalLastName = validationPayload.getLegalLastName();
    if (StringUtils.isBlank(legalFirstName)) {
      results.add(this.createValidationEntity(validationPayload.getIsInteractive() ? WARNING : ERROR, BLANK_FIELD, LEGAL_FIRST));
    } else if (legalFirstName.trim().equals("'")) {
      results.add(this.createValidationEntity(ERROR, APOSTROPHE, LEGAL_FIRST));
    } else if (StringUtils.equalsIgnoreCase(legalFirstName, legalLastName)) {
      results.add(this.createValidationEntity(validationPayload.getIsInteractive() ? WARNING : ERROR, SAME_NAME, LEGAL_FIRST));
    } else {
      this.defaultValidationForNameFields(results, legalFirstName, LEGAL_FIRST, validationPayload.getIsInteractive());
    }
    return this.checkForInvalidTextAndOneChar(validationPayload, stopwatch, results, legalFirstName, LEGAL_FIRST, this.penNameTextService);
  }


}
