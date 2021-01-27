package ca.bc.gov.educ.api.pen.services.constants;

import lombok.Getter;

public enum StudentMergeDirectionCodes {
  /**
   * Merged from
   * The Student record was merged to the other record. The Student record was merged to the other record. This Student record is deprecated as a result of a merge.
   */
  FROM("FROM"),
  /**
   * Merged to
   * The Student record was the target of, or survivor of the merge. Also referred to as the True PEN.
   */
  TO("TO");

  /**
   * The Code.
   */
  @Getter
  private final String code;

  /**
   * Instantiates a new student merge direction code.
   *
   * @param code the code
   */
  StudentMergeDirectionCodes(String code) {
    this.code = code;
  }
}
