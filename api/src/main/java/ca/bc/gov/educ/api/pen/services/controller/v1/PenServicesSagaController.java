package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.constants.SagaEnum;
import ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.pen.services.endpoint.v1.PenServicesSagaEndpoint;
import ca.bc.gov.educ.api.pen.services.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.pen.services.mapper.SagaMapper;
import ca.bc.gov.educ.api.pen.services.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.v1.BaseStudentSagaData;
import ca.bc.gov.educ.api.pen.services.struct.v1.Saga;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentDemergeCompleteSagaData;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeCompleteSagaData;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.PEN_SERVICES_STUDENT_DEMERGE_COMPLETE_SAGA;
import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.PEN_SERVICES_STUDENT_MERGE_COMPLETE_SAGA;
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

  /**
   * Process services saga response entity.
   *
   * @param sagaName the saga name
   * @param sagaData the saga data
   * @return the response entity
   */
  private ResponseEntity<String> processServicesSaga(final SagaEnum sagaName, final BaseStudentSagaData sagaData) {
    try {
      final var studentID = sagaData.getStudentID();
      final var sagaInProgress = this.getSagaService().findAllByStudentIDAndStatusIn(studentID, sagaName.toString(), this.getStatusesFilter());
      if (!sagaInProgress.isEmpty()) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
      }
      final String payload = JsonUtil.getJsonStringFromObject(sagaData);
      final var saga = this.getOrchestratorMap()
          .get(sagaName.toString())
          .startSaga(payload, studentID, sagaData.getCreateUser());
      return ResponseEntity.ok(saga.getSagaId().toString());
    } catch (final Exception e) {
      Thread.currentThread().interrupt();
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
