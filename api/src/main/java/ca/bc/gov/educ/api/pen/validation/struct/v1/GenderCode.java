package ca.bc.gov.educ.api.pen.validation.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The type Gender code.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("squid:S1700")
public class GenderCode {
  /**
   * The Gender code.
   */
  String genderCode;
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
  LocalDateTime effectiveDate;
  /**
   * The Expiry date.
   */
  LocalDateTime expiryDate;

}
