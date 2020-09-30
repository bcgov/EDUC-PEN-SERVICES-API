package ca.bc.gov.educ.api.pen.validation.rules.impl;

import ca.bc.gov.educ.api.pen.validation.rules.BaseRule;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestBatchStudentValidationFieldCode.SUBMITTED_PEN;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestBatchStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestBatchStudentValidationIssueTypeCode.CHECK_DIGIT;


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
    final List<PenRequestStudentValidationIssue> results = new LinkedList<>();
    if (StringUtils.isNotBlank(validationPayload.getSubmittedPen())) {
      boolean isInvalidCheckDigit = validateCheckDigit(validationPayload.getSubmittedPen().trim(), validationPayload.getTransactionID());
      if (isInvalidCheckDigit) {
        results.add(createValidationEntity(WARNING, CHECK_DIGIT, SUBMITTED_PEN));
      }
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    return results;
  }

  /**
   * Validate check digit boolean.
   *
   * @param pen           the pen
   * @param transactionID the transaction id
   * @return the boolean
   */
  protected boolean validateCheckDigit(String pen, String transactionID) {
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
