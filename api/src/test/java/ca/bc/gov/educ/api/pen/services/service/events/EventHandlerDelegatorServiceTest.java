package ca.bc.gov.educ.api.pen.services.service.events;

import ca.bc.gov.educ.api.pen.services.constants.EventOutcome;
import ca.bc.gov.educ.api.pen.services.constants.StudentMergeDirectionCodes;
import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMergeMapper;
import ca.bc.gov.educ.api.pen.services.messaging.MessagePublisher;
import ca.bc.gov.educ.api.pen.services.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.pen.services.model.ServicesEvent;
import ca.bc.gov.educ.api.pen.services.repository.ServicesEventRepository;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeRepository;
import ca.bc.gov.educ.api.pen.services.struct.v1.Event;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import ca.bc.gov.educ.api.pen.services.support.NatsMessageImpl;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Message;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.EventOutcome.MERGE_DELETED;
import static ca.bc.gov.educ.api.pen.services.constants.EventOutcome.MERGE_FOUND;
import static ca.bc.gov.educ.api.pen.services.constants.EventType.*;
import static ca.bc.gov.educ.api.pen.services.constants.TopicsEnum.PEN_SERVICES_API_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EventHandlerDelegatorServiceTest {

  @Autowired
  MessagePublisher messagePublisher;
  @Autowired
  Publisher publisher;

  @Autowired
  Connection connection;

  @Autowired
  EventHandlerDelegatorService eventHandlerDelegatorService;

  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  @Captor
  ArgumentCaptor<ServicesEvent> servicesEventArgumentCaptor;

  @Autowired
  StudentMergeRepository studentMergeRepository;
  @Autowired
  ServicesEventRepository servicesEventRepository;
  private static final StudentMergeMapper mapper = StudentMergeMapper.mapper;

  @After
  public void after() {
    this.servicesEventRepository.deleteAll();
    this.studentMergeRepository.deleteAll();
  }

  @Test
  public void handleEvent_givenCreateMergeEvent_shouldRespondToNatsAndBroadcastEventToSTAN() throws JsonProcessingException {
    final var payload = this.createStudentMergePayload();
    final var event = Event.builder()
        .eventType(CREATE_MERGE)
        .replyTo(PEN_SERVICES_API_TOPIC.toString())
        .eventPayload(JsonUtil.getJsonStringFromObject(payload))
        .sagaId(UUID.randomUUID())
        .build();
    final Message message = NatsMessageImpl.builder()
        .connection(this.connection)
        .data(JsonUtil.getJsonBytesFromObject(event))
        .SID("SID")
        .replyTo("TEST_TOPIC")
        .build();
    this.eventHandlerDelegatorService.handleEvent(event, message);
    verify(this.publisher, atLeastOnce()).dispatchChoreographyEvent(this.servicesEventArgumentCaptor.capture());
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(any(), this.eventCaptor.capture());
    final var replyEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    final List<StudentMerge> merges = new ObjectMapper().readValue(replyEvent.getEventPayload(), new TypeReference<>() {
    });
    final ServicesEvent servicesEvent = this.servicesEventArgumentCaptor.getValue();
    assertThat(replyEvent).isNotNull();
    assertThat(replyEvent.getEventOutcome()).isEqualTo(EventOutcome.MERGE_CREATED);
    assertThat(merges).size().isEqualTo(2);
    assertThat(servicesEvent).isNotNull();
    assertThat(servicesEvent.getEventPayload()).isNotBlank();
    assertThat(servicesEvent.getEventOutcome()).isEqualTo(EventOutcome.MERGE_CREATED.toString());
    final List<StudentMerge> mergesFromChoreography = new ObjectMapper().readValue(servicesEvent.getEventPayload(), new TypeReference<>() {
    });
    assertThat(mergesFromChoreography).size().isEqualTo(2);
  }


  @Test
  public void testHandleDeleteMergeEvent_givenStudentMergePayload_whenSuccessfullyProcessed_shouldHaveEventOutcomeMERGE_DELETED() throws JsonProcessingException {
    final var payload = this.createStudentMergePayload();
    final var event = Event.builder()
        .eventType(CREATE_MERGE)
        .replyTo(PEN_SERVICES_API_TOPIC.toString())
        .eventPayload(JsonUtil.getJsonStringFromObject(payload))
        .sagaId(UUID.randomUUID())
        .build();
    final Message message = NatsMessageImpl.builder()
        .connection(this.connection)
        .data(JsonUtil.getJsonBytesFromObject(event))
        .SID("SID")
        .replyTo("TEST_TOPIC")
        .build();
    this.eventHandlerDelegatorService.handleEvent(event, message);
    final var deletePayload = this.createStudentMergePayload();
    final var deleteEvent = Event.builder().eventType(DELETE_MERGE).replyTo(PEN_SERVICES_API_TOPIC.toString()).eventPayload(JsonUtil.getJsonStringFromObject(deletePayload)).sagaId(UUID.randomUUID()).build();
    final Message deleteMessage = NatsMessageImpl.builder()
        .connection(this.connection)
        .data(JsonUtil.getJsonBytesFromObject(event))
        .SID("SID")
        .replyTo("TEST_TOPIC")
        .build();
    this.eventHandlerDelegatorService.handleEvent(deleteEvent, deleteMessage);
    verify(this.publisher, atLeastOnce()).dispatchChoreographyEvent(this.servicesEventArgumentCaptor.capture());
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(any(), this.eventCaptor.capture());
    final var replyEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    final List<StudentMerge> merges = new ObjectMapper().readValue(replyEvent.getEventPayload(), new TypeReference<>() {
    });
    final ServicesEvent servicesEvent = this.servicesEventArgumentCaptor.getValue();
    assertThat(replyEvent).isNotNull();
    assertThat(replyEvent.getEventOutcome()).isEqualTo(MERGE_DELETED);
    assertThat(merges).size().isEqualTo(2);
    assertThat(servicesEvent).isNotNull();
    assertThat(servicesEvent.getEventPayload()).isNotBlank();
    assertThat(servicesEvent.getEventOutcome()).isEqualTo(MERGE_DELETED.toString());
    final List<StudentMerge> mergesFromChoreography = new ObjectMapper().readValue(servicesEvent.getEventPayload(), new TypeReference<>() {
    });
    assertThat(mergesFromChoreography).size().isEqualTo(2);
  }

  @Test
  public void testHandleGetMergeEvent_givenStudentId_whenSuccessfullyProcessed_shouldHaveEventOutcomeMERGE_FOUND() throws JsonProcessingException {
    // Given
    final var studentMerge = mapper.toModel(createStudentMergePayload());
    studentMerge.setStudentMergeDirectionCode(StudentMergeDirectionCodes.FROM.getCode());
    final var studentId = studentMerge.getStudentID();
    this.studentMergeRepository.save(studentMerge);

    final var event = Event.builder()
            .eventType(GET_MERGES)
            .replyTo(PEN_SERVICES_API_TOPIC.toString())
            .eventPayload(studentId.toString())
            .sagaId(UUID.randomUUID())
            .build();

    final Message message = NatsMessageImpl.builder()
            .connection(this.connection)
            .data(JsonUtil.getJsonBytesFromObject(event))
            .SID("SID")
            .replyTo("TEST_TOPIC")
            .build();

    // When
    this.eventHandlerDelegatorService.handleEvent(event, message);

    // Then
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq("TEST_TOPIC"), eventCaptor.capture());

    final var replyEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    final List<StudentMerge> merges = new ObjectMapper().readValue(replyEvent.getEventPayload(), new TypeReference<>() {});

    assertThat(replyEvent).isNotNull();
    assertThat(replyEvent.getEventOutcome()).isEqualTo(MERGE_FOUND);
    assertThat(merges).isNotEmpty().hasSize(1);
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
