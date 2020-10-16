package ca.bc.gov.educ.api.pen.services.exception;

/**
 * The type Pen reg api runtime exception.
 */
public class PenServicesAPIRuntimeException extends RuntimeException {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 5241655513745148898L;

  /**
   * Instantiates a new Pen reg api runtime exception.
   *
   * @param message the message
   */
  public PenServicesAPIRuntimeException(String message) {
		super(message);
	}

}
