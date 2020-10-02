package ca.bc.gov.educ.api.pen.validation.controller.v1;

import ca.bc.gov.educ.api.pen.validation.rest.RestUtils;
import ca.bc.gov.educ.api.pen.validation.struct.v1.GenderCode;
import ca.bc.gov.educ.api.pen.validation.struct.v1.GradeCode;
import ca.bc.gov.educ.api.pen.validation.struct.v1.PenRequestStudentValidationPayload;
import ca.bc.gov.educ.api.pen.validation.support.WithMockOAuth2Scope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@SuppressWarnings({"java:S112", "java:S100", "java:S1192"})
public class PenValidationAPIControllerTest {

  private List<GenderCode> genderCodes;
  private List<GradeCode> gradeCodes;
  @Autowired
  PenValidationAPIController controller;
  /**
   * The Mock mvc.
   */
  private MockMvc mockMvc;
  @Autowired
  RestUtils restUtils;


  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
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
  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenValidPayload_shouldReturnStatusOkWithBlankArray() throws Exception {
    String payload = validationPayloadAsJSONString(createValidationPayload());
    log.info(payload);
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(payload))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidSubmittedPEN_shouldReturnStatusOkWithValidationResults() throws Exception {
    var payload = createValidationPayload();
    payload.setSubmittedPen("120164446");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenBlankLegalSurname_shouldReturnStatusOkWithValidationResults() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalLastName("");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidLegalSurnameWhichMatchedTheBlockedName_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalLastName("AVAILABLE");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidLegalSurnameWhichMatchedTheBlockedName_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalLastName("BLANK");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenOneCharLegalSurname_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalLastName("B");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenBlankLegalFirstName_shouldReturnStatusOkWithIssueTypeCodeBlankField() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenApostropheInLegalFirstName_shouldReturnStatusOkWithIssueTypeCodeApostrophe() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidLegalFirstNameWhichMatchedTheBlockedName_shouldReturnStatusOkWithIssueTypeCodeBlockedName() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("AVAILABLE");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidLegalFirstName_shouldReturnStatusOkWithValidationResultsAsWarningIssueTypeCodeBLANKINNAME() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("DS TAM");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenOneCharLegalFirstName_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("B");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidCharInLegalFirstName_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("OK^");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidCharInLegalFirstName2_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("OK_");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidCharInLegalFirstName3_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("OK'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidCharInLegalFirstName4_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("*OK");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidCharInLegalFirstName5_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("_O'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidCharInLegalFirstName6_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("-OK'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidCharInLegalFirstName7_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("\"OK'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidCharInLegalFirstName8_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("XXOK'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidCharInLegalFirstName9_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("ZZOK'");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenSameLegalMidAsLegalFirst_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalFirstName("MARCO");
    payload.setLegalMiddleNames("MARCO");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenSameLegalMidAsLegalLast_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setLegalLastName("MARCO");
    payload.setLegalMiddleNames("MARCO");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidGenderCode_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setGenderCode("O");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenBlankGenderCode_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setGenderCode("");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidPostalCode_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setPostalCode("123456");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenDOBInInvalidFormat_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setDob("2020-02-02"); // it expects to be YYYYMMDD
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenBlankDOB_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setDob(""); // it expects to be YYYYMMDD
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenDOBEarlierTo1900_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setDob("18990101"); // it expects to be YYYYMMDD
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenDOBLaterThanCurrentDate_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setDob("29990101"); // it expects to be YYYYMMDD
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInvalidGradeCode_shouldReturnStatusOkWithValidationResultsAsError() throws Exception {
    var payload = createValidationPayload();
    payload.setGradeCode("XX");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInappropriateAgeForGradeCode_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setGradeCode("01");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

  }

  @Test
  @WithMockOAuth2Scope(scope = "VALIDATE_STUDENT_DEMOGRAPHICS")
  public void testValidateStudentData_givenInappropriateAgeForGradeCode2_shouldReturnStatusOkWithValidationResultsAsWarning() throws Exception {
    var payload = createValidationPayload();
    payload.setDob(LocalDate.now().toString().replaceAll("-", ""));
    payload.setGradeCode("01");
    when(restUtils.getGenderCodes()).thenReturn(genderCodes);
    when(restUtils.getGradeCodes()).thenReturn(gradeCodes);
    mockMvc
        .perform(post("/api/v1/pen-validation/student-request")
            .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(validationPayloadAsJSONString(payload)))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

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