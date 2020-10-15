package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rules.BaseRule;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.BIRTH_DATE;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.*;

/**
 * The type Birth date rule.
 */
@Slf4j
public class BirthDateRule extends BaseRule {
  /**
   * Validates the student record for the given rule.
   * Note on pattern "uuuuMMdd" this is from  Java 8
   * Java 8 uses 'uuuu' for year, not 'yyyy'. In Java 8, ‘yyyy’ means “year of era” (BC or AD).
   *
   * @param validationPayload the validation payload
   * @return the validation result as a list.
   */
  @Override
  public List<PenRequestStudentValidationIssue> validate(PenRequestStudentValidationPayload validationPayload) {
    var stopwatch = Stopwatch.createStarted();
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    String birthDate = validationPayload.getDob();
    if (StringUtils.isBlank(birthDate)) {
      results.add(createValidationEntity(ERROR, DOB_INVALID, BIRTH_DATE));
    }
    if (results.isEmpty()) {
      birthDate = birthDate.trim();
      try {
        var dobDate = LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));
        var dobPast = LocalDate.parse("19000101", DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));
        if (dobDate.isAfter(LocalDate.now())) {
          results.add(createValidationEntity(ERROR, DOB_FUTURE, BIRTH_DATE));
        }
        if (dobDate.isBefore(dobPast)) {
          results.add(createValidationEntity(ERROR, DOB_PAST, BIRTH_DATE));
        }
      } catch (Exception ex) {
        results.add(createValidationEntity(ERROR, DOB_INVALID, BIRTH_DATE));
      }
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds",validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }
}
