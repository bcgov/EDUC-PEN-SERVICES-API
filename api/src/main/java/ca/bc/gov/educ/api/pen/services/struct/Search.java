package ca.bc.gov.educ.api.pen.services.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The type Search.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Search {

  /**
   * The Condition.  ENUM to hold and AND OR
   */
  Condition condition;

  /**
   * The Search criteria list.
   */
  List<SearchCriteria> searchCriteriaList;
}
