package ca.bc.gov.educ.api.pen.services.rest;

import ca.bc.gov.educ.api.pen.services.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.services.struct.v1.GenderCode;
import ca.bc.gov.educ.api.pen.services.struct.v1.GradeCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
  @Scheduled(cron = "0 0 0 * * *")
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
}
