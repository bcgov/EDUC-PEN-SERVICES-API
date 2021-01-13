package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.PenServicesApiResourceApplication;
import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationFieldCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationIssueSeverityCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationIssueTypeCodeEntity;
import ca.bc.gov.educ.api.pen.services.repository.PenRequestBatchValidationFieldCodeRepository;
import ca.bc.gov.educ.api.pen.services.repository.PenRequestBatchValidationIssueSeverityCodeRepository;
import ca.bc.gov.educ.api.pen.services.repository.PenRequestBatchValidationIssueTypeCodeRepository;
import ca.bc.gov.educ.api.pen.services.rest.RestUtils;
import ca.bc.gov.educ.api.pen.services.struct.v1.GenderCode;
import ca.bc.gov.educ.api.pen.services.struct.v1.GradeCode;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import ca.bc.gov.educ.api.pen.services.support.TestRedisConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.v1.URL.MERGES;
import static ca.bc.gov.educ.api.pen.services.constants.v1.URL.PEN_SERVICES;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The type Pen validation api controller test.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestRedisConfiguration.class, PenServicesApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Slf4j
@SuppressWarnings({"java:S112", "java:S100", "java:S1192","java:S2699"})
public class PenServicesAPIControllerTest {

  private List<GenderCode> genderCodes;
  private List<GradeCode> gradeCodes;
  private static final String STUDENT_REQUEST_URL = "/api/v1/pen-services/validation/student-request";
  /**
   * The Controller.
   */
  @Autowired
  PenServicesAPIController controller;
  /**
   * The Mock mvc.
   */
  @Autowired
  private MockMvc mockMvc;
  /**
   * The Rest utils.
   */
  @Autowired
  RestUtils restUtils;

  @Autowired
  PenRequestBatchValidationFieldCodeRepository penRequestBatchValidationFieldCodeRepo;

  @Autowired
  PenRequestBatchValidationIssueSeverityCodeRepository penRequestBatchValidationIssueSeverityCodeRepo;

  @Autowired
  PenRequestBatchValidationIssueTypeCodeRepository penRequestBatchValidationIssueTypeCodeRepo;

  /**
   * Sets up.
   *
   * @throws IOException the io exception
   */
  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    if (genderCodes == null) {
      final File file = new File(
          Objects.requireNonNull(getClass().getClassLoader().getResource("gender_codes.json")).getFile()
      );
      genderCodes = new ObjectMapper().readValue(file, new TypeReference<>() {
      });
    }
    if (gradeCodes == null) {
      final File file = new File(
          Objects.requireNonNull(getClass().getClassLoader().getResource("grade_codes.json")).getFile()
      );
      gradeCodes = new ObjectMapper().readValue(file, new TypeReference<>() {
      });
    }
    penRequestBatchValidationFieldCodeRepo.save(createPenRequestBatchValidationFieldCodeData());
    penRequestBatchValidationIssueSeverityCodeRepo.save(createPenRequestBatchValidationSeverityCodeData());
    penRequestBatchValidationIssueTypeCodeRepo.save(createPenRequestBatchValidationTypeCodeData());

  }

  /**
   * need to delete the records to make it working in unit tests assertion, else the records will keep growing and assertions will fail.
   */
  @After
  public void after() {
    penRequestBatchValidationFieldCodeRepo.deleteAll();
    penRequestBatchValidationIssueSeverityCodeRepo.deleteAll();
    penRequestBatchValidationIssueTypeCodeRepo.deleteAll();
  }

  /**
   * Test validate student data given valid payload should return status ok with blank array.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenValidPayload_shouldReturnStatusOkWithBlankArray() throws Exception {
    String payload = validationPayloadAsJSONString(createValidationPayload());
    log.info(payload);
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(payload))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));

  }

  /**
   * Test validate student data given invalid submitted pen should return status ok with validation results.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidSubmittedPEN_shouldReturnStatusOkWithValidationResults() throws Exception {
    var payload = createValidationPayload();
    payload.setSubmittedPen("120164446");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given blank legal surname should return status ok with validation results.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenBlankLegalSurname_shouldReturnStatusOkWithValidationResults() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalLastName("");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given invalid legal surname which matched the blocked name should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidLegalSurnameWhichMatchedTheBlockedName_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalLastName("AVAILABLE");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given invalid legal surname which matched the blocked name should return status ok with validation results as warning.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidLegalSurnameWhichMatchedTheBlockedName_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalLastName("BLANK");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given one char legal surname should return status ok with validation results as warning.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenOneCharLegalSurname_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalLastName("B");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given blank legal first name should return status ok with issue type code blank field.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenBlankLegalFirstName_shouldReturnStatusOkWithIssueTypeCodeBlankField() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given apostrophe in legal first name should return status ok with issue type code apostrophe.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenApostropheInLegalFirstName_shouldReturnStatusOkWithIssueTypeCodeApostrophe() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given invalid legal first name which matched the blocked name should return status ok with issue type code blocked name.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidLegalFirstNameWhichMatchedTheBlockedName_shouldReturnStatusOkWithIssueTypeCodeBlockedName() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("AVAILABLE");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given invalid legal first name should return status ok with validation results as warning issue type code blankinname.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidLegalFirstName_shouldReturnStatusOkWithValidationResultsAsWarningIssueTypeCodeBLANKINNAME() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("DS TAM");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given one char legal first name should return status ok with validation results as warning.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenOneCharLegalFirstName_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("B");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given invalid char in legal first name should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidCharInLegalFirstName_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("OK^");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given invalid char in legal first name 2 should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidCharInLegalFirstName2_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("OK_");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given invalid char in legal first name 3 should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidCharInLegalFirstName3_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("OK'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given invalid char in legal first name 4 should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidCharInLegalFirstName4_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("*OK");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given invalid char in legal first name 5 should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidCharInLegalFirstName5_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("_O'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

  }

  /**
   * Test validate student data given invalid char in legal first name 6 should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidCharInLegalFirstName6_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("-OK'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

  }

  /**
   * Test validate student data given invalid char in legal first name 7 should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidCharInLegalFirstName7_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("\"OK'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

  }

  /**
   * Test validate student data given invalid char in legal first name 8 should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidCharInLegalFirstName8_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("XXOK'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

  }

  /**
   * Test validate student data given invalid char in legal first name 9 should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidCharInLegalFirstName9_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("ZZOK'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

  }

  /**
   * Test validate student data given same legal mid as legal first should return status ok with validation results as warning.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenSameLegalMidAsLegalFirst_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("MARCO");
    payload.setLegalMiddleNames("MARCO");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given same legal mid as legal last should return status ok with validation results as warning.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenSameLegalMidAsLegalLast_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalLastName("MARCO");
    payload.setLegalMiddleNames("MARCO");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given invalid gender code should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidGenderCode_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setGenderCode("O");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given blank gender code should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenBlankGenderCode_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setGenderCode("");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given invalid postal code should return status ok with validation results as warning.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidPostalCode_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setPostalCode("123456");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given dob in invalid format should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenDOBInInvalidFormat_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setDob("2020-02-02"); // it expects to be YYYYMMDD
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given blank dob should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenBlankDOB_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setDob(""); // it expects to be YYYYMMDD
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given dob earlier to 1900 should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenDOBEarlierTo1900_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setDob("18990101"); // it expects to be YYYYMMDD
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given dob later than current date should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenDOBLaterThanCurrentDate_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setDob("29990101"); // it expects to be YYYYMMDD
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given invalid grade code should return status ok with validation results as error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInvalidGradeCode_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setGradeCode("XX");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
            .content(validationPayloadAsJSONString(payload))).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
  }

  /**
   * Test validate student data given inappropriate age for grade code should return status ok with validation results as warning.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInappropriateAgeForGradeCode_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setGradeCode("01");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  /**
   * Test validate student data given inappropriate age for grade code 2 should return status ok with validation results as warning.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateStudentData_givenInappropriateAgeForGradeCode2_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setDob(LocalDate.now().toString().replaceAll("-", ""));
    payload.setGradeCode("01");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post(STUDENT_REQUEST_URL)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "VALIDATE_STUDENT_DEMOGRAPHICS")))
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  public void testGetValidationIssueFieldCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get("/api/v1/pen-services/validation/issue-field-code")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_VALIDATION_CODES")))).andDo(print()).andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].code").value("LOCALID"));
  }

  @Test
  public void testGetValidationIssueSeverityCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get("/api/v1/pen-services/validation/issue-severity-code")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_VALIDATION_CODES")))).andDo(print()).andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].code").value("ERROR"));
  }

  @Test
  public void testGetValidationIssueTypeCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get("/api/v1/pen-services/validation/issue-type-code")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_VALIDATION_CODES")))).andDo(print()).andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].code").value("1CHARNAME"));
  }

  private PenRequestBatchValidationFieldCodeEntity createPenRequestBatchValidationFieldCodeData() {
    return PenRequestBatchValidationFieldCodeEntity.builder().code("LOCALID").description("Local identifier used by the school for the student")
            .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("School Student ID").createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }

  private PenRequestBatchValidationIssueSeverityCodeEntity createPenRequestBatchValidationSeverityCodeData() {
    return PenRequestBatchValidationIssueSeverityCodeEntity.builder().code("ERROR").description("The condition is a hard or fatal error")
            .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("Error").createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }

  private PenRequestBatchValidationIssueTypeCodeEntity createPenRequestBatchValidationTypeCodeData() {
    return PenRequestBatchValidationIssueTypeCodeEntity.builder().code("1CHARNAME").description("Field consists of just one character")
            .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).legacyLabel("C6, C8, C11, C13, C77, C78").label("Field is just one character").createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }

  private String validationPayloadAsJSONString(PenRequestStudentValidationPayload payload) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(payload);
  }

  private PenRequestStudentValidationPayload createValidationPayload() {
    return PenRequestStudentValidationPayload.builder()
        .isInteractive(false)
        .dob("20000101")
        .genderCode("M")
        .gradeCode("SU")
        .legalFirstName("OM")
        .legalMiddleNames("MARCO")
        .legalLastName("COX")
        .usualFirstName("MARCO")
        .usualMiddleNames("MINGWEI")
        .usualLastName("JOHN")
        .postalCode("V8R4N4")
        .transactionID(UUID.randomUUID().toString())
        .submittedPen("120164447")
        .submissionNumber("TSWWEB01")
        .build();
  }
}
