package ca.bc.gov.educ.api.pen.services.schedulers;

import ca.bc.gov.educ.api.pen.services.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.pen.services.model.Saga;
import ca.bc.gov.educ.api.pen.services.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.pen.services.repository.SagaRepository;
import ca.bc.gov.educ.api.pen.services.util.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Component
public class EventTaskScheduler {
  @Getter(PRIVATE)
  private final Map<String, Orchestrator> sagaOrchestrators = new HashMap<>();
  private final Executor taskExecutor = new EnhancedQueueExecutor.Builder()
      .setThreadFactory(new ThreadFactoryBuilder().withNameFormat("async-executor-%d").get())
      .setCorePoolSize(2).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build();
  @Getter(PRIVATE)
  private final SagaRepository sagaRepository;
  @Setter
  private List<String> statusFilters;

  public EventTaskScheduler(SagaRepository sagaRepository, List<Orchestrator> orchestrators) {
    this.sagaRepository = sagaRepository;
    orchestrators.forEach(orchestrator -> sagaOrchestrators.put(orchestrator.getSagaName(), orchestrator));
    log.info("'{}' Saga Orchestrators are loaded.", String.join(",", sagaOrchestrators.keySet()));
  }

  @Scheduled(cron = "1 * * * * *") //
  @SchedulerLock(name = "REPLAY_UNCOMPLETED_SAGAS",
      lockAtLeastFor = "PT50S", lockAtMostFor = "PT55S")
  public void findAndProcessUncompletedSagas() {
    final List<Saga> sagas = getSagaRepository().findAllByStatusIn(getStatusFilters());
    if (!sagas.isEmpty()) {
      taskExecutor.execute(() -> processUncompletedSagas(sagas));
    }
  }

  private void processUncompletedSagas(List<Saga> sagas) {
    for (val saga : sagas) {
      if (saga.getCreateDate().isBefore(LocalDateTime.now().minusMinutes(1))
          && getSagaOrchestrators().containsKey(saga.getSagaName())) {
        try {
          getSagaOrchestrators().get(saga.getSagaName()).replaySaga(saga);
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
    if (statusFilters != null && !statusFilters.isEmpty()) {
      return statusFilters;
    } else {
      var statuses = new ArrayList<String>();
      statuses.add(SagaStatusEnum.IN_PROGRESS.toString());
      statuses.add(SagaStatusEnum.STARTED.toString());
      return statuses;
    }
  }
}
