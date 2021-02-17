package ca.bc.gov.educ.api.pen.services.constants;

/**
 * The enum Topics enum.
 */
public enum TopicsEnum {
  /**
   * Pen services api topic topics enum.
   */
// APIs
  PEN_SERVICES_API_TOPIC,
  /**
   * Student api topic topics enum.
   */
  STUDENT_API_TOPIC,
  /**
   * Pen match api topic topics enum.
   */
  PEN_MATCH_API_TOPIC,
  /**
   * Pen services events topic topics enum.
   */
  PEN_SERVICES_EVENTS_TOPIC,

  /**
   * The Pen services merge students saga topic.
   */
// Saga - should start with PEN_SERVICES
  PEN_SERVICES_MERGE_STUDENTS_SAGA_TOPIC,
  /**
   * Pen services demerge students saga topic topics enum.
   */
  PEN_SERVICES_DEMERGE_STUDENTS_SAGA_TOPIC,
}
