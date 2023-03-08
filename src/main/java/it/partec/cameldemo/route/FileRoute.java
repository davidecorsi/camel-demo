package it.partec.cameldemo.route;

import it.partec.cameldemo.aggregation.ArrayListAggregationStrategy;
import it.partec.cameldemo.dto.PaymentDto;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class FileRoute extends RouteBuilder {

  @Override
  public void configure() {

    from("ftp://127.0.0.1:21/input" +
        "?passiveMode=true" +
        "&username=test" +
        "&password=1234" +
        "&noop=true" +
        "&autoCreate=true" +
        "&include=.*.csv" +
        "&initialDelay=10000" +
        "&delete=true"
    )
        .routeId("fileRoute")
        .unmarshal(new BindyCsvDataFormat(PaymentDto.class))
        .log("Lettura del file ${header.CamelFileName}")
        .split().body()
        .choice()
        .when().simple("${body.processed} == true")
          .setHeader(KafkaConstants.KEY, constant("Camel"))
          .marshal().json(JsonLibrary.Jackson)
          .to("kafka:payment")
        .otherwise()
          .aggregate(header("CamelFileName"), new ArrayListAggregationStrategy())
          .completionTimeout(10)
          .marshal(new BindyCsvDataFormat(PaymentDto.class))
          .setHeader("CamelOverruleFileName", simple("/output${date:now:yyyyMMdd}.csv"))
          .to("ftp://127.0.0.1:21/output" +
              "?passiveMode=true" +
              "&username=test" +
              "&password=1234" +
              "&autoCreate=true");
  }
}
