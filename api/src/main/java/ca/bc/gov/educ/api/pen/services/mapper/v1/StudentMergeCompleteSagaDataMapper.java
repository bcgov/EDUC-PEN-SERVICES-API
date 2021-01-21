package ca.bc.gov.educ.api.pen.services.mapper.v1;

import ca.bc.gov.educ.api.pen.services.mapper.UUIDMapper;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeCompleteSagaData;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentUpdateSagaData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses={UUIDMapper.class})
public interface StudentMergeCompleteSagaDataMapper {
  StudentMergeCompleteSagaDataMapper mapper = Mappers.getMapper(StudentMergeCompleteSagaDataMapper.class);

  @Mapping(target = "legalMiddleNames", source= "studentMergeCompleteSagaData.legalMiddleNames")
  @Mapping(target = "legalLastName", source= "studentMergeCompleteSagaData.legalLastName")
  @Mapping(target = "legalFirstName", source= "studentMergeCompleteSagaData.legalFirstName")
  @Mapping(target = "usualMiddleNames", source= "studentMergeCompleteSagaData.usualMiddleNames")
  @Mapping(target = "usualLastName", source= "studentMergeCompleteSagaData.usualLastName")
  @Mapping(target = "usualFirstName", source= "studentMergeCompleteSagaData.usualFirstName")
  @Mapping(target = "studentID", source= "studentMergeCompleteSagaData.studentID")
  @Mapping(target = "statusCode", constant="A")
  @Mapping(target = "sexCode", expression="java(ca.bc.gov.educ.api.pen.services.util.CodeUtil.getSexCodeFromGenderCode(studentMergeCompleteSagaData.getGenderCode()))")
  @Mapping(target = "postalCode", source= "studentMergeCompleteSagaData.postalCode")
  @Mapping(target = "pen", ignore = true)
  @Mapping(target = "mincode", source= "studentMergeCompleteSagaData.mincode")
  @Mapping(target = "memo", ignore = true)
  @Mapping(target = "localID", source= "studentMergeCompleteSagaData.localID")
  @Mapping(target = "gradeYear", ignore = true)
  @Mapping(target = "gradeCode", ignore = true)
  @Mapping(target = "genderCode", source= "studentMergeCompleteSagaData.genderCode")
  @Mapping(target = "emailVerified", ignore = true)
  @Mapping(target = "email", ignore = true)
  @Mapping(target = "dob", expression="java(ca.bc.gov.educ.api.pen.services.util.LocalDateTimeUtil.getAPIFormattedDateOfBirth(studentMergeCompleteSagaData.getDob()))")
  @Mapping(target = "demogCode", ignore = true)
  @Mapping(target = "deceasedDate",ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "updateUser", source= "studentMergeCompleteSagaData.updateUser")
  @Mapping(target = "historyActivityCode", ignore = true)
  @Mapping(target = "trueStudentID", ignore = true)
  StudentUpdateSagaData toMergedToStudent(StudentMergeCompleteSagaData studentMergeCompleteSagaData);

  @Mapping(target = "studentID", source= "studentMergeCompleteSagaData.mergeStudentID")
  @Mapping(target = "statusCode", constant="M")
  @Mapping(target = "trueStudentID", source= "studentMergeCompleteSagaData.studentID")
  @Mapping(target = "updateUser", source= "studentMergeCompleteSagaData.updateUser")
  @Mapping(target = "historyActivityCode", source= "studentMergeCompleteSagaData.historyActivityCode")
  @Mapping(target = "legalMiddleNames", ignore = true)
  @Mapping(target = "legalLastName", ignore = true)
  @Mapping(target = "legalFirstName", ignore = true)
  @Mapping(target = "usualMiddleNames", ignore = true)
  @Mapping(target = "usualLastName", ignore = true)
  @Mapping(target = "usualFirstName", ignore = true)
  @Mapping(target = "sexCode", ignore = true)
  @Mapping(target = "postalCode", ignore = true)
  @Mapping(target = "pen", ignore = true)
  @Mapping(target = "mincode", ignore = true)
  @Mapping(target = "memo", ignore = true)
  @Mapping(target = "localID", ignore = true)
  @Mapping(target = "gradeYear", ignore = true)
  @Mapping(target = "gradeCode", ignore = true)
  @Mapping(target = "genderCode", ignore = true)
  @Mapping(target = "emailVerified", ignore = true)
  @Mapping(target = "email", ignore = true)
  @Mapping(target = "dob", ignore = true)
  @Mapping(target = "demogCode", ignore = true)
  @Mapping(target = "deceasedDate",ignore = true)
  @Mapping(target = "createUser", ignore = true)
  StudentUpdateSagaData toMergedFromStudent(StudentMergeCompleteSagaData studentMergeCompleteSagaData);

  @Mapping(target = "studentID", source= "studentMergeCompleteSagaData.studentID")
  @Mapping(target = "mergeStudentID", source= "studentMergeCompleteSagaData.mergeStudentID")
  @Mapping(target = "studentMergeDirectionCode", source= "studentMergeCompleteSagaData.studentMergeDirectionCode")
  @Mapping(target = "studentMergeSourceCode", source= "studentMergeCompleteSagaData.studentMergeSourceCode")
  @Mapping(target = "updateUser", source= "studentMergeCompleteSagaData.updateUser")
  @Mapping(target = "studentMergeID", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  StudentMerge toStudentMerge(StudentMergeCompleteSagaData studentMergeCompleteSagaData);
}
