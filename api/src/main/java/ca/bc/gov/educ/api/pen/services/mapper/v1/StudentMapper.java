package ca.bc.gov.educ.api.pen.services.mapper.v1;

import ca.bc.gov.educ.api.pen.services.mapper.UUIDMapper;
import ca.bc.gov.educ.api.pen.services.struct.v1.SplitPenSagaData;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentSagaData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * The interface Student mapper.
 */
@Mapper(uses = {UUIDMapper.class})
@SuppressWarnings("squid:S1214")
public interface StudentMapper {

  /**
   * The constant mapper.
   */
  StudentMapper mapper = Mappers.getMapper(StudentMapper.class);

  /**
   * To student structure.
   *
   * @param splitPenSagaData the split pen saga data
   * @return the student structure
   */
  StudentSagaData toStudent(SplitPenSagaData splitPenSagaData);
}
