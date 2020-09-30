package ca.bc.gov.educ.api.pen.validation.rules;

import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;

import java.util.List;

/**
 * The interface Rule.
 * see the {@link ca.bc.gov.educ.api.pen.validation.config.RulesConfig} to see how spring creates the implementation beans.
 */
@FunctionalInterface
public interface Rule {

  /**
   * Validates the student record for the given rule.
   *
   * @param validationPayload the validation payload
   * @return the validation result as a list.
   */
  List<PenRequestStudentValidationIssue> validate(PenRequestStudentValidationPayload validationPayload);
}
