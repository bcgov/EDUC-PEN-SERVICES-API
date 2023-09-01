package ca.bc.gov.educ.api.pen.services.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;

import static java.time.temporal.ChronoField.*;

/**
 * The type Pen name text.
 */
@Entity
@Data
@Table(name = "PEN_NAME_TEXT")
@Immutable
public class PENNameText {
  private static final DateTimeFormatter YYYY_MM_DD_FORMATTER = new DateTimeFormatterBuilder()
    .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
    .appendValue(MONTH_OF_YEAR, 2)
    .appendValue(DAY_OF_MONTH, 2).toFormatter(); //yyyyMMdd
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
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @JsonSerialize(using = LocalDateSerializer.class)
  @Transient
  LocalDate effectiveDate;

  /**
   * The Expiry date.
   */
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @JsonSerialize(using = LocalDateSerializer.class)
  @Transient
  LocalDate expiryDate;
  /**
   * The Effective date.
   */
  @Column(name = "EFFECTIVE_DATE")
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @JsonSerialize(using = LocalDateSerializer.class)
  String effectiveDateStr;
  /**
   * The Expiry date.
   */
  @Column(name = "EXPIRY_DATE")
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @JsonSerialize(using = LocalDateSerializer.class)
  String expiryDateStr;
  /**
   * The Create user.
   */
  @Basic
  @Column(name = "CREATE_USER_NAME", updatable = false, length = 32)
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
  @Column(name = "UPDATE_USER_NAME", length = 32)
  String updateUser;
  /**
   * The Update date.
   */
  @Column(name = "UPDATE_DATE")
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  LocalDateTime updateDate;

  public LocalDate getEffectiveDate() {
    return this.effectiveDateStr == null ? null : LocalDate.parse(this.effectiveDateStr, YYYY_MM_DD_FORMATTER);
  }

  public void setEffectiveDate(final LocalDate effectiveDate) {
    this.effectiveDate = effectiveDate;
    this.effectiveDateStr = effectiveDate == null ? null : effectiveDate.format(YYYY_MM_DD_FORMATTER);
  }

  public LocalDate getExpiryDate() {
    return this.expiryDateStr == null ? null : LocalDate.parse(this.expiryDateStr, YYYY_MM_DD_FORMATTER);
  }

  public void setExpiryDate(final LocalDate expiryDate) {
    this.expiryDate = expiryDate;
    this.expiryDateStr = expiryDate == null ? null : expiryDate.format(YYYY_MM_DD_FORMATTER);
  }


}
