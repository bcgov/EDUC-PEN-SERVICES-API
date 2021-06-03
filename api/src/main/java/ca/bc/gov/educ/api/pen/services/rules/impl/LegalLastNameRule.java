package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rules.BaseLastNameFirstNameRule;
import ca.bc.gov.educ.api.pen.services.service.PENNameTextService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.LEGAL_LAST;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.APOSTROPHE;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.BLANK_FIELD;


/**
 * The type Legal last name rule.
 */
@Slf4j
public class LegalLastNameRule extends BaseLastNameFirstNameRule {
  /**
   * The Pen name text service.
   */
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
    } else if (StringUtils.equals("'", legalLastName)) {
      results.add(this.createValidationEntity(ERROR, APOSTROPHE, LEGAL_LAST));
    } else {
      this.defaultValidationForNameFields(results, legalLastName, LEGAL_LAST);
    }
    return this.checkForInvalidTextAndOneChar(validationPayload, stopwatch, results, legalLastName, LEGAL_LAST, this.penNameTextService);
  }


}
