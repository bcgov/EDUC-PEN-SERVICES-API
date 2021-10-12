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
   * Initiate success event outcome.
   */
  INITIATE_SUCCESS,
  /**
   * Student found event outcome.
   */
  STUDENT_FOUND,
  /**
   * Student created event outcome.
   */
  STUDENT_CREATED,
  /**
   * Student already exist event outcome.
   */
  STUDENT_ALREADY_EXIST,
  /**
   * Student updated event outcome.
   */
  STUDENT_UPDATED,
  /**
   * Merge created event outcome.
   */
  MERGE_CREATED,
  /**
   * Merge deleted event outcome.
   */
  MERGE_DELETED,
  /**
   * Student history found event outcome.
   */
  STUDENT_HISTORY_FOUND,
  /**
   * Student history created event outcome.
   */
  STUDENT_HISTORY_CREATED,
  /**
   * Possible match found event outcome.
   */
  POSSIBLE_MATCH_FOUND,
  /**
   * Possible match not found event outcome.
   */
  POSSIBLE_MATCH_NOT_FOUND,
  /**
   * Possible match added event outcome.
   */
  POSSIBLE_MATCH_ADDED,
  /**
   * Possible match deleted event outcome.
   */
  POSSIBLE_MATCH_DELETED,
  /**
   * Sld student updated event outcome.
   */
  SLD_STUDENT_UPDATED,
  /**
   * Sld student program updated event outcome.
   */
  SLD_STUDENT_PROGRAM_UPDATED,
  /**
   * Saga completed event outcome.
   */
  SAGA_COMPLETED
  }
