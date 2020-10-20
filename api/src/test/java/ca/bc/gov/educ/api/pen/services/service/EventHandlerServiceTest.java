package ca.bc.gov.educ.api.pen.services.service;

import ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.struct.v1.Event;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationIssue;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.pen.services.constants.EventType.GET_NEXT_PEN_NUMBER;
import static ca.bc.gov.educ.api.pen.services.constants.EventType.VALIDATE_STUDENT_DEMOGRAPHICS;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.PEN_SERVICES_API_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHandlerServiceTest {

  @Mock
  PenRequestStudentRecordValidationService validationService;

  @Mock
  MessagePublisher messagePublisher;

  @Mock
  PenService penService;

  private EventHandlerService eventHandlerServiceUnderTest;

  @Before
  public void setUp() {
    eventHandlerServiceUnderTest = new EventHandlerService(validationService, messagePublisher, penService);
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_NEXT_PEN_NUMBER__whenCallSuccess_shouldHaveEventOutcomeNEXT_PEN_NUMBER_RETRIEVED() throws JsonProcessingException {
    var transactionId = UUID.randomUUID().toString();
    var event = Event.builder().eventType(GET_NEXT_PEN_NUMBER).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(transactionId).build();

    when(penService.getNextPenNumber(transactionId)).thenReturn("120164446");
    eventHandlerServiceUnderTest.handleEvent(event);
    assertThat(event.getEventOutcome()).isEqualTo(NEXT_PEN_NUMBER_RETRIEVED);
    verify(messagePublisher).dispatchMessage(PEN_SERVICES_API_TOPIC.toString(), JsonUtil.getJsonStringFromObject(event).getBytes());
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_NEXT_PEN_NUMBER__whenCallFailed_shouldNotDispatchMessage() throws JsonProcessingException {
    var transactionId = UUID.randomUUID().toString();
    var event = Event.builder().eventType(GET_NEXT_PEN_NUMBER).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(transactionId).build();

    when(penService.getNextPenNumber(transactionId)).thenThrow(RuntimeException.class);
    eventHandlerServiceUnderTest.handleEvent(event);
    verifyNoMoreInteractions(messagePublisher);
  }

  @Test
  public void testHandleEvent_givenEventTypeVALIDATE_STUDENT_DEMOGRAPHICS__whenValidationResultIsEmpty_shouldHaveEventOutcomeNEXT_PEN_NUMBER_RETRIEVED() throws JsonProcessingException {
    var payload = createValidationPayload();
    var event = Event.builder().eventType(VALIDATE_STUDENT_DEMOGRAPHICS).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    when(validationService.validateStudentRecord(payload)).thenReturn(Collections.emptyList());
    eventHandlerServiceUnderTest.handleEvent(event);
    assertThat(event.getEventOutcome()).isEqualTo(VALIDATION_SUCCESS_NO_ERROR_WARNING);
    verify(messagePublisher).dispatchMessage(PEN_SERVICES_API_TOPIC.toString(), JsonUtil.getJsonStringFromObject(event).getBytes());
  }

  @Test
  public void testHandleEvent_givenEventTypeVALIDATE_STUDENT_DEMOGRAPHICS__whenValidationResultWithError_shouldHaveEventOutcomeVALIDATION_SUCCESS_WITH_ERROR() throws JsonProcessingException {
    var payload = createValidationPayload();
    var event = Event.builder().eventType(VALIDATE_STUDENT_DEMOGRAPHICS).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    var validationResult = List.of(PenRequestStudentValidationIssue.builder().penRequestBatchValidationIssueSeverityCode(PenRequestStudentValidationIssueSeverityCode.ERROR.toString()).build());
    when(validationService.validateStudentRecord(payload)).thenReturn(validationResult);
    eventHandlerServiceUnderTest.handleEvent(event);
    assertThat(event.getEventOutcome()).isEqualTo(VALIDATION_SUCCESS_WITH_ERROR);
    verify(messagePublisher).dispatchMessage(PEN_SERVICES_API_TOPIC.toString(), JsonUtil.getJsonStringFromObject(event).getBytes());
  }

  @Test
  public void testHandleEvent_givenEventTypeVALIDATE_STUDENT_DEMOGRAPHICS__whenValidationResultWithWarning_shouldHaveEventOutcomeVALIDATION_SUCCESS_WITH_ONLY_WARNING() throws JsonProcessingException {
    var payload = createValidationPayload();
    var event = Event.builder().eventType(VALIDATE_STUDENT_DEMOGRAPHICS).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    var validationResult = List.of(PenRequestStudentValidationIssue.builder().penRequestBatchValidationIssueSeverityCode(PenRequestStudentValidationIssueSeverityCode.WARNING.toString()).build());
    when(validationService.validateStudentRecord(payload)).thenReturn(validationResult);
    eventHandlerServiceUnderTest.handleEvent(event);
    assertThat(event.getEventOutcome()).isEqualTo(VALIDATION_SUCCESS_WITH_ONLY_WARNING);
    verify(messagePublisher).dispatchMessage(PEN_SERVICES_API_TOPIC.toString(), JsonUtil.getJsonStringFromObject(event).getBytes());
  }

  @Test
  public void testHandleEvent_givenEventTypeVALIDATE_STUDENT_DEMOGRAPHICS__whenPayloadError_shouldNotDispatchMessage() throws JsonProcessingException {
    var payload = "Error";
    var event = Event.builder().eventType(VALIDATE_STUDENT_DEMOGRAPHICS).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();

    eventHandlerServiceUnderTest.handleEvent(event);
    verifyNoMoreInteractions(messagePublisher);
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
