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
   * Initiated event type.
   */
  INITIATED,

  /**
   * Get student event type.
   */
  GET_STUDENT,

  /**
   * Create student event type.
   */
  CREATE_STUDENT,

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
   * Get Merge Data.
   */
  GET_MERGES,
  GET_MERGES_IN_DATE_RANGE,

  /**
   * Read Audit History from student.
   */
  GET_STUDENT_HISTORY,

  /**
   * Add Audit History into student.
   */
  CREATE_STUDENT_HISTORY,

  /**
   * Get possible match event type.
   */
  GET_POSSIBLE_MATCH,

  /**
   * Add possible match event type.
   */
  ADD_POSSIBLE_MATCH,

  /**
   * Delete possible match event type.
   */
  DELETE_POSSIBLE_MATCH,

  /**
   * Update sld students by ids event type.
   */
  UPDATE_SLD_STUDENTS_BY_IDS,

  /**
   * Update sld student programs by data event type.
   */
  UPDATE_SLD_STUDENT_PROGRAMS_BY_DATA,

  /**
   * Mark saga complete event type.
   */
  MARK_SAGA_COMPLETE,

}
