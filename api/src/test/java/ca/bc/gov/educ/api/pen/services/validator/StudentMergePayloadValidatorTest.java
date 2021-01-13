package ca.bc.gov.educ.api.pen.services.validator;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeDirectionCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeSourceCodeEntity;
import ca.bc.gov.educ.api.pen.services.repository.*;
import ca.bc.gov.educ.api.pen.services.service.StudentMergeService;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StudentMergePayloadValidatorTest {

  @Mock
  StudentMergeRepository studentMergeRepo;

  @Mock
  StudentMergeDirectionCodeTableRepository studentMergeDirectionCodeTableRepo;

  @Mock
  StudentMergeSourceCodeTableRepository studentMergeSourceCodeTableRepo;


  @Mock
  StudentMergeService studentMergeService;

  @InjectMocks
  StudentMergePayloadValidator studentMergePayloadValidator;


  @Before
  public void before() {
    studentMergeService = new StudentMergeService(studentMergeRepo, studentMergeDirectionCodeTableRepo, studentMergeSourceCodeTableRepo);
    studentMergePayloadValidator = new StudentMergePayloadValidator(studentMergeService);
  }

  @Test
  public void testValidateMergeDirectionCode_WhenMergeDirectionCodeIsInvalid_ShouldAddAnErrorTOTheReturnedList() {
    StudentMerge merge = StudentMerge.builder().studentMergeDirectionCode("WRONG_CODE").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentMergeDirectionCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeDirectionCodeRecords());
    studentMergePayloadValidator.validateMergeDirectionCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeDirectionCode_WhenEffectiveDateIsAfterCurrentTime_ShouldAddAnErrorTOTheReturnedList() {
    StudentMerge merge = StudentMerge.builder().studentMergeDirectionCode("TO").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentMergeDirectionCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeDirectionCodeRecords(LocalDateTime.MAX, LocalDateTime.MAX));
    studentMergePayloadValidator.validateMergeDirectionCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeDirectionCode_WhenExpiryDateisBeforeCurrentTime_ShouldAddAnErrorTOTheReturnedList() {
    StudentMerge merge = StudentMerge.builder().studentMergeDirectionCode("FROM").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentMergeDirectionCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeDirectionCodeRecords(LocalDateTime.now(), LocalDateTime.MIN));
    studentMergePayloadValidator.validateMergeDirectionCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeSourceCode_WhenMergeSourceCodeIsNotExisted_ShouldAddAnErrorTOTheReturnedList() {
    StudentMerge merge = StudentMerge.builder().studentMergeSourceCode("WRONG_CODE").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentMergeSourceCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeSourceCodeRecords());
    studentMergePayloadValidator.validateMergeSourceCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeSourceCode_WhenEffectiveDateIsAfterCurrentTime_ShouldAddAnErrorTOTheReturnedList() {
    StudentMerge merge = StudentMerge.builder().studentMergeSourceCode("SCHOOL").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentMergeSourceCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeSourceCodeRecords(LocalDateTime.MAX, LocalDateTime.MAX));
    studentMergePayloadValidator.validateMergeSourceCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeSourceCode_WhenExpiryDateisBeforeCurrentTime_ShouldAddAnErrorTOTheReturnedList() {
    StudentMerge merge = StudentMerge.builder().studentMergeSourceCode("MINISTRY").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentMergeSourceCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeSourceCodeRecords(LocalDateTime.now(), LocalDateTime.MIN));
    studentMergePayloadValidator.validateMergeSourceCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  private List<StudentMergeDirectionCodeEntity> createDummyStudentMergeDirectionCodeRecords(LocalDateTime effectiveDate, LocalDateTime expiryDate) {
    return List.of(StudentMergeDirectionCodeEntity.builder().mergeDirectionCode("TO").effectiveDate(effectiveDate).expiryDate(expiryDate).build(),
        StudentMergeDirectionCodeEntity.builder().mergeDirectionCode("FROM").effectiveDate(effectiveDate).expiryDate(expiryDate).build());
  }

  private List<StudentMergeDirectionCodeEntity> createDummyStudentMergeDirectionCodeRecords() {
    return createDummyStudentMergeDirectionCodeRecords(LocalDateTime.now(), LocalDateTime.MAX);
  }

  private List<StudentMergeSourceCodeEntity> createDummyStudentMergeSourceCodeRecords(LocalDateTime effectiveDate, LocalDateTime expiryDate) {
    return List.of(StudentMergeSourceCodeEntity.builder().mergeSourceCode("SCHOOL").effectiveDate(effectiveDate).expiryDate(expiryDate).build(),
        StudentMergeSourceCodeEntity.builder().mergeSourceCode("MINISTRY").effectiveDate(effectiveDate).expiryDate(expiryDate).build());
  }

  private List<StudentMergeSourceCodeEntity> createDummyStudentMergeSourceCodeRecords() {
    return createDummyStudentMergeSourceCodeRecords(LocalDateTime.now(), LocalDateTime.MAX);
  }

}
