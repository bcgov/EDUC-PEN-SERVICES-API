package ca.bc.gov.educ.api.pen.services.mapper.v1;

import ca.bc.gov.educ.api.pen.services.mapper.UUIDMapper;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Move sld saga data mapper.
 */
@Mapper(uses = {UUIDMapper.class})
public interface MoveSldSagaDataMapper {
  /**
   * The constant mapper.
   */
  MoveSldSagaDataMapper mapper = Mappers.getMapper(MoveSldSagaDataMapper.class);

  /**
   * To sld student id struct.
   *
   * @param moveSldSagaData the move sld saga data
   * @return the sld id struct
   */
  SldStudentId toSldStudentId(MoveSldSagaData moveSldSagaData);

  /**
   * To sld student program struct.
   *
   * @param moveSldSagaData the move sld saga data
   * @return the sld student program struct
   */
  SldStudentProgram toSldStudentProgram(MoveSldSagaData moveSldSagaData);
}
