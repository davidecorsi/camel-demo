package it.partec.cameldemo.integration;

import it.partec.cameldemo.configuration.AbstractContainerBaseTest;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

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
  @DisplayName("Test di integrazione che segue il normale flusso con successo")
  void integrationTest() throws IOException {
    FTPClient client = new FTPClient();

    client.connect("localhost");
    client.login("test", "1234");
    client.enterLocalPassiveMode();
    client.storeFile("input/" + inputFile.getFilename(), inputFile.getInputStream());
    client.logout();

    await().atMost(20, TimeUnit.SECONDS).until(() -> {
      ArrayList<LinkedHashMap<String, Object>> count = producerTemplate.requestBody("jdbc:dataSource",
          "select count(*) from PAYMENT", ArrayList.class);
      return "0".equals(count.get(0).get("count(*)").toString());
    });

    await().atMost(20, TimeUnit.SECONDS).until(() -> {
      client.connect("localhost");
      client.login("test", "1234");
      client.enterLocalPassiveMode();
      int size = client.listDirectories().length;
      client.logout();
      return 2 == size;
    });

    await().atMost(50, TimeUnit.SECONDS).until(() -> {
      ArrayList<LinkedHashMap<String, Object>> count = producerTemplate.requestBody("jdbc:dataSource",
          "select count(*) from PAYMENT", ArrayList.class);
      producerTemplate.sendBody("jdbc:dataSource", "truncate PAYMENT");
      return "2".equals(count.get(0).get("count(*)").toString());
    });
  }
}
