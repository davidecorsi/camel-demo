package it.partec.cameldemo.route;

import it.partec.cameldemo.configuration.AbstractContainerBaseTest;
import it.partec.cameldemo.dto.PaymentDto;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

@CamelSpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EnableRouteCoverage
class RestRouteTests extends AbstractContainerBaseTest {

  @Autowired
  private ProducerTemplate producerTemplate;

  @Autowired
  private TestRestTemplate restTemplate;

  @AfterEach
  void cleanup() {
    producerTemplate.sendBody("jdbc:dataSource", "truncate PAYMENT");
  }

  @Test
  @DisplayName("Test dell'endpoint per leggere i dati sul database")
  void getTest() throws Exception {
    PaymentDto paymentDto = PaymentDto.builder()
        .idPayment(1L)
        .name("Rebecca")
        .surname("Trentino")
        .processed(false)
        .build();

    producerTemplate.sendBody("jdbc:dataSource", "insert into PAYMENT (ID_PAYMENT, NAME, SURNAME) VALUES (" +
            "'" + paymentDto.getIdPayment() + "', " +
            "'" + paymentDto.getName() + "', " +
            "'" + paymentDto.getSurname() + "'" +
            ")");

    ResponseEntity<PaymentDto[]> paymentDtoArray = restTemplate.getForEntity("/payments", PaymentDto[].class);
    Assertions.assertEquals(1, paymentDtoArray.getBody().length);
  }
}
