package ca.bc.gov.educ.api.pen.services.service.events;

import ca.bc.gov.educ.api.pen.services.constants.EventOutcome;
import ca.bc.gov.educ.api.pen.services.constants.PenRequestStudentValidationIssueSeverityCode;
import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMergeMapper;
import ca.bc.gov.educ.api.pen.services.model.ServicesEvent;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeEntity;
import ca.bc.gov.educ.api.pen.services.service.PenRequestStudentRecordValidationService;
import ca.bc.gov.educ.api.pen.services.service.PenService;
import ca.bc.gov.educ.api.pen.services.service.StudentMergeService;
import ca.bc.gov.educ.api.pen.services.struct.v1.Event;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMerge;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
   * The constant RESPONDING_BACK.
   */
  public static final String RESPONDING_BACK = "responding back :: {}";
  /**
   * The constant studentMapper.
   */
  private static final StudentMergeMapper studentMergeMapper = StudentMergeMapper.mapper;
  /**
   * The Validation service.
   */
  @Getter(PRIVATE)
  private final PenRequestStudentRecordValidationService validationService;

  /**
   * The Pen service.
   */
  @Getter(PRIVATE)
  private final PenService penService;

  /**
   * The Student merge service.
   */
  @Getter(PRIVATE)
  private final StudentMergeService studentMergeService;

  /**
   * The Ob mapper.
   */
  private final ObjectMapper obMapper = new ObjectMapper();

  /**
   * Instantiates a new Event handler service.
   *
   * @param validationService   the validation service
   * @param penService          the pen service
   * @param studentMergeService the student merge
   */
  @Autowired
  public EventHandlerService(final PenRequestStudentRecordValidationService validationService, final PenService penService, final StudentMergeService studentMergeService) {
    this.validationService = validationService;
    this.penService = penService;
    this.studentMergeService = studentMergeService;
  }

  /**
   * Handle validate student demog data event.
   *
   * @param event the event
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = REQUIRES_NEW)
  public byte[] handleValidateStudentDemogDataEvent(@NonNull final Event event) throws JsonProcessingException {
    final var validationPayload = JsonUtil.getJsonObjectFromString(PenRequestStudentValidationPayload.class, event.getEventPayload());
    final var result = this.getValidationService().validateStudentRecord(validationPayload);
    final EventOutcome eventOutcome;
    final String eventPayload;
    if (result.isEmpty()) {
      eventOutcome = VALIDATION_SUCCESS_NO_ERROR_WARNING;
      eventPayload = VALIDATION_SUCCESS_NO_ERROR_WARNING.toString();
    } else {
      final var isError = result.stream().anyMatch(x -> x.getPenRequestBatchValidationIssueSeverityCode().equals(PenRequestStudentValidationIssueSeverityCode.ERROR.toString()));
      if (isError) {
        eventOutcome = VALIDATION_SUCCESS_WITH_ERROR;
      } else {
        eventOutcome = VALIDATION_SUCCESS_WITH_ONLY_WARNING;
      }
      eventPayload = JsonUtil.getJsonStringFromObject(result);
    }

    final Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(eventOutcome)
        .eventPayload(eventPayload).build();
    if (log.isDebugEnabled()) {
      log.debug(RESPONDING_BACK, newEvent);
    }
    return this.obMapper.writeValueAsBytes(newEvent);
  }

  /**
   * Handle get next PEN number event.
   *
   * @param event the event
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = REQUIRES_NEW)
  public byte[] handleGetNextPenNumberEvent(@NonNull final Event event) throws JsonProcessingException {
    final var transactionID = event.getEventPayload();
    final var nextPenNumber = this.getPenService().getNextPenNumber(transactionID);

    final Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(NEXT_PEN_NUMBER_RETRIEVED)
        .eventPayload(nextPenNumber).build();
    if (log.isDebugEnabled()) {
      log.debug(RESPONDING_BACK, newEvent);
    }
    return this.obMapper.writeValueAsBytes(newEvent);
  }

  /**
   * Delete student merges for two ways
   *
   * @param event event with the payload for one student merge
   * @return the list of two created student merges as two ways persistence
   * @throws JsonProcessingException the exception
   */
  @Transactional(propagation = REQUIRES_NEW)
  public Pair<byte[], Optional<ServicesEvent>> handleDeleteMergeEvent(@NonNull final Event event) throws JsonProcessingException {
    final List<StudentMergeEntity> mergeEntities = new ArrayList<>();

    // MergedToStudent
    final StudentMerge mergedToPEN = JsonUtil.getJsonObjectFromString(StudentMerge.class, event.getEventPayload());
    mergeEntities.add(studentMergeMapper.toModel(mergedToPEN));
    // MergedFromStudent
    final StudentMerge mergedFromPEN = StudentMerge.builder().studentID(mergedToPEN.getMergeStudentID()).mergeStudentID(mergedToPEN.getStudentID())
        .studentMergeDirectionCode("TO").studentMergeSourceCode(mergedToPEN.getStudentMergeSourceCode()).build();
    mergeEntities.add(studentMergeMapper.toModel(mergedFromPEN));
    val pair = this.studentMergeService.deleteMerge(mergeEntities);
    val deletedItems = pair.getLeft().stream().map(StudentMergeMapper.mapper::toStructure).collect(Collectors.toList());
    final Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(MERGE_DELETED)
        .eventPayload(JsonUtil.getJsonStringFromObject(deletedItems)).build();
    if (log.isDebugEnabled()) {
      log.debug(RESPONDING_BACK, newEvent);
    }
    return Pair.of(this.obMapper.writeValueAsBytes(newEvent), pair.getRight());
  }

  /**
   * Create student merges for two ways
   *
   * @param event event with the payload for one student merge
   * @return the list of two created student merges as two ways persistence
   * @throws JsonProcessingException the exception
   */
  @Transactional(propagation = REQUIRES_NEW)
  public Pair<byte[], Optional<ServicesEvent>> handleCreateMergeEvent(@NonNull final Event event) throws JsonProcessingException {
    final List<StudentMergeEntity> mergeEntities = new ArrayList<>();

    /**
     * MergedToStudent - Surviving / TruePEN that has a status of "ACTIVE" and a merge direction of "FROM" against merged student
     *
     * TruePEN is merged "FROM" MergedPEN
     * StudentMerge entity
     *    studentID       : TruePEN's studentID     (active student)
     *    directionCode   : "FROM"
     *    mergeStudentID  : MergedPEN's studentID   (merged student)
     */
    final StudentMerge mergedToPEN = JsonUtil.getJsonObjectFromString(StudentMerge.class, event.getEventPayload());
    mergeEntities.add(studentMergeMapper.toModel(mergedToPEN));
    /**
     * MergedFromStudent - Non-surviving / MergedPEN that has a status of "MERGED" and a merge direction of "TO" against active student
     *
     * MergedPEN is merged "TO" TruePEN
     * StudentMerge entity
     *    studentID       : MergedPEN's studentID   (merged student)
     *    directionCode   : "TO"
     *    mergeStudentID  : TruePEN's studentID     (active student)
     */
    final StudentMerge mergedFromPEN = StudentMerge.builder().studentID(mergedToPEN.getMergeStudentID()).mergeStudentID(mergedToPEN.getStudentID())
        .studentMergeDirectionCode("TO").studentMergeSourceCode(mergedToPEN.getStudentMergeSourceCode())
        .createUser(mergedToPEN.getCreateUser()).updateUser(mergedToPEN.getUpdateUser()).build();
    mergeEntities.add(studentMergeMapper.toModel(mergedFromPEN));
    val pair = this.studentMergeService.createMerge(mergeEntities);
    val savedItems = pair.getLeft().stream().map(StudentMergeMapper.mapper::toStructure).collect(Collectors.toList());
    final Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(MERGE_CREATED)
        .eventPayload(JsonUtil.getJsonStringFromObject(savedItems)).build();
    if (log.isDebugEnabled()) {
      log.debug(RESPONDING_BACK, newEvent);
    }
    return Pair.of(this.obMapper.writeValueAsBytes(newEvent), pair.getRight());
  }


}
