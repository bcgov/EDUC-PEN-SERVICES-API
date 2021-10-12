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
   * To sld update single student event.
   *
   * @param moveSldSagaData the move sld saga data
   * @return the sld update single student event
   */
  @Mapping(target = "sldStudent.pen", source = "movedToPen")
  SldUpdateSingleStudentEvent toSldUpdateSingleStudentEvent(MoveSldSagaData moveSldSagaData);

  /**
   * To sld update student programs event.
   *
   * @param moveSldSagaData the move sld saga data
   * @return the sld update student programs event
   */
  @Mapping(target = "sldStudentProgram.pen", source = "movedToPen")
  SldUpdateStudentProgramsEvent toSldUpdateStudentProgramsEvent(MoveSldSagaData moveSldSagaData);
}
