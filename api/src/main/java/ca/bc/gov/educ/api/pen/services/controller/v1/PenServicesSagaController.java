package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.pen.services.endpoint.v1.PenServicesSagaEndpoint;
import ca.bc.gov.educ.api.pen.services.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.orchestrator.StudentMergeCompleteOrchestrator;
import ca.bc.gov.educ.api.pen.services.service.SagaService;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeCompleteSagaData;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static ca.bc.gov.educ.api.pen.services.constants.SagaEnum.STUDENT_MERGE_COMPLETE_SAGA;
import static lombok.AccessLevel.PRIVATE;

@RestController
@Slf4j
public class PenServicesSagaController implements PenServicesSagaEndpoint {
  @Getter(PRIVATE)
  private final SagaService sagaService;

  @Getter(PRIVATE)
  private final StudentMergeCompleteOrchestrator studentMergeCompleteOrchestrator;

  @Autowired
  public PenServicesSagaController(final SagaService sagaService, StudentMergeCompleteOrchestrator studentMergeCompleteOrchestrator) {
    this.sagaService = sagaService;
    this.studentMergeCompleteOrchestrator = studentMergeCompleteOrchestrator;
  }

  @Override
  public ResponseEntity<String> completeStudentMerge(StudentMergeCompleteSagaData studentMergeCompleteSagaData) {
    try {
      var studentID = studentMergeCompleteSagaData.getStudentID();
      var sagaInProgress = getSagaService().findAllByStudentIDAndStatusIn(studentID, STUDENT_MERGE_COMPLETE_SAGA.toString(), getStatusesFilter());
      if (!sagaInProgress.isEmpty()) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
      }
      String payload = JsonUtil.getJsonStringFromObject(studentMergeCompleteSagaData);
      final Saga saga = getSagaService().createSagaRecordInDB(STUDENT_MERGE_COMPLETE_SAGA.toString(), studentMergeCompleteSagaData.getCreateUser(), payload, studentID);
      getStudentMergeCompleteOrchestrator().startSaga(payload, studentID, studentMergeCompleteSagaData.getCreateUser());
      return ResponseEntity.ok(saga.getSagaId().toString());
    } catch (final Exception e) {
      Thread.currentThread().interrupt();
      throw new SagaRuntimeException(e.getMessage());
    }
  }

  protected List<String> getStatusesFilter() {
    var statuses = new ArrayList<String>();
    statuses.add(SagaStatusEnum.IN_PROGRESS.toString());
    statuses.add(SagaStatusEnum.STARTED.toString());
    return statuses;
  }

}
