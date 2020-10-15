package ca.bc.gov.educ.api.pen.services.config;

import ca.bc.gov.educ.api.pen.services.rest.RestUtils;
import ca.bc.gov.educ.api.pen.services.rules.Rule;
import ca.bc.gov.educ.api.pen.services.rules.impl.*;
import ca.bc.gov.educ.api.pen.services.service.PENNameTextService;
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
   * @param penNameTextService the pen name text service
   * @return the rule
   */
  @Bean
  @Order(2)
  @Autowired
  public Rule legalLastNameRule(final PENNameTextService penNameTextService) {
    return new LegalLastNameRule(penNameTextService);
  }

  /**
   * Legal first name rule rule.
   *
   * @param penNameTextService the pen name text service
   * @return the rule
   */
  @Bean
  @Order(3)
  @Autowired
  public Rule legalFirstNameRule(final PENNameTextService penNameTextService) {
    return new LegalFirstNameRule(penNameTextService);
  }

  /**
   * Legal middle name rule rule.
   *
   * @param penNameTextService the pen name text service
   * @return the rule
   */
  @Bean
  @Order(4)
  @Autowired
  public Rule legalMiddleNameRule(final PENNameTextService penNameTextService) {
    return new LegalMiddleNameRule(penNameTextService);
  }

  /**
   * Usual first name rule rule.
   *
   * @param penNameTextService the pen name text service
   * @return the rule
   */
  @Bean
  @Order(5)
  @Autowired
  public Rule usualFirstNameRule(final PENNameTextService penNameTextService) {
    return new UsualFirstNameRule(penNameTextService);
  }

  /**
   * Usual last name rule rule.
   *
   * @param penNameTextService the pen name text service
   * @return the rule
   */
  @Bean
  @Order(6)
  @Autowired
  public Rule usualLastNameRule(final PENNameTextService penNameTextService) {
    return new UsualLastNameRule(penNameTextService);
  }

  /**
   * Usual middle name rule rule.
   *
   * @param penNameTextService the pen name text service
   * @return the rule
   */
  @Bean
  @Order(7)
  @Autowired
  public Rule usualMiddleNameRule(final PENNameTextService penNameTextService) {
    return new UsualMiddleNameRule(penNameTextService);
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

  /**
   * Grade code rule rule.
   *
   * @param restUtils the rest utils
   * @return the rule
   */
  @Bean
  @Order(11)
  @Autowired
  public Rule gradeCodeRule(final RestUtils restUtils) {
    return new GradeCodeRule(restUtils);
  }

}
