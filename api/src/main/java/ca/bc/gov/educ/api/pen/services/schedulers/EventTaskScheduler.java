package ca.bc.gov.educ.api.pen.services.schedulers;

import ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.pen.services.helpers.LogHelper;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.pen.services.repository.SagaRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Event task scheduler.
 */
@Slf4j
@Component
public class EventTaskScheduler {
  /**
   * The Saga orchestrators.
   */
  @Getter(PRIVATE)
  private final Map<String, Orchestrator> sagaOrchestrators = new HashMap<>();
  /**
   * The Saga repository.
   */
  @Getter(PRIVATE)
  private final SagaRepository sagaRepository;
  /**
   * The Status filters.
   */
  @Setter
  private List<String> statusFilters;

  /**
   * Instantiates a new Event task scheduler.
   *
   * @param sagaRepository the saga repository
   * @param orchestrators  the orchestrators
   */
  public EventTaskScheduler(final SagaRepository sagaRepository, final List<Orchestrator> orchestrators) {
    this.sagaRepository = sagaRepository;
    orchestrators.forEach(orchestrator -> this.sagaOrchestrators.put(orchestrator.getSagaName(), orchestrator));
    log.info("'{}' Saga Orchestrators are loaded.", String.join(",", this.sagaOrchestrators.keySet()));
  }

  /**
   * Find and process uncompleted sagas.
   */
  @Scheduled(cron = "1 * * * * *") //
  @SchedulerLock(name = "REPLAY_UNCOMPLETED_SAGAS",
      lockAtLeastFor = "PT50S", lockAtMostFor = "PT55S")
  public void findAndProcessUncompletedSagas() {
    final List<Saga> sagas = this.getSagaRepository().findAllByStatusIn(this.getStatusFilters());
    if (!sagas.isEmpty()) {
      this.processUncompletedSagas(sagas);
    }
  }

  /**
   * Process uncompleted sagas.
   *
   * @param sagas the sagas
   */
  private void processUncompletedSagas(final List<Saga> sagas) {
    for (val saga : sagas) {
      if (saga.getCreateDate().isBefore(LocalDateTime.now().minusMinutes(1))
          && this.getSagaOrchestrators().containsKey(saga.getSagaName())) {
        try {
          this.setRetryCountAndLog(saga);
          this.getSagaOrchestrators().get(saga.getSagaName()).replaySaga(saga);
        } catch (final InterruptedException ex) {
          Thread.currentThread().interrupt();
          log.error("InterruptedException while findAndProcessPendingSagaEvents :: for saga :: {} :: {}", saga, ex);
        } catch (final Exception e) {
          log.error("Exception while findAndProcessPendingSagaEvents :: for saga :: {} :: {}", saga, e);
        }
      }
    }
  }

  /**
   * Gets status filters.
   *
   * @return the status filters
   */
  public List<String> getStatusFilters() {
    if (this.statusFilters != null && !this.statusFilters.isEmpty()) {
      return this.statusFilters;
    } else {
      final var statuses = new ArrayList<String>();
      statuses.add(SagaStatusEnum.IN_PROGRESS.toString());
      statuses.add(SagaStatusEnum.STARTED.toString());
      return statuses;
    }
  }

  private void setRetryCountAndLog(final Saga saga) {
    Integer retryCount = saga.getRetryCount();
    if (retryCount == null || retryCount == 0) {
      retryCount = 1;
    } else {
      retryCount += 1;
    }
    saga.setRetryCount(retryCount);
    this.getSagaRepository().save(saga);
    LogHelper.logSagaRetry(saga);
  }
}
