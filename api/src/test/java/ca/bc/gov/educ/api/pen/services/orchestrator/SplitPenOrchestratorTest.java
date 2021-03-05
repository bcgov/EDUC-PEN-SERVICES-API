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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.pen.services.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.PEN_SERVICES_SPLIT_PEN_SAGA;
import static ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum.COMPLETED;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {TestRedisConfiguration.class, PenServicesApiResourceApplication.class})
@AutoConfigureMockMvc
public class SplitPenOrchestratorTest {
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
  private SplitPenOrchestrator orchestrator;

  /**
   * The Saga.
   */
  private Saga saga;
  /**
   * The Saga data.
   */
  private SplitPenSagaData sagaData;

  /**
   * The Event captor.
   */
  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  String sagaPayload;

  String studentID = UUID.randomUUID().toString();

  /**
   * Sets up.
   */
  @Before
  public void setUp() throws JsonProcessingException {
    MockitoAnnotations.openMocks(this);
    var payload = placeholderSplitPenSagaData();
    sagaData = getSplitPenSagaDataFromJsonString(payload);
    sagaData.setStudentID(studentID);
    sagaData.setPen("123456789");
    sagaData.setLocalID("20345678");
    sagaData.setGradeCode("01");
    sagaData.setLegalFirstName("Jack");
    sagaData.setLegalLastName("Saga");
    sagaData.setGenderCode("M");
    sagaData.setStatusCode("A");
    sagaData.setCreateUser("Create User");
    sagaData.setUpdateUser("Update User");
    sagaPayload = JsonUtil.getJsonStringFromObject(sagaData);
    saga = sagaService.createSagaRecordInDB(PEN_SERVICES_SPLIT_PEN_SAGA.toString(), "Test",
      sagaPayload, UUID.fromString(studentID));
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
  public void testUpdateOriginalStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
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
    assertThat(newEvent.getEventType()).isEqualTo(UPDATE_STUDENT);
    var student = JsonUtil.getJsonObjectFromString(StudentSagaData.class, newEvent.getEventPayload());
    assertThat(student.getStudentID()).isEqualTo(studentID);
    assertThat(student.getLegalFirstName()).isEqualTo("Jack");
    assertThat(student.getLocalID()).isEqualTo("20345678");
    assertThat(student.getStatusCode()).isEqualTo("A");
    assertThat(student.getHistoryActivityCode()).isEqualTo(StudentHistoryActivityCodes.SPLITNEW.getCode());

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(UPDATE_STUDENT.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
  }

  @Test
  public void testGetNextPenNumber_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var studentPayload = Student.builder().studentID(studentID).legalFirstName("Jack").localID("20345678").statusCode("A").build();
    var event = Event.builder()
      .eventType(EventType.UPDATE_STUDENT)
      .eventOutcome(EventOutcome.STUDENT_UPDATED)
      .sagaId(saga.getSagaId())
      .studentID(studentID)
      .eventPayload(JsonUtil.getJsonStringFromObject(studentPayload))
      .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_SERVICES_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(GET_NEXT_PEN_NUMBER);
    assertThat(newEvent.getEventPayload()).isEqualTo(saga.getSagaId().toString());

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(GET_NEXT_PEN_NUMBER.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.UPDATE_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_UPDATED.toString());
  }

  @Test
  public void testCreateStudent_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var pen = "987654321";
    var event = Event.builder()
      .eventType(EventType.GET_NEXT_PEN_NUMBER)
      .eventOutcome(EventOutcome.NEXT_PEN_NUMBER_RETRIEVED)
      .sagaId(saga.getSagaId())
      .studentID(studentID)
      .eventPayload(pen)
      .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(STUDENT_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_STUDENT);
    var student = JsonUtil.getJsonObjectFromString(StudentSagaData.class, newEvent.getEventPayload());
    assertThat(student.getStudentID()).isNull();
    assertThat(student.getLegalFirstName()).isEqualTo("Jack");
    assertThat(student.getPen()).isEqualTo(pen);
    assertThat(student.getDemogCode()).isEqualTo(StudentDemogCodes.ACCEPTED.getCode());
    assertThat(student.getStatusCode()).isEqualTo(StudentStatusCodes.ACTIVE.getCode());
    assertThat(student.getHistoryActivityCode()).isEqualTo(StudentHistoryActivityCodes.SPLITNEW.getCode());
    assertThat(student.getCreateUser()).isEqualTo(sagaData.getCreateUser());
    assertThat(student.getUpdateUser()).isEqualTo(sagaData.getUpdateUser());

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_STUDENT.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.GET_NEXT_PEN_NUMBER.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.NEXT_PEN_NUMBER_RETRIEVED.toString());
  }

  @Test
  public void testAddPossibleMatchesToStudent_givenEventAndSagaData_andSTUDENT_CREATED_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var newStudentID = UUID.randomUUID().toString();
    var studentPayload = Student.builder().studentID(newStudentID).legalFirstName("Jack").statusCode("A").build();
    var event = Event.builder()
      .eventType(EventType.CREATE_STUDENT)
      .eventOutcome(EventOutcome.STUDENT_CREATED)
      .sagaId(saga.getSagaId())
      .studentID(studentID)
      .eventPayload(JsonUtil.getJsonStringFromObject(studentPayload))
      .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_MATCH_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(ADD_POSSIBLE_MATCH);
    List<PossibleMatch> payload = new ObjectMapper().readValue(newEvent.getEventPayload(), new TypeReference<>() {
    });
    assertThat(payload.size()).isEqualTo(1);
    payload.forEach(m -> {
      assertThat(m.getStudentID()).isEqualTo(newStudentID);
      assertThat(m.getMatchedStudentID()).isEqualTo(studentID);
      assertThat(m.getMatchReasonCode()).isEqualTo(MatchReasonCodes.SPLIT);
      assertThat(m.getCreateUser()).isEqualTo(sagaData.getCreateUser());
    });

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(ADD_POSSIBLE_MATCH.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.CREATE_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_CREATED.toString());
  }

  @Test
  public void testAddPossibleMatchesToStudent_givenEventAndSagaData_andSTUDENT_ALREADY_EXIST_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var newStudentID = UUID.randomUUID().toString();
    var event = Event.builder()
      .eventType(EventType.CREATE_STUDENT)
      .eventOutcome(EventOutcome.STUDENT_ALREADY_EXIST)
      .sagaId(saga.getSagaId())
      .studentID(studentID)
      .eventPayload(newStudentID)
      .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_MATCH_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(ADD_POSSIBLE_MATCH);
    List<PossibleMatch> payload = new ObjectMapper().readValue(newEvent.getEventPayload(), new TypeReference<>() {
    });
    assertThat(payload.size()).isEqualTo(1);
    payload.forEach(m -> {
      assertThat(m.getStudentID()).isEqualTo(newStudentID);
      assertThat(m.getMatchedStudentID()).isEqualTo(studentID);
      assertThat(m.getMatchReasonCode()).isEqualTo(MatchReasonCodes.SPLIT);
      assertThat(m.getCreateUser()).isEqualTo(sagaData.getCreateUser());
    });

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(ADD_POSSIBLE_MATCH.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.CREATE_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.STUDENT_ALREADY_EXIST.toString());
  }

  @Test
  public void testMarkSplitPenSagaComplete_givenEventAndSagaData_shouldPostEventToStudentApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var newStudentID = UUID.randomUUID().toString();
    var possibleMatch = PossibleMatch.builder()
      .possibleMatchID(UUID.randomUUID().toString())
      .studentID(newStudentID)
      .matchedStudentID(UUID.randomUUID().toString())
      .build();
    var possibleMatchesPayload = List.of(possibleMatch);

    var event = Event.builder()
      .eventType(EventType.ADD_POSSIBLE_MATCH)
      .eventOutcome(EventOutcome.POSSIBLE_MATCH_ADDED)
      .sagaId(saga.getSagaId())
      .studentID(studentID)
      .eventPayload(JsonUtil.getJsonStringFromObject(possibleMatchesPayload))
      .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(PEN_SERVICES_SPLIT_PEN_SAGA_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(MARK_SAGA_COMPLETE);
    assertThat(newEvent.getEventPayload()).isEqualTo(newStudentID);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(COMPLETED.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.ADD_POSSIBLE_MATCH.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.POSSIBLE_MATCH_ADDED.toString());
  }

  /**
   * Dummy split pen saga data json string.
   *
   * @return the string
   */
  protected String placeholderSplitPenSagaData() {
    return " {\n" +
      "    \"createUser\": \"test\",\n" +
      "    \"updateUser\": \"test\",\n" +
      "    \"studentID\": \"" + studentID + "\",\n" +
      "    \"historyActivityCode\": \"MERGE\",\n" +
      "    \"legalFirstName\": \"Jack\",\n" +
      "    \"newStudent\": {\n" +
      "       \"studentID\": \"" + studentID + "\",\n" +
      "       \"legalFirstName\": \"Jack\"\n" +
      "    }\n" +
      "  }";
  }

  /**
   * Gets split pen saga data from json string.
   *
   * @param json the json
   * @return the split pen saga data from json string
   */
  protected SplitPenSagaData getSplitPenSagaDataFromJsonString(String json) {
    try {
      return JsonUtil.getJsonObjectFromString(SplitPenSagaData.class, json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
