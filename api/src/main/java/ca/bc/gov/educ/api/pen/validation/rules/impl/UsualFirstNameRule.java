package ca.bc.gov.educ.api.pen.validation.rules.impl;

import ca.bc.gov.educ.api.pen.validation.rules.BaseRule;
import ca.bc.gov.educ.api.pen.validation.service.PENNameTextService;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationFieldCode.USUAL_FIRST;

/**
 * The type Usual first name rule.
 */
@Slf4j
public class UsualFirstNameRule extends BaseRule {
  private final PENNameTextService penNameTextService;

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
  public List<PenRequestStudentValidationIssue> validate(PenRequestStudentValidationPayload validationPayload) {
    var stopwatch = Stopwatch.createStarted();
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    doValidate(validationPayload.getIsInteractive(), results, validationPayload.getUsualFirstName(), USUAL_FIRST, penNameTextService);
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds",validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }
}
