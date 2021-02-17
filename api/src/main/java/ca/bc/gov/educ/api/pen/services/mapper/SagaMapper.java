package ca.bc.gov.educ.api.pen.services.mapper;

import ca.bc.gov.educ.api.pen.services.model.Saga;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * The interface Saga mapper.
 */
@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface SagaMapper {
  /**
   * The constant mapper.
   */
  SagaMapper mapper = Mappers.getMapper(SagaMapper.class);

  /**
   * To struct ca . bc . gov . educ . api . pen . services . struct . v 1 . saga.
   *
   * @param saga the saga
   * @return the ca . bc . gov . educ . api . pen . services . struct . v 1 . saga
   */
  ca.bc.gov.educ.api.pen.services.struct.v1.Saga toStruct(Saga saga);
}
