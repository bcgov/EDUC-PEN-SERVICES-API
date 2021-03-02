package ca.bc.gov.educ.api.pen.services.orchestrator;

import ca.bc.gov.educ.api.pen.services.PenServicesApiResourceApplication;
import ca.bc.gov.educ.api.pen.services.constants.*;
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
import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.PEN_SERVICES_STUDENT_DEMERGE_COMPLETE_SAGA;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {TestRedisConfiguration.class, PenServicesApiResourceApplication.class})
@AutoConfigureMockMvc
public class StudentDemergeCompleteOrchestratorTest {
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
  private StudentDemergeCompleteOrchestrator orchestrator;

  /**
   * The Saga.
   */
  private Saga saga;
  /**
   * The Saga data.
   */
  private StudentDemergeCompleteSagaData sagaData;

  /**
   * The Event captor.
   */
  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  String mergedFromStudentID = UUID.randomUUID().toString();
  String mergedFromPen = "987654321";
  String mergedToStudentID = UUID.randomUUID().toString();
  String mergedToPen = "123456789";
  String studentHistoryID = UUID.randomUUID().toString();

  /**
   * Sets up.
   */
  @Before
  public void setUp() throws JsonProcessingException {
    MockitoAnnotations.openMocks(this);
    var payload = placeholderStudentDemergeCompleteSagaData();
    sagaData = getStudentDemergeCompleteSagaDataFromJsonString(payload);
    sagaData.setMergedFromPen(mergedFromPen);
    sagaData.setMergedToPen(mergedToPen);
    sagaData.setLocalID("20345678");
    sagaData.setGradeCode("01");
    sagaData.setLegalFirstName("Jack");
    sagaData.setLegalLastName("Saga");
    sagaData.setGenderCode("M");
    sagaData.setStatusCode("M");
    saga = sagaService.createSagaRecordInDB(PEN_SERVICES_STUDENT_DEMERGE_COMPLETE_SAGA.toString(), "Test",
            JsonUtil.getJsonStringFromObject(sagaData), UUID.fromString(mergedFromStudentID));
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
  public void testGetStudentHistory_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(EventType.INITIATED)
            .eventOutcome(EventOutcome.INITIATE_SUCCESS)
            .sagaId(saga.getSagaId())
            .studentID(mergedFromStudentID)
            .build();
    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(GET_STUDENT_HISTORY);
    var response = newEvent.getEventPayload();
    assertThat(mergedFromStudentID).isEqualTo(response);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(GET_STUDENT_HISTORY.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
  }

  @Test
  public void testGetMergedFromStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentDemergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(mergedFromStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var studentHistoryPayload = StudentHistory.builder().studentHistoryID(studentHistoryID).studentID(mergedFromStudentID).legalFirstName("Jackson").build();
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
    assertThat(newEvent.getEventType()).isEqualTo(GET_STUDENT);
    var responsePen = newEvent.getEventPayload();
    assertThat(mergedFromPen).isEqualTo(responsePen);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(GET_STUDENT.toString());
    var payload = JsonUtil.getJsonObjectFromString(StudentDemergeCompleteSagaData.class, sagaFromDB.get().getPayload());
    assertThat(payload.getLegalFirstName()).isEqualTo("Jackson");
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.GET_STUDENT_HISTORY.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_HISTORY_FOUND.toString());
  }

  @Test
  public void testUpdateMergedFromStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentDemergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setLegalFirstName("Jackson");
      payload.setRequestStudentID(mergedFromStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var studentPayload = Student.builder().studentID(mergedFromStudentID).legalFirstName("Jack").localID("20345678").statusCode("M").build();
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
    assertThat(student.getStudentID()).isEqualTo(mergedFromStudentID);
    assertThat(student.getLegalFirstName()).isEqualTo("Jackson");
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
  public void testDeleteMerge_givenEventAndSagaData_shouldPostEventToPenServicesApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentDemergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(mergedFromStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var studentPayload = Student.builder().studentID(mergedFromStudentID).legalFirstName("Jackson").localID("20345678").statusCode("A").build();
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
    assertThat(newEvent.getEventType()).isEqualTo(DELETE_MERGE);
    var studentMerge = new ObjectMapper().readValue(newEvent.getEventPayload(), StudentMerge.class);
    assertThat(studentMerge.getStudentID()).isEqualTo(mergedToStudentID);
    assertThat(studentMerge.getMergeStudentID()).isEqualTo(mergedFromStudentID);
    assertThat(studentMerge.getStudentMergeDirectionCode()).isEqualTo(StudentMergeDirectionCodes.FROM.getCode());
    assertThat(studentMerge.getStudentMergeSourceCode()).isEqualTo(StudentMergeSourceCodes.MI.getCode());

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(DELETE_MERGE.toString());
    assertThat(getStudentDemergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getStudentID()).isEqualTo(mergedFromStudentID);
    assertThat(getStudentDemergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID()).isEqualTo(mergedFromStudentID);
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(UPDATE_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_UPDATED.toString());
  }

  @Test
  public void testGetMergedToStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentDemergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(mergedToStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var studentMergePayload = StudentMerge.builder().studentID(mergedToStudentID).mergeStudentID(mergedFromStudentID).studentMergeDirectionCode("FROM").studentMergeSourceCode("MI").build();
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(DELETE_MERGE)
            .eventOutcome(EventOutcome.MERGE_DELETED)
            .sagaId(saga.getSagaId())
            .eventPayload(JsonUtil.getJsonStringFromObject(studentMergePayload))
            .build();
    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(GET_STUDENT);
    var responsePen = newEvent.getEventPayload();
    assertThat(mergedToPen).isEqualTo(responsePen);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(GET_STUDENT.toString());
    assertThat(getStudentDemergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergedToStudentID()).isEqualTo(mergedToStudentID);
    assertThat(getStudentDemergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergedFromStudentID()).isEqualTo(mergedFromStudentID);
    assertThat(getStudentDemergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID()).isEqualTo(mergedToStudentID);
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(DELETE_MERGE.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.MERGE_DELETED.toString());
  }

  @Test
  public void testUpdateMergedToStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentDemergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(mergedToStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var studentPayload = Student.builder().studentID(mergedToStudentID).legalFirstName("Jack").localID("20345678").statusCode("A").build();
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
    assertThat(student.getStudentID()).isEqualTo(mergedToStudentID);
    assertThat(student.getLegalFirstName()).isEqualTo("Jack");
    assertThat(student.getLocalID()).isEqualTo("20345678");
    assertThat(student.getStatusCode()).isEqualTo("A");

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(UPDATE_STUDENT.toString());
    assertThat(getStudentDemergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergedToStudentID()).isEqualTo(mergedToStudentID);
    assertThat(getStudentDemergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergedFromStudentID()).isEqualTo(mergedFromStudentID);
    assertThat(getStudentDemergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID()).isEqualTo(mergedToStudentID);
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(GET_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_FOUND.toString());
  }

  @Test
  public void testCreatePossibleMatches_givenEventAndSagaData_shouldPostEventToPenMatchApi() throws IOException, InterruptedException, TimeoutException {
    var sagaFromDBtoUpdateOptional = sagaService.findSagaById(saga.getSagaId());
    if (sagaFromDBtoUpdateOptional.isPresent()) {
      var sagaFromDBtoUpdate = sagaFromDBtoUpdateOptional.get();
      var payload = JsonUtil.getJsonObjectFromString(StudentDemergeCompleteSagaData.class, sagaFromDBtoUpdate.getPayload());
      payload.setRequestStudentID(mergedToStudentID);
      sagaFromDBtoUpdate.setPayload(JsonUtil.getJsonStringFromObject(payload));
      sagaService.updateAttachedEntityDuringSagaProcess(sagaFromDBtoUpdate);
      saga = sagaService.findSagaById(saga.getSagaId()).orElseThrow();
    }
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(UPDATE_STUDENT)
            .eventOutcome(EventOutcome.STUDENT_UPDATED)
            .sagaId(saga.getSagaId())
            .build();
    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_MATCH_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(ADD_POSSIBLE_MATCH);
    List<PossibleMatch> payload = new ObjectMapper().readValue(newEvent.getEventPayload(), new TypeReference<>() {
    });
    assertThat(payload.size()).isEqualTo(1);
    assertThat(payload.get(0).getStudentID()).isEqualTo(mergedToStudentID);
    assertThat(payload.get(0).getMatchedStudentID()).isEqualTo(mergedFromStudentID);
    assertThat(payload.get(0).getMatchReasonCode()).isEqualTo(MatchReasonCodes.DEMERGE);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    var currentSaga = sagaFromDB.get();
    assertThat(currentSaga.getSagaState()).isEqualTo(ADD_POSSIBLE_MATCH.toString());
    assertThat(getStudentDemergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergedToStudentID()).isEqualTo(mergedToStudentID);
    assertThat(getStudentDemergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getMergedFromStudentID()).isEqualTo(mergedFromStudentID);
    assertThat(getStudentDemergeCompleteSagaDataFromJsonString(currentSaga.getPayload()).getRequestStudentID()).isEqualTo(mergedToStudentID);
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(UPDATE_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_UPDATED.toString());
  }

  /**
   * Dummy student demerge complete saga data json string.
   *
   * @return the string
   */
  protected String placeholderStudentDemergeCompleteSagaData() {
    return " {\n" +
            "    \"createUser\": \"test\",\n" +
            "    \"updateUser\": \"test\",\n" +
            "    \"studentID\": \"" + mergedFromStudentID + "\",\n" +
            "    \"mergedFromStudentID\": \"" + mergedFromStudentID + "\",\n" +
            "    \"mergedToStudentID\": \"" + mergedToStudentID + "\",\n" +
            "    \"historyActivityCode\": \"DEMERGE\"\n" +
            "  }";
  }

  /**
   * Gets student merge complete saga data from json string.
   *
   * @param json the json
   * @return the student merge complete saga data from json string
   */
  protected StudentDemergeCompleteSagaData getStudentDemergeCompleteSagaDataFromJsonString(String json) {
    try {
      return JsonUtil.getJsonObjectFromString(StudentDemergeCompleteSagaData.class, json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
