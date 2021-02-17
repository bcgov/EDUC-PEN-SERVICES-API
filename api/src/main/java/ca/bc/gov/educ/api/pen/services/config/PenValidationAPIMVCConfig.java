package ca.bc.gov.educ.api.pen.services.config;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The type Pen reg api mvc config.
 *
 * @author Om
 */
@Configuration
public class PenValidationAPIMVCConfig implements WebMvcConfigurer {

  /**
   * The Pen reg api interceptor.
   */
  @Getter(AccessLevel.PRIVATE)
  private final PenServicesAPIInterceptor penServicesAPIInterceptor;

  /**
   * Instantiates a new Pen reg api mvc config.
   *
   * @param penServicesAPIInterceptor the pen reg api interceptor
   */
  @Autowired
  public PenValidationAPIMVCConfig(final PenServicesAPIInterceptor penServicesAPIInterceptor) {
    this.penServicesAPIInterceptor = penServicesAPIInterceptor;
  }

  /**
   * Add interceptors.
   *
   * @param registry the registry
   */
  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor(this.penServicesAPIInterceptor).addPathPatterns("/**");
  }
}
