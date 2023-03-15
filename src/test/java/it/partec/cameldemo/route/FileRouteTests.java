package it.partec.cameldemo.route;

import it.partec.cameldemo.configuration.AbstractContainerBaseTest;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.TimeUnit;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@MockEndpointsAndSkip("kafka:.*|ftp://127.0.0.1:21/output.*")
@DirtiesContext
public class FileRouteTests extends AbstractContainerBaseTest {

  @Value("classpath:test.csv")
  private Resource inputFile;

  @EndpointInject(uri = "mock:kafka:payment")
  private MockEndpoint kafkaMockEndpoint;

  @EndpointInject(uri = "mock:ftp:127.0.0.1:21/output")
  private MockEndpoint ftpMock;

  @Autowired
  private CamelContext camelContext;

  @Test
  public void routeTest() throws Exception {
    FTPClient client = new FTPClient();

    client.connect("localhost");
    client.login("test", "1234");
    client.enterLocalPassiveMode();
    client.storeFile("input/" + inputFile.getFilename(), inputFile.getInputStream());
    client.logout();

    NotifyBuilder notify = new NotifyBuilder(camelContext)
        .whenDone(1)
        .create();

    boolean done = notify.matches(20, TimeUnit.SECONDS);
    Assert.assertTrue(done);

    kafkaMockEndpoint.expectedMessageCount(2);
    kafkaMockEndpoint.setAssertPeriod(20000);
    kafkaMockEndpoint.assertIsSatisfied();

    ftpMock.expectedMessageCount(1);
    ftpMock.setAssertPeriod(20000);
    ftpMock.assertIsSatisfied();
  }
}
