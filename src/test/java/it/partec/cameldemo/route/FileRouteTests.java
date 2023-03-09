package it.partec.cameldemo.route;

import it.partec.cameldemo.configuration.AbstractContainerBaseTest;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.apache.camel.test.spring.junit5.MockEndpointsAndSkip;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.TimeUnit;

@CamelSpringBootTest
@SpringBootTest
@MockEndpointsAndSkip("kafka:.*|ftp://127.0.0.1:21/output.*")
@DirtiesContext
@EnableRouteCoverage
class FileRouteTests extends AbstractContainerBaseTest {

  @Value("classpath:test.csv")
  private Resource inputFile;

  @EndpointInject("mock:kafka:payment")
  private MockEndpoint kafkaMock;

  @EndpointInject("mock:ftp:127.0.0.1:21/output")
  private MockEndpoint ftpMock;

  @Autowired
  private CamelContext camelContext;

  @Test
  @DisplayName("Test di lettura dal server ftp ed inserimento nella coda kafka")
  void routeTest() throws Exception {
    FTPClient client = new FTPClient();

    client.connect("localhost");
    client.login("test", "1234");
    client.enterLocalPassiveMode();
    client.storeFile("input/" + inputFile.getFilename(), inputFile.getInputStream());
    client.logout();

    NotifyBuilder notify = new NotifyBuilder(camelContext)
        .from("ftp://127.0.0.1:21/input" +
            "?passiveMode=true" +
            "&username=test" +
            "&password=1234" +
            "&noop=true" +
            "&autoCreate=true" +
            "&include=.*.csv" +
            "&initialDelay=10000" +
            "&delete=true").whenDone(1)
        .create();

    boolean done = notify.matches(20, TimeUnit.SECONDS);
    Assertions.assertTrue(done);

    kafkaMock.expectedMessageCount(2);
    kafkaMock.setAssertPeriod(20000);
    kafkaMock.assertIsSatisfied();

    ftpMock.expectedMessageCount(1);
    ftpMock.setAssertPeriod(20000);
    ftpMock.assertIsSatisfied();
  }
}
