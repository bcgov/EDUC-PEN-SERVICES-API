package ca.bc.gov.educ.api.pen.validation.rules;

import ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationFieldCode;
import ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueSeverityCode;
import ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueTypeCode;
import ca.bc.gov.educ.api.pen.validation.model.PENNameText;
import ca.bc.gov.educ.api.pen.validation.service.PENNameTextService;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationIssue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.validation.constants.PenRequestStudentValidationIssueTypeCode.*;


/**
 * The type Base rule.
 */
@Slf4j
public abstract class BaseRule implements Rule {
  /**
   * The constant FC.
   */
  public static final String FC = "FC";
  /**
   * The constant SPACE.
   */
  protected static final String SPACE = " ";
  /**
   * The Not allowed chars.
   */
  protected static List<String> notAllowedChars = Arrays.asList("^", "_", "'");
  /**
   * The Not allowed chars to start with.
   */
  protected static List<String> notAllowedCharsToStartWith = Arrays.asList("*", "_", "\"", "-");

  /**
   * The Not allowed chars to start with.
   */
  protected static List<String> invertedPrefix = Arrays.asList("XX", "ZZ");

  /**
   * This method checks whether this field contains any not allowed characters.
   * <p>
   * Field contains invalid characters boolean.
   *
   * @param fieldValue      the field value
   * @param notAllowedChars the not allowed chars
   * @return the boolean
   */
  protected boolean fieldContainsInvalidCharacters(String fieldValue, List<String> notAllowedChars) {
    for (Character character : fieldValue.toCharArray()) {
      String letter = String.valueOf(character);
      if (notAllowedChars.contains(letter)) {
        return true;
      }
    }
    return false;
  }

  /**
   * This method checks whether this field contains any not allowed characters.
   * <p>
   * Field contains invalid characters boolean.
   *
   * @param fieldValue                 the field value
   * @param notAllowedCharsToStartWith the not allowed chars
   * @return the boolean
   */
  protected boolean fieldBeginsWithInvalidCharacters(String fieldValue, List<String> notAllowedCharsToStartWith) {
    for (String notAllowedChar : notAllowedCharsToStartWith) {
      if (fieldValue.startsWith(notAllowedChar)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets entity.
   *
   * @param issueSeverityCode the issue severity code
   * @param issueTypeCode     the issue type code
   * @param fieldCode         the field code
   * @return the entity
   */
  protected PenRequestStudentValidationIssue createValidationEntity(
      PenRequestStudentValidationIssueSeverityCode issueSeverityCode,
      PenRequestStudentValidationIssueTypeCode issueTypeCode,
      PenRequestStudentValidationFieldCode fieldCode) {

    return PenRequestStudentValidationIssue.builder()
        .penRequestBatchValidationIssueSeverityCode(issueSeverityCode.toString())
        .penRequestBatchValidationIssueTypeCode(issueTypeCode.getCode())
        .penRequestBatchValidationFieldCode(fieldCode.getCode())
        .build();
  }

  /**
   * Field contains space boolean.
   *
   * @param fieldValue the field value
   * @return the boolean
   */
  protected boolean fieldContainsSpace(String fieldValue) {
    return fieldValue.contains(SPACE);
  }

  /**
   * Field starts with inverted prefix boolean.
   *
   * @param fieldValue the field value
   * @return the boolean
   */
  protected boolean fieldStartsWithInvertedPrefix(String fieldValue) {
    for (String invPrefix : invertedPrefix) {
      if (fieldValue.startsWith(invPrefix)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Default validation for name fields.
   * <pre>
   * |  ID| Validation Check                                                                           |          Field Codes                                                | Severity| Type Code   |
   * |----|--------------------------------------------------------------------------------------------|---------------------------------------------------------------------|---------|-------------|
   * | V4 | PreReq:   Skip this check on LEGALFIRST if V3 already recorded for LEGALFIRST.             | LEGALLAST,   LEGALFIRST, LEGALMID, USUALLAST, USUALFIRST, USUALMID  | ERROR   | INVCHARS    |
   * |    | Check: Field contains any of: ^ (carat), _   (underscore), ' (single quote/apostrophe).    |                                                                     |         |             |
   * | V5 | PreReq: Skip this check on LEGALFIRST if V3 already   recorded for LEGALFIRST.             | LEGALLAST, LEGALFIRST, LEGALMID, USUALLAST, USUALFIRST,   USUALMID  | ERROR   | BEGININVALID  |
   * |    | Check: Field begins with any of: *(asterisk), _   (underscore), " (double quote), - (dash) |                                                                     |         |             |
   * | V7 | PreReq: Skip this check on LEGALFIRST if V3 already   recorded for LEGALFIRST.             | LEGALLAST, LEGALFIRST, USUALLAST, USUALFIRST                        | WARNING | BLANKINNAME |
   * |    | Check: Field has blanks within the field                                                   |                                                                     |         |             |
   * | V8 | PreReq: Skip this check on LEGALFIRST if V3 already   recorded for LEGALFIRST.             | LEGALLAST, LEGALFIRST, LEGALMID, USUALLAST, USUALFIRST,   USUALMID  | WARNING | INVPREFIX   |
   * |    | Check: Field starts with XX or ZZ                                                          |                                                                     |         |             |
   * </pre>
   *
   * @param results    the results
   * @param fieldValue the field value
   * @param fieldCode  the field code
   */
  protected void defaultValidationForNameFields(@NonNull List<PenRequestStudentValidationIssue> results, @NonNull String fieldValue,
                                                @NonNull PenRequestStudentValidationFieldCode fieldCode) {
    fieldValue = fieldValue.trim();
    if (fieldContainsInvalidCharacters(fieldValue, notAllowedChars)) {
      results.add(createValidationEntity(ERROR, INV_CHARS, fieldCode));
    }
    if (fieldBeginsWithInvalidCharacters(fieldValue, notAllowedCharsToStartWith)) {
      results.add(createValidationEntity(ERROR, BEGIN_INVALID, fieldCode));
    }
    if (fieldContainsSpace(fieldValue)) {
      results.add(createValidationEntity(WARNING, BLANK_IN_NAME, fieldCode));
    }
    if (fieldStartsWithInvertedPrefix(fieldValue)) {
      results.add(createValidationEntity(WARNING, INV_PREFIX, fieldCode));
    }
  }


  /**
   * Check field value exact match with invalid text.
   * <pre>
   *    PreReq: Skip this check if any of these issues has been reported for the current field: V2, V3, V4, V5, V6, V7, V8
   *    Check: Field exactly matches a value in PEN_NAME_TEXT.INVALID_TEXT that is active (Current Date is between EffectiveDate and ExpiryDate). Result: Look at value of the corresponding field column (one of these columns: LEGAL_SURNAME_CHECK LEGAL_GIVEN_CHECK LEGAL_MIDDLE_CHECK USUAL_SURNAME_CHECK USUAL_GIVEN_CHECK USUAL_MIDDLE_CHECK), and if the value is FC, the severity is ERROR, else if the value is QC, the severity is WARNING.
   *    Interactive Mode: If processing interactive (not batch), perform the check, but if issue occurs, set severity to WARNING, even if code is FC.
   * </pre>
   *
   * @param results       the results
   * @param fieldValue    the field value
   * @param fieldCode     the field code
   * @param isInteractive the is interactive
   * @param penNameTexts  the pen name texts
   */
  protected void checkFieldValueExactMatchWithInvalidText(List<PenRequestStudentValidationIssue> results, String fieldValue, PenRequestStudentValidationFieldCode fieldCode, boolean isInteractive, List<PENNameText> penNameTexts) {
    if (fieldValue != null && fieldCode != null && penNameTexts != null) {
      var filteredList = penNameTexts.stream().filter(el ->
          (el.getEffectiveDate() != null && el.getExpiryDate() != null
              && el.getEffectiveDate().isBefore(LocalDateTime.now())
              && el.getExpiryDate().isAfter(LocalDateTime.now())
              && fieldValue.equalsIgnoreCase(el.getInvalidText())))
          .collect(Collectors.toList());
      switch (fieldCode) {
        case LEGAL_FIRST:
          if (!filteredList.isEmpty()) {
            boolean isError = filteredList.stream().anyMatch(el -> FC.equalsIgnoreCase(el.getLegalGivenCheck()));
            createValidationErrorForInteractiveAndBatch(results, fieldCode, isInteractive, isError);
          }
          break;
        case LEGAL_LAST:
          if (!filteredList.isEmpty()) {
            boolean isError = filteredList.stream().anyMatch(el -> FC.equalsIgnoreCase(el.getLegalSurnameCheck()));
            createValidationErrorForInteractiveAndBatch(results, fieldCode, isInteractive, isError);
          }
          break;
        case LEGAL_MID:
          if (!filteredList.isEmpty()) {
            boolean isError = filteredList.stream().anyMatch(el -> FC.equalsIgnoreCase(el.getLegalMiddleCheck()));
            createValidationErrorForInteractiveAndBatch(results, fieldCode, isInteractive, isError);
          }
          break;
        case USUAL_LAST:
          if (!filteredList.isEmpty()) {
            boolean isError = filteredList.stream().anyMatch(el -> FC.equalsIgnoreCase(el.getUsualSurnameCheck()));
            createValidationErrorForInteractiveAndBatch(results, fieldCode, isInteractive, isError);
          }
          break;
        case USUAL_FIRST:
          if (!filteredList.isEmpty()) {
            boolean isError = filteredList.stream().anyMatch(el -> FC.equalsIgnoreCase(el.getUsualGivenCheck()));
            createValidationErrorForInteractiveAndBatch(results, fieldCode, isInteractive, isError);
          }
          break;
        case USUAL_MID:
          if (!filteredList.isEmpty()) {
            boolean isError = filteredList.stream().anyMatch(el -> FC.equalsIgnoreCase(el.getUsualMiddleCheck()));
            createValidationErrorForInteractiveAndBatch(results, fieldCode, isInteractive, isError);
          }
          break;
        default:
          break;
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Skip this check as one of the values are null  fieldValue :: {}, fieldCode :: {} ", fieldValue, fieldCode);
      }
    }
  }

  private void createValidationErrorForInteractiveAndBatch(List<PenRequestStudentValidationIssue> results, PenRequestStudentValidationFieldCode fieldCode, boolean isInteractive, boolean isError) {
    if (isInteractive) {
      results.add(createValidationEntity(WARNING, BLOCKED_NAME, fieldCode));
    } else {
      if (isError) {
        results.add(createValidationEntity(ERROR, BLOCKED_NAME, fieldCode));
      } else {
        results.add(createValidationEntity(WARNING, BLOCKED_NAME, fieldCode));
      }
    }
  }

  /**
   * Do validate.
   *
   * @param isInteractive      the is interactive
   * @param results            the results
   * @param fieldValue         the field value
   * @param fieldCode          the field code
   * @param penNameTextService the pen name text service
   */
  protected void doValidate(boolean isInteractive, List<PenRequestStudentValidationIssue> results, String fieldValue, PenRequestStudentValidationFieldCode fieldCode, PENNameTextService penNameTextService) {
    if (StringUtils.isNotBlank(fieldValue)) {
      defaultValidationForNameFields(results, fieldValue, fieldCode);
    }
    if (results.isEmpty()) {
      checkFieldValueExactMatchWithInvalidText(results, fieldValue, fieldCode, isInteractive, penNameTextService.getPenNameTexts());
    }
  }
}
