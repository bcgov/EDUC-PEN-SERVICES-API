package ca.bc.gov.educ.api.pen.services.constants;

/**
 * The enum Event type.
 */
public enum EventType {
  /**
   * Validate student demographics event type.
   */
  VALIDATE_STUDENT_DEMOGRAPHICS,

  /**
   * Get the next PEN number.
   */
  GET_NEXT_PEN_NUMBER,

  /**
   *
   */
  INITIATED,

  /**
   *
   */
  GET_STUDENT,

  /**
   * Update Student.
   */
  UPDATE_STUDENT,

  /**
   * Create Merge Data.
   */
  CREATE_MERGE,

  /**
   * Delete Merge Data.
   */
  DELETE_MERGE,

  /**
   * Read Audit History from student.
   */
  GET_STUDENT_HISTORY,

  /**
   * Add Audit History into student.
   */
  CREATE_STUDENT_HISTORY,

  GET_POSSIBLE_MATCH,

  ADD_POSSIBLE_MATCH,

  DELETE_POSSIBLE_MATCH,

  MARK_SAGA_COMPLETE,

}
