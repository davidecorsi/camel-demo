package it.partec.cameldemo.route;

import it.partec.cameldemo.configuration.AbstractContainerBaseTest;
import it.partec.cameldemo.dto.PaymentDto;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

@CamelSpringBootTest
@SpringBootTest
@DirtiesContext
@EnableRouteCoverage
class ErrorRouteTests extends AbstractContainerBaseTest {

  @Autowired
  private ProducerTemplate producerTemplate;

  @Autowired
  private CamelContext camelContext;

  @Test
  @DisplayName("Test della rotta in caso di errore nello scodamento da kafka")
  void errorTest() throws Exception {
    PaymentDto paymentDto = PaymentDto.builder()
        .idPayment(1L)
        .name("Rebecca")
        .surname("Trentino")
        .processed(false)
        .build();

    producerTemplate.sendBodyAndHeader("direct:error", paymentDto, "FailedBecause", "Eccezione");

    NotifyBuilder notify = new NotifyBuilder(camelContext)
        .from("direct:error").whenDone(1)
        .create();

    boolean done = notify.matches(30, TimeUnit.SECONDS);
    Assertions.assertTrue(done);

    File outputDir = new File("output");
    Assertions.assertTrue(outputDir.exists());
    Assertions.assertTrue(FileSystemUtils.deleteRecursively(outputDir));
  }
}
