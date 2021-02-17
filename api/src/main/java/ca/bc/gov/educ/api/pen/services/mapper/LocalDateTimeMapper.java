package ca.bc.gov.educ.api.pen.services.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The type Local date time mapper.
 */
public class LocalDateTimeMapper {

  /**
   * Map string.
   *
   * @param dateTime the date time
   * @return the string
   */
  public String map(final LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
  }

  /**
   * Map local date time.
   *
   * @param dateTime the date time
   * @return the local date time
   */
  public LocalDateTime map(final String dateTime) {
    if (dateTime == null) {
      return null;
    }
    return LocalDateTime.parse(dateTime);
  }

}
