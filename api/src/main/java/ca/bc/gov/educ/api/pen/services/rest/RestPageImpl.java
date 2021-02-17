package ca.bc.gov.educ.api.pen.services.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Rest page.
 *
 * @param <T> the type parameter
 */
public class RestPageImpl<T> extends PageImpl<T> {

  private static final long serialVersionUID = -3052126580816581861L;

  /**
   * Instantiates a new Rest page.
   *
   * @param content          the content
   * @param number           the number
   * @param size             the size
   * @param totalElements    the total elements
   * @param pageable         the pageable
   * @param last             the last
   * @param totalPages       the total pages
   * @param sort             the sort
   * @param first            the first
   * @param numberOfElements the number of elements
   */
  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public RestPageImpl(@JsonProperty("content") final List<T> content,
                      @JsonProperty("number") final int number,
                      @JsonProperty("size") final int size,
                      @JsonProperty("totalElements") final Long totalElements,
                      @JsonProperty("pageable") final JsonNode pageable,
                      @JsonProperty("last") final boolean last,
                      @JsonProperty("totalPages") final int totalPages,
                      @JsonProperty("sort") final JsonNode sort,
                      @JsonProperty("first") final boolean first,
                      @JsonProperty("numberOfElements") final int numberOfElements) {

    super(content, PageRequest.of(number, size), totalElements);
  }

  /**
   * Instantiates a new Rest page.
   *
   * @param content  the content
   * @param pageable the pageable
   * @param total    the total
   */
  public RestPageImpl(final List<T> content, final Pageable pageable, final long total) {
    super(content, pageable, total);
  }

  /**
   * Instantiates a new Rest page.
   *
   * @param content the content
   */
  public RestPageImpl(final List<T> content) {
    super(content);
  }

  /**
   * Instantiates a new Rest page.
   */
  public RestPageImpl() {
        super(new ArrayList<>());
    }


}
