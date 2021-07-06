package ca.bc.gov.educ.api.pen.services.validator;

import ca.bc.gov.educ.api.pen.services.PenServicesApiResourceApplication;
import ca.bc.gov.educ.api.pen.services.model.PenServicesMacroTypeCodeEntity;
import ca.bc.gov.educ.api.pen.services.repository.PenServicesMacroRepository;
import ca.bc.gov.educ.api.pen.services.repository.PenServicesMacroTypeCodeRepository;
import ca.bc.gov.educ.api.pen.services.service.PenServicesMacroService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenServicesMacro;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PenServicesApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PenServicesMacroPayloadValidatorTest {

  @Autowired
  PenServicesMacroTypeCodeRepository penServicesMacroTypeCodeRepository;

  @Mock
  PenServicesMacroRepository penServicesMacroRepository;

  @Autowired
  PenServicesMacroService penServicesMacroService;
  @InjectMocks
  PenServicesMacroPayloadValidator penServicesMacroPayloadValidator;

  @Before
  public void before() {
    this.penServicesMacroTypeCodeRepository.deleteAll();
    this.penServicesMacroService = new PenServicesMacroService(this.penServicesMacroRepository, this.penServicesMacroTypeCodeRepository);
    this.penServicesMacroPayloadValidator = new PenServicesMacroPayloadValidator(this.penServicesMacroService);
  }

  @Test
  public void testValidatePayload_WhenMacroIdGivenForPost_ShouldAddAnErrorTOTheReturnedList() {
    val errorList = this.penServicesMacroPayloadValidator.validatePayload(this.getPenServicesMacroEntityFromJsonString(), true);
    assertEquals(2, errorList.size());
    assertEquals("macroId should be null for post operation.", errorList.get(0).getDefaultMessage());
  }
  @Test
  public void testValidatePayload_WhenMacroTypeCodeIsInvalid_ShouldAddAnErrorTOTheReturnedList() {
    val entity = this.getPenServicesMacroEntityFromJsonString();
    entity.setMacroId(null);
    val errorList = this.penServicesMacroPayloadValidator.validatePayload(entity, true);
    assertEquals(1, errorList.size());
    assertEquals("macroTypeCode Invalid.", errorList.get(0).getDefaultMessage());
  }

  @Test
  public void testValidatePayload_WhenMacroTypeCodeIsNotEffective_ShouldAddAnErrorTOTheReturnedList() {
    var macroTypeCode = this.createPenServicesMacroTypeCode();
    macroTypeCode.setEffectiveDate(LocalDate.MAX);
    this.penServicesMacroTypeCodeRepository.save(macroTypeCode);
    val entity = this.getPenServicesMacroEntityFromJsonString();
    val errorList = this.penServicesMacroPayloadValidator.validatePayload(entity, false);
    assertEquals(1, errorList.size());
    assertEquals("macroTypeCode is not yet effective.", errorList.get(0).getDefaultMessage());
  }
  @Test
  public void testValidatePayload_WhenMacroTypeCodeIsExpired_ShouldAddAnErrorTOTheReturnedList() {
    var macroTypeCode = this.createPenServicesMacroTypeCode();
    macroTypeCode.setEffectiveDate(LocalDate.now());
    macroTypeCode.setExpiryDate(LocalDate.now().minusDays(1));
    this.penServicesMacroTypeCodeRepository.save(macroTypeCode);
    val entity = this.getPenServicesMacroEntityFromJsonString();
    val errorList = this.penServicesMacroPayloadValidator.validatePayload(entity, false);
    assertEquals(1, errorList.size());
    assertEquals("macroTypeCode is expired.", errorList.get(0).getDefaultMessage());
  }

  private PenServicesMacroTypeCodeEntity createPenServicesMacroTypeCode() {
    return PenServicesMacroTypeCodeEntity.builder()
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser("TEST")
            .updateUser("TEST")
            .description("TEST")
            .displayOrder(1)
            .effectiveDate(LocalDate.MIN)
            .expiryDate(LocalDate.MAX)
            .label("TEST")
            .macroTypeCode("MERGE")
            .build();
  }

  protected String dummyPenServicesMacroJson() {
    return " {\n" +
            "    \"createUser\": \"om\",\n" +
            "    \"updateUser\": \"om\",\n" +
            "    \"macroId\": \"7f000101-7151-1d84-8171-5187006c0000\",\n" +
            "    \"macroCode\": \"hi\",\n" +
            "    \"macroTypeCode\": \"MERGE\",\n" +
            "    \"macroText\": \"hello\"\n" +
            "  }";
  }

  protected PenServicesMacro getPenServicesMacroEntityFromJsonString() {
    try {
      return new ObjectMapper().readValue(this.dummyPenServicesMacroJson(), PenServicesMacro.class);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
