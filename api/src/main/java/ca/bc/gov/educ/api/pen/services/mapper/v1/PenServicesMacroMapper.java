package ca.bc.gov.educ.api.pen.services.mapper.v1;

import ca.bc.gov.educ.api.pen.services.mapper.LocalDateTimeMapper;
import ca.bc.gov.educ.api.pen.services.mapper.UUIDMapper;
import ca.bc.gov.educ.api.pen.services.model.PenServicesMacroEntity;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenServicesMacro;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface PenServicesMacroMapper {

  PenServicesMacroMapper mapper = Mappers.getMapper(PenServicesMacroMapper.class);

  PenServicesMacro toStructure(PenServicesMacroEntity entity);

  PenServicesMacroEntity toModel(PenServicesMacro struct);
}
