package it.partec.cameldemo.route;

import it.partec.cameldemo.configuration.AbstractContainerBaseTest;
import it.partec.cameldemo.dto.PaymentDto;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class RestRouteTests extends AbstractContainerBaseTest {

  @Autowired
  private ProducerTemplate producerTemplate;

  @Autowired
  private TestRestTemplate restTemplate;

  @After
  public void cleanup() {
    producerTemplate.sendBody("jdbc:dataSource", "truncate PAYMENT");
  }

  @Test
  public void getTest() throws Exception {
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
    Assert.assertEquals(1, paymentDtoArray.getBody().length);
  }
}
