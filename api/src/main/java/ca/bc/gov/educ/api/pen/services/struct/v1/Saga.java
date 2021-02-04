package ca.bc.gov.educ.api.pen.services.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * The type Saga.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Saga {
  UUID sagaId;
  UUID studentID;
  String sagaName;
  String sagaState;
  String payload;
  String status;
  String createUser;
  String updateUser;
  String createDate;
  String updateDate;
}
