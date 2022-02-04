package ca.bc.gov.educ.api.pen.services.orchestrator;

import ca.bc.gov.educ.api.pen.services.PenServicesApiResourceApplication;
import ca.bc.gov.educ.api.pen.services.constants.EventOutcome;
import ca.bc.gov.educ.api.pen.services.constants.EventType;
import ca.bc.gov.educ.api.pen.services.constants.MatchReasonCodes;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.repository.SagaEventRepository;
import ca.bc.gov.educ.api.pen.services.repository.SagaRepository;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.Student;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
import ca.bc.gov.educ.api.pen.services.support.TestRedisConfiguration;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.pen.services.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.PEN_SERVICES_STUDENT_MERGE_COMPLETE_SAGA;
import static ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum.COMPLETED;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {PenServicesApiResourceApplication.class})
@AutoConfigureMockMvc
public class StudentMergeCompleteOrchestratorTest {
  /**
   * The Repository.
   */
  @Autowired
  SagaRepository repository;
  /**
   * The Saga event repository.
   */
  @Autowired
  SagaEventRepository sagaEventRepository;
  /**
   * The Saga service.
   */
  @Autowired
  private SagaService sagaService;

  /**
   * The Message publisher.
   */
  @Autowired
  private MessagePublisher messagePublisher;

  @Autowired
  private StudentMergeCompleteOrchestrator orchestrator;

  /**
   * The Saga.
   */
  private Saga saga;
  /**
   * The Saga data.
   */
  private StudentMergeCompleteSagaData sagaData;

  /**
   * The Event captor.
   */
  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  @Captor
  ArgumentCaptor<byte[]> requestEventCaptor;

  String studentID = UUID.randomUUID().toString();
  String mergedToPen = "123456789";
  String mergeStudentID = UUID.randomUUID().toString();
  String mergedFromPen = "987654321";
  String studentHistoryID = UUID.randomUUID().toString();

  /**
   * Sets up.
   */
  @Before
  public void setUp() throws JsonProcessingException {
    MockitoAnnotations.openMocks(this);
    final var payload = this.placeholderStudentMergeCompleteSagaData();
    this.sagaData = this.getStudentMergeCompleteSagaDataFromJsonString(payload);
    this.sagaData.setMergedToPen("123456789");
    this.sagaData.setMergedFromPen("987654321");
    this.sagaData.setLocalID("20345678");
    this.sagaData.setGradeCode("01");
    this.sagaData.setLegalFirstName("Jack");
    this.sagaData.setLegalLastName("Saga");
    this.sagaData.setGenderCode("M");
    this.sagaData.setStatusCode("A");
    this.saga = this.sagaService.createSagaRecordInDB(PEN_SERVICES_STUDENT_MERGE_COMPLETE_SAGA.toString(), "Test",
        JsonUtil.getJsonStringFromObject(this.sagaData), UUID.fromString(this.studentID));
  }

  /**
   * After.
   */
  @After
  public void after() {
    this.sagaEventRepository.deleteAll();
    this.repository.deleteAll();
  }

  @Test
  public void testGetMergedToStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
        .eventType(EventType.INITIATED)
        .eventOutcome(EventOutcome.INITIATE_SUCCESS)
        .sagaId(this.saga.getSagaId())
        .studentID(this.studentID)
        .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(GET_STUDENT);
    final var responsePen = newEvent.getEventPayload();
    assertThat(this.mergedToPen).isEqualTo(responsePen);

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(GET_STUDENT.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
  }

  @Test
  public void testUpdateMergedToStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    final var sagaFromDBtoUpdateOptional = this.sagaService.findSagaById(this.saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      final var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      final var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(this.studentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      this.sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      this.saga = this.sagaService.findSagaById(this.saga.getSagaId()).orElseThrow();
    }
    final var studentPayload = Student.builder().studentID(this.studentID).legalFirstName("Jack").localID("20345678").statusCode("A").build();
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
        .eventType(GET_STUDENT)
        .eventOutcome(EventOutcome.STUDENT_FOUND)
        .sagaId(this.saga.getSagaId())
        .eventPayload(JsonUtil.getJsonStringFromObject(studentPayload))
        .build();
    when(this.messagePublisher.requestMessage(eq(STUDENT_API_TOPIC.toString()), any())).thenReturn(Optional.of(JsonUtil.getJsonStringFromObject(studentPayload)));
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atMost(1)).requestMessage(eq(STUDENT_API_TOPIC.toString()), this.requestEventCaptor.capture());
    verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), this.eventCaptor.capture());

    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(UPDATE_STUDENT);
    final var student = JsonUtil.getJsonObjectFromString(StudentSagaData.class, newEvent.getEventPayload());
    assertThat(student.getStudentID()).isEqualTo(this.studentID);
    assertThat(student.getLegalFirstName()).isEqualTo("Jack");
    assertThat(student.getLocalID()).isEqualTo("20345678");
    assertThat(student.getStatusCode()).isEqualTo("A");

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(UPDATE_STUDENT.toString());
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.GET_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_FOUND.toString());
  }

  @Test
  public void testCreateMerge_givenEventAndSagaData_shouldPostEventToPenServicesApi() throws IOException, InterruptedException, TimeoutException {
    final var sagaFromDBtoUpdateOptional = this.sagaService.findSagaById(this.saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      final var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      final var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(this.studentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      this.sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      this.saga = this.sagaService.findSagaById(this.saga.getSagaId()).orElseThrow();
    }
    final var studentPayload = Student.builder().studentID(this.studentID).legalFirstName("Jack").localID("20345678").statusCode("A").build();
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
        .eventType(UPDATE_STUDENT)
        .eventOutcome(EventOutcome.STUDENT_UPDATED)
        .sagaId(this.saga.getSagaId())
        .eventPayload(JsonUtil.getJsonStringFromObject(studentPayload))
        .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_SERVICES_API_TOPIC.toString()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_MERGE);
    final var studentMerge = new ObjectMapper().readValue(newEvent.getEventPayload(), StudentMerge.class);
    assertThat(studentMerge.getStudentID()).isEqualTo(this.studentID);
    assertThat(studentMerge.getMergeStudentID()).isEqualTo(this.mergeStudentID);
    assertThat(studentMerge.getStudentMergeDirectionCode()).isEqualTo(this.sagaData.getStudentMergeDirectionCode());

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    final var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(CREATE_MERGE.toString());
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID()).isEqualTo(this.studentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID()).isEqualTo(this.studentID);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(UPDATE_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_UPDATED.toString());
  }

  @Test
  public void testGetMergedFromStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    final var sagaFromDBtoUpdateOptional = this.sagaService.findSagaById(this.saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      final var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      final var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(this.mergeStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      this.sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      this.saga = this.sagaService.findSagaById(this.saga.getSagaId()).orElseThrow();
    }
    final var studentHistoryPayload = StudentHistory.builder().studentHistoryID(this.studentHistoryID).studentID(this.studentID).legalFirstName("Jackson").build();
    final List<StudentHistory> copiedHistoryList = new ArrayList<>();
    copiedHistoryList.add(studentHistoryPayload);
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
        .eventType(CREATE_STUDENT_HISTORY)
        .eventOutcome(EventOutcome.STUDENT_HISTORY_CREATED)
        .sagaId(this.saga.getSagaId())
        .eventPayload(JsonUtil.getJsonStringFromObject(copiedHistoryList))
        .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(GET_STUDENT);
    final var responsePen = newEvent.getEventPayload();
    assertThat(this.mergedFromPen).isEqualTo(responsePen);

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    final var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(GET_STUDENT.toString());
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID()).isEqualTo(this.mergeStudentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID()).isEqualTo(this.mergeStudentID);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(CREATE_STUDENT_HISTORY.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_HISTORY_CREATED.toString());
  }

  @Test
  public void testUpdateMergedFromStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    final var sagaFromDBtoUpdateOptional = this.sagaService.findSagaById(this.saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      final var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      final var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(this.mergeStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      this.sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      this.saga = this.sagaService.findSagaById(this.saga.getSagaId()).orElseThrow();
    }
    final var studentPayload = Student.builder().studentID(this.mergeStudentID).legalFirstName("Jack").localID("20345678").statusCode("A").build();
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
        .eventType(GET_STUDENT)
        .eventOutcome(EventOutcome.STUDENT_FOUND)
        .sagaId(this.saga.getSagaId())
        .eventPayload(JsonUtil.getJsonStringFromObject(studentPayload))
        .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(UPDATE_STUDENT);
    final var student = JsonUtil.getJsonObjectFromString(StudentSagaData.class, newEvent.getEventPayload());
    assertThat(student.getStudentID()).isEqualTo(this.mergeStudentID);
    assertThat(student.getLegalFirstName()).isEqualTo("Jack");
    assertThat(student.getLocalID()).isEqualTo("20345678");
    assertThat(student.getStatusCode()).isEqualTo("M");

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    final var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(UPDATE_STUDENT.toString());
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID()).isEqualTo(this.mergeStudentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID()).isEqualTo(this.mergeStudentID);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(GET_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_FOUND.toString());
  }

  @Test
  public void testReadAuditHistory_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    final var sagaFromDBtoUpdateOptional = this.sagaService.findSagaById(this.saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      final var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      final var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(this.mergeStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      this.sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      this.saga = this.sagaService.findSagaById(this.saga.getSagaId()).orElseThrow();
    }
    final var studentMergePayload = StudentMerge.builder().studentID(this.studentID).mergeStudentID(this.mergeStudentID).studentMergeDirectionCode("FROM").studentMergeSourceCode("MI").build();
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
        .eventType(CREATE_MERGE)
        .eventOutcome(EventOutcome.MERGE_CREATED)
        .sagaId(this.saga.getSagaId())
        .eventPayload(JsonUtil.getJsonStringFromObject(studentMergePayload))
        .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(GET_STUDENT_HISTORY);
    final var mergedFromStudentID = newEvent.getEventPayload();
    assertThat(mergedFromStudentID).isEqualTo(this.mergeStudentID);

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    final var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(GET_STUDENT_HISTORY.toString());
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID()).isEqualTo(this.studentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID()).isEqualTo(this.mergeStudentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID()).isEqualTo(this.mergeStudentID);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(CREATE_MERGE.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.MERGE_CREATED.toString());
  }

  @Test
  public void testCopyAuditHistory_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    final var sagaFromDBtoUpdateOptional = this.sagaService.findSagaById(this.saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      final var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      final var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(this.mergeStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      this.sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      this.saga = this.sagaService.findSagaById(this.saga.getSagaId()).orElseThrow();
    }
    final var studentHistoryPayload = StudentHistory.builder().studentHistoryID(this.studentHistoryID).studentID(this.mergeStudentID).legalFirstName("Jackson").build();
    final List<StudentHistory> readHistoryList = new ArrayList<>();
    readHistoryList.add(studentHistoryPayload);
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
        .eventType(GET_STUDENT_HISTORY)
        .eventOutcome(EventOutcome.STUDENT_HISTORY_FOUND)
        .sagaId(this.saga.getSagaId())
        .eventPayload(JsonUtil.getJsonStringFromObject(readHistoryList))
        .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_STUDENT_HISTORY);
    final ObjectMapper objectMapper = new ObjectMapper();
    final JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, StudentHistory.class);
    final List<StudentHistory> copyHistoryList = objectMapper.readValue(newEvent.getEventPayload(), type);
    assertThat(copyHistoryList.size()).isEqualTo(1);
    assertThat(copyHistoryList.get(0).getStudentID()).isEqualTo(this.studentID);
    assertThat(copyHistoryList.get(0).getStudentHistoryID()).isNotEqualTo(this.studentHistoryID);

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    final var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(CREATE_STUDENT_HISTORY.toString());
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID()).isEqualTo(this.studentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID()).isEqualTo(this.mergeStudentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID()).isEqualTo(this.mergeStudentID);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(GET_STUDENT_HISTORY.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_HISTORY_FOUND.toString());
  }

  @Test
  public void testGetPossibleMatches_givenEventAndSagaData_shouldPostEventToPenMatchApi() throws IOException, InterruptedException, TimeoutException {
    final var sagaFromDBtoUpdateOptional = this.sagaService.findSagaById(this.saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      final var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      final var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(this.mergeStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      this.sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      this.saga = this.sagaService.findSagaById(this.saga.getSagaId()).orElseThrow();
    }
    final var studentPayload = Student.builder().studentID(this.studentID).legalFirstName("Jack").build();
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
        .eventType(UPDATE_STUDENT)
        .eventOutcome(EventOutcome.STUDENT_UPDATED)
        .sagaId(this.saga.getSagaId())
        .eventPayload(JsonUtil.getJsonStringFromObject(studentPayload))
        .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_MATCH_API_TOPIC.toString()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(GET_POSSIBLE_MATCH);
    final var possibleMatchStudentID = newEvent.getEventPayload();
    assertThat(possibleMatchStudentID).isEqualTo(this.studentID);

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    final var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(GET_POSSIBLE_MATCH.toString());
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID()).isEqualTo(this.studentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID()).isEqualTo(this.mergeStudentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID()).isEqualTo(this.mergeStudentID);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(UPDATE_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_UPDATED.toString());
  }

  @Test
  public void testDeletePossibleMatches_givenEventAndSagaData_shouldPostEventToPenMatchApi() throws IOException, InterruptedException, TimeoutException {
    final var sagaFromDBtoUpdateOptional = this.sagaService.findSagaById(this.saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      final var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      final var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(this.mergeStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      this.sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      this.saga = this.sagaService.findSagaById(this.saga.getSagaId()).orElseThrow();
    }
    final var match1 = PossibleMatch.builder().studentID(this.mergeStudentID).matchedStudentID(this.studentID).matchReasonCode(MatchReasonCodes.PENMATCH).build();
    final var match2 = PossibleMatch.builder().studentID(this.studentID).matchedStudentID(this.mergeStudentID).matchReasonCode(MatchReasonCodes.PENMATCH).build();
    final List<PossibleMatch> possibleMatches = new ArrayList<>();
    possibleMatches.add(match1);
    possibleMatches.add(match2);
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
        .eventType(GET_POSSIBLE_MATCH)
        .eventOutcome(EventOutcome.POSSIBLE_MATCH_FOUND)
        .sagaId(this.saga.getSagaId())
        .eventPayload(JsonUtil.getJsonStringFromObject(possibleMatches))
        .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_MATCH_API_TOPIC.toString()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(DELETE_POSSIBLE_MATCH);
    final List<PossibleMatch> payload = new ObjectMapper().readValue(newEvent.getEventPayload(), new TypeReference<>() {
    });
    assertThat(payload.size()).isEqualTo(1);
    payload.forEach(m -> {
      assertThat(m.getStudentID()).isIn(this.mergeStudentID, this.studentID);
      assertThat(m.getMatchedStudentID()).isIn(this.mergeStudentID, this.studentID);
    });

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    final var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(DELETE_POSSIBLE_MATCH.toString());
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID()).isEqualTo(this.studentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID()).isEqualTo(this.mergeStudentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID()).isEqualTo(this.mergeStudentID);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(GET_POSSIBLE_MATCH.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.POSSIBLE_MATCH_FOUND.toString());
  }

  @Test
  public void testPossibleMatchesNotFound_givenEventAndSagaData_shouldMarkCompleted() throws IOException, InterruptedException, TimeoutException {
    final var sagaFromDBtoUpdateOptional = this.sagaService.findSagaById(this.saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      final var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      final var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(this.mergeStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      this.sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      this.saga = this.sagaService.findSagaById(this.saga.getSagaId()).orElseThrow();
    }

    final List<PossibleMatch> possibleMatches = new ArrayList<>();
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
        .eventType(GET_POSSIBLE_MATCH)
        .eventOutcome(EventOutcome.POSSIBLE_MATCH_NOT_FOUND)
        .sagaId(this.saga.getSagaId())
        .eventPayload(JsonUtil.getJsonStringFromObject(possibleMatches))
        .build();
    this.orchestrator.handleEvent(event);
    verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_SERVICES_MERGE_STUDENTS_SAGA_TOPIC.toString()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(MARK_SAGA_COMPLETE);

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    final var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(COMPLETED.toString());
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID()).isEqualTo(this.studentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID()).isEqualTo(this.mergeStudentID);
    assertThat(this.getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID()).isEqualTo(this.mergeStudentID);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(GET_POSSIBLE_MATCH.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.POSSIBLE_MATCH_NOT_FOUND.toString());
  }

  /**
   * Dummy student merge complete saga data json string.
   *
   * @return the string
   */
  protected String placeholderStudentMergeCompleteSagaData() {
    return " {\n" +
        "    \"createUser\": \"test\",\n" +
        "    \"updateUser\": \"test\",\n" +
        "    \"studentID\": \"" + this.studentID + "\",\n" +
        "    \"mergeStudentID\": \"" + this.mergeStudentID + "\",\n" +
        "    \"studentMergeDirectionCode\": \"FROM\",\n" +
        "    \"studentMergeSourceCode\": \"MI\",\n" +
        "    \"historyActivityCode\": \"MERGE\"\n" +
        "  }";
  }

  /**
   * Gets student merge complete saga data from json string.
   *
   * @param json the json
   * @return the student merge complete saga data from json string
   */
  protected StudentMergeCompleteSagaData getStudentMergeCompleteSagaDataFromJsonString(final String json) {
    try {
      return JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, json);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

}
