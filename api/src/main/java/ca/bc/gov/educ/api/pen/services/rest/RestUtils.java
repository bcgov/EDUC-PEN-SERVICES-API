package ca.bc.gov.educ.api.pen.services.rest;

import ca.bc.gov.educ.api.pen.services.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.services.struct.*;
import ca.bc.gov.educ.api.pen.services.struct.v1.GenderCode;
import ca.bc.gov.educ.api.pen.services.struct.v1.GradeCode;
import ca.bc.gov.educ.api.pen.services.struct.v1.SchoolTombstone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The type Rest utils.
 */
@Component
@Slf4j
public class RestUtils {
  /**
   * The constant GRADE_CODES.
   */
  public static final String GRADE_CODES = "gradeCodes";
  /**
   * The constant GENDER_CODES.
   */
  public static final String GENDER_CODES = "genderCodes";
  /**
   * The constant CONTENT_TYPE.
   */
  public static final String CONTENT_TYPE = "Content-Type";
  /**
   * The Gender codes map.
   */
  private final Map<String, List<GenderCode>> genderCodesMap = new ConcurrentHashMap<>();
  /**
   * The Grade codes map.
   */
  private final Map<String, List<GradeCode>> gradeCodesMap = new ConcurrentHashMap<>();
  /**
   * The Gender lock.
   */
  private final ReadWriteLock genderLock = new ReentrantReadWriteLock();
  /**
   * The Grade lock.
   */
  private final ReadWriteLock gradeLock = new ReentrantReadWriteLock();
  /**
   * The School lock
   */
  private final ReadWriteLock schoolLock = new ReentrantReadWriteLock();
  /**
   * The Props.
   */
  private final ApplicationProperties props;

  /**
   * The Web client.
   */
  private final WebClient webClient;

  private final Map<String, SchoolTombstone> schoolMap = new ConcurrentHashMap<>();

  /**
   * Instantiates a new Rest utils.
   *
   * @param props     the props
   * @param webClient the web client
   */
  public RestUtils(@Autowired final ApplicationProperties props, final WebClient webClient) {
    this.props = props;
    this.webClient = webClient;
  }


  /**
   * Init. let the main thread be free for starting up.
   */
  @PostConstruct
  public void init() {
    if (this.props.getIsHttpRampUp() != null && this.props.getIsHttpRampUp()) {
      this.setGenderCodesMap();
      log.info("Called student api and loaded {} gender codes", this.genderCodesMap.values().size());
      this.setGradeCodesMap();
      log.info("Called student api and loaded {} grade codes", this.gradeCodesMap.values().size());
      this.populateSchoolMap();
    }
  }

  /**
   * Gets school by min code.
   *
   * @param mincode the mincode
   * @return the school by min code
   */
  public Optional<SchoolTombstone> getSchoolByMincode(final String mincode) {
    if (this.schoolMap.isEmpty()) {
      log.info("School map is empty reloading schools");
      this.populateSchoolMap();
    }
    return Optional.ofNullable(this.schoolMap.get(mincode));
  }

  /**
   * Populate school map.
   */
  public void populateSchoolMap() {
    val writeLock = this.schoolLock.writeLock();
    try {
      writeLock.lock();
      for (val school : this.getSchools()) {
        this.schoolMap.putIfAbsent(school.getMincode(), school);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache school {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("loaded  {} schools to memory", this.schoolMap.values().size());
  }

  public List<SchoolTombstone> getSchools() {
    log.info("Calling Institute api to load schools to memory");
    return this.webClient.get()
            .uri(this.props.getInstituteApiURL() + "/school")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(SchoolTombstone.class)
            .collectList()
            .block();
  }

  /**
   * Gets gender codes from student api.
   *
   * @return the gender codes from student api
   */
  public List<GenderCode> getGenderCodes() {
    return this.genderCodesMap.get(GENDER_CODES);
  }

  /**
   * Gets grade codes.
   *
   * @return the grade codes
   */
  public List<GradeCode> getGradeCodes() {
      return this.gradeCodesMap.get(GRADE_CODES);
  }

  /**
   * Reload cache.
   */
  @Scheduled(cron = "@midnight")
  @Retryable(value = {Exception.class}, maxAttempts = 20, backoff = @Backoff(multiplier = 2, delay = 2000))
  public void reloadCache() {
    log.info("started reloading cache..");
    this.setGenderCodesMap();
    this.setGradeCodesMap();
    this.populateSchoolMap();
    log.info("reloading cache completed..");
  }

  /**
   * Sets gender codes map.
   */
  public void setGenderCodesMap() {
    val writeLock = this.genderLock.writeLock();
    try {
      writeLock.lock();
      this.genderCodesMap.clear();
      final List<GenderCode> genderCodes = this.webClient.get().uri(this.props.getStudentApiURL(), uri -> uri.path("/gender-codes").build())
        .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve().bodyToFlux(GenderCode.class).collectList().block();
      this.genderCodesMap.put(GENDER_CODES, genderCodes);
    } finally {
      writeLock.unlock();
    }
  }


  /**
   * Sets grade codes map.
   */
  public void setGradeCodesMap() {
    val writeLock = this.gradeLock.writeLock();
    try {
      writeLock.lock();
      this.gradeCodesMap.clear();
      final List<GradeCode> gradeCodes = this.webClient.get().uri(this.props.getStudentApiURL(), uri -> uri.path("/grade-codes").build()).header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve().bodyToFlux(GradeCode.class).collectList().block();
      this.gradeCodesMap.put(GRADE_CODES, gradeCodes);
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Gets latest pen number from student api.
   *
   * @param transactionID the transactionID
   * @return the latest pen number from student api
   * @throws JsonProcessingException the json processing exception
   */
  @Retryable(value = {Exception.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public int getLatestPenNumberFromStudentAPI(final String transactionID) throws JsonProcessingException {
    final SearchCriteria criteria = SearchCriteria.builder().key("pen").operation(FilterOperation.STARTS_WITH).value("1").valueType(ValueType.STRING).build();
    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    final List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    val objectMapper = new ObjectMapper();
    val criteriaJSON = objectMapper.writeValueAsString(searches);
    final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.props.getStudentApiURL() + "/paginated")
      .queryParam("searchCriteriaList", criteriaJSON)
      .queryParam("pageSize", 1)
      .queryParam("sort", "{\"pen\":\"DESC\"}");

    final var url = builder.toUriString();
    log.info("url is :: {}", url);
    final ParameterizedTypeReference<RestPageImpl<Student>> responseType = new ParameterizedTypeReference<>() {
    };
    final var studentResponse = this.webClient.get()
      .uri(url)
      .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .retrieve().bodyToMono(responseType).block();
    final var optionalStudent = Objects.requireNonNull(studentResponse).getContent().stream().findFirst();
    if (optionalStudent.isPresent()) {
      val firstStudent = optionalStudent.get();
      return Integer.parseInt(firstStudent.getPen().substring(0, 8));
    }
    log.warn("PEN could not be retrieved, returning 0 for transactionID :: {}", transactionID);
    return 0;
  }
}
