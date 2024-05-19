package it.partec.cameldemo.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.partec.cameldemo.configuration.AbstractContainerBaseTest;
import it.partec.cameldemo.dto.PaymentDto;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.apache.camel.test.spring.junit5.MockEndpointsAndSkip;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.ContainerState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

@CamelSpringBootTest
@SpringBootTest
@MockEndpointsAndSkip("direct:error")
@DirtiesContext
@EnableRouteCoverage
class KafkaRouteTests extends AbstractContainerBaseTest {

  @Autowired
  private ProducerTemplate producerTemplate;

  @Autowired
  private ConsumerTemplate consumerTemplate;

  @Autowired
  private CamelContext camelContext;

  @EndpointInject("mock:direct:error")
  private MockEndpoint errorMock;

  @Test
  @DisplayName("Test dello scodamento da kafka ed inserimento nel database")
  void insertDbTest() throws Exception {
    PaymentDto paymentDto = PaymentDto.builder()
        .idPayment(1L)
        .name("Rebecca")
        .surname("Trentino")
        .processed(false)
        .build();

    producerTemplate.sendBody("kafka:payment", new ObjectMapper().writeValueAsString(paymentDto));

    NotifyBuilder notify = new NotifyBuilder(camelContext)
        .from("kafka:payment?groupId=test").whenDone(1)
        .create();

    boolean done = notify.matches(10, TimeUnit.SECONDS);
    Assertions.assertTrue(done);
    ArrayList<LinkedHashMap<String, Object>> count = producerTemplate.requestBody("jdbc:dataSource",
        "select count(*) from PAYMENT", ArrayList.class);
    Assertions.assertEquals("1", count.get(0).get("count(*)").toString());
    producerTemplate.sendBody("jdbc:dataSource", "truncate PAYMENT");
  }

  @Test
  @DisplayName("Test dead letter channel in caso di errore nell'inserimento dei dati sul database")
  void deadMessageTest() throws JsonProcessingException {
    ContainerState mysql = (ContainerState) environment.getContainerByServiceName("db").get();
    String id = mysql.getContainerId();
    DockerClientFactory.lazyClient().stopContainerCmd(id).exec();

    PaymentDto paymentDto = PaymentDto.builder()
        .idPayment(1L)
        .name("Rebecca")
        .surname("Trentino")
        .processed(false)
        .build();

    producerTemplate.sendBody("kafka:payment", new ObjectMapper().writeValueAsString(paymentDto));

    NotifyBuilder notify = new NotifyBuilder(camelContext)
        .from("kafka:payment?groupId=test").whenDone(1)
        .create();

    boolean done = notify.matches(10, TimeUnit.SECONDS);
    Assertions.assertTrue(done);

    errorMock.expectedMessageCount(1);
    errorMock.setAssertPeriod(10);

    environment.start();
  }
}
