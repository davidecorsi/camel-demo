package it.partec.cameldemo.configuration;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

public abstract class AbstractContainerBaseTest {

  public static final DockerComposeContainer environment;

  static {
    environment = new DockerComposeContainer(new File("docker-compose.yml"))
        .waitingFor("db_1", Wait.forLogMessage(".*3306.*", 1))
        .withLocalCompose(true);
    environment.start();
  }
}
