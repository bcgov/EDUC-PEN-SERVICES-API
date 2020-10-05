package ca.bc.gov.educ.api.pen.validation.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * The type Pen name text.
 */
@Entity
@Data
@Table(name = "PEN_NAME_TEXT")
@Immutable
public class PENNameText {

  /**
   * The Record number.
   */
  @Id
  @Column(name = "RECORD_NUMBER", nullable = false, unique = true)
  Integer recordNumber;

  /**
   * The Invalid text.
   */
  @Basic
  @Column(name = "INVALID_TEXT", length = 100)
  String invalidText;

  /**
   * The Legal surname check.
   */
  @Basic
  @Column(name = "LEGAL_SURNAME_CHECK", length = 2)
  String legalSurnameCheck;

  /**
   * The Legal given check.
   */
  @Basic
  @Column(name = "LEGAL_GIVEN_CHECK", length = 2)
  String legalGivenCheck;

  /**
   * The Legal middle check.
   */
  @Basic
  @Column(name = "LEGAL_MIDDLE_CHECK", length = 2)
  String legalMiddleCheck;

  /**
   * The Usual surname check.
   */
  @Basic
  @Column(name = "USUAL_SURNAME_CHECK", length = 2)
  String usualSurnameCheck;

  /**
   * The Usual given check.
   */
  @Basic
  @Column(name = "USUAL_GIVEN_CHECK", length = 2)
  String usualGivenCheck;

  /**
   * The Usual middle check.
   */
  @Basic
  @Column(name = "USUAL_MIDDLE_CHECK", length = 2)
  String usualMiddleCheck;
  /**
   * The Effective date.
   */
  @Column(name = "EFFECTIVE_DATE")
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  LocalDateTime effectiveDate;

  /**
   * The Expiry date.
   */
  @Column(name = "EXPIRY_DATE")
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  LocalDateTime expiryDate;

  /**
   * The Create user.
   */
  @Basic
  @Column(name = "CREATE_USER", updatable = false , length = 32)
  String createUser;

  /**
   * The Create date.
   */
  @Column(name = "CREATE_DATE", updatable = false)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  LocalDateTime createDate;

  /**
   * The Update user.
   */
  @Basic
  @Column(name = "UPDATE_USER", length = 32)
  String updateUser;

  /**
   * The Update date.
   */
  @Column(name = "UPDATE_DATE")
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  LocalDateTime updateDate;


}
