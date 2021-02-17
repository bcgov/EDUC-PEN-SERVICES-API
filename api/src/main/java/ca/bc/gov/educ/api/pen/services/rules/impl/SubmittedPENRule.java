package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.rules.BaseRule;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode.SUBMITTED_PEN;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.CHECK_DIGIT;


/**
 * The type Submitted pen rule.
 */
@Slf4j
public class SubmittedPENRule extends BaseRule {

  /**
   * Validate th submitted PEN.
   *
   * @param validationPayload the validation payload
   * @return the list
   */
  @Override
  public List<PenRequestStudentValidationIssue> validate(final PenRequestStudentValidationPayload validationPayload) {
    final var stopwatch = Stopwatch.createStarted();
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    if (StringUtils.isNotBlank(validationPayload.getSubmittedPen())) {
      final boolean isInvalidCheckDigit = !this.validCheckDigit(validationPayload.getSubmittedPen().trim(), validationPayload.getTransactionID());
      if (isInvalidCheckDigit) {
        results.add(this.createValidationEntity(WARNING, CHECK_DIGIT, SUBMITTED_PEN));
      }
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds", validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }

  /**
   * Validate check digit boolean.
   *
   * @param pen           the pen
   * @param transactionID the transaction id
   * @return the boolean
   */
  protected boolean validCheckDigit(final String pen, final String transactionID) {
    log.debug(" transactionID :: {}, input :: pen={}", transactionID, pen);
    if (pen.length() != 9 || !pen.matches("-?\\d+(\\.\\d+)?")) {
      return false;
    }
    final List<Integer> odds = new ArrayList<>();
    final List<Integer> evens = new ArrayList<>();
    for (int i = 0; i < pen.length() - 1; i++) {
      final int number = Integer.parseInt(pen.substring(i, i + 1));
      if (i % 2 == 0) {
        odds.add(number);
      } else {
        evens.add(number);
      }
    }

    final int sumOdds = odds.stream().mapToInt(Integer::intValue).sum();

    final StringBuilder fullEvenStringBuilder = new StringBuilder();
    for (final int i : evens) {
      fullEvenStringBuilder.append(i);
    }

    final List<Integer> listOfFullEvenValueDoubled = new ArrayList<>();
    final String fullEvenValueDoubledString = Integer.toString(Integer.parseInt(fullEvenStringBuilder.toString()) * 2);
    for (int i = 0; i < fullEvenValueDoubledString.length(); i++) {
      listOfFullEvenValueDoubled.add(Integer.parseInt(fullEvenValueDoubledString.substring(i, i + 1)));
    }

    final int sumEvens = listOfFullEvenValueDoubled.stream().mapToInt(Integer::intValue).sum();

    final int finalSum = sumEvens + sumOdds;

    final String penCheckDigit = pen.substring(8, 9);


    final boolean result = ((finalSum % 10 == 0 && penCheckDigit.equals("0")) || ((10 - finalSum % 10) == Integer.parseInt(penCheckDigit)));
    log.debug(" transactionID :: {} , output :: booleanResult={}", transactionID, result);
    return result;
  }
}
