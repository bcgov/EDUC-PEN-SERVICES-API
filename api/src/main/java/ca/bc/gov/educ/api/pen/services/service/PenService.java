package ca.bc.gov.educ.api.pen.services.service;

import ca.bc.gov.educ.api.pen.services.exception.PenServicesAPIRuntimeException;
import ca.bc.gov.educ.api.pen.services.rest.RestUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Pen service.
 */
@Service
@Slf4j
public class PenService {

  /**
   * The Redisson client.
   */
  @Getter(PRIVATE)
  private final RedissonClient redissonClient;

  /**
   * The Rest utils.
   */
  @Getter(PRIVATE)
  private final RestUtils restUtils;

  /**
   * Instantiates a new Pen service.
   *
   * @param redissonClient the redisson client
   * @param restUtils      the rest utils
   */
  @Autowired
  public PenService(final RedissonClient redissonClient, final RestUtils restUtils) {
    this.redissonClient = redissonClient;
    this.restUtils = restUtils;
  }

  /**
   * Gets next pen number.
   *
   * @param transactionID the transactionID to identify the transaction.
   * @return the next pen number
   */
  @Retryable(value = {Exception.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public String getNextPenNumber(final String transactionID) {
    final RBucket<String> penForTransaction = this.getRedissonClient().getBucket(transactionID);
    if (penForTransaction.isExists()) {
      return penForTransaction.get();
    } else {
      final long penWithoutCheckDigit = this.getNextPenNumberWithoutCheckDigit(transactionID);
      final int checkDigit = this.calculateCheckDigit(String.valueOf(penWithoutCheckDigit), transactionID);
      final String penGenerated = penWithoutCheckDigit + "" + checkDigit;
      penForTransaction.set(penGenerated, 2, TimeUnit.DAYS);
      return penGenerated;
    }

  }

  /**
   * Example:
   * <pre>
   *  the original PEN number is 746282656
   *  1. First 8 digits are 74628265
   *  2. Sum the odd digits: 7 + 6 + 8 + 6 = 27 (S1)
   *  3. Extract the even digits 4,2,2,5 to get A = 4225.
   *  4. Multiply A times 2 to get B = 8450
   *  5. Sum the digits of B: 8 + 4 + 5 + 0 = 17 (S2)
   *  6. 27 + 17 = 44 (S3)
   *  7. S3 is not a multiple of 10
   *  8. Calculate check-digit as 10 - MOD(S3,10): 10 - MOD(44,10) = 10 - 4 = 6
   *  A) Alternatively, round up S3 to next multiple of 10: 44 becomes 50 B) Subtract S3 from this: 50 - 44 = 6
   * </pre>
   *
   * @param penWithoutCheckDigit the pen without check digit
   * @param guid                 the guid to identify the transaction.
   * @return checkDigit a number
   */
  private int calculateCheckDigit(@NonNull final String penWithoutCheckDigit, final String guid) {
    final List<Integer> odds = new LinkedList<>();
    final List<Integer> evens = new LinkedList<>();
    this.createOddAndEven(penWithoutCheckDigit, odds, evens);
    final int sumOdds = odds.stream().mapToInt(Integer::intValue).sum();
    final String fullEvenValueDoubledString = String.valueOf(Integer.parseInt(evens.stream().map(Object::toString).collect(Collectors.joining(""))) * 2);
    final List<Integer> listOfFullEvenValueDoubled = new LinkedList<>();
    for (int i = 0; i < fullEvenValueDoubledString.length(); i++) {
      listOfFullEvenValueDoubled.add(Integer.parseInt(fullEvenValueDoubledString.substring(i, i + 1)));
    }
    final int sumEvens = listOfFullEvenValueDoubled.stream().mapToInt(Integer::intValue).sum();
    final int finalSum = sumEvens + sumOdds;
    int checkDigit = 0;
    if (finalSum % 10 != 0) {
      checkDigit = 10 - (finalSum % 10);
    }
    log.info("Check Digit calculated for :: {} , is :: {}", guid, checkDigit);
    return checkDigit;
  }

  /**
   * Create odd and even.
   *
   * @param penWithoutCheckDigit the pen without check digit
   * @param odds                 the odds
   * @param evens                the evens
   */
  private void createOddAndEven(final String penWithoutCheckDigit, final List<Integer> odds, final List<Integer> evens) {
    for (int i = 0; i < penWithoutCheckDigit.length(); i++) {
      final int number = Integer.parseInt(penWithoutCheckDigit.substring(i, i + 1));
      if (i % 2 == 0) {
        odds.add(number);
      } else {
        evens.add(number);
      }
    }
  }

  /**
   * Gets next pen number without check digit.
   *
   * @param transactionID the transactionID to identify transaction
   * @return the next pen number without check digit
   */
  private long getNextPenNumberWithoutCheckDigit(final String transactionID) {
    log.info("getNextPenNumberWithoutCheckDigit called for transactionID :: {}", transactionID);
    final RPermitExpirableSemaphore semaphore = this.getRedissonClient().getPermitExpirableSemaphore("getNextPen");
    semaphore.trySetPermits(1);
    semaphore.expire(120, TimeUnit.SECONDS);
    try {
      final String id = semaphore.tryAcquire(120, 40, TimeUnit.SECONDS);
      long pen;
      if (id != null) {
        RAtomicLong atomicLong = this.getRedissonClient().getAtomicLong("PEN_NUMBER");
        if (!atomicLong.isExists()) {
          log.info("RAtomicLong PEN_NUMBER doesn't exist, getting the latest PEN from Student API");
          pen = this.restUtils.getLatestPenNumberFromStudentAPI(transactionID);
          if (pen == 0) {
            throw new PenServicesAPIRuntimeException("Invalid Pen Returned from downstream method.");
          } else {
            atomicLong.set(pen);
          }
        }
        pen = atomicLong.incrementAndGet();
        semaphore.tryRelease(id);
        log.info("PEN IS :: {} for transactionID :: {}", pen, transactionID);
        return pen;
      } else {
        log.warn("PEN could not be retrieved, as lock could not be acquired for transactionID :: {}", transactionID);
        throw new PenServicesAPIRuntimeException("PEN could not be retrieved, as lock could not be acquired.");
      }

    } catch (final Exception e) {
      log.warn("PEN could not be retrieved, for transactionID :: {} :: {}", transactionID, e.getMessage());
      throw new PenServicesAPIRuntimeException("PEN could not be retrieved ".concat(e.getMessage()));
    }
  }
}
