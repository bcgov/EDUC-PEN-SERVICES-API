package ca.bc.gov.educ.api.pen.services.mapper.v1;

import ca.bc.gov.educ.api.pen.services.mapper.UUIDMapper;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeSourceCodeEntity;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeSourceCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Student merge mapper.
 */
@Mapper(uses = {UUIDMapper.class})
@SuppressWarnings("squid:S1214")
public interface StudentMergeMapper {

  /**
   * The constant mapper.
   */
  StudentMergeMapper mapper = Mappers.getMapper(StudentMergeMapper.class);

  /**
   * To model student merge entity.
   *
   * @param studentMerge the student merge
   * @return the student merge entity
   */
  StudentMergeEntity toModel(StudentMerge studentMerge);

  /**
   * To structure student merge.
   *
   * @param studentMergeEntity the student merge entity
   * @return the student merge
   */
  StudentMerge toStructure(StudentMergeEntity studentMergeEntity);

  /**
   * To model student merge source code entity.
   *
   * @param structure the structure
   * @return the student merge source code entity
   */
  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  StudentMergeSourceCodeEntity toModel(StudentMergeSourceCode structure);

  /**
   * To structure student merge source code.
   *
   * @param entity the entity
   * @return the student merge source code
   */
  StudentMergeSourceCode toStructure(StudentMergeSourceCodeEntity entity);
}
