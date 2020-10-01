package ca.bc.gov.educ.api.pen.validation.service;

import ca.bc.gov.educ.api.pen.validation.model.PENNameText;
import ca.bc.gov.educ.api.pen.validation.repository.PenNameTextRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Service
@Slf4j
public class PENNameTextService {

  public static final String PEN_NAME_TEXT = "PEN_NAME_TEXT";
  private final Map<String, List<PENNameText>> penNameTextMap = new ConcurrentHashMap<>();
  private final ReadWriteLock penNameTextLock = new ReentrantReadWriteLock();
  @Getter(PRIVATE)
  private final PenNameTextRepository penNameTextRepository;

  @Autowired
  public PENNameTextService(final PenNameTextRepository penNameTextRepository) {
    this.penNameTextRepository = penNameTextRepository;
  }

  @PostConstruct
  public void init() {
    this.setPenNameTexts();
    log.info("loaded {} entries into pen name text map ", penNameTextMap.values().size());
  }

  /**
   * Reload cache.
   */
  @Scheduled(cron = "0 1 0 * * *")
  public void reloadCache() {
    log.info("started reloading cache..");
    this.setPenNameTexts();
    log.info("reloading cache completed..");
  }

  public List<PENNameText> getPenNameTexts() {
    Lock readLock = penNameTextLock.readLock();
    try {
      readLock.lock();
      return this.penNameTextMap.get(PEN_NAME_TEXT);
    } finally {
      readLock.unlock();
    }
  }

  private void setPenNameTexts() {
    Lock writeLock = penNameTextLock.writeLock();
    try {
      writeLock.lock();
      this.penNameTextMap.put(PEN_NAME_TEXT, getPenNameTextRepository().findAll().stream()
          .peek(x -> x.setInvalidText(x.getInvalidText() == null ? "" : x.getInvalidText().trim())).collect(Collectors.toList()));
      log.info("loaded {} entries into pen name text map ", penNameTextMap.values().size());
    } finally {
      writeLock.unlock();
    }
  }

}
