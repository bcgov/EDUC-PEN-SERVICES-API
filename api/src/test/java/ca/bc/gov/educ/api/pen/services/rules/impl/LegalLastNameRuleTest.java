package ca.bc.gov.educ.api.pen.services.rules.impl;

import ca.bc.gov.educ.api.pen.services.model.PENNameText;
import ca.bc.gov.educ.api.pen.services.service.PENNameTextService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenRequestStudentValidationPayload;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * The type Legal last name rule test.
 */
@RunWith(JUnitParamsRunner.class)
public class LegalLastNameRuleTest {
  private LegalLastNameRule rule;
  private static List<PENNameText> penNameTexts;
  /**
   * The Service.
   */
  @Mock
  PENNameTextService service;

  /**
   * Sets .
   *
   * @throws IOException the io exception
   */
  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    this.rule = new LegalLastNameRule(this.service);
    if (penNameTexts == null) {
      final File file = new File(
        Objects.requireNonNull(this.getClass().getClassLoader().getResource("pen_names_text_sample.json")).getFile()
      );
      penNameTexts = new ObjectMapper().readValue(file, new TypeReference<>() {
      });
    }
  }

  /**
   * Test validate given different legal last name should return results.
   *
   * @param legalLastName  the legal last name
   * @param expectedErrors the expected errors
   */
  @Test
  @Parameters({
    "null, 1",
    ", 1",
    "JJ, 1",
    "JJXXY, 0",
    "JJJ, 1",
    "JJJJ, 1",
    "XX, 1",
    "ZZ, 1",
    "BLANK, 1",
    "AVAILABLE, 1",
    "ABC, 1",
    "mishra, 0",
    "ALIAS, 1",
    "ASD, 1",
    "BITCH,1",
    "BRING, 1",
    "CASH ONLY, 0",
    "DATA, 1",
    "DOE, 1",
    "DOESN'T HAVE ONE, 1",
    "DS TAM, 1",
    "FICTITIOUS, 1",
    "FANCYPANTS, 1",
    "ESTATE, 1",
    "DUMMY, 1",
    "DUPLICATE, 1"
  })
  public void testValidate_givenDifferentLegalLastName_shouldReturnResults(String legalLastName, final int expectedErrors) {
    if ("null".equals(legalLastName)) {
      legalLastName = null;
    }
    when(this.service.getPenNameTexts()).thenReturn(penNameTexts);
    final PenRequestStudentValidationPayload payload = PenRequestStudentValidationPayload.builder().isInteractive(false).transactionID(UUID.randomUUID().toString()).legalLastName(legalLastName).build();
    final var result = this.rule.validate(payload);
    assertThat(result).size().isEqualTo(expectedErrors);
  }
}
