package ca.bc.gov.educ.api.pen.validation.rest;

import ca.bc.gov.educ.api.pen.validation.properties.ApplicationProperties;
import ca.bc.gov.educ.api.pen.validation.struct.v1.GenderCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Rest utils.
 */
@Component
@Slf4j
public class RestUtils {
  private final Map<String, List<GenderCode>> genderCodesMap = new ConcurrentHashMap<>();
  private static final String PARAMETERS_ATTRIBUTE = "parameters";

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
   * Gets gender codes from student api.
   *
   * @return the gender codes from student api
   */
  @Retryable(value = {Exception.class}, maxAttempts = 10, backoff = @Backoff(multiplier = 2, delay = 2000))
  public synchronized List<GenderCode> getGenderCodesFromStudentAPI() {
    if (this.genderCodesMap.size() == 0) {
      RestTemplate restTemplate = getRestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      ParameterizedTypeReference<List<GenderCode>> responseType = new ParameterizedTypeReference<>() {
      };
      var responseBody = restTemplate.exchange(props.getStudentApiURL() + "/gender-codes", HttpMethod.GET, new HttpEntity<>(PARAMETERS_ATTRIBUTE, headers), responseType).getBody();
      this.genderCodesMap.put("genderCodes", responseBody);
      return responseBody;
    } else {
      return this.genderCodesMap.get("genderCodes");
    }
  }

  /**
   * Reload cache.
   */
  @Scheduled( cron="0 0 0 * * *")
  public void reloadCache(){
    log.info("started reloading cache..");
    this.genderCodesMap.clear();
    getGenderCodesFromStudentAPI();
    log.info("reloading cache completed..");
  }
}
