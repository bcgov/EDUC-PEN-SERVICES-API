package ca.bc.gov.educ.api.pen.services.rest;

import ca.bc.gov.educ.api.pen.services.struct.Student;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("testWebclient")
public class RestUtilsTest {

  @Autowired
  RestUtils restUtils;

  @Autowired
  WebClient webClient;
  @Mock
  private WebClient.RequestHeadersSpec requestHeadersMock;
  @Mock
  private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
  @Mock
  private WebClient.RequestBodySpec requestBodyMock;
  @Mock
  private WebClient.RequestBodyUriSpec requestBodyUriMock;
  @Mock
  private WebClient.ResponseSpec responseMock;

  @Before
  public void setUp() throws Exception {
    openMocks(this);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetLatestPenNumberFromStudentAPI_givenAPICallSuccess_shouldNotReturnZERO() throws JsonProcessingException {
    final String transactionID = UUID.randomUUID().toString();
    when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
    when(this.requestHeadersUriMock.uri("http://abcxyz.com/paginated?searchCriteriaList=%5B%7B%22condition%22:null,%22searchCriteriaList%22:%5B%7B%22key%22:%22pen%22,%22operation%22:%22starts_with%22,%22value%22:%221%22,%22valueType%22:%22STRING%22,%22condition%22:null%7D%5D%7D%5D&pageSize=1&sort=%7B%22pen%22:%22DESC%22%7D")).thenReturn(this.requestHeadersMock);
    when(this.requestHeadersMock.header(any(), any())).thenReturn(this.requestHeadersMock);
    when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
    final ParameterizedTypeReference<RestPageImpl<Student>> responseType = new ParameterizedTypeReference<>() {
    };
    when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(this.createPLaceHolderStudent()));
    val result = this.restUtils.getLatestPenNumberFromStudentAPI(transactionID);
    assertThat(result).isNotZero();
  }

  private RestPageImpl<Student> createPLaceHolderStudent() {
    final Student student = new Student();
    student.setPen("123456789");
    final List<Student> studentList = new ArrayList<>();
    studentList.add(student);
    return new RestPageImpl<>(studentList);
  }
}
