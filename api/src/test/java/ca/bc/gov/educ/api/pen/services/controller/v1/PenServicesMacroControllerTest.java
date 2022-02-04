package ca.bc.gov.educ.api.pen.services.controller.v1;

import ca.bc.gov.educ.api.pen.services.PenServicesApiResourceApplication;
import ca.bc.gov.educ.api.pen.services.mapper.v1.PenServicesMacroMapper;
import ca.bc.gov.educ.api.pen.services.model.PenServicesMacroTypeCodeEntity;
import ca.bc.gov.educ.api.pen.services.repository.PenServicesMacroRepository;
import ca.bc.gov.educ.api.pen.services.repository.PenServicesMacroTypeCodeRepository;
import ca.bc.gov.educ.api.pen.services.service.PenServicesMacroService;
import ca.bc.gov.educ.api.pen.services.struct.v1.PenServicesMacro;
import ca.bc.gov.educ.api.pen.services.support.TestRedisConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.v1.URL.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PenServicesApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PenServicesMacroControllerTest {

  private static final PenServicesMacroMapper mapper = PenServicesMacroMapper.mapper;
  @Autowired
  PenServicesMacroController controller;

  @Autowired
  PenServicesMacroService service;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  PenServicesMacroTypeCodeRepository penServicesMacroTypeCodeRepository;

  @Autowired
  PenServicesMacroRepository penServicesMacroRepository;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    this.penServicesMacroTypeCodeRepository.save(this.createPenServicesMacroTypeCode());
  }

  @After
  public void after() {
    this.penServicesMacroTypeCodeRepository.deleteAll();
    this.penServicesMacroRepository.deleteAll();
  }

  @Test
  public void testRetrievePenServicesMacros_ShouldReturnStatusOK() throws Exception {
    this.mockMvc.perform(get(PEN_SERVICES+PEN_SERVICES_MACRO)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_SERVICES_MACRO"))))
            .andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testRetrievePenServicesMacros_GivenInvalidMacroID_ShouldReturnStatusNotFound() throws Exception {
    this.mockMvc.perform(get(PEN_SERVICES+PEN_SERVICES_MACRO+MACRO_ID,UUID.randomUUID().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_SERVICES_MACRO"))))
            .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testRetrievePenServicesMacros_GivenValidMacroID_ShouldReturnStatusOK() throws Exception {
    val entity = mapper.toModel(this.getPenServicesMacroEntityFromJsonString());
    entity.setMacroId(null);
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    val savedEntity = this.service.createMacro(entity);
    final var result = this.mockMvc.perform(get(PEN_SERVICES+PEN_SERVICES_MACRO+MACRO_ID, savedEntity.getMacroId().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_SERVICES_MACRO"))))
            .andDo(print()).andExpect(jsonPath("$.macroId").value(entity.getMacroId().toString())).andExpect(status().isOk()).andReturn();
    assertThat(result).isNotNull();
  }

  @Test
  public void testRetrievePenServicesMacros_GivenValidMacroTypeCode_ShouldReturnStatusOK() throws Exception {
    val entity = mapper.toModel(this.getPenServicesMacroEntityFromJsonString());
    entity.setMacroId(null);
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    val savedEntity = this.service.createMacro(entity);
    final var result = this.mockMvc.perform(get(PEN_SERVICES+PEN_SERVICES_MACRO+"?macroTypeCode=" + savedEntity.getMacroTypeCode())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_SERVICES_MACRO"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    assertThat(result).isNotNull();
  }

  @Test
  public void testCreatePenServicesMacros_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
    this.mockMvc.perform(post(PEN_SERVICES+PEN_SERVICES_MACRO)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_SERVICES_MACRO")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(this.dummyPenServicesMacroJson())).andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testCreatePenServicesMacros_GivenInValidPayload_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post(PEN_SERVICES+PEN_SERVICES_MACRO)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_SERVICES_MACRO")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(this.dummyPenServicesMacroJsonWithId())).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testUpdatePenServicesMacros_GivenValidPayload_ShouldReturnStatusOK() throws Exception {
    val entity = mapper.toModel(this.getPenServicesMacroEntityFromJsonString());
    entity.setMacroId(null);
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    val savedEntity = this.service.createMacro(entity);
    savedEntity.setCreateDate(null);
    savedEntity.setUpdateDate(null);
    savedEntity.setMacroText("updated text");
    final String jsonString = new ObjectMapper().writeValueAsString(mapper.toStructure(savedEntity));
    final var result = this.mockMvc.perform(put(PEN_SERVICES+PEN_SERVICES_MACRO+MACRO_ID, savedEntity.getMacroId().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_SERVICES_MACRO")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(jsonString)).andDo(print()).andExpect(status().isOk());
    assertThat(result).isNotNull();

  }
  @Test
  public void testUpdatePenServicesMacros_GivenInValidPayload_ShouldReturnStatusNotFound() throws Exception {
    val entity = mapper.toModel(this.getPenServicesMacroEntityFromJsonString());
    entity.setMacroId(null);
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    val savedEntity = this.service.createMacro(entity);
    savedEntity.setCreateDate(null);
    savedEntity.setUpdateDate(null);
    savedEntity.setMacroText("updated text");
    final String jsonString = new ObjectMapper().writeValueAsString(mapper.toStructure(savedEntity));
    final var result = this.mockMvc.perform(put(PEN_SERVICES+PEN_SERVICES_MACRO+MACRO_ID, UUID.randomUUID().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_SERVICES_MACRO")))
            .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(jsonString)).andDo(print()).andExpect(status().isNotFound());
    assertThat(result).isNotNull();

  }

  private PenServicesMacroTypeCodeEntity createPenServicesMacroTypeCode() {
    return PenServicesMacroTypeCodeEntity.builder()
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser("TEST")
            .updateUser("TEST")
            .description("TEST")
            .displayOrder(1)
            .effectiveDate(LocalDate.now().minusDays(2))
            .expiryDate(LocalDate.now().plusDays(2))
            .label("TEST")
            .macroTypeCode("MERGE")
            .build();
  }

  protected String dummyPenServicesMacroJson() {
    return " {\n" +
            "    \"createUser\": \"om\",\n" +
            "    \"updateUser\": \"om\",\n" +
            "    \"macroCode\": \"hi\",\n" +
            "    \"macroTypeCode\": \"MERGE\",\n" +
            "    \"macroText\": \"hello\"\n" +
            "  }";
  }

  protected String dummyPenServicesMacroJsonWithId() {
    return " {\n" +
            "    \"createUser\": \"om\",\n" +
            "    \"updateUser\": \"om\",\n" +
            "    \"macroCode\": \"hi\",\n" +
            "    \"macroId\": \"7f000101-7151-1d84-8171-5187006c0000\",\n" +
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
