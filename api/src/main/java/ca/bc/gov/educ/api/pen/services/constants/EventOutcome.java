package ca.bc.gov.educ.api.pen.services.constants;

/**
 * The enum Event outcome.
 */
public enum EventOutcome {
  /**
   * Read from topic success event outcome.
   */
  VALIDATION_SUCCESS_NO_ERROR_WARNING,
  /**
   * Validation success with error event outcome.
   */
  VALIDATION_SUCCESS_WITH_ERROR,
  /**
   * Validation success with only warning event outcome.
   */
  VALIDATION_SUCCESS_WITH_ONLY_WARNING,

  /**
   * Get the next PEN number
   */
  NEXT_PEN_NUMBER_RETRIEVED,

  /**
   *
   */
  INITIATE_SUCCESS,
  /**
   *
   */
  STUDENT_FOUND,
  /**
   *
   */
  STUDENT_UPDATED,
  /**
   *
   */
  MERGE_CREATED,
  /**
   *
   */
  MERGE_DELETED,
  /**
   *
   */
  STUDENT_HISTORY_FOUND,
  /**
   *
   */
  STUDENT_HISTORY_CREATED,
  /**
   *
   */
  POSSIBLE_MATCH_FOUND,
  /**
   *
   */
  POSSIBLE_MATCH_NOT_FOUND,
  /**
   *
   */
  POSSIBLE_MATCH_ADDED,
  /**
   *
   */
  POSSIBLE_MATCH_DELETED,
  /**
   *
   */
  SAGA_COMPLETED
  }
