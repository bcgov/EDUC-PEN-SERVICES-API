package ca.bc.gov.educ.api.pen.services.orchestrator.base;

import ca.bc.gov.educ.api.pen.services.model.Saga;

import java.io.IOException;
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
   * Start saga saga.
   *
   * @param payload                  the payload
   * @param studentID                the student id
   * @param userName                 the user who created the saga
   * @return the saga
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   * @throws IOException          the io exception
   */
  Saga startSaga(String payload, UUID studentID, String userName) throws InterruptedException, TimeoutException, IOException;

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
