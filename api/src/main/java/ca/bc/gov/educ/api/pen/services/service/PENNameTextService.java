package ca.bc.gov.educ.api.pen.services.service;

import ca.bc.gov.educ.api.pen.services.model.PENNameText;
import ca.bc.gov.educ.api.pen.services.repository.PenNameTextRepository;
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

/**
 * The type Pen name text service.
 */
@Service
@Slf4j
public class PENNameTextService {

  /**
   * The constant PEN_NAME_TEXT.
   */
  public static final String PEN_NAME_TEXT = "PEN_NAME_TEXT";
  private final Map<String, List<PENNameText>> penNameTextMap = new ConcurrentHashMap<>();
  private final ReadWriteLock penNameTextLock = new ReentrantReadWriteLock();
  @Getter(PRIVATE)
  private final PenNameTextRepository penNameTextRepository;

  /**
   * Instantiates a new Pen name text service.
   *
   * @param penNameTextRepository the pen name text repository
   */
  @Autowired
  public PENNameTextService(final PenNameTextRepository penNameTextRepository) {
    this.penNameTextRepository = penNameTextRepository;
  }

  /**
   * Init.
   */
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

  /**
   * Gets pen name texts.
   *
   * @return the pen name texts
   */
  public List<PENNameText> getPenNameTexts() {
    if (this.penNameTextMap.get(PEN_NAME_TEXT) == null || this.penNameTextMap.get(PEN_NAME_TEXT).isEmpty()) {
      this.setPenNameTexts();
    }
    return this.penNameTextMap.get(PEN_NAME_TEXT);
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
