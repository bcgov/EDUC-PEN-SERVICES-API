package ca.bc.gov.educ.api.pen.services.service.events;

import ca.bc.gov.educ.api.pen.services.constants.EventOutcome;
import ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode;
import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMergeMapper;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeRepository;
import ca.bc.gov.educ.api.pen.services.service.PenRequestStudentRecordValidationService;
import ca.bc.gov.educ.api.pen.services.service.PenService;
import ca.bc.gov.educ.api.pen.services.struct.v1.Event;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import ca.bc.gov.educ.api.pen.services.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static ca.bc.gov.educ.api.pen.services.constants.EventOutcome.*;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
public class EventHandlerService {

  /**
   * The constant studentMapper.
   */
  @Getter(PRIVATE)
  private static final StudentMergeMapper studentMergeMapper = StudentMergeMapper.mapper;

  @Getter(PRIVATE)
  private final PenRequestStudentRecordValidationService validationService;

  @Getter(PRIVATE)
  private final PenService penService;

  @Getter(PRIVATE)
  private final StudentMergeRepository studentMergeRepository;

  /**
   * The Ob mapper.
   */
  private final ObjectMapper obMapper = new ObjectMapper();

  /**
   * Instantiates a new Event handler service.
   *
   * @param validationService       the validation service
   * @param penService              the pen service
   * @param studentMergeRepository  the student merge repository
   */
  @Autowired
  public EventHandlerService(PenRequestStudentRecordValidationService validationService, PenService penService, StudentMergeRepository studentMergeRepository) {
    this.validationService = validationService;
    this.penService = penService;
    this.studentMergeRepository = studentMergeRepository;
  }

  /**
   * Handle validate student demog data event.
   *
   * @param event the event
   */
  @Transactional(propagation = REQUIRES_NEW)
  public byte[] handleValidateStudentDemogDataEvent(@NonNull Event event) throws JsonProcessingException {
    var validationPayload = JsonUtil.getJsonObjectFromString(PenRequestStudentValidationPayload.class, event.getEventPayload());
    var result = getValidationService().validateStudentRecord(validationPayload);
    EventOutcome eventOutcome;
    String eventPayload;
    if (result.isEmpty()) {
      eventOutcome = VALIDATION_SUCCESS_NO_ERROR_WARNING;
      eventPayload = VALIDATION_SUCCESS_NO_ERROR_WARNING.toString();
    } else {
      var isError = result.stream().anyMatch(x -> x.getPenRequestBatchValidationIssueSeverityCode().equals(PenRequestStudentValidationIssueSeverityCode.ERROR.toString()));
      if (isError) {
        eventOutcome = VALIDATION_SUCCESS_WITH_ERROR;
      } else {
        eventOutcome = VALIDATION_SUCCESS_WITH_ONLY_WARNING;
      }
      eventPayload = JsonUtil.getJsonStringFromObject(result);
    }

    Event newEvent = Event.builder()
            .sagaId(event.getSagaId())
            .eventType(event.getEventType())
            .eventOutcome(eventOutcome)
            .eventPayload(eventPayload).build();
    if (log.isDebugEnabled()) {
      log.debug("responding back :: {}", newEvent);
    }
    return obMapper.writeValueAsBytes(newEvent);
  }

  /**
   * Handle get next PEN number event.
   *
   * @param event the event
   */
  @Transactional(propagation = REQUIRES_NEW)
  public byte[] handleGetNextPenNumberEvent(@NonNull Event event) throws JsonProcessingException {
    var transactionID = event.getEventPayload();
    var nextPenNumber = getPenService().getNextPenNumber(transactionID);

    Event newEvent = Event.builder()
            .sagaId(event.getSagaId())
            .eventType(event.getEventType())
            .eventOutcome(NEXT_PEN_NUMBER_RETRIEVED)
            .eventPayload(nextPenNumber).build();
    if (log.isDebugEnabled()) {
      log.debug("responding back :: {}", newEvent);
    }
    return obMapper.writeValueAsBytes(newEvent);
  }

  @Transactional(propagation = REQUIRES_NEW)
  public byte[] handleCreateMergeEvent(@NonNull Event event) throws JsonProcessingException {
    List<StudentMerge> payload = new ArrayList<>();

    // MergedToStudent
    StudentMerge mergedToPEN = JsonUtil.getJsonObjectFromString(StudentMerge.class, event.getEventPayload());
    RequestUtil.setAuditColumnsForCreate(mergedToPEN);
    getStudentMergeRepository().save(studentMergeMapper.toModel(mergedToPEN));
    payload.add(mergedToPEN);

    // MergedFromStudent
    StudentMerge mergedFromPEN = StudentMerge.builder().studentID(mergedToPEN.getMergeStudentID()).mergeStudentID(mergedToPEN.getStudentID())
            .studentMergeDirectionCode("TO").studentMergeSourceCode(mergedToPEN.getStudentMergeSourceCode()).build();
    RequestUtil.setAuditColumnsForCreate(mergedFromPEN);
    getStudentMergeRepository().save(studentMergeMapper.toModel(mergedFromPEN));
    payload.add(mergedFromPEN);

    Event newEvent = Event.builder()
            .sagaId(event.getSagaId())
            .eventType(event.getEventType())
            .eventOutcome(MERGE_CREATED)
            .eventPayload(JsonUtil.getJsonStringFromObject(payload)).build();
    if (log.isDebugEnabled()) {
      log.debug("responding back :: {}", newEvent);
    }
    return obMapper.writeValueAsBytes(newEvent);
  }
}
