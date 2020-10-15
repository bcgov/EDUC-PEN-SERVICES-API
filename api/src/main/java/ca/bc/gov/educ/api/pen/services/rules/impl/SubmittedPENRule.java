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
  public List<PenRequestStudentValidationIssue> validate(PenRequestStudentValidationPayload validationPayload) {
    var stopwatch = Stopwatch.createStarted();
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    if (StringUtils.isNotBlank(validationPayload.getSubmittedPen())) {
      boolean isInvalidCheckDigit = !validCheckDigit(validationPayload.getSubmittedPen().trim(), validationPayload.getTransactionID());
      if (isInvalidCheckDigit) {
        results.add(createValidationEntity(WARNING, CHECK_DIGIT, SUBMITTED_PEN));
      }
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds",validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }

  /**
   * Validate check digit boolean.
   *
   * @param pen           the pen
   * @param transactionID the transaction id
   * @return the boolean
   */
  protected boolean validCheckDigit(String pen, String transactionID) {
    log.debug(" transactionID :: {}, input :: pen={}", transactionID, pen);
    if (pen.length() != 9 || !pen.matches("-?\\d+(\\.\\d+)?")) {
      return false;
    }
    List<Integer> odds = new ArrayList<>();
    List<Integer> evens = new ArrayList<>();
    for (int i = 0; i < pen.length() - 1; i++) {
      int number = Integer.parseInt(pen.substring(i, i + 1));
      if (i % 2 == 0) {
        odds.add(number);
      } else {
        evens.add(number);
      }
    }

    int sumOdds = odds.stream().mapToInt(Integer::intValue).sum();

    StringBuilder fullEvenStringBuilder = new StringBuilder();
    for (int i : evens) {
      fullEvenStringBuilder.append(i);
    }

    List<Integer> listOfFullEvenValueDoubled = new ArrayList<>();
    String fullEvenValueDoubledString = Integer.toString(Integer.parseInt(fullEvenStringBuilder.toString()) * 2);
    for (int i = 0; i < fullEvenValueDoubledString.length(); i++) {
      listOfFullEvenValueDoubled.add(Integer.parseInt(fullEvenValueDoubledString.substring(i, i + 1)));
    }

    int sumEvens = listOfFullEvenValueDoubled.stream().mapToInt(Integer::intValue).sum();

    int finalSum = sumEvens + sumOdds;

    String penCheckDigit = pen.substring(8, 9);


    boolean result = ((finalSum % 10 == 0 && penCheckDigit.equals("0")) || ((10 - finalSum % 10) == Integer.parseInt(penCheckDigit)));
    log.debug(" transactionID :: {} , output :: booleanResult={}", transactionID, result);
    return result;
  }
}
