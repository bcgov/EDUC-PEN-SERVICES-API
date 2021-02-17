package ca.bc.gov.educ.api.pen.services.mapper.v1;

import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationFieldCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationIssueSeverityCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationIssueTypeCodeEntity;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestBatchStudentValidationFieldCode;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestBatchStudentValidationIssueSeverityCode;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestBatchStudentValidationIssueTypeCode;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * The interface Pen services mapper.
 */
@Mapper
@SuppressWarnings("squid:S1214")
public interface PenServicesMapper {

  /**
   * The constant mapper.
   */
  PenServicesMapper mapper = Mappers.getMapper(PenServicesMapper.class);

  /**
   * To structure pen request batch student validation field code.
   *
   * @param entity the entity
   * @return the pen request batch student validation field code
   */
  PenRequestBatchStudentValidationFieldCode toStructure(PenRequestBatchValidationFieldCodeEntity entity);

  /**
   * To structure pen request batch student validation issue severity code.
   *
   * @param entity the entity
   * @return the pen request batch student validation issue severity code
   */
  PenRequestBatchStudentValidationIssueSeverityCode toStructure(PenRequestBatchValidationIssueSeverityCodeEntity entity);

  /**
   * To structure pen request batch student validation issue type code.
   *
   * @param entity the entity
   * @return the pen request batch student validation issue type code
   */
  PenRequestBatchStudentValidationIssueTypeCode toStructure(PenRequestBatchValidationIssueTypeCodeEntity entity);
}
