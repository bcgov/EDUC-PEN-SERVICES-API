package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.PenServicesApiResourceApplication;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeDirectionCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeSourceCodeEntity;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeDirectionCodeTableRepository;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeRepository;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeSourceCodeTableRepository;
import ca.bc.gov.educ.api.pen.services.support.TestRedisConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.v1.URL.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {PenServicesApiResourceApplication.class})
@AutoConfigureMockMvc
public class StudentMergeControllerTest {

  @Autowired
  StudentMergeController controller;
  @Autowired
  StudentMergeRepository studentMergeRepo;
  @Autowired
  StudentMergeDirectionCodeTableRepository mergeDirectionCodeRepo;
  @Autowired
  StudentMergeSourceCodeTableRepository mergeSourceCodeRepo;
  @Autowired
  private MockMvc mockMvc;

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    this.mergeDirectionCodeRepo.save(this.createStudentMergeDirectionCodeData());
    this.mergeSourceCodeRepo.save(this.createStudentMergeSourceCodeData());
  }

  /**
   * need to delete the records to make it working in unit tests assertion, else the records will keep growing and assertions will fail.
   */
  @After
  public void after() {
    this.studentMergeRepo.deleteAll();
    this.mergeDirectionCodeRepo.deleteAll();
    this.mergeSourceCodeRepo.deleteAll();
  }

  @Test
  public void testFindStudentMerges_GivenValidStudentID_ShouldReturnMergedStudentIDs() throws Exception {
    final UUID fromStudentID = UUID.randomUUID();
    final UUID toStudentID = UUID.randomUUID();

    final StudentMergeEntity studentMergeFrom = new StudentMergeEntity();
    studentMergeFrom.setStudentID(fromStudentID);
    studentMergeFrom.setMergeStudentID(toStudentID);
    studentMergeFrom.setStudentMergeDirectionCode("FROM");
    studentMergeFrom.setStudentMergeSourceCode("MINISTRY");
    this.studentMergeRepo.save(studentMergeFrom);

    final StudentMergeEntity studentMergeTo = new StudentMergeEntity();
    studentMergeTo.setStudentID(toStudentID);
    studentMergeTo.setMergeStudentID(fromStudentID);
    studentMergeTo.setStudentMergeDirectionCode("TO");
    studentMergeTo.setStudentMergeSourceCode("MINISTRY");
    this.studentMergeRepo.save(studentMergeTo);

    this.mockMvc.perform(get(PEN_SERVICES + "/" + fromStudentID + MERGES)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_MERGE")))).andDo(print()).andExpect(status().isOk())
      .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.studentMergeDirectionCode=='FROM')].studentID").value(fromStudentID.toString()));
  }


  @Test
  public void testFindStudentMerges_GivenNOCreateDateBetweenAndDataPresentInDB_ShouldReturnBadRequest() throws Exception {
    this.mockMvc.perform(get(PEN_SERVICES + MERGES + BETWEEN_DATES_CREATED)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_MERGE")))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testFindStudentMerges_GivenCreateDateBetweenAndDataPresentInDB_ShouldReturnListOfStudentMerges() throws Exception {
    final UUID fromStudentID = UUID.randomUUID();
    final UUID toStudentID = UUID.randomUUID();

    final StudentMergeEntity studentMergeFrom = new StudentMergeEntity();
    studentMergeFrom.setStudentID(fromStudentID);
    studentMergeFrom.setMergeStudentID(toStudentID);
    studentMergeFrom.setStudentMergeDirectionCode("FROM");
    studentMergeFrom.setStudentMergeSourceCode("MINISTRY");
    studentMergeFrom.setCreateDate(LocalDateTime.now());
    this.studentMergeRepo.save(studentMergeFrom);

    final StudentMergeEntity studentMergeTo = new StudentMergeEntity();
    studentMergeTo.setStudentID(toStudentID);
    studentMergeTo.setMergeStudentID(fromStudentID);
    studentMergeTo.setStudentMergeDirectionCode("TO");
    studentMergeTo.setStudentMergeSourceCode("MINISTRY");
    studentMergeTo.setCreateDate(LocalDateTime.now());
    this.studentMergeRepo.save(studentMergeTo);

    this.mockMvc.perform(get(PEN_SERVICES + MERGES + BETWEEN_DATES_CREATED)
      .param("createDateStart", LocalDateTime.now().minusDays(1).toString())
      .param("createDateEnd", LocalDateTime.now().plusDays(1).toString())
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_MERGE")))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$").isArray()).andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1));
  }

  @Test
  public void testFindStudentMerges_GivenCreateDateBetweenAndNoDataPresentInDB_ShouldReturnEmptyList() throws Exception {
    final UUID fromStudentID = UUID.randomUUID();
    final UUID toStudentID = UUID.randomUUID();

    final StudentMergeEntity studentMergeFrom = new StudentMergeEntity();
    studentMergeFrom.setStudentID(fromStudentID);
    studentMergeFrom.setMergeStudentID(toStudentID);
    studentMergeFrom.setStudentMergeDirectionCode("FROM");
    studentMergeFrom.setStudentMergeSourceCode("MINISTRY");
    studentMergeFrom.setCreateDate(LocalDateTime.now());
    this.studentMergeRepo.save(studentMergeFrom);

    final StudentMergeEntity studentMergeTo = new StudentMergeEntity();
    studentMergeTo.setStudentID(toStudentID);
    studentMergeTo.setMergeStudentID(fromStudentID);
    studentMergeTo.setStudentMergeDirectionCode("TO");
    studentMergeTo.setStudentMergeSourceCode("MINISTRY");
    studentMergeTo.setCreateDate(LocalDateTime.now());
    this.studentMergeRepo.save(studentMergeTo);

    this.mockMvc.perform(get(PEN_SERVICES + MERGES + BETWEEN_DATES_CREATED)
      .param("createDateStart", LocalDateTime.now().minusDays(5).toString())
      .param("createDateEnd", LocalDateTime.now().minusDays(1).toString())
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_MERGE")))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$").isArray()).andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
  }

  private StudentMergeDirectionCodeEntity createStudentMergeDirectionCodeData() {
    return StudentMergeDirectionCodeEntity.builder().mergeDirectionCode("FROM").description("Merge From")
      .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("label").createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }

  private StudentMergeSourceCodeEntity createStudentMergeSourceCodeData() {
    return StudentMergeSourceCodeEntity.builder().mergeSourceCode("MINISTRY").description("MINISTRY")
      .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("label").createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }
}
