package ca.bc.gov.educ.api.pen.services.mapper.v1;

import ca.bc.gov.educ.api.pen.services.mapper.UUIDMapper;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
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
  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  StudentMerge toStudentMerge(StudentMergeCompleteSagaData studentMergeCompleteSagaData);

  @Mapping(target = "studentID", source= "studentDemergeCompleteSagaData.mergedToStudentID")
  @Mapping(target = "mergeStudentID", source= "studentDemergeCompleteSagaData.mergedFromStudentID")
  @Mapping(target = "studentMergeDirectionCode", ignore = true)
  @Mapping(target = "studentMergeSourceCode", ignore = true)
  @Mapping(target = "updateUser", source= "studentDemergeCompleteSagaData.updateUser")
  @Mapping(target = "studentMergeID", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  StudentMerge toStudentMerge(StudentDemergeCompleteSagaData studentDemergeCompleteSagaData);

  @Mapping(target = "createUser", ignore = true)
  StudentSagaData toStudentSaga(StudentHistory studentHistory);
}
