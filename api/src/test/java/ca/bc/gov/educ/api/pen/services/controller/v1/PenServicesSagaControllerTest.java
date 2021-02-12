package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.PenServicesApiResourceApplication;
import ca.bc.gov.educ.api.pen.services.repository.SagaEventRepository;
import ca.bc.gov.educ.api.pen.services.repository.SagaRepository;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.support.TestRedisConfiguration;
import lombok.extern.slf4j.Slf4j;
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

import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.PEN_SERVICES_STUDENT_DEMERGE_COMPLETE_SAGA;
import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.PEN_SERVICES_STUDENT_MERGE_COMPLETE_SAGA;
import static ca.bc.gov.educ.api.pen.services.constants.v1.URL.PEN_SERVICES;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The Pen services saga controller tests
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {TestRedisConfiguration.class, PenServicesApiResourceApplication.class})
@AutoConfigureMockMvc
@Slf4j
public class PenServicesSagaControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  PenServicesSagaController controller;

  @Autowired
  SagaRepository repository;

  @Autowired
  SagaEventRepository sagaEventRepository;

  @Autowired
  SagaService sagaService;

  private final String studentID = "7f000101-7151-1d84-8171-5187006c0001";
  private final String mergedToPen = "123456789";
  private final String mergedStudentID = "7f000101-7151-1d84-8171-5187006c0003";
  private final String mergedFromPen = "987654321";

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void after() {
    sagaEventRepository.deleteAll();
    repository.deleteAll();
  }

  @Test
  public void testProcessStudentMerge_GivenInValidID_ShouldReturnStatusNotFound() throws Exception {
    this.mockMvc.perform(get(PEN_SERVICES + "/saga/" + UUID.randomUUID().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "PEN_SERVICES_READ_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(status().isNotFound());
  }

  // Merge
  @Test
  public void testProcessStudentMerge_GivenValidID_ShouldReturnStatusOK() throws Exception {
    var payload = placeholderStudentMergeCompleteSagaData();
    var sagaFromDB = sagaService.createSagaRecordInDB(PEN_SERVICES_STUDENT_MERGE_COMPLETE_SAGA.toString(), "Test", payload, UUID.fromString(studentID));

    this.mockMvc.perform(get(PEN_SERVICES + "/saga/" + sagaFromDB.getSagaId().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "PEN_SERVICES_READ_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testProcessStudentMerge_GivenInvalidPayload_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post(PEN_SERVICES + "/student-merge-complete-saga")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "STUDENT_MERGE_COMPLETE_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(placeholderInvalidSagaData()))
            .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testProcessStudentMerge_GivenValidPayload_ShouldReturnStatusOk() throws Exception {
    this.mockMvc.perform(post(PEN_SERVICES + "/student-merge-complete-saga")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "STUDENT_MERGE_COMPLETE_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(placeholderStudentMergeCompleteSagaData()))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$").exists());
  }

  @Test
  public void testProcessStudentMerge_GivenValidPayload_ShouldReturnStatusConflict() throws Exception {
    var payload = placeholderStudentMergeCompleteSagaData();
    sagaService.createSagaRecordInDB(PEN_SERVICES_STUDENT_MERGE_COMPLETE_SAGA.toString(), "Test", payload, UUID.fromString(studentID));
    this.mockMvc.perform(post(PEN_SERVICES + "/student-merge-complete-saga")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "STUDENT_MERGE_COMPLETE_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(payload))
            .andDo(print()).andExpect(status().isConflict());
  }

  // Demerge
  @Test
  public void testProcessStudentDemerge_GivenValidID_ShouldReturnStatusOK() throws Exception {
    var payload = placeholderStudentMergeCompleteSagaData();
    var sagaFromDB = sagaService.createSagaRecordInDB(PEN_SERVICES_STUDENT_DEMERGE_COMPLETE_SAGA.toString(), "Test", payload, UUID.fromString(studentID));

    this.mockMvc.perform(get(PEN_SERVICES + "/saga/" + sagaFromDB.getSagaId().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "PEN_SERVICES_READ_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testProcessStudentDemerge_GivenInvalidPayload_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post(PEN_SERVICES + "/student-demerge-complete-saga")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "STUDENT_DEMERGE_COMPLETE_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(placeholderInvalidSagaData()))
            .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testProcessStudentDemerge_GivenValidPayload_ShouldReturnStatusOk() throws Exception {
    this.mockMvc.perform(post(PEN_SERVICES + "/student-demerge-complete-saga")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "STUDENT_DEMERGE_COMPLETE_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(placeholderStudentDemergeCompleteSagaData()))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$").exists());
  }

  @Test
  public void testProcessStudentDemerge_GivenValidPayload_ShouldReturnStatusConflict() throws Exception {
    var payload = placeholderStudentDemergeCompleteSagaData();
    sagaService.createSagaRecordInDB(PEN_SERVICES_STUDENT_DEMERGE_COMPLETE_SAGA.toString(), "Test", payload, UUID.fromString(studentID));
    this.mockMvc.perform(post(PEN_SERVICES + "/student-demerge-complete-saga")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "STUDENT_DEMERGE_COMPLETE_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(payload))
            .andDo(print()).andExpect(status().isConflict());
  }


  protected String placeholderInvalidSagaData() {
    return " {\n" +
            "    \"createUser\": \"test\",\n" +
            "    \"updateUser\": \"test\",\n" +
            "  }";
  }

  protected String placeholderStudentMergeCompleteSagaData() {
    return " {\n" +
            "    \"createUser\": \"test\",\n" +
            "    \"updateUser\": \"test\",\n" +
            "    \"studentID\": \"" + studentID + "\",\n" +
            "    \"mergeStudentID\": \"" + mergedStudentID + "\",\n" +
            "    \"mergedToPen\": \"" + mergedToPen + "\",\n" +
            "    \"mergedFromPen\": \"" + mergedFromPen + "\",\n" +
            "    \"studentMergeDirectionCode\": \"From\",\n" +
            "    \"studentMergeSourceCode\": \"MI\",\n" +
            "    \"historyActivityCode\": \"MERGE\",\n" +
            "    \"legalFirstName\": \"Jack\"\n" +
            "  }";
  }

  protected String placeholderStudentDemergeCompleteSagaData() {
    return " {\n" +
            "    \"createUser\": \"test\",\n" +
            "    \"updateUser\": \"test\",\n" +
            "    \"studentID\": \"" + studentID + "\",\n" +
            "    \"mergedToStudentID\": \"" + studentID + "\",\n" +
            "    \"mergedFromStudentID\": \"" + mergedStudentID + "\",\n" +
            "    \"mergedToPen\": \"" + mergedToPen + "\",\n" +
            "    \"mergedFromPen\": \"" + mergedFromPen + "\",\n" +
            "    \"historyActivityCode\": \"DEMERGE\"\n" +
            "  }";
  }


}
