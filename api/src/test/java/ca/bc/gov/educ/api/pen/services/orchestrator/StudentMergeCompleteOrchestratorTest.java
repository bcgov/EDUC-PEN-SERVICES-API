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
@SpringBootTest(classes = {TestRedisConfiguration.class, PenServicesApiResourceApplication.class})
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
    var payload = placeholderStudentMergeCompleteSagaData();
    sagaData = getStudentMergeCompleteSagaDataFromJsonString(payload);
    sagaData.setMergedToPen("123456789");
    sagaData.setMergedFromPen("987654321");
    sagaData.setLocalID("20345678");
    sagaData.setGradeCode("01");
    sagaData.setLegalFirstName("Jack");
    sagaData.setLegalLastName("Saga");
    sagaData.setGenderCode("M");
    sagaData.setStatusCode("A");
    saga = sagaService.createSagaRecordInDB(PEN_SERVICES_STUDENT_MERGE_COMPLETE_SAGA.toString(), "Test",
            JsonUtil.getJsonStringFromObject(sagaData), UUID.fromString(studentID));
  }

  /**
   * After.
   */
  @After
  public void after() {
    sagaEventRepository.deleteAll();
    repository.deleteAll();
  }

  @Test
  public void testGetMergedToStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(EventType.INITIATED)
            .eventOutcome(EventOutcome.INITIATE_SUCCESS)
            .sagaId(saga.getSagaId())
            .studentID(studentID)
            .build();
    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(GET_STUDENT);
    var responsePen = newEvent.getEventPayload();
    assertThat(mergedToPen).isEqualTo(responsePen);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(GET_STUDENT.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
  }

  @Test
  public void testUpdateMergedToStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(UUID.fromString(studentID));
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var studentPayload = Student.builder().studentID(studentID).legalFirstName("Jack").localID("20345678").statusCode("A").build();
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(GET_STUDENT)
            .eventOutcome(EventOutcome.STUDENT_FOUND)
            .sagaId(saga.getSagaId())
            .eventPayload(JsonUtil.getJsonStringFromObject(studentPayload))
            .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(UPDATE_STUDENT);
    var student = JsonUtil.getJsonObjectFromString(StudentSagaData.class, newEvent.getEventPayload());
    assertThat(student.getStudentID()).isEqualTo(studentID);
    assertThat(student.getLegalFirstName()).isEqualTo("Jack");
    assertThat(student.getLocalID()).isEqualTo("20345678");
    assertThat(student.getStatusCode()).isEqualTo("A");

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(UPDATE_STUDENT.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.GET_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_FOUND.toString());
  }

  @Test
  public void testCreateMerge_givenEventAndSagaData_shouldPostEventToPenServicesApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(UUID.fromString(studentID));
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var studentPayload = Student.builder().studentID(studentID).legalFirstName("Jack").localID("20345678").statusCode("A").build();
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(UPDATE_STUDENT)
            .eventOutcome(EventOutcome.STUDENT_UPDATED)
            .sagaId(saga.getSagaId())
            .eventPayload(JsonUtil.getJsonStringFromObject(studentPayload))
            .build();
    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_SERVICES_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_MERGE);
    var studentMerge = new ObjectMapper().readValue(newEvent.getEventPayload(), StudentMerge.class);
    assertThat(studentMerge.getStudentID()).isEqualTo(studentID);
    assertThat(studentMerge.getMergeStudentID()).isEqualTo(mergeStudentID);
    assertThat(studentMerge.getStudentMergeDirectionCode()).isEqualTo(sagaData.getStudentMergeDirectionCode());

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(CREATE_MERGE.toString());
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID().toString()).isEqualTo(studentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID().toString()).isEqualTo(studentID);
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(UPDATE_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_UPDATED.toString());
  }

  @Test
  public void testGetMergedFromStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(UUID.fromString(mergeStudentID));
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var studentMergePayload = StudentMerge.builder().studentID(studentID).mergeStudentID(mergeStudentID).studentMergeDirectionCode("FROM").studentMergeSourceCode("MI").build();
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(CREATE_MERGE)
            .eventOutcome(EventOutcome.MERGE_CREATED)
            .sagaId(saga.getSagaId())
            .eventPayload(JsonUtil.getJsonStringFromObject(studentMergePayload))
            .build();
    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(GET_STUDENT);
    var responsePen = newEvent.getEventPayload();
    assertThat(mergedFromPen).isEqualTo(responsePen);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(GET_STUDENT.toString());
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID().toString()).isEqualTo(mergeStudentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID().toString()).isEqualTo(mergeStudentID);
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(CREATE_MERGE.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.MERGE_CREATED.toString());
  }

  @Test
  public void testUpdateMergedFromStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(UUID.fromString(mergeStudentID));
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var studentPayload = Student.builder().studentID(mergeStudentID).legalFirstName("Jack").localID("20345678").statusCode("A").build();
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(GET_STUDENT)
            .eventOutcome(EventOutcome.STUDENT_FOUND)
            .sagaId(saga.getSagaId())
            .eventPayload(JsonUtil.getJsonStringFromObject(studentPayload))
            .build();
    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(UPDATE_STUDENT);
    var student = JsonUtil.getJsonObjectFromString(StudentSagaData.class, newEvent.getEventPayload());
    assertThat(student.getStudentID()).isEqualTo(mergeStudentID);
    assertThat(student.getLegalFirstName()).isEqualTo("Jack");
    assertThat(student.getLocalID()).isEqualTo("20345678");
    assertThat(student.getStatusCode()).isEqualTo("M");

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(UPDATE_STUDENT.toString());
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID().toString()).isEqualTo(mergeStudentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID().toString()).isEqualTo(mergeStudentID);
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(GET_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_FOUND.toString());
  }

  @Test
  public void testReadAuditHistory_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(UUID.fromString(mergeStudentID));
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var studentPayload = Student.builder().studentID(studentID).legalFirstName("Jack").build();
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(UPDATE_STUDENT)
            .eventOutcome(EventOutcome.STUDENT_UPDATED)
            .sagaId(saga.getSagaId())
            .eventPayload(JsonUtil.getJsonStringFromObject(studentPayload))
            .build();
    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(GET_STUDENT_HISTORY);
    var mergedFromStudentID = newEvent.getEventPayload();
    assertThat(mergedFromStudentID).isEqualTo(mergeStudentID);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(GET_STUDENT_HISTORY.toString());
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID().toString()).isEqualTo(studentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID().toString()).isEqualTo(mergeStudentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID().toString()).isEqualTo(mergeStudentID);
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(UPDATE_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_UPDATED.toString());
  }

  @Test
  public void testCopyAuditHistory_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(UUID.fromString(mergeStudentID));
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var studentHistoryPayload = StudentHistory.builder().studentHistoryID(studentHistoryID).studentID(mergeStudentID).legalFirstName("Jackson").build();
    List<StudentHistory> readHistoryList = new ArrayList<>();
    readHistoryList.add(studentHistoryPayload);
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(GET_STUDENT_HISTORY)
            .eventOutcome(EventOutcome.STUDENT_HISTORY_FOUND)
            .sagaId(saga.getSagaId())
            .eventPayload(JsonUtil.getJsonStringFromObject(readHistoryList))
            .build();
    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_STUDENT_HISTORY);
    ObjectMapper objectMapper = new ObjectMapper();
    JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, StudentHistory.class);
    List<StudentHistory> copyHistoryList = objectMapper.readValue(newEvent.getEventPayload(), type);
    assertThat(copyHistoryList.size()).isEqualTo(1);
    assertThat(copyHistoryList.get(0).getStudentID()).isEqualTo(studentID);
    assertThat(copyHistoryList.get(0).getStudentHistoryID()).isNotEqualTo(studentHistoryID);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(CREATE_STUDENT_HISTORY.toString());
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID().toString()).isEqualTo(studentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID().toString()).isEqualTo(mergeStudentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID().toString()).isEqualTo(mergeStudentID);
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(GET_STUDENT_HISTORY.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_HISTORY_FOUND.toString());
  }

  @Test
  public void testGetPossibleMatches_givenEventAndSagaData_shouldPostEventToPenMatchApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(UUID.fromString(mergeStudentID));
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var studentHistoryPayload = StudentHistory.builder().studentHistoryID(studentHistoryID).studentID(studentID).legalFirstName("Jackson").build();
    List<StudentHistory> copiedHistoryList = new ArrayList<>();
    copiedHistoryList.add(studentHistoryPayload);
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(CREATE_STUDENT_HISTORY)
            .eventOutcome(EventOutcome.STUDENT_HISTORY_CREATED)
            .sagaId(saga.getSagaId())
            .eventPayload(JsonUtil.getJsonStringFromObject(copiedHistoryList))
            .build();
    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_MATCH_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(GET_POSSIBLE_MATCH);
    var possibleMatchStudentID = newEvent.getEventPayload();
    assertThat(possibleMatchStudentID).isEqualTo(mergeStudentID);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(GET_POSSIBLE_MATCH.toString());
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID().toString()).isEqualTo(studentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID().toString()).isEqualTo(mergeStudentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID().toString()).isEqualTo(mergeStudentID);
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(CREATE_STUDENT_HISTORY.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_HISTORY_CREATED.toString());
  }

  @Test
  public void testDeletePossibleMatches_givenEventAndSagaData_shouldPostEventToPenMatchApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(UUID.fromString(mergeStudentID));
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var match1 = PossibleMatch.builder().studentID(mergeStudentID).matchedStudentID(studentID).matchReasonCode(MatchReasonCodes.PENMATCH).build();
    var match2 = PossibleMatch.builder().studentID(studentID).matchedStudentID(mergeStudentID).matchReasonCode(MatchReasonCodes.PENMATCH).build();
    List<PossibleMatch> possibleMatches = new ArrayList<>();
    possibleMatches.add(match1);
    possibleMatches.add(match2);
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(GET_POSSIBLE_MATCH)
            .eventOutcome(EventOutcome.POSSIBLE_MATCH_FOUND)
            .sagaId(saga.getSagaId())
            .eventPayload(JsonUtil.getJsonStringFromObject(possibleMatches))
            .build();
    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_MATCH_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(DELETE_POSSIBLE_MATCH);
    List<PossibleMatch> payload = new ObjectMapper().readValue(newEvent.getEventPayload(), new TypeReference<>() {
    });
    assertThat(payload.size()).isEqualTo(1);
    payload.stream().forEach(m -> {
      assertThat(m.getStudentID().toString()).isIn(mergeStudentID, studentID);
      assertThat(m.getMatchedStudentID().toString()).isIn(mergeStudentID, studentID);
    });

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(DELETE_POSSIBLE_MATCH.toString());
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID().toString()).isEqualTo(studentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID().toString()).isEqualTo(mergeStudentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID().toString()).isEqualTo(mergeStudentID);
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(GET_POSSIBLE_MATCH.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.POSSIBLE_MATCH_FOUND.toString());
  }

  @Test
  public void testPossibleMatchesNotFound_givenEventAndSagaData_shouldMarkCompleted() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(UUID.fromString(mergeStudentID));
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }

    List<PossibleMatch> possibleMatches = new ArrayList<>();
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(GET_POSSIBLE_MATCH)
            .eventOutcome(EventOutcome.POSSIBLE_MATCH_NOT_FOUND)
            .sagaId(saga.getSagaId())
            .eventPayload(JsonUtil.getJsonStringFromObject(possibleMatches))
            .build();
    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_SERVICES_MERGE_STUDENTS_SAGA_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(MARK_SAGA_COMPLETE);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(COMPLETED.toString());
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID().toString()).isEqualTo(studentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergeStudentID().toString()).isEqualTo(mergeStudentID);
    assertThat(getStudentMergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID().toString()).isEqualTo(mergeStudentID);
    var sagaStates = sagaService.findAllSagaStates(saga);
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
            "    \"studentID\": \"" + studentID + "\",\n" +
            "    \"mergeStudentID\": \"" + mergeStudentID + "\",\n" +
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
  protected StudentMergeCompleteSagaData getStudentMergeCompleteSagaDataFromJsonString(String json) {
    try {
      return JsonUtil.getJsonObjectFromString(StudentMergeCompleteSagaData.class, json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
