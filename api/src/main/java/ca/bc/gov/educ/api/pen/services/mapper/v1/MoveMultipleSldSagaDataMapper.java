package ca.bc.gov.educ.api.pen.services.mapper.v1;

import ca.bc.gov.educ.api.pen.services.mapper.UUIDMapper;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Move multiple sld saga data mapper.
 */
@Mapper(uses = {UUIDMapper.class, MoveSldSagaDataMapper.class})
public interface MoveMultipleSldSagaDataMapper {
  /**
   * The constant mapper.
   */
  MoveMultipleSldSagaDataMapper mapper = Mappers.getMapper(MoveMultipleSldSagaDataMapper.class);

  /**
   * To sld update students by ids event.
   *
   * @param moveMultipleSldSagaData the move multiple sld saga data
   * @return the sld update students by ids event
   */
  @Mapping(target = "ids", source = "moveSldSagaData")
  @Mapping(target = "sldStudent.pen", source = "movedToPen")
  SldUpdateStudentsByIdsEvent toSldUpdateStudentsByIdsEvent(MoveMultipleSldSagaData moveMultipleSldSagaData);

  /**
   * To sld update student programs by data event.
   *
   * @param moveMultipleSldSagaData the move multiple sld saga data
   * @return the sld update student programs by data event
   */
  @Mapping(target = "examples", source = "moveSldSagaData")
  @Mapping(target = "sldStudentProgram.pen", source = "movedToPen")
  SldUpdateStudentProgramsByDataEvent toSldUpdateStudentProgramsByDataEvent(MoveMultipleSldSagaData moveMultipleSldSagaData);
}
