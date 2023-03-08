package it.partec.cameldemo.route;

import it.partec.cameldemo.configuration.AbstractContainerBaseTest;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.apache.camel.test.spring.junit5.MockEndpointsAndSkip;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;

@CamelSpringBootTest
@SpringBootTest
@MockEndpointsAndSkip("kafka:.*")
@DirtiesContext
@EnableRouteCoverage
class FileRouteTests extends AbstractContainerBaseTest {

  @Value("classpath:test.csv")
  private Resource inputFile;

  @EndpointInject("mock:kafka:payment")
  private MockEndpoint kafkaMock;

  @Test
  void routeTest() throws Exception {
    Thread.sleep(10000);
    FTPClient client = new FTPClient();

    client.connect("localhost");
    client.login("test", "1234");
    client.enterLocalPassiveMode();
    client.storeFile("input/" + inputFile.getFilename(), inputFile.getInputStream());
    client.logout();

    kafkaMock.expectedMessageCount(2);
    kafkaMock.setAssertPeriod(20000);
    kafkaMock.assertIsSatisfied();
  }
}
