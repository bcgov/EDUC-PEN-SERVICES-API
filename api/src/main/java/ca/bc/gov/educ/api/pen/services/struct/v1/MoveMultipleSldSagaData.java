package ca.bc.gov.educ.api.pen.services.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The type Move multiple sld saga data.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoveMultipleSldSagaData {
  /**
   * The List of sld data.
   */
  @NotNull(message = "moveSldSagaData cannot be null")
  List<MoveSldSagaData> moveSldSagaData;

  /**
   * The Student id.
   */
  @NotNull(message = "studentID cannot be null")
  String studentID;

  /**
   * The Create user.
   */
  @NotNull(message = "studentID cannot be null")
  String createUser;
}
