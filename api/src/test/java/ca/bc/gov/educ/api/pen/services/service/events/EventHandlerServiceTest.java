package ca.bc.gov.educ.api.pen.services.service.events;

import ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeRepository;
import ca.bc.gov.educ.api.pen.services.service.PenRequestStudentRecordValidationService;
import ca.bc.gov.educ.api.pen.services.service.PenService;
import ca.bc.gov.educ.api.pen.services.struct.v1.Event;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.services.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.PEN_SERVICES_API_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventHandlerServiceTest {

  @Mock
  PenRequestStudentRecordValidationService validationService;

  @Mock
  PenService penService;

  @Mock
  StudentMergeRepository studentMergeRepository;

  private EventHandlerService eventHandlerServiceUnderTest;

  @Before
  public void setUp() {
    eventHandlerServiceUnderTest = new EventHandlerService(validationService, penService, studentMergeRepository);
  }

  /**
   * need to delete the records to make it working in unit tests assertion, else the records will keep growing and assertions will fail.
   */
  @After
  public void after() {
    studentMergeRepository.deleteAll();
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_NEXT_PEN_NUMBER__whenCallSuccess_shouldHaveEventOutcomeNEXT_PEN_NUMBER_RETRIEVED() throws JsonProcessingException {
    var transactionId = UUID.randomUUID().toString();
    var event = Event.builder().eventType(GET_NEXT_PEN_NUMBER).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(transactionId).build();

    when(penService.getNextPenNumber(transactionId)).thenReturn("120164446");
    var rawResponse = eventHandlerServiceUnderTest.handleGetNextPenNumberEvent(event);
    assertThat(rawResponse).hasSizeGreaterThan(0);
    var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse));
    assertThat(response.getEventOutcome()).isEqualTo(NEXT_PEN_NUMBER_RETRIEVED);
  }

  @Test(expected = RuntimeException.class)
  public void testHandleEvent_givenEventTypeGET_NEXT_PEN_NUMBER__whenCallFailed_shouldNotDispatchMessage() throws JsonProcessingException {
    var transactionId = UUID.randomUUID().toString();
    var event = Event.builder().eventType(GET_NEXT_PEN_NUMBER).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(transactionId).build();

    when(penService.getNextPenNumber(transactionId)).thenThrow(RuntimeException.class);
    eventHandlerServiceUnderTest.handleGetNextPenNumberEvent(event);
  }

  @Test
  public void testHandleEvent_givenEventTypeVALIDATE_STUDENT_DEMOGRAPHICS__whenValidationResultIsEmpty_shouldHaveEventOutcomeNEXT_PEN_NUMBER_RETRIEVED() throws JsonProcessingException {
    var payload = createValidationPayload();
    var event = Event.builder().eventType(VALIDATE_STUDENT_DEMOGRAPHICS).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    when(validationService.validateStudentRecord(payload)).thenReturn(Collections.emptyList());
    var rawResponse = eventHandlerServiceUnderTest.handleValidateStudentDemogDataEvent(event);
    assertThat(rawResponse).hasSizeGreaterThan(0);
    var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse));
    assertThat(response.getEventOutcome()).isEqualTo(VALIDATION_SUCCESS_NO_ERROR_WARNING);
  }

  @Test
  public void testHandleEvent_givenEventTypeVALIDATE_STUDENT_DEMOGRAPHICS__whenValidationResultWithError_shouldHaveEventOutcomeVALIDATION_SUCCESS_WITH_ERROR() throws JsonProcessingException {
    var payload = createValidationPayload();
    var event = Event.builder().eventType(VALIDATE_STUDENT_DEMOGRAPHICS).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    var validationResult = List.of(PenRequestStudentValidationIssue.builder().penRequestBatchValidationIssueSeverityCode(PenRequestStudentValidationIssueSeverityCode.ERROR.toString()).build());
    when(validationService.validateStudentRecord(payload)).thenReturn(validationResult);
    var rawResponse = eventHandlerServiceUnderTest.handleValidateStudentDemogDataEvent(event);
    assertThat(rawResponse).hasSizeGreaterThan(0);
    var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse));
    assertThat(response.getEventOutcome()).isEqualTo(VALIDATION_SUCCESS_WITH_ERROR);
  }

  @Test
  public void testHandleEvent_givenEventTypeVALIDATE_STUDENT_DEMOGRAPHICS__whenValidationResultWithWarning_shouldHaveEventOutcomeVALIDATION_SUCCESS_WITH_ONLY_WARNING() throws JsonProcessingException {
    var payload = createValidationPayload();
    var event = Event.builder().eventType(VALIDATE_STUDENT_DEMOGRAPHICS).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    var validationResult = List.of(PenRequestStudentValidationIssue.builder().penRequestBatchValidationIssueSeverityCode(PenRequestStudentValidationIssueSeverityCode.WARNING.toString()).build());
    when(validationService.validateStudentRecord(payload)).thenReturn(validationResult);
    var rawResponse = eventHandlerServiceUnderTest.handleValidateStudentDemogDataEvent(event);
    assertThat(rawResponse).hasSizeGreaterThan(0);
    var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse));
    assertThat(response.getEventOutcome()).isEqualTo(VALIDATION_SUCCESS_WITH_ONLY_WARNING);
  }

  @Test(expected = JsonProcessingException.class)
  public void testHandleEvent_givenEventTypeVALIDATE_STUDENT_DEMOGRAPHICS__whenPayloadError_shouldNotDispatchMessage() throws JsonProcessingException {
    var payload = "Error";
    var event = Event.builder().eventType(VALIDATE_STUDENT_DEMOGRAPHICS).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    eventHandlerServiceUnderTest.handleValidateStudentDemogDataEvent(event);
  }

  @Test
  public void testHandleCreateMergeEvent_givenStudentMergePayload_whenSuccessfullyProcessed_shouldHaveEventOutcomeMERGE_CREATED() throws JsonProcessingException {
    var payload = createStudentMergePayload();
    var event = Event.builder().eventType(CREATE_MERGE).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    var rawResponse = eventHandlerServiceUnderTest.handleCreateMergeEvent(event);
    assertThat(rawResponse).hasSizeGreaterThan(0);
    var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse));
    assertThat(response.getEventOutcome()).isEqualTo(MERGE_CREATED);

    ObjectMapper objectMapper = new ObjectMapper();
    JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, StudentMerge.class);
    List<StudentMerge> addedStudentMerges = objectMapper.readValue(response.getEventPayload(), type);
    assertThat(addedStudentMerges.size()).isEqualTo(2);
  }

  @Test
  public void testHandleDeleteMergeEvent_givenStudentMergePayload_whenSuccessfullyProcessed_shouldHaveEventOutcomeMERGE_DELETED() throws JsonProcessingException {
    var payload = createStudentMergePayload();
    var event = Event.builder().eventType(DELETE_MERGE).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    var rawResponse = eventHandlerServiceUnderTest.handleDeleteMergeEvent(event);
    assertThat(rawResponse).hasSizeGreaterThan(0);
    var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse));
    assertThat(response.getEventOutcome()).isEqualTo(MERGE_DELETED);

    ObjectMapper objectMapper = new ObjectMapper();
    JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
    List<String> deletedIDs = objectMapper.readValue(response.getEventPayload(), type);
    assertThat(deletedIDs.size()).isEqualTo(0);
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

  private StudentMerge createStudentMergePayload() {
    return StudentMerge.builder()
            .studentID("7f000101-7151-1d84-8171-5187006c0001")
            .mergeStudentID("7f000101-7151-1d84-8171-5187006c0003")
            .studentMergeDirectionCode("FROM")
            .studentMergeSourceCode("MI")
            .build();
  }

}
