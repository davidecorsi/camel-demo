package it.partec.cameldemo.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class RestRoute extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    restConfiguration()
        .component("servlet").bindingMode(RestBindingMode.json)
        .port(8080);

    rest("/payments")
        .get("/")
        .routeId("getPayments")
        .to("direct:getPayments");

    from("direct:getPayments")
        .routeId("directGetPayments")
        .log("Richiesta lista pagamenti")
        .to("jpa:it.partec.cameldemo.model.Payment?query=select p from it.partec.cameldemo.model.Payment p&consumeDelete=false&persistenceUnit=mysql");
  }
}
