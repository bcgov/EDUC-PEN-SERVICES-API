package ca.bc.gov.educ.api.pen.services.service.events;

import ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode;
import ca.bc.gov.educ.api.pen.services.constants.StudentMergeDirectionCodes;
import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMergeMapper;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.pen.services.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.services.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.PEN_SERVICES_API_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EventHandlerServiceTest {

  @MockBean
  PenRequestStudentRecordValidationService validationService;
  @MockBean
  PenService penService;
  @Autowired
  StudentMergeRepository studentMergeRepository;
  @Autowired
  private EventHandlerService eventHandlerServiceUnderTest;

  private static final StudentMergeMapper mapper = StudentMergeMapper.mapper;


  /**
   * need to delete the records to make it working in unit tests assertion, else the records will keep growing and assertions will fail.
   */
  @After
  public void after() {
    this.studentMergeRepository.deleteAll();
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_NEXT_PEN_NUMBER__whenCallSuccess_shouldHaveEventOutcomeNEXT_PEN_NUMBER_RETRIEVED() throws JsonProcessingException {
    final var transactionId = UUID.randomUUID().toString();
    final var event = Event.builder().eventType(GET_NEXT_PEN_NUMBER).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(transactionId).build();

    when(this.penService.getNextPenNumber(transactionId)).thenReturn("120164446");
    final var rawResponse = this.eventHandlerServiceUnderTest.handleGetNextPenNumberEvent(event);
    assertThat(rawResponse).hasSizeGreaterThan(0);
    final var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse));
    assertThat(response.getEventOutcome()).isEqualTo(NEXT_PEN_NUMBER_RETRIEVED);
  }

  @Test(expected = RuntimeException.class)
  public void testHandleEvent_givenEventTypeGET_NEXT_PEN_NUMBER__whenCallFailed_shouldNotDispatchMessage() throws JsonProcessingException {
    final var transactionId = UUID.randomUUID().toString();
    final var event = Event.builder().eventType(GET_NEXT_PEN_NUMBER).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(transactionId).build();

    when(this.penService.getNextPenNumber(transactionId)).thenThrow(RuntimeException.class);
    this.eventHandlerServiceUnderTest.handleGetNextPenNumberEvent(event);
  }

  @Test
  public void testHandleEvent_givenEventTypeVALIDATE_STUDENT_DEMOGRAPHICS__whenValidationResultIsEmpty_shouldHaveEventOutcomeNEXT_PEN_NUMBER_RETRIEVED() throws JsonProcessingException {
    final var payload = this.createValidationPayload();
    final var event = Event.builder().eventType(VALIDATE_STUDENT_DEMOGRAPHICS).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    when(this.validationService.validateStudentRecord(payload)).thenReturn(Collections.emptyList());
    final var rawResponse = this.eventHandlerServiceUnderTest.handleValidateStudentDemogDataEvent(event);
    assertThat(rawResponse).hasSizeGreaterThan(0);
    final var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse));
    assertThat(response.getEventOutcome()).isEqualTo(VALIDATION_SUCCESS_NO_ERROR_WARNING);
  }

  @Test
  public void testHandleEvent_givenEventTypeVALIDATE_STUDENT_DEMOGRAPHICS__whenValidationResultWithError_shouldHaveEventOutcomeVALIDATION_SUCCESS_WITH_ERROR() throws JsonProcessingException {
    final var payload = this.createValidationPayload();
    final var event = Event.builder().eventType(VALIDATE_STUDENT_DEMOGRAPHICS).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    final var validationResult = List.of(PenRequestStudentValidationIssue.builder().penRequestBatchValidationIssueSeverityCode(PenRequestStudentValidationIssueSeverityCode.ERROR.toString()).build());
    when(this.validationService.validateStudentRecord(payload)).thenReturn(validationResult);
    final var rawResponse = this.eventHandlerServiceUnderTest.handleValidateStudentDemogDataEvent(event);
    assertThat(rawResponse).hasSizeGreaterThan(0);
    final var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse));
    assertThat(response.getEventOutcome()).isEqualTo(VALIDATION_SUCCESS_WITH_ERROR);
  }

  @Test
  public void testHandleEvent_givenEventTypeVALIDATE_STUDENT_DEMOGRAPHICS__whenValidationResultWithWarning_shouldHaveEventOutcomeVALIDATION_SUCCESS_WITH_ONLY_WARNING() throws JsonProcessingException {
    final var payload = this.createValidationPayload();
    final var event = Event.builder().eventType(VALIDATE_STUDENT_DEMOGRAPHICS).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    final var validationResult = List.of(PenRequestStudentValidationIssue.builder().penRequestBatchValidationIssueSeverityCode(PenRequestStudentValidationIssueSeverityCode.WARNING.toString()).build());
    when(this.validationService.validateStudentRecord(payload)).thenReturn(validationResult);
    final var rawResponse = this.eventHandlerServiceUnderTest.handleValidateStudentDemogDataEvent(event);
    assertThat(rawResponse).hasSizeGreaterThan(0);
    final var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse));
    assertThat(response.getEventOutcome()).isEqualTo(VALIDATION_SUCCESS_WITH_ONLY_WARNING);
  }

  @Test(expected = JsonProcessingException.class)
  public void testHandleEvent_givenEventTypeVALIDATE_STUDENT_DEMOGRAPHICS__whenPayloadError_shouldNotDispatchMessage() throws JsonProcessingException {
    final var payload = "Error";
    final var event = Event.builder().eventType(VALIDATE_STUDENT_DEMOGRAPHICS).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    this.eventHandlerServiceUnderTest.handleValidateStudentDemogDataEvent(event);
  }

  @Test
  public void testHandleCreateMergeEvent_givenStudentMergePayload_whenSuccessfullyProcessed_shouldHaveEventOutcomeMERGE_CREATED() throws JsonProcessingException {
    final var payload = this.createStudentMergePayload();
    final var event = Event.builder().eventType(CREATE_MERGE).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    final var rawResponse = this.eventHandlerServiceUnderTest.handleCreateMergeEvent(event);
    assertThat(rawResponse.getLeft()).hasSizeGreaterThan(0);
    final var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse.getLeft()));
    assertThat(response.getEventOutcome()).isEqualTo(MERGE_CREATED);

    final ObjectMapper objectMapper = new ObjectMapper();
    final JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, StudentMerge.class);
    final List<StudentMerge> addedStudentMerges = objectMapper.readValue(response.getEventPayload(), type);
    assertThat(addedStudentMerges).hasSize(2);
  }

  @Test
  public void testHandleDeleteMergeEvent_givenStudentMergePayload_whenSuccessfullyProcessed_shouldHaveEventOutcomeMERGE_DELETED() throws JsonProcessingException {
    final var payload = this.createStudentMergePayload();
    final var event = Event.builder().eventType(DELETE_MERGE).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    final var rawResponse = this.eventHandlerServiceUnderTest.handleDeleteMergeEvent(event);
    assertThat(rawResponse.getLeft()).hasSizeGreaterThan(0);
    final var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse.getLeft()));
    assertThat(response.getEventOutcome()).isEqualTo(MERGE_DELETED);

    final ObjectMapper objectMapper = new ObjectMapper();
    final JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
    final List<String> deletedIDs = objectMapper.readValue(response.getEventPayload(), type);
    assertThat(deletedIDs).isEmpty();
  }

  @Test
  public void testHandleGetMergeEvent_givenStudentIdPayload_whenSuccessfullyProcessed_shouldHaveEventOutcomeMERGE_FOUND() throws JsonProcessingException {
    final var studentMerge = this.createStudentMergePayload();
    studentMerge.setStudentMergeDirectionCode(StudentMergeDirectionCodes.FROM.getCode());
    final var studentMergeEntity = this.studentMergeRepository.save(mapper.toModel(studentMerge));
    final var event = Event.builder().eventType(GET_MERGES).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(studentMergeEntity.getStudentID().toString()).build();

    final var rawResponse = this.eventHandlerServiceUnderTest.handleGetMergeEvent(event);
    assertThat(rawResponse).hasSizeGreaterThan(0);
    final var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse));
    assertThat(response.getEventOutcome()).isEqualTo(MERGE_FOUND);

    final ObjectMapper objectMapper = new ObjectMapper();
    final JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, StudentMerge.class);
    final List<StudentMerge> addedStudentMerges = objectMapper.readValue(response.getEventPayload(), type);
    assertThat(addedStudentMerges).hasSize(1);
  }

  @Test
  public void testHandleGetMergeInDateRangeEvent_givenDateRangePayload_whenSuccessfullyProcessed_shouldHaveEventOutcomeMERGE_FOUND() throws JsonProcessingException {
    final var studentMerge = this.createStudentMergePayload();
    studentMerge.setStudentMergeDirectionCode(StudentMergeDirectionCodes.TO.getCode());
    var studentMergeEntity = mapper.toModel(studentMerge);
    studentMergeEntity.setCreateDate(LocalDateTime.now().minusDays(1));
    this.studentMergeRepository.save(studentMergeEntity);

    final String eventPayload = "createDateStart=" + LocalDateTime.now().getYear() + "-01-01T00:00:00&createDateEnd=" + LocalDateTime.now().getYear() + "-12-31T23:59:59";
    final var event = Event.builder()
            .eventType(GET_MERGES_IN_DATE_RANGE)
            .replyTo(PEN_SERVICES_API_TOPIC.toString())
            .eventPayload(eventPayload)
            .build();

    final var rawResponse = this.eventHandlerServiceUnderTest.handleGetMergeInDateRangeEvent(event);

    assertThat(rawResponse).hasSizeGreaterThan(0);
    final var response = JsonUtil.getJsonObjectFromString(Event.class, new String(rawResponse));
    assertThat(response.getEventOutcome()).isEqualTo(MERGE_FOUND);


    final ObjectMapper objectMapper = new ObjectMapper();
    final JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, StudentMerge.class);

    final List<StudentMerge> addedStudentMerges = objectMapper.readValue(response.getEventPayload(), type);
    assertThat(addedStudentMerges).hasSize(1);
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
