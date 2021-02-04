package ca.bc.gov.educ.api.pen.services.mapper;

import ca.bc.gov.educ.api.pen.services.model.Saga;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface SagaMapper {
  SagaMapper mapper = Mappers.getMapper(SagaMapper.class);

  ca.bc.gov.educ.api.pen.services.struct.v1.Saga toStruct(Saga saga);
}
