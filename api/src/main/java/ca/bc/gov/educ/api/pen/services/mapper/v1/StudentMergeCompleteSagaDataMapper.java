package ca.bc.gov.educ.api.pen.services.mapper.v1;

import ca.bc.gov.educ.api.pen.services.mapper.UUIDMapper;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeCompleteSagaData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses={UUIDMapper.class})
public interface StudentMergeCompleteSagaDataMapper {
  StudentMergeCompleteSagaDataMapper mapper = Mappers.getMapper(StudentMergeCompleteSagaDataMapper.class);

  @Mapping(target = "studentID", source= "studentMergeCompleteSagaData.studentID")
  @Mapping(target = "mergeStudentID", source= "studentMergeCompleteSagaData.mergeStudentID")
  @Mapping(target = "studentMergeDirectionCode", source= "studentMergeCompleteSagaData.studentMergeDirectionCode")
  @Mapping(target = "studentMergeSourceCode", source= "studentMergeCompleteSagaData.studentMergeSourceCode")
  @Mapping(target = "updateUser", source= "studentMergeCompleteSagaData.updateUser")
  @Mapping(target = "studentMergeID", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  StudentMerge toStudentMerge(StudentMergeCompleteSagaData studentMergeCompleteSagaData);
}
