package ca.bc.gov.educ.api.pen.services.rules;

import ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationFieldCode;
import ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode;
import ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode;
import ca.bc.gov.educ.api.pen.services.model.PENNameText;
import ca.bc.gov.educ.api.pen.services.service.PENNameTextService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode.WARNING;
import static ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueTypeCode.*;


/**
 * The type Base rule.
 */
@Slf4j
public abstract class BaseRule implements Rule {
  /**
   * The constant FC.
   */
  public static final String FC = "FC";
  public static final String QC = "QC";
  /**
   * The constant SPACE.
   */
  protected static final String SPACE = " ";
  /**
   * The Not allowed chars.
   */
  protected static List<String> notAllowedChars = Arrays.asList("^", "_");
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
  protected boolean fieldContainsInvalidCharacters(final String fieldValue, final List<String> notAllowedChars) {
    for (val character : fieldValue.toCharArray()) {
      if (notAllowedChars.contains(String.valueOf(character))) {
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
  protected boolean fieldBeginsWithInvalidCharacters(final String fieldValue, final List<String> notAllowedCharsToStartWith) {
    for (final String notAllowedChar : notAllowedCharsToStartWith) {
      if (fieldValue.startsWith(notAllowedChar)) {
        return true;
      }
    }
    return false;
  }

  protected boolean fieldContainsNonAsciiCharacter(final String fieldValue) {
    if (fieldValue == null) {
      return false;
    }
    var fieldValueWhole = StringUtils.stripAccents(fieldValue).toUpperCase();
    var fieldValueStripped = fieldValueWhole.replaceAll("[^\\p{ASCII}]", "").toUpperCase();
    log.debug("Returning fieldContainsNonAsciiCharacter result for fieldValueWhole :: {} :: fieldValueStripped: {} :: Result :: {}", fieldValueWhole, fieldValueStripped, !fieldValueWhole.equals(fieldValueStripped));
    return !fieldValueWhole.equals(fieldValueStripped);
  }

  protected boolean fieldContainsRepeatedCharacters(final String fieldValue) {
    if (StringUtils.length(fieldValue) < 2) {
      return false;
    }

    final Character firstChar = fieldValue.charAt(0);
    for (final Character character : fieldValue.toCharArray()) {
      if (!firstChar.equals(character)) {
        return false;
      }
    }
    return true;
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
    final PenRequestStudentValidationIssueSeverityCode issueSeverityCode,
    final PenRequestStudentValidationIssueTypeCode issueTypeCode,
    final PenRequestStudentValidationFieldCode fieldCode) {

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
  protected boolean fieldContainsSpace(final String fieldValue) {
    return fieldValue.contains(SPACE);
  }


  /**
   * Field starts with inverted prefix boolean.
   *
   * @param fieldValue the field value
   * @return the boolean
   */
  protected boolean fieldStartsWithInvertedPrefix(final String fieldValue) {
    for (final String invPrefix : invertedPrefix) {
      if (fieldValue.length() > invPrefix.length() && fieldValue.startsWith(invPrefix)) {
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
   * | V6 | Check: Name field has only repeated characters (i.e. JJJJJ)                                | LEGALLAST, LEGALFIRST, USUALLAST, USUALFIRST                        | ERROR   | REPEATCHARS |
   * | V7 | PreReq: Skip this check on LEGALFIRST if V3 already   recorded for LEGALFIRST.             | LEGALLAST, LEGALFIRST, USUALLAST, USUALFIRST                        | WARNING | BLANKINNAME |
   * |    | Check: Field has blanks within the field                                                   |                                                                     |         |             |
   * | V8 | PreReq: Skip this check on LEGALFIRST if V3 already   recorded for LEGALFIRST.             | LEGALLAST, LEGALFIRST, LEGALMID, USUALLAST, USUALFIRST,   USUALMID  | WARNING | INVPREFIX   |
   * |    | Check: Field starts with XX or ZZ                                                          |                                                                     |         |             |
   * | V24| PreReq: Skip this check if any of these issues has been reported for the current field:    | LEGALLAST, LEGALFIRST, LEGALMID, USUALLAST, USUALFIRST,   USUALMID  | ERROR   | INVPREFIX   |
   * |    | V2, V3, V4, V5, V6, V7, V8                                                                 |                                                                     |         |             |
   * |    | Check: Field contains digits 0-9                                                           |                                                                     |         |             |
   * </pre>
   *
   * @param results       the results
   * @param fieldValue    the field value
   * @param fieldCode     the field code
   * @param isInteractive if it is interactive or batch mode
   */
  protected void defaultValidationForNameFields(@NonNull final List<PenRequestStudentValidationIssue> results, @NonNull String fieldValue,
                                                @NonNull final PenRequestStudentValidationFieldCode fieldCode, final boolean isInteractive) {
    fieldValue = fieldValue.trim();
    if (this.fieldContainsInvalidCharacters(fieldValue, notAllowedChars)) {
      results.add(this.createValidationEntity(ERROR, INV_CHARS, fieldCode));
    } else if (this.fieldContainsNonAsciiCharacter(fieldValue)) {
      results.add(this.createValidationEntity(ERROR, INV_CHARS, fieldCode));
    } else if (this.fieldBeginsWithInvalidCharacters(fieldValue, notAllowedCharsToStartWith)) {
      results.add(this.createValidationEntity(ERROR, BEGIN_INVALID, fieldCode));
    } else if (this.fieldStartsWithInvertedPrefix(fieldValue)) {
      results.add(this.createValidationEntity(isInteractive ? WARNING : ERROR, INV_PREFIX, fieldCode));
    } else if (this.resultsContainNoError(results) && fieldValue.trim().matches(".*\\d.*")) {
      results.add(this.createValidationEntity(isInteractive ? WARNING : ERROR, NUMBER_NAME, fieldCode));
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
  protected void checkFieldValueExactMatchWithInvalidText(final List<PenRequestStudentValidationIssue> results, final String fieldValue, final PenRequestStudentValidationFieldCode fieldCode, final boolean isInteractive, final List<PENNameText> penNameTexts) {
    if (fieldValue != null && fieldCode != null && penNameTexts != null) {
      final var filteredList = penNameTexts.stream().filter(el ->
          (el.getEffectiveDate() != null && el.getExpiryDate() != null
            && el.getEffectiveDate().isBefore(LocalDate.now())
            && el.getExpiryDate().isAfter(LocalDate.now())
            && fieldValue.equalsIgnoreCase(el.getInvalidText())))
        .collect(Collectors.toList());
      if (!filteredList.isEmpty()) {
        final boolean isError;
        final boolean isWarning;
        switch (fieldCode) {
          case LEGAL_FIRST:
            isError = filteredList.stream().anyMatch(el -> FC.equalsIgnoreCase(el.getLegalGivenCheck()));
            isWarning = filteredList.stream().anyMatch(el -> QC.equalsIgnoreCase(el.getLegalGivenCheck()));
            this.createValidationEntry(results, fieldCode, isInteractive, isError, isWarning);
            break;
          case LEGAL_LAST:
            isError = filteredList.stream().anyMatch(el -> FC.equalsIgnoreCase(el.getLegalSurnameCheck()));
            isWarning = filteredList.stream().anyMatch(el -> QC.equalsIgnoreCase(el.getLegalSurnameCheck()));
            this.createValidationEntry(results, fieldCode, isInteractive, isError, isWarning);
            break;
          case LEGAL_MID:
            isError = filteredList.stream().anyMatch(el -> FC.equalsIgnoreCase(el.getLegalMiddleCheck()));
            isWarning = filteredList.stream().anyMatch(el -> QC.equalsIgnoreCase(el.getLegalMiddleCheck()));
            this.createValidationEntry(results, fieldCode, isInteractive, isError, isWarning);
            break;
          case USUAL_LAST:
            isError = filteredList.stream().anyMatch(el -> FC.equalsIgnoreCase(el.getUsualSurnameCheck()));
            isWarning = filteredList.stream().anyMatch(el -> QC.equalsIgnoreCase(el.getUsualSurnameCheck()));
            this.createValidationEntry(results, fieldCode, isInteractive, isError, isWarning);
            break;
          case USUAL_FIRST:
            isError = filteredList.stream().anyMatch(el -> FC.equalsIgnoreCase(el.getUsualGivenCheck()));
            isWarning = filteredList.stream().anyMatch(el -> QC.equalsIgnoreCase(el.getUsualGivenCheck()));
            this.createValidationEntry(results, fieldCode, isInteractive, isError, isWarning);
            break;
          case USUAL_MID:
            isError = filteredList.stream().anyMatch(el -> FC.equalsIgnoreCase(el.getUsualMiddleCheck()));
            isWarning = filteredList.stream().anyMatch(el -> QC.equalsIgnoreCase(el.getUsualMiddleCheck()));
            this.createValidationEntry(results, fieldCode, isInteractive, isError, isWarning);
            break;
          default:
            break;
        }
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Skip this check as one of the values are null  fieldValue :: {}, fieldCode :: {} ", fieldValue, fieldCode);
      }
    }
  }

  private void createValidationEntry(final List<PenRequestStudentValidationIssue> results, final PenRequestStudentValidationFieldCode fieldCode, final boolean isInteractive, final boolean isError, final boolean isWarning) {
    if (isError || isWarning) {
      this.createValidationErrorForInteractiveAndBatch(results, fieldCode, isInteractive, isError);
    }
  }

  /**
   * Create validation error for interactive and batch.
   *
   * @param results       the results
   * @param fieldCode     the field code
   * @param isInteractive the is interactive
   * @param isError       the is error
   */
  private void createValidationErrorForInteractiveAndBatch(final List<PenRequestStudentValidationIssue> results, final PenRequestStudentValidationFieldCode fieldCode, final boolean isInteractive, final boolean isError) {
    if (isInteractive) {
      results.add(this.createValidationEntity(WARNING, BLOCKED_NAME, fieldCode));
    } else {
      if (isError) {
        results.clear();
        results.add(this.createValidationEntity(ERROR, BLOCKED_NAME, fieldCode));
      } else if (results.isEmpty()) {
        results.add(this.createValidationEntity(WARNING, BLOCKED_NAME, fieldCode));
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
  protected void doValidate(final boolean isInteractive, final List<PenRequestStudentValidationIssue> results, final String fieldValue, final PenRequestStudentValidationFieldCode fieldCode, final PENNameTextService penNameTextService) {
    if (StringUtils.isNotBlank(fieldValue)) {
      if (StringUtils.equals("'", fieldValue)) {
        results.add(this.createValidationEntity(ERROR, APOSTROPHE, fieldCode));
      } else {
        this.defaultValidationForNameFields(results, fieldValue, fieldCode, isInteractive);
      }
    }
    if (this.resultsContainNoError(results)) {
      this.checkFieldValueExactMatchWithInvalidText(results, fieldValue, fieldCode, isInteractive, penNameTextService.getPenNameTexts());
    }
  }

  protected boolean resultsContainNoError(final List<PenRequestStudentValidationIssue> results) {
    return results.stream().noneMatch(el -> el.getPenRequestBatchValidationIssueSeverityCode().equals(ERROR.toString()));
  }

  protected List<PenRequestStudentValidationIssue> checkForInvalidTextAndOneChar(final PenRequestStudentValidationPayload validationPayload, final Stopwatch stopwatch, final List<PenRequestStudentValidationIssue> results, final String fieldValue, final PenRequestStudentValidationFieldCode penRequestStudentValidationFieldCode, final PENNameTextService penNameTextService) {
    //PreReq: Skip this check if any of these issues has been reported for the current field: V2, V3, V4, V5, V6, V7, V8
    // to achieve above we do an empty check here and proceed only if there were no validation error till now, for this field.
    if (this.resultsContainNoError(results)) {
      this.checkFieldValueExactMatchWithInvalidText(results, fieldValue, penRequestStudentValidationFieldCode, validationPayload.getIsInteractive(), penNameTextService.getPenNameTexts());
    }
    if (this.resultsContainNoError(results) && fieldValue.trim().length() == 1) {
      results.add(this.createValidationEntity(WARNING, ONE_CHAR_NAME, penRequestStudentValidationFieldCode));
    }
    log.debug("transaction ID :: {} , returning results size :: {}", validationPayload.getTransactionID(), results.size());
    stopwatch.stop();
    log.info("Completed for {} in {} milli seconds", validationPayload.getTransactionID(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return results;
  }
}
