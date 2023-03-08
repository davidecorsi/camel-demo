package it.partec.cameldemo.integration;

import it.partec.cameldemo.configuration.AbstractContainerBaseTest;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@CamelSpringBootTest
@SpringBootTest
@DirtiesContext
@EnableRouteCoverage
class IntegrationTests extends AbstractContainerBaseTest {

  @Value("classpath:test.csv")
  private Resource inputFile;

  @Autowired
  private ProducerTemplate producerTemplate;

  @Test
  void integrationTest() throws InterruptedException, IOException {
    Thread.sleep(10000);
    FTPClient client = new FTPClient();

    client.connect("localhost");
    client.login("test", "1234");
    client.enterLocalPassiveMode();
    client.storeFile("input/" + inputFile.getFilename(), inputFile.getInputStream());
    client.logout();

    Thread.sleep(10000);
    client.connect("localhost");
    client.login("test", "1234");
    client.enterLocalPassiveMode();
    Assertions.assertEquals(2, client.listDirectories().length);

    ArrayList<LinkedHashMap<String, Object>> count = producerTemplate.requestBody("jdbc:dataSource",
        "select count(*) from PAYMENT", ArrayList.class);
    Assertions.assertEquals("2", count.get(0).get("count(*)").toString());
    producerTemplate.sendBody("jdbc:dataSource", "truncate PAYMENT");
  }
}
