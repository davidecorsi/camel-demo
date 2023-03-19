package it.partec.cameldemo.route;

import it.partec.cameldemo.configuration.AbstractContainerBaseTest;
import it.partec.cameldemo.dto.PaymentDto;
import it.partec.cameldemo.model.Payment;
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
import org.springframework.jdbc.core.JdbcTemplate;
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

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @AfterEach
  void cleanup() {
    jdbcTemplate.execute("truncate payment");
  }

  @Test
  @DisplayName("Test dell'endpoint per leggere i dati sul database")
  void getTest() throws Exception {
    Payment payment = Payment.builder()
        .idPayment(1L)
        .name("Rebecca")
        .surname("Trentino")
        .build();

    jdbcTemplate.execute("insert into payment (id_payment, name, surname) VALUES (" +
            "'" + payment.getIdPayment() + "', " +
            "'" + payment.getName() + "', " +
            "'" + payment.getSurname() + "'" +
            ")");

    ResponseEntity<Payment[]> paymentDtoArray = restTemplate.getForEntity("/payments", Payment[].class);
    Assertions.assertEquals(1, paymentDtoArray.getBody().length);
  }
}
