package ca.bc.gov.educ.api.pen.services.service;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeEntity;
import ca.bc.gov.educ.api.pen.services.repository.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
public class StudentMergeServiceTest {
  StudentMergeService studentMergeService;

  @Autowired
  StudentMergeRepository studentMergeRepo;

  @Autowired
  StudentMergeDirectionCodeTableRepository studentMergeDirectionRepo;

  @Autowired
  StudentMergeSourceCodeTableRepository studentMergeSourceRepo;


  @Before
  public void before() {
    studentMergeService = new StudentMergeService(studentMergeRepo, studentMergeDirectionRepo, studentMergeSourceRepo);
  }

  @Test
  public void testFindStudentMerges_WhenStudentMergesDoNotExistInDB_ShouldReturnEmptyList() {
    UUID studentID = UUID.randomUUID();
    assertNotNull(studentID);
    assertThat(studentMergeService.findStudentMerges(studentID, null).size()).isZero();
  }

  @Test
  public void testFindStudentMerges_WhenStudentMergesExistInDB_ShouldReturnList() {
    UUID studentID = UUID.randomUUID();
    assertNotNull(studentID);
    UUID mergeStudentID = UUID.randomUUID();
    assertNotNull(mergeStudentID);
    StudentMergeEntity studentMerge = new StudentMergeEntity();
    studentMerge.setStudentID(studentID);
    studentMerge.setMergeStudentID(mergeStudentID);
    studentMerge.setStudentMergeDirectionCode("FROM");
    studentMerge.setStudentMergeSourceCode("MINISTRY");
    assertNotNull(studentMergeService.createStudentMerge(studentMerge));
    assertThat(studentMergeService.findStudentMerges(studentID, null).size()).isEqualTo(1);
  }


  @Test
  public void testFindStudentMerges_WhenStudentMergesToExistInDB_ShouldReturnList() {
    UUID studentID = UUID.randomUUID();
    assertNotNull(studentID);
    UUID mergeStudentID = UUID.randomUUID();
    assertNotNull(mergeStudentID);
    StudentMergeEntity studentMerge = new StudentMergeEntity();
    studentMerge.setStudentID(studentID);
    studentMerge.setMergeStudentID(mergeStudentID);
    studentMerge.setStudentMergeDirectionCode("TO");
    studentMerge.setStudentMergeSourceCode("MINISTRY");
    assertNotNull(studentMergeService.createStudentMerge(studentMerge));
    assertThat(studentMergeService.findStudentMerges(studentID, "TO").size()).isEqualTo(1);
  }

}
