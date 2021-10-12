package ca.bc.gov.educ.api.pen.services.orchestrator;

import ca.bc.gov.educ.api.pen.services.PenServicesApiResourceApplication;
import ca.bc.gov.educ.api.pen.services.constants.*;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.repository.SagaEventRepository;
import ca.bc.gov.educ.api.pen.services.repository.SagaRepository;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
import ca.bc.gov.educ.api.pen.services.support.TestRedisConfiguration;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.pen.services.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.PEN_SERVICES_MOVE_SLD_SAGA;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {TestRedisConfiguration.class, PenServicesApiResourceApplication.class})
@AutoConfigureMockMvc
public class MoveSldOrchestratorTest {
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
  private MoveSldOrchestrator orchestrator;

  /**
   * The Saga.
   */
  private Saga saga;
  /**
   * The Saga data.
   */
  private MoveSldSagaData sagaData;

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
    sagaPayload = placeholderMoveSldSagaData();
    sagaData = getMoveSldSagaDataFromJsonString(sagaPayload);
    saga = sagaService.createSagaRecordInDB(PEN_SERVICES_MOVE_SLD_SAGA.toString(), "Test",
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
  public void testUpdateSldStudent_givenEventAndSagaData_shouldPostEventToSldApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
      .eventType(EventType.INITIATED)
      .eventOutcome(EventOutcome.INITIATE_SUCCESS)
      .sagaId(saga.getSagaId())
      .studentID(studentID)
      .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(SLD_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(UPDATE_SLD_STUDENT);
    var sldUpdateSingleStudentEvent = JsonUtil.getJsonObjectFromString(SldUpdateSingleStudentEvent.class, newEvent.getEventPayload());
    assertThat(sldUpdateSingleStudentEvent.getDistNo()).isEqualTo(sagaData.getDistNo());
    assertThat(sldUpdateSingleStudentEvent.getSchlNo()).isEqualTo(sagaData.getSchlNo());
    assertThat(sldUpdateSingleStudentEvent.getPen()).isEqualTo(sagaData.getPen());
    assertThat(sldUpdateSingleStudentEvent.getReportDate()).isEqualTo(sagaData.getReportDate());
    assertThat(sldUpdateSingleStudentEvent.getSldStudent().getPen()).isEqualTo(sagaData.getMovedToPen());

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(UPDATE_SLD_STUDENT.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
  }

  @Test
  public void testUpdateSldStudentPrograms_givenEventAndSagaData_shouldPostEventToSldApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var sldStudentPayload = SldStudent.builder()
      .pen(sagaData.getPen())
      .distNo(sagaData.getDistNo())
      .schlNo(sagaData.getSchlNo())
      .reportDate(sagaData.getReportDate())
      .studentId(sagaData.getStudentId())
      .build();
    var event = Event.builder()
      .eventType(EventType.UPDATE_SLD_STUDENT)
      .eventOutcome(EventOutcome.SLD_STUDENT_UPDATED)
      .sagaId(saga.getSagaId())
      .studentID(studentID)
      .eventPayload(JsonUtil.getJsonStringFromObject(sldStudentPayload))
      .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(SLD_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(UPDATE_SLD_STUDENT_PROGRAMS);
    var sldUpdateStudentProgramsEvent = JsonUtil.getJsonObjectFromString(SldUpdateStudentProgramsEvent.class, newEvent.getEventPayload());
    assertThat(sldUpdateStudentProgramsEvent.getDistNo()).isEqualTo(sagaData.getDistNo());
    assertThat(sldUpdateStudentProgramsEvent.getSchlNo()).isEqualTo(sagaData.getSchlNo());
    assertThat(sldUpdateStudentProgramsEvent.getPen()).isEqualTo(sagaData.getPen());
    assertThat(sldUpdateStudentProgramsEvent.getReportDate()).isEqualTo(sagaData.getReportDate());
    assertThat(sldUpdateStudentProgramsEvent.getStudentId()).isEqualTo(sagaData.getStudentId());
    assertThat(sldUpdateStudentProgramsEvent.getSldStudentProgram().getPen()).isEqualTo(sagaData.getMovedToPen());

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(UPDATE_SLD_STUDENT_PROGRAMS.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.UPDATE_SLD_STUDENT.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.SLD_STUDENT_UPDATED.toString());
  }

  /**
   * Dummy move sld saga data json string.
   *
   * @return the string
   */
  protected String placeholderMoveSldSagaData() {
    return " {\n" +
      "     \"pen\": \"120164447\",\n" +
      "     \"distNo\": \"069\",\n" +
      "     \"schlNo\": \"69015\",\n" +
      "     \"reportDate\": 20030930,\n" +
      "     \"studentId\": \"120164447\",\n" +
      "     \"movedToPen\": \"100100010\"\n" +
      "  }";
  }

  /**
   * Gets move sld saga data from json string.
   *
   * @param json the json
   * @return the move sld saga data from json string
   */
  protected MoveSldSagaData getMoveSldSagaDataFromJsonString(String json) {
    try {
      return JsonUtil.getJsonObjectFromString(MoveSldSagaData.class, json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
