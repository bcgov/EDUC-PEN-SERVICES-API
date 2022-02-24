package ca.bc.gov.educ.api.pen.services.rules;

import ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import org.springframework.lang.NonNull;

import java.util.List;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.REPEATED_CHARS;

/**
 * for usual and legal , first name and last name this is an extra rule of repeat characters.
 */
public abstract class BaseLastNameFirstNameRule extends BaseRule {

  @Override
  protected void defaultValidationForNameFields(@NonNull final List<PenRequestStudentValidationIssue> results, @NonNull final String fieldValue, @NonNull final PenRequestStudentValidationFieldCode fieldCode, boolean isInteractive) {
    super.defaultValidationForNameFields(results, fieldValue, fieldCode, isInteractive);
    if (this.fieldContainsRepeatedCharacters(fieldValue)) {
      results.add(this.createValidationEntity(isInteractive ? WARNING : ERROR, REPEATED_CHARS, fieldCode));
    }
  }
}
