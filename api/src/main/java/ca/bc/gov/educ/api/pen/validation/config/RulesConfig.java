package ca.bc.gov.educ.api.pen.validation.config;

import ca.bc.gov.educ.api.pen.validation.rest.RestUtils;
import ca.bc.gov.educ.api.pen.validation.rules.Rule;
import ca.bc.gov.educ.api.pen.validation.rules.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * The type Rules config.
 */
@Configuration
public class RulesConfig {


  /**
   * Submitted pen rule rule.
   *
   * @return the rule
   */
  @Bean
  @Order(1)
  public Rule submittedPENRule() {
    return new SubmittedPENRule();
  }

  /**
   * Legal last name rule rule.
   *
   * @return the rule
   */
  @Bean
  @Order(2)
  public Rule legalLastNameRule() {
    return new LegalLastNameRule();
  }

  /**
   * Legal first name rule rule.
   *
   * @return the rule
   */
  @Bean
  @Order(3)
  public Rule legalFirstNameRule() {
    return new LegalFirstNameRule();
  }

  /**
   * Legal middle name rule rule.
   *
   * @return the rule
   */
  @Bean
  @Order(4)
  public Rule legalMiddleNameRule() {
    return new LegalMiddleNameRule();
  }

  /**
   * Usual first name rule rule.
   *
   * @return the rule
   */
  @Bean
  @Order(5)
  public Rule usualFirstNameRule() {
    return new UsualFirstNameRule();
  }

  /**
   * Usual last name rule rule.
   *
   * @return the rule
   */
  @Bean
  @Order(6)
  public Rule usualLastNameRule() {
    return new UsualLastNameRule();
  }

  /**
   * Usual middle name rule rule.
   *
   * @return the rule
   */
  @Bean
  @Order(7)
  public Rule usualMiddleNameRule() {
    return new UsualMiddleNameRule();
  }

  /**
   * Gender rule rule.
   *
   * @param restUtils the rest utils
   * @return the rule
   */
  @Bean
  @Order(8)
  @Autowired
  public Rule genderRule(final RestUtils restUtils) {
    return new GenderRule(restUtils);
  }

  /**
   * Postal code rule rule.
   *
   * @return the rule
   */
  @Bean
  @Order(9)
  public Rule postalCodeRule() {
    return new PostalCodeRule();
  }

  /**
   * Birth date rule rule.
   *
   * @return the rule
   */
  @Bean
  @Order(10)
  public Rule birthDateRule() {
    return new BirthDateRule();
  }

}
