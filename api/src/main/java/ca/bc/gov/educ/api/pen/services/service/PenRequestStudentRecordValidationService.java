package ca.bc.gov.educ.api.pen.services.service;

import ca.bc.gov.educ.api.pen.services.rules.Rule;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The type Pen request batch student record validation service.
 * see the {@link ca.bc.gov.educ.api.pen.services.config.RulesConfig} to see how spring creates the implementation beans of {@link Rule}.
 */
@Service
@Slf4j
public class PenRequestStudentRecordValidationService {

  /**
   * The Rules.
   */
  private final List<Rule> rules;

  /**
   * Instantiates a new Pen request batch student record validation service.
   *
   * @param rules the rules
   */
  @Autowired
  public PenRequestStudentRecordValidationService(List<Rule> rules) {
    this.rules = rules;
  }

  /**
   * This method implements the data validation as mentioned here
   * <pre>
   *  <a href="https://gww.wiki.educ.gov.bc.ca/display/PEN/Story%3A+v1+Pre-Match+Validation+of+PEN+Request+Data"></a>
   * </pre>
   * Validate student record and return the result as a list.
   *
   * @param validationPayload the validation payload
   * @return the list
   */
  public List<PenRequestStudentValidationIssue> validateStudentRecord(final PenRequestStudentValidationPayload validationPayload) {
    var stopwatch = Stopwatch.createStarted();
    var validationResult = validationPayload.getIssueList();
    rules.forEach(rule -> {
      var result = rule.validate(validationPayload);
      if (!result.isEmpty()) {
        validationResult.addAll(result);
      }
    });
    log.debug("found {} error/warnings for this transaction :: {}", validationResult.size(), validationPayload.getTransactionID());
    stopwatch.stop();
    log.info("Completed validateStudentRecord for {} in {} milli seconds",validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return validationResult;
  }

}
