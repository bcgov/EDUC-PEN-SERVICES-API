package ca.bc.gov.educ.api.pen.services.mapper.v1;

import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationFieldCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationIssueSeverityCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationIssueTypeCodeEntity;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestBatchStudentValidationFieldCode;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestBatchStudentValidationIssueSeverityCode;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestBatchStudentValidationIssueTypeCode;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
@SuppressWarnings("squid:S1214")
public interface PenServicesMapper {

    PenServicesMapper mapper = Mappers.getMapper(PenServicesMapper.class);

    PenRequestBatchStudentValidationFieldCode toStructure(PenRequestBatchValidationFieldCodeEntity entity);

    PenRequestBatchStudentValidationIssueSeverityCode toStructure(PenRequestBatchValidationIssueSeverityCodeEntity entity);

    PenRequestBatchStudentValidationIssueTypeCode toStructure(PenRequestBatchValidationIssueTypeCodeEntity entity);
}
