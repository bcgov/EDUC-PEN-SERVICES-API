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
   * Read Audit History from student.
   */
  READ_AUDIT_EVENT,

  /**
   * Add Audit History into student.
   */
  ADD_AUDIT_EVENT,

  GET_POSSIBLE_MATCH,

  DELETE_POSSIBLE_MATCH,

  MARK_SAGA_COMPLETE,

}
