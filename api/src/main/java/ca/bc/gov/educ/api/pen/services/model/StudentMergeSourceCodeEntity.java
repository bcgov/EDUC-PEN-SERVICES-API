package ca.bc.gov.educ.api.pen.services.model;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

/**
 * The type Student merge source code entity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "STUDENT_MERGE_SOURCE_CODE")
public class StudentMergeSourceCodeEntity {

  /**
   * The Merge source code.
   */
  @Id
  @Column(name = "MERGE_SOURCE_CODE", unique = true, updatable = false)
  String mergeSourceCode;

  /**
   * The Label.
   */
  @NotNull(message = "label cannot be null")
  @Column(name = "LABEL")
  String label;

  /**
   * The Description.
   */
  @NotNull(message = "description cannot be null")
  @Column(name = "DESCRIPTION")
  String description;

  /**
   * The Display order.
   */
  @NotNull(message = "displayOrder cannot be null")
  @Column(name = "DISPLAY_ORDER")
  Integer displayOrder;

  /**
   * The Effective date.
   */
  @NotNull(message = "effectiveDate cannot be null")
  @Column(name = "EFFECTIVE_DATE")
  LocalDateTime effectiveDate;

  /**
   * The Expiry date.
   */
  @NotNull(message = "expiryDate cannot be null")
  @Column(name = "EXPIRY_DATE")
  LocalDateTime expiryDate;

  /**
   * The Create user.
   */
  @Column(name = "CREATE_USER", updatable = false)
  String createUser;

  /**
   * The Create date.
   */
  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;

  /**
   * The Update user.
   */
  @Column(name = "UPDATE_USER", updatable = false)
  String updateUser;

  /**
   * The Update date.
   */
  @PastOrPresent
  @Column(name = "UPDATE_DATE", updatable = false)
  LocalDateTime updateDate;
}
