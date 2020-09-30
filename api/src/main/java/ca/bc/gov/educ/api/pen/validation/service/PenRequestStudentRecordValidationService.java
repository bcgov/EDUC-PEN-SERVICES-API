package ca.bc.gov.educ.api.pen.validation.service;

import ca.bc.gov.educ.api.pen.validation.rules.Rule;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The type Pen request batch student record validation service.
 * see the {@link ca.bc.gov.educ.api.pen.validation.config.RulesConfig} to see how spring creates the implementation beans of {@link Rule}.
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
    var validationResult = validationPayload.getIssueList();
    rules.forEach(rule -> {
      var result = rule.validate(validationPayload);
      if (!result.isEmpty()) {
        validationResult.addAll(result);
      }
    });
    return validationResult;
  }

}
