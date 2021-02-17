package ca.bc.gov.educ.api.pen.services.validator;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeDirectionCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeSourceCodeEntity;
import ca.bc.gov.educ.api.pen.services.repository.ServicesEventRepository;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeDirectionCodeTableRepository;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeRepository;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeSourceCodeTableRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StudentMergePayloadValidatorTest {

  @Mock
  StudentMergeRepository studentMergeRepo;
  @Mock
  ServicesEventRepository eventRepository;
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
    this.studentMergeService = new StudentMergeService(this.studentMergeRepo, this.eventRepository, this.studentMergeDirectionCodeTableRepo, this.studentMergeSourceCodeTableRepo);
    this.studentMergePayloadValidator = new StudentMergePayloadValidator(this.studentMergeService);
  }

  @Test
  public void testValidateMergeDirectionCode_WhenMergeDirectionCodeIsInvalid_ShouldAddAnErrorTOTheReturnedList() {
    final StudentMerge merge = StudentMerge.builder().studentMergeDirectionCode("WRONG_CODE").build();
    final List<FieldError> errorList = new ArrayList<>();
    when(this.studentMergeDirectionCodeTableRepo.findAll()).thenReturn(this.createDummyStudentMergeDirectionCodeRecords());
    this.studentMergePayloadValidator.validateMergeDirectionCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeDirectionCode_WhenEffectiveDateIsAfterCurrentTime_ShouldAddAnErrorTOTheReturnedList() {
    final StudentMerge merge = StudentMerge.builder().studentMergeDirectionCode("TO").build();
    final List<FieldError> errorList = new ArrayList<>();
    when(this.studentMergeDirectionCodeTableRepo.findAll()).thenReturn(this.createDummyStudentMergeDirectionCodeRecords(LocalDateTime.MAX, LocalDateTime.MAX));
    this.studentMergePayloadValidator.validateMergeDirectionCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeDirectionCode_WhenExpiryDateisBeforeCurrentTime_ShouldAddAnErrorTOTheReturnedList() {
    final StudentMerge merge = StudentMerge.builder().studentMergeDirectionCode("FROM").build();
    final List<FieldError> errorList = new ArrayList<>();
    when(this.studentMergeDirectionCodeTableRepo.findAll()).thenReturn(this.createDummyStudentMergeDirectionCodeRecords(LocalDateTime.now(), LocalDateTime.MIN));
    this.studentMergePayloadValidator.validateMergeDirectionCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeSourceCode_WhenMergeSourceCodeIsNotExisted_ShouldAddAnErrorTOTheReturnedList() {
    final StudentMerge merge = StudentMerge.builder().studentMergeSourceCode("WRONG_CODE").build();
    final List<FieldError> errorList = new ArrayList<>();
    when(this.studentMergeSourceCodeTableRepo.findAll()).thenReturn(this.createDummyStudentMergeSourceCodeRecords());
    this.studentMergePayloadValidator.validateMergeSourceCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeSourceCode_WhenEffectiveDateIsAfterCurrentTime_ShouldAddAnErrorTOTheReturnedList() {
    final StudentMerge merge = StudentMerge.builder().studentMergeSourceCode("SCHOOL").build();
    final List<FieldError> errorList = new ArrayList<>();
    when(this.studentMergeSourceCodeTableRepo.findAll()).thenReturn(this.createDummyStudentMergeSourceCodeRecords(LocalDateTime.MAX, LocalDateTime.MAX));
    this.studentMergePayloadValidator.validateMergeSourceCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeSourceCode_WhenExpiryDateisBeforeCurrentTime_ShouldAddAnErrorTOTheReturnedList() {
    final StudentMerge merge = StudentMerge.builder().studentMergeSourceCode("MINISTRY").build();
    final List<FieldError> errorList = new ArrayList<>();
    when(this.studentMergeSourceCodeTableRepo.findAll()).thenReturn(this.createDummyStudentMergeSourceCodeRecords(LocalDateTime.now(), LocalDateTime.MIN));
    this.studentMergePayloadValidator.validateMergeSourceCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  private List<StudentMergeDirectionCodeEntity> createDummyStudentMergeDirectionCodeRecords(final LocalDateTime effectiveDate, final LocalDateTime expiryDate) {
    return List.of(StudentMergeDirectionCodeEntity.builder().mergeDirectionCode("TO").effectiveDate(effectiveDate).expiryDate(expiryDate).build(),
        StudentMergeDirectionCodeEntity.builder().mergeDirectionCode("FROM").effectiveDate(effectiveDate).expiryDate(expiryDate).build());
  }

  private List<StudentMergeDirectionCodeEntity> createDummyStudentMergeDirectionCodeRecords() {
    return this.createDummyStudentMergeDirectionCodeRecords(LocalDateTime.now(), LocalDateTime.MAX);
  }

  private List<StudentMergeSourceCodeEntity> createDummyStudentMergeSourceCodeRecords(final LocalDateTime effectiveDate, final LocalDateTime expiryDate) {
    return List.of(StudentMergeSourceCodeEntity.builder().mergeSourceCode("SCHOOL").effectiveDate(effectiveDate).expiryDate(expiryDate).build(),
        StudentMergeSourceCodeEntity.builder().mergeSourceCode("MINISTRY").effectiveDate(effectiveDate).expiryDate(expiryDate).build());
  }

  private List<StudentMergeSourceCodeEntity> createDummyStudentMergeSourceCodeRecords() {
    return this.createDummyStudentMergeSourceCodeRecords(LocalDateTime.now(), LocalDateTime.MAX);
  }

}
