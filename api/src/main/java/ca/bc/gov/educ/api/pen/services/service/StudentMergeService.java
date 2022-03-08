package ca.bc.gov.educ.api.pen.services.service;

import ca.bc.gov.educ.api.pen.services.constants.EventOutcome;
import ca.bc.gov.educ.api.pen.services.constants.EventType;
import ca.bc.gov.educ.api.pen.services.constants.StatsType;
import ca.bc.gov.educ.api.pen.services.mapper.v1.StudentMergeMapper;
import ca.bc.gov.educ.api.pen.services.model.ServicesEvent;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeDirectionCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeEntity;
import ca.bc.gov.educ.api.pen.services.model.StudentMergeSourceCodeEntity;
import ca.bc.gov.educ.api.pen.services.repository.ServicesEventRepository;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeDirectionCodeTableRepository;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeRepository;
import ca.bc.gov.educ.api.pen.services.repository.StudentMergeSourceCodeTableRepository;
import ca.bc.gov.educ.api.pen.services.struct.v1.StudentMergeStats;
import ca.bc.gov.educ.api.pen.services.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.pen.services.constants.EventStatus.DB_COMMITTED;

/**
 * StudentMergeService
 *
 * @author Mingwei
 */
@Service
public class StudentMergeService {

  /**
   * The Student merge repo.
   */
  private final StudentMergeRepository studentMergeRepo;

  /**
   * The Event repository.
   */
  private final ServicesEventRepository eventRepository;

  /**
   * The Student merge direction code table repo.
   */
  private final StudentMergeDirectionCodeTableRepository studentMergeDirectionCodeTableRepo;

  /**
   * The Student merge source code table repo.
   */
  private final StudentMergeSourceCodeTableRepository studentMergeSourceCodeTableRepo;

  /**
   * Instantiates a new Student merge service.
   *
   * @param studentMergeRepo                   the student merge repo
   * @param eventRepository                    the event repository
   * @param studentMergeDirectionCodeTableRepo the student merge direction code table repo
   * @param studentMergeSourceCodeTableRepo    the student merge source code table repo
   */
  @Autowired
  public StudentMergeService(final StudentMergeRepository studentMergeRepo, final ServicesEventRepository eventRepository, final StudentMergeDirectionCodeTableRepository studentMergeDirectionCodeTableRepo,
                             final StudentMergeSourceCodeTableRepository studentMergeSourceCodeTableRepo) {
    this.studentMergeRepo = studentMergeRepo;
    this.eventRepository = eventRepository;
    this.studentMergeDirectionCodeTableRepo = studentMergeDirectionCodeTableRepo;
    this.studentMergeSourceCodeTableRepo = studentMergeSourceCodeTableRepo;
  }

  /**
   * Check for idempotency list.
   *
   * @param studentMergeEntities the student merge entities
   * @return the list
   */
  private List<StudentMergeEntity> checkForIdempotency(final List<StudentMergeEntity> studentMergeEntities) {
    return studentMergeEntities.stream().filter(this.getStudentMergeEntityNotPresentPredicate()).collect(Collectors.toList());
  }

  /**
   * Update list based on db existence list.
   *
   * @param studentMergeEntities the student merge entities
   * @return the list
   */
  private List<StudentMergeEntity> updateListBasedOnDBExistence(final List<StudentMergeEntity> studentMergeEntities) {
    final List<StudentMergeEntity> updatedEntities = new ArrayList<>();
    for (val entity : studentMergeEntities) {
      this.getStudentMergeEntityIfPresent(entity).ifPresent(updatedEntities::add);
    }
    return updatedEntities;
  }

  /**
   * Gets student merge entity not present predicate.
   *
   * @return the student merge entity not present predicate
   */
  private Predicate<StudentMergeEntity> getStudentMergeEntityNotPresentPredicate() {
    return el -> {
      val mergeOptional = this.studentMergeRepo.
        findByStudentIDAndMergeStudentIDAndStudentMergeDirectionCode(el.getStudentID(), el.getMergeStudentID(), el.getStudentMergeDirectionCode());
      return mergeOptional.isEmpty();
    };
  }

  /**
   * Gets student merge entity present predicate.
   *
   * @return the student merge entity present predicate
   */
  private Optional<StudentMergeEntity> getStudentMergeEntityIfPresent(final StudentMergeEntity entity) {
    return this.studentMergeRepo.
      findByStudentIDAndMergeStudentIDAndStudentMergeDirectionCode(entity.getStudentID(), entity.getMergeStudentID(), entity.getStudentMergeDirectionCode());
  }

  /**
   * Create merge pair.
   *
   * @param studentMergeEntities the student merge entities
   * @return the pair
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public Pair<List<StudentMergeEntity>, Optional<ServicesEvent>> createMerge(final List<StudentMergeEntity> studentMergeEntities) throws JsonProcessingException {
    final var updatedList = this.checkForIdempotency(studentMergeEntities);
    if (!updatedList.isEmpty()) {
      this.studentMergeRepo.saveAll(updatedList);
      return Pair.of(updatedList, Optional.of(this.eventRepository.save(this.createServicesEvent(updatedList.get(0).getCreateUser(), updatedList.get(0).getUpdateUser(),
        JsonUtil.getJsonStringFromObject(updatedList.stream().map(StudentMergeMapper.mapper::toStructure).collect(Collectors.toList())), EventType.CREATE_MERGE, EventOutcome.MERGE_CREATED))));
    }
    return Pair.of(new ArrayList<>(), Optional.empty());
  }

  /**
   * Delete merge pair.
   *
   * @param studentMergeEntities the student merge entities
   * @return the pair
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public Pair<List<StudentMergeEntity>, Optional<ServicesEvent>> deleteMerge(final List<StudentMergeEntity> studentMergeEntities) throws JsonProcessingException {
    final var updatedList = this.updateListBasedOnDBExistence(studentMergeEntities);
    if (!updatedList.isEmpty()) {
      this.studentMergeRepo.deleteAll(updatedList);
      return Pair.of(updatedList, Optional.of(this.eventRepository.save(this.createServicesEvent(studentMergeEntities.get(0).getCreateUser(), studentMergeEntities.get(0).getUpdateUser(),
        JsonUtil.getJsonStringFromObject(updatedList.stream().map(StudentMergeMapper.mapper::toStructure).collect(Collectors.toList())), EventType.DELETE_MERGE, EventOutcome.MERGE_DELETED))));
    }
    return Pair.of(new ArrayList<>(), Optional.empty());
  }

  /**
   * Returns the list of student merge record
   *
   * @param studentID      the student id
   * @param mergeDirection the merge direction
   * @return {@link List<StudentMergeEntity>}
   */
  public List<StudentMergeEntity> findStudentMerges(final UUID studentID, final String mergeDirection) {
    if (mergeDirection == null) {
      return this.studentMergeRepo.findStudentMergeEntityByStudentID(studentID);
    } else {
      return this.studentMergeRepo.findStudentMergeEntityByStudentIDAndStudentMergeDirectionCode(studentID, mergeDirection.toUpperCase());
    }
  }


  public List<StudentMergeEntity> findStudentMerges(final LocalDateTime createDateStart, final LocalDateTime createDateEnd, final String mergeDirection) {

    return this.studentMergeRepo.findAllByCreateDateBetweenAndStudentMergeDirectionCode(createDateStart, createDateEnd, mergeDirection.toUpperCase());
  }

  /**
   * Returns the full list of student merge direction codes
   *
   * @return {@link List<StudentMergeDirectionCodeEntity>}
   */
  @Cacheable("mergeDirectionCodes")
  public List<StudentMergeDirectionCodeEntity> getStudentMergeDirectionCodesList() {
    return this.studentMergeDirectionCodeTableRepo.findAll();
  }

  /**
   * Find student merge direction code optional.
   *
   * @param mergeDirectionCode the merge direction code
   * @return the optional
   */
  public Optional<StudentMergeDirectionCodeEntity> findStudentMergeDirectionCode(final String mergeDirectionCode) {
    return Optional.ofNullable(this.loadStudentMergeDirectionCodes().get(mergeDirectionCode));
  }

  /**
   * Load student merge direction codes map.
   *
   * @return the map
   */
  private Map<String, StudentMergeDirectionCodeEntity> loadStudentMergeDirectionCodes() {
    return this.getStudentMergeDirectionCodesList().stream().collect(Collectors.toMap(StudentMergeDirectionCodeEntity::getMergeDirectionCode, Function.identity()));
  }

  /**
   * Returns the full list of student merge source codes
   *
   * @return {@link List<StudentMergeSourceCodeEntity>}
   */
  @Cacheable("mergeSourceCodes")
  public List<StudentMergeSourceCodeEntity> getStudentMergeSourceCodesList() {
    return this.studentMergeSourceCodeTableRepo.findAll();
  }

  /**
   * Find student merge source code optional.
   *
   * @param mergeSourceCode the merge source code
   * @return the optional
   */
  public Optional<StudentMergeSourceCodeEntity> findStudentMergeSourceCode(final String mergeSourceCode) {
    return Optional.ofNullable(this.loadStudentMergeSourceCodes().get(mergeSourceCode));
  }

  /**
   * Load student merge source codes map.
   *
   * @return the map
   */
  private Map<String, StudentMergeSourceCodeEntity> loadStudentMergeSourceCodes() {
    return this.getStudentMergeSourceCodesList().stream().collect(Collectors.toMap(StudentMergeSourceCodeEntity::getMergeSourceCode, Function.identity()));
  }

  /**
   * Create services event services event.
   *
   * @param createUser   the create user
   * @param updateUser   the update user
   * @param jsonString   the json string
   * @param eventType    the event type
   * @param eventOutcome the event outcome
   * @return the services event
   */
  private ServicesEvent createServicesEvent(final String createUser, final String updateUser, final String jsonString, final EventType eventType, final EventOutcome eventOutcome) {
    return ServicesEvent.builder()
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .createUser(createUser)
      .updateUser(updateUser)
      .eventPayload(jsonString)
      .eventType(eventType.toString())
      .eventStatus(DB_COMMITTED.toString())
      .eventOutcome(eventOutcome.toString())
      .build();

  }

  public StudentMergeStats getMergeStats(final StatsType statsType) {
    final LocalDateTime currentDate = LocalDateTime.now();
    if (statsType == StatsType.NUMBER_OF_MERGES_IN_LAST_13_MONTH) {
      final Map<String, Long> mergeNumbersMap = new LinkedHashMap<>();
      for (int i = 12; i >= 0; i--) {
        final LocalDateTime startDate = currentDate.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        final LocalDateTime endDate = currentDate.minusMonths(i).withDayOfMonth(currentDate.minusMonths(i).toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        val mergeNumbers = this.studentMergeRepo.countAllByCreateDateBetweenAndStudentMergeDirectionCode(startDate, endDate, "TO");
        val monthName = (i == 0) ? "CURRENT" : startDate.getMonth().toString();
        mergeNumbersMap.put(monthName, mergeNumbers);
      }
      return StudentMergeStats.builder().numberOfMergesInLastMonths(mergeNumbersMap).build();
    }
    return StudentMergeStats.builder().build();
  }

  public Optional<StudentMergeEntity> findStudentMergeByID(final UUID studentMergeID) {
    return this.studentMergeRepo.findById(studentMergeID);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteStudentMerge(final StudentMergeEntity studentMergeEntity) {
    this.studentMergeRepo.delete(studentMergeEntity);
  }
}
