package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.PenServicesApiResourceApplication;
import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMergeMapper;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeDirectionCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeSourceCodeEntity;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeDirectionCodeTableRepository;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeRepository;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeSourceCodeTableRepository;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.v1.URL.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = PenServicesApiResourceApplication.class)
@AutoConfigureMockMvc
public class StudentMergeControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  StudentMergeController controller;

  @Autowired
  StudentMergeRepository studentMergeRepo;

  @Autowired
  StudentMergeDirectionCodeTableRepository mergeDirectionCodeRepo;

  @Autowired
  StudentMergeSourceCodeTableRepository mergeSourceCodeRepo;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    mergeDirectionCodeRepo.save(createStudentMergeDirectionCodeData());
    mergeSourceCodeRepo.save(createStudentMergeSourceCodeData());
  }

  /**
   * need to delete the records to make it working in unit tests assertion, else the records will keep growing and assertions will fail.
   */
  @After
  public void after() {
    studentMergeRepo.deleteAll();
    mergeDirectionCodeRepo.deleteAll();
    mergeSourceCodeRepo.deleteAll();
  }

  @Test
  public void testFindStudentMerges_GivenValidStudentID_ShouldReturnMergedStudentIDs() throws Exception {
    UUID fromStudentID = UUID.randomUUID();
    UUID toStudentID = UUID.randomUUID();

    StudentMergeEntity studentMergeFrom = new StudentMergeEntity();
    studentMergeFrom.setStudentID(fromStudentID);
    studentMergeFrom.setMergeStudentID(toStudentID);
    studentMergeFrom.setStudentMergeDirectionCode("FROM");
    studentMergeFrom.setStudentMergeSourceCode("MINISTRY");
    studentMergeRepo.save(studentMergeFrom);

    StudentMergeEntity studentMergeTo = new StudentMergeEntity();
    studentMergeTo.setStudentID(toStudentID);
    studentMergeTo.setMergeStudentID(fromStudentID);
    studentMergeTo.setStudentMergeDirectionCode("TO");
    studentMergeTo.setStudentMergeSourceCode("MINISTRY");
    studentMergeRepo.save(studentMergeTo);

    this.mockMvc.perform(get(PEN_SERVICES +"/"+ fromStudentID + MERGES)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_MERGE")))).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.studentMergeDirectionCode=='FROM')].studentID").value(fromStudentID.toString()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.studentMergeDirectionCode=='TO')].mergeStudentID").value(toStudentID.toString()));
  }

  @Test
  public void testCreateStudentMerge_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
    UUID fromStudentID = UUID.randomUUID();
    UUID toStudentID = UUID.randomUUID();

    StudentMergeEntity studentMergeFrom = new StudentMergeEntity();
    studentMergeFrom.setStudentID(fromStudentID);
    studentMergeFrom.setMergeStudentID(toStudentID);
    studentMergeFrom.setStudentMergeDirectionCode("FROM");
    studentMergeFrom.setStudentMergeSourceCode("MINISTRY");
    studentMergeFrom.setUpdateUser("Test User");

    StudentMerge studentMergeFromStruct = StudentMergeMapper.mapper.toStructure(studentMergeFrom);

    this.mockMvc.perform(post(PEN_SERVICES +"/"+ fromStudentID + MERGES)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_STUDENT_MERGE")))
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
        .content(asJsonString(studentMergeFromStruct))).andDo(print()).andExpect(status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.studentID").value(fromStudentID.toString()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.updateUser").value("Test User"));
  }

  @Test
  public void testCreateStudentMerge_GivenInvalidStudentID_ShouldReturnStatusBadRequest() throws Exception {
    UUID fromStudentID = UUID.randomUUID();
    UUID toStudentID = UUID.randomUUID();

    StudentMergeEntity studentMergeFrom = new StudentMergeEntity();
    studentMergeFrom.setStudentID(fromStudentID);
    studentMergeFrom.setMergeStudentID(toStudentID);
    studentMergeFrom.setStudentMergeDirectionCode("FROM");
    studentMergeFrom.setStudentMergeSourceCode("MINISTRY");

    this.mockMvc.perform(post(PEN_SERVICES +"/"+ fromStudentID + MERGES)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_STUDENT_MERGE")))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(StudentMergeMapper.mapper.toStructure(studentMergeFrom)))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateStudentMerge_GivenInvalidMergeSourceCode_ShouldReturnStatusBadRequest() throws Exception {
    UUID fromStudentID = UUID.randomUUID();
    UUID toStudentID = UUID.randomUUID();

    StudentMergeEntity studentMergeFrom = new StudentMergeEntity();
    studentMergeFrom.setStudentID(fromStudentID);
    studentMergeFrom.setMergeStudentID(toStudentID);
    studentMergeFrom.setStudentMergeDirectionCode("FROM");
    studentMergeFrom.setStudentMergeSourceCode("INVALID");

    this.mockMvc.perform(post(PEN_SERVICES +"/"+ fromStudentID + MERGES)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_STUDENT_MERGE")))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(StudentMergeMapper.mapper.toStructure(studentMergeFrom)))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testGetStudentMergeSourceCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get(PEN_SERVICES+MERGE_SOURCE_CODES)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_MERGE_CODES")))).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].mergeSourceCode").value("MINISTRY"));
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

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
