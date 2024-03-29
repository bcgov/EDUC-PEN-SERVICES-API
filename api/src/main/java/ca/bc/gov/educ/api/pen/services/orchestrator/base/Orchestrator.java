package ca.bc.gov.educ.api.pen.services.orchestrator.base;

import ca.bc.gov.educ.api.pen.services.model.Saga;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * The interface Orchestrator.
 */
public interface Orchestrator {

  /**
   * Gets saga name.
   *
   * @return the saga name
   */
  String getSagaName();

  /**
   * Start saga.
   *
   * @param saga  the saga data
   */
  void startSaga(Saga saga);

  /**
   * create saga.
   *
   * @param payload   the payload
   * @param studentID the student id
   * @param userName  the user who created the saga
   * @return the saga
   */
  Saga createSaga(String payload, UUID studentID, String userName);

  /**
   * create multiple sagas.
   *
   * @param payloads   the list of  pair of student id and payload
   * @param userName  the user who created the saga
   * @return the saga
   */
  List<Saga> createMultipleSagas(List<Pair<UUID, String>> payloads, String userName);

  /**
   * Replay saga.
   *
   * @param saga the saga
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  void replaySaga(Saga saga) throws IOException, InterruptedException, TimeoutException;
}
