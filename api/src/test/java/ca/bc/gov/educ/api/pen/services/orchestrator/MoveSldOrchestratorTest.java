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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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
  private MoveMultipleSldSagaData sagaData;

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
    sagaData = getMoveMultipleSldSagaDataFromJsonString(sagaPayload);
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
    assertThat(newEvent.getEventType()).isEqualTo(UPDATE_SLD_STUDENTS_BY_IDS);
    var sldUpdateStudentsEvent = JsonUtil.getJsonObjectFromString(SldUpdateStudentsByIdsEvent.class, newEvent.getEventPayload());
    assertThat(sldUpdateStudentsEvent.getIds().size()).isEqualTo(sagaData.getMoveSldSagaData().size());
    int i = 0;
    for (Iterator<SldStudentId> it = sldUpdateStudentsEvent.getIds().iterator(); it.hasNext(); i++) {
      var id = it.next();
      assertThat(id.getDistNo()).isEqualTo(sagaData.getMoveSldSagaData().get(i).getDistNo());
      assertThat(id.getSchlNo()).isEqualTo(sagaData.getMoveSldSagaData().get(i).getSchlNo());
      assertThat(id.getPen()).isEqualTo(sagaData.getMoveSldSagaData().get(i).getPen());
      assertThat(id.getReportDate()).isEqualTo(sagaData.getMoveSldSagaData().get(i).getReportDate());
    }
    assertThat(sldUpdateStudentsEvent.getSldStudent().getPen()).isEqualTo(sagaData.getMovedToPen());

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(UPDATE_SLD_STUDENTS_BY_IDS.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
  }

  @Test
  public void testUpdateSldStudentPrograms_givenEventAndSagaData_shouldPostEventToSldApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var sldStudentPayload = sagaData.getMoveSldSagaData().stream().map(sldData -> SldStudent.builder()
      .pen(sldData.getPen())
      .distNo(sldData.getDistNo())
      .schlNo(sldData.getSchlNo())
      .reportDate(sldData.getReportDate())
      .studentId(sldData.getStudentId())
      .build()
    ).collect(Collectors.toList());
    var event = Event.builder()
      .eventType(EventType.UPDATE_SLD_STUDENTS_BY_IDS)
      .eventOutcome(EventOutcome.SLD_STUDENT_UPDATED)
      .sagaId(saga.getSagaId())
      .studentID(studentID)
      .eventPayload(JsonUtil.getJsonStringFromObject(List.of(sldStudentPayload)))
      .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(SLD_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(UPDATE_SLD_STUDENT_PROGRAMS_BY_DATA);
    var sldUpdateStudentProgramsEvent = JsonUtil.getJsonObjectFromString(SldUpdateStudentProgramsByDataEvent.class, newEvent.getEventPayload());
    int i = 0;
    for (Iterator<SldStudentProgram> it = sldUpdateStudentProgramsEvent.getExamples().iterator(); it.hasNext(); i++) {
      var id = it.next();
      assertThat(id.getDistNo()).isEqualTo(sagaData.getMoveSldSagaData().get(i).getDistNo());
      assertThat(id.getSchlNo()).isEqualTo(sagaData.getMoveSldSagaData().get(i).getSchlNo());
      assertThat(id.getPen()).isEqualTo(sagaData.getMoveSldSagaData().get(i).getPen().substring(0, 9));
      assertThat(id.getReportDate()).isEqualTo(sagaData.getMoveSldSagaData().get(i).getReportDate());
      assertThat(id.getStudentId()).isEqualTo(sagaData.getMoveSldSagaData().get(i).getStudentId());
    }
    assertThat(sldUpdateStudentProgramsEvent.getSldStudentProgram().getPen()).isEqualTo(sagaData.getMovedToPen());

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(UPDATE_SLD_STUDENT_PROGRAMS_BY_DATA.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.UPDATE_SLD_STUDENTS_BY_IDS.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.SLD_STUDENT_UPDATED.toString());
  }

  /**
   * Dummy move sld saga data json string.
   *
   * @return the string
   */
  protected String placeholderMoveSldSagaData() {
    return " {\n" +
      "    \"createUser\": \"test\",\n" +
      "    \"studentID\": \"" + studentID + "\",\n" +
      "    \"moveSldSagaData\": [{\n" +
      "       \"pen\": \"120164447\",\n" +
      "       \"distNo\": \"069\",\n" +
      "       \"schlNo\": \"69015\",\n" +
      "       \"reportDate\": 20030930,\n" +
      "       \"studentId\": \"120164447\",\n" +
      "       \"movedToPen\": \"100100010\"\n" +
      "     },\n" +
      "     {\n" +
      "       \"pen\": \"120164447D\",\n" +
      "       \"distNo\": \"069\",\n" +
      "       \"schlNo\": \"69015\",\n" +
      "       \"reportDate\": 20040201,\n" +
      "       \"studentId\": \"120164447\",\n" +
      "       \"movedToPen\": \"100100010\"\n" +
      "    }]\n" +
      "  }";
  }

  /**
   * Gets move multiple sld saga data from json string.
   *
   * @param json the json
   * @return the move multiple sld saga data from json string
   */
  protected MoveMultipleSldSagaData getMoveMultipleSldSagaDataFromJsonString(String json) {
    try {
      return JsonUtil.getJsonObjectFromString(MoveMultipleSldSagaData.class, json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
