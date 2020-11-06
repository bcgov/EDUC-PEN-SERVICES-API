package ca.bc.gov.educ.api.pen.services.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * The type Pen request batch student validation field code entity.
 *
 * @author John
 */
@Data
@SuppressWarnings("squid:S1700")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PenRequestBatchStudentValidationFieldCode {

  /**
   * The Pen request batch student validation field code.
   */
  String code;

  /**
   * The Label.
   */
  String label;

  /**
   * The Description.
   */
  String description;

  /**
   * The Display order.
   */
  Integer displayOrder;

  /**
   * The Effective date.
   */
  String effectiveDate;

  /**
   * The Expiry date.
   */
  String expiryDate;
}
