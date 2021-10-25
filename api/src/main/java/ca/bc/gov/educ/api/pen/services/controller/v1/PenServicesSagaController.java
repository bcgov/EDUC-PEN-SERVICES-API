package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.pen.services.endpoint.v1.PenServicesSagaEndpoint;
import ca.bc.gov.educ.api.pen.services.exception.InvalidParameterException;
import ca.bc.gov.educ.api.pen.services.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.pen.services.mapper.SagaMapper;
import ca.bc.gov.educ.api.pen.services.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.v1.*;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.*;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Pen services saga controller.
 */
@RestController
@Slf4j
public class PenServicesSagaController implements PenServicesSagaEndpoint {

  /**
   * The constant sagaMapper.
   */
  private static final SagaMapper sagaMapper = SagaMapper.mapper;
  /**
   * The Saga service.
   */
  @Getter(PRIVATE)
  private final SagaService sagaService;
  /**
   * The Handlers.
   */
  @Getter(PRIVATE)
  private final Map<String, Orchestrator> orchestratorMap = new HashMap<>();

  /**
   * Instantiates a new Pen services saga controller.
   *
   * @param sagaService   the saga service
   * @param orchestrators the orchestrators
   */
  @Autowired
  public PenServicesSagaController(final SagaService sagaService, final List<Orchestrator> orchestrators) {
    this.sagaService = sagaService;
    orchestrators.forEach(orchestrator -> this.orchestratorMap.put(orchestrator.getSagaName(), orchestrator));
    log.info("'{}' Saga Orchestrators are loaded.", String.join(",", this.orchestratorMap.keySet()));
  }

  @Override
  public ResponseEntity<Saga> readSaga(final UUID sagaID) {
    return this.getSagaService().findSagaById(sagaID)
        .map(sagaMapper::toStruct)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @Override
  public ResponseEntity<String> completeStudentMerge(final StudentMergeCompleteSagaData studentMergeCompleteSagaData) {
    return this.processServicesSaga(PEN_SERVICES_STUDENT_MERGE_COMPLETE_SAGA, studentMergeCompleteSagaData);
  }

  @Override
  public ResponseEntity<String> completeStudentDemerge(final StudentDemergeCompleteSagaData studentDemergeCompleteSagaData) {
    return this.processServicesSaga(PEN_SERVICES_STUDENT_DEMERGE_COMPLETE_SAGA, studentDemergeCompleteSagaData);
  }

  @Override
  public ResponseEntity<String> splitPen(final SplitPenSagaData splitPenSagaData) {
    return this.processServicesSaga(PEN_SERVICES_SPLIT_PEN_SAGA, splitPenSagaData);
  }

  @Override
  public ResponseEntity<String> moveSld(final MoveMultipleSldSagaData moveMultipleSldSagaData) {
    final var studentID = UUID.fromString(moveMultipleSldSagaData.getStudentID());
    return this.processServicesSaga(PEN_SERVICES_MOVE_SLD_SAGA, studentID, moveMultipleSldSagaData, moveMultipleSldSagaData.getCreateUser());
  }

  /**
   * Process services saga response entity.
   *
   * @param sagaName the saga name
   * @param sagaData the saga data
   * @return the response entity
   */
  private ResponseEntity<String> processServicesSaga(final SagaEnum sagaName, final BaseStudentSagaData sagaData) {
    final var studentID = UUID.fromString(sagaData.getStudentID());
    return this.processServicesSaga(sagaName, studentID, sagaData, sagaData.getCreateUser());
  }

  /**
   * Process services saga response entity.
   *
   * @param sagaName the saga name
   * @param studentID the student ID
   * @param sagaPayload the saga payload
   * @param createUser the create user
   * @return the response entity
   */
  private ResponseEntity<String> processServicesSaga(final SagaEnum sagaName, final UUID studentID, final Object sagaPayload, final String createUser) {
    try {
      final var sagaInProgress = this.getSagaService().findAllByStudentIDAndStatusIn(studentID, sagaName.toString(), this.getStatusesFilter());
      if (!sagaInProgress.isEmpty()) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
      }
      final String payload = JsonUtil.getJsonStringFromObject(sagaPayload);
      final var orchestrator = this.getOrchestratorMap().get(sagaName.toString());
      final var saga = this.getOrchestratorMap()
        .get(sagaName.toString())
        .createSaga(payload, studentID, createUser);
      orchestrator.startSaga(saga);
      return ResponseEntity.ok(saga.getSagaId().toString());
    } catch (final Exception e) {
      throw new SagaRuntimeException(e.getMessage());
    }
  }

  /**
   * Gets statuses filter.
   *
   * @return the statuses filter
   */
  protected List<String> getStatusesFilter() {
    final var statuses = new ArrayList<String>();
    statuses.add(SagaStatusEnum.IN_PROGRESS.toString());
    statuses.add(SagaStatusEnum.STARTED.toString());
    return statuses;
  }

}
