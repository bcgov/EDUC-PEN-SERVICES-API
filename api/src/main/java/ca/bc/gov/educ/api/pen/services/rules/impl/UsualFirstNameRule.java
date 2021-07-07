package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rules.BaseLastNameFirstNameRule;
import ca.bc.gov.educ.api.pen.services.service.PENNameTextService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.USUAL_FIRST;

/**
 * The type Usual first name rule.
 */
@Slf4j
public class UsualFirstNameRule extends BaseLastNameFirstNameRule {
  /**
   * The Pen name text service.
   */
  private final PENNameTextService penNameTextService;

  /**
   * Instantiates a new Usual first name rule.
   *
   * @param penNameTextService the pen name text service
   */
  public UsualFirstNameRule(final PENNameTextService penNameTextService) {
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
    this.doValidate(validationPayload.getIsInteractive(), results, validationPayload.getUsualFirstName(), USUAL_FIRST, this.penNameTextService, true);
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds", validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }
}
