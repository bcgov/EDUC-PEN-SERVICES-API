package ca.bc.gov.educ.api.pen.services.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Saga.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "PEN_SERVICES_SAGA")
@DynamicUpdate
public class Saga {
  /**
   * The Saga id.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
      @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "SAGA_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID sagaId;

  /**
   * The Saga name.
   */
  @NotNull(message = "saga name cannot be null")
  @Column(name = "SAGA_NAME")
  String sagaName;

  /**
   * The Saga state.
   */
  @NotNull(message = "saga state cannot be null")
  @Column(name = "SAGA_STATE")
  String sagaState;

  /**
   * The student id.
   */
  @Column(name = "STUDENT_ID", columnDefinition = "BINARY(16)")
  UUID studentID;

  /**
   * The Payload.
   */
  @NotNull(message = "payload cannot be null")
  @Lob
  @Column(name = "PAYLOAD")
  byte[] payloadBytes;

  /**
   * The Status.
   */
  @NotNull(message = "status cannot be null")
  @Column(name = "STATUS")
  String status;

  /**
   * The Create user.
   */
  @NotNull(message = "create user cannot be null")
  @Column(name = "CREATE_USER", updatable = false)
  @Size(max = 100)
  String createUser;

  /**
   * The Update user.
   */
  @NotNull(message = "update user cannot be null")
  @Column(name = "UPDATE_USER")
  @Size(max = 100)
  String updateUser;

  /**
   * The Create date.
   */
  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;

  /**
   * The Update date.
   */
  @PastOrPresent
  @Column(name = "UPDATE_DATE")
  LocalDateTime updateDate;

  @Column(name = "RETRY_COUNT")
  private Integer retryCount;

  /**
   * Gets payload.
   *
   * @return the payload
   */
  public String getPayload() {
    return new String(this.getPayloadBytes(), StandardCharsets.UTF_8);
  }

  /**
   * Sets payload.
   *
   * @param payload the payload
   */
  public void setPayload(final String payload) {
    this.setPayloadBytes(payload.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * The type Saga builder.
   */
  public static class SagaBuilder {
    /**
     * The Payload bytes.
     */
    byte[] payloadBytes;

    /**
     * Payload saga builder.
     *
     * @param payload the payload
     * @return the saga builder
     */
    public SagaBuilder payload(final String payload) {
      this.payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
      return this;
    }
  }

}
