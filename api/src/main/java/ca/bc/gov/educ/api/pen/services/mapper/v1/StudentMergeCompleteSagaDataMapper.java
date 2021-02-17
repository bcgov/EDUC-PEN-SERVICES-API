package ca.bc.gov.educ.api.pen.services.mapper.v1;

import ca.bc.gov.educ.api.pen.services.mapper.UUIDMapper;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Student merge complete saga data mapper.
 */
@Mapper(uses = {UUIDMapper.class})
public interface StudentMergeCompleteSagaDataMapper {
  /**
   * The constant mapper.
   */
  StudentMergeCompleteSagaDataMapper mapper = Mappers.getMapper(StudentMergeCompleteSagaDataMapper.class);

  /**
   * To student merge student merge.
   *
   * @param studentMergeCompleteSagaData the student merge complete saga data
   * @return the student merge
   */
  @Mapping(target = "studentID", source = "studentMergeCompleteSagaData.studentID")
  @Mapping(target = "mergeStudentID", source = "studentMergeCompleteSagaData.mergeStudentID")
  @Mapping(target = "studentMergeDirectionCode", source = "studentMergeCompleteSagaData.studentMergeDirectionCode")
  @Mapping(target = "studentMergeSourceCode", source = "studentMergeCompleteSagaData.studentMergeSourceCode")
  @Mapping(target = "updateUser", source = "studentMergeCompleteSagaData.updateUser")
  @Mapping(target = "studentMergeID", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  StudentMerge toStudentMerge(StudentMergeCompleteSagaData studentMergeCompleteSagaData);

  /**
   * To student merge student merge.
   *
   * @param studentDemergeCompleteSagaData the student demerge complete saga data
   * @return the student merge
   */
  @Mapping(target = "studentID", source = "studentDemergeCompleteSagaData.mergedToStudentID")
  @Mapping(target = "mergeStudentID", source = "studentDemergeCompleteSagaData.mergedFromStudentID")
  @Mapping(target = "studentMergeDirectionCode", ignore = true)
  @Mapping(target = "studentMergeSourceCode", ignore = true)
  @Mapping(target = "updateUser", source = "studentDemergeCompleteSagaData.updateUser")
  @Mapping(target = "studentMergeID", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  StudentMerge toStudentMerge(StudentDemergeCompleteSagaData studentDemergeCompleteSagaData);

  /**
   * To student saga student saga data.
   *
   * @param studentHistory the student history
   * @return the student saga data
   */
  @Mapping(target = "createUser", ignore = true)
  StudentSagaData toStudentSaga(StudentHistory studentHistory);
}
