package ca.bc.gov.educ.api.pen.services.rest;

import ca.bc.gov.educ.api.pen.services.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.services.struct.*;
import ca.bc.gov.educ.api.pen.services.struct.v1.GenderCode;
import ca.bc.gov.educ.api.pen.services.struct.v1.GradeCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
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
  private final Map<String, List<GenderCode>> genderCodesMap = new ConcurrentHashMap<>();
  private final Map<String, List<GradeCode>> gradeCodesMap = new ConcurrentHashMap<>();
  private static final String PARAMETERS_ATTRIBUTE = "parameters";
  private final ReadWriteLock genderLock = new ReentrantReadWriteLock();
  private final ReadWriteLock gradeLock = new ReentrantReadWriteLock();
  private final ApplicationProperties props;

  /**
   * Instantiates a new Rest utils.
   *
   * @param props the props
   */
  public RestUtils(@Autowired final ApplicationProperties props) {
    this.props = props;
  }

  /**
   * Gets rest template.
   *
   * @return the rest template
   */
  public RestTemplate getRestTemplate() {
    return getRestTemplate(null);
  }

  /**
   * Gets rest template.
   *
   * @param scopes the scopes
   * @return the rest template
   */
  public RestTemplate getRestTemplate(List<String> scopes) {
    log.debug("Calling get token method");
    ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
    resourceDetails.setClientId(props.getClientID());
    resourceDetails.setClientSecret(props.getClientSecret());
    resourceDetails.setAccessTokenUri(props.getTokenURL());
    if (scopes != null) {
      resourceDetails.setScope(scopes);
    }
    return new OAuth2RestTemplate(resourceDetails, new DefaultOAuth2ClientContext());
  }

  /**
   * Init.
   */
  @PostConstruct
  public void init() {
    setGenderCodesMap();
    log.info("Called student api and loaded {} gender codes", this.genderCodesMap.values().size());
    setGradeCodesMap();
    log.info("Called student api and loaded {} grade codes", this.gradeCodesMap.values().size());
  }

  /**
   * Gets gender codes from student api.
   *
   * @return the gender codes from student api
   */
  public List<GenderCode> getGenderCodes() {
    Lock readLock = genderLock.readLock();
    try {
      readLock.lock();
      return this.genderCodesMap.get(GENDER_CODES);
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Gets grade codes.
   *
   * @return the grade codes
   */
  public List<GradeCode> getGradeCodes() {
    Lock readLock = gradeLock.readLock();
    try {
      readLock.lock();
      return this.gradeCodesMap.get(GRADE_CODES);
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Reload cache.
   */
  @Scheduled(cron = "@midnight")
  @Retryable(value = {Exception.class}, maxAttempts = 20, backoff = @Backoff(multiplier = 2, delay = 2000))
  public void reloadCache() {
    log.info("started reloading cache..");
    setGenderCodesMap();
    setGradeCodesMap();
    log.info("reloading cache completed..");
  }

  /**
   * Sets gender codes map.
   */
  public void setGenderCodesMap() {
    Lock writeLock = genderLock.writeLock();
    try {
      writeLock.lock();
      this.genderCodesMap.clear();
      RestTemplate restTemplate = getRestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      ParameterizedTypeReference<List<GenderCode>> responseType = new ParameterizedTypeReference<>() {
      };
      var responseBody = restTemplate.exchange(props.getStudentApiURL() + "/gender-codes", HttpMethod.GET, new HttpEntity<>(PARAMETERS_ATTRIBUTE, headers), responseType).getBody();
      this.genderCodesMap.put(GENDER_CODES, responseBody);
    } finally {
      writeLock.unlock();
    }
  }


  /**
   * Sets grade codes map.
   */
  public void setGradeCodesMap() {
    Lock writeLock = gradeLock.writeLock();
    try {
      writeLock.lock();
      this.gradeCodesMap.clear();
      RestTemplate restTemplate = getRestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      ParameterizedTypeReference<List<GradeCode>> responseType = new ParameterizedTypeReference<>() {
      };
      var responseBody = restTemplate.exchange(props.getStudentApiURL() + "/grade-codes", HttpMethod.GET, new HttpEntity<>(PARAMETERS_ATTRIBUTE, headers), responseType).getBody();
      this.gradeCodesMap.put(GRADE_CODES, responseBody);
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
  public int getLatestPenNumberFromStudentAPI(String transactionID) throws JsonProcessingException {
    RestTemplate restTemplate = getRestTemplate();
    SearchCriteria criteria = SearchCriteria.builder().key("pen").operation(FilterOperation.STARTS_WITH).value("1").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(props.getStudentApiURL() + "/paginated")
        .queryParam("searchCriteriaList", criteriaJSON)
        .queryParam("pageSize", 1)
        .queryParam("sort", "{\"pen\":\"DESC\"}");

    DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
    defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
    restTemplate.setUriTemplateHandler(defaultUriBuilderFactory);

    ParameterizedTypeReference<RestPageImpl<Student>> responseType = new ParameterizedTypeReference<>() {
    };
    var url = builder.toUriString();
    log.info("url is :: {}", url);
    ResponseEntity<RestPageImpl<Student>> studentResponse = restTemplate.exchange(url, HttpMethod.GET, null, responseType);

    var optionalStudent = Objects.requireNonNull(studentResponse.getBody()).getContent().stream().findFirst();
    if (optionalStudent.isPresent()) {
      var firstStudent = optionalStudent.get();
      return Integer.parseInt(firstStudent.getPen().substring(0, 8));
    }
    log.warn("PEN could not be retrieved, returning 0 for transactionID :: {}", transactionID);
    return 0;
  }
}
