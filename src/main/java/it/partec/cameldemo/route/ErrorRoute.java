package it.partec.cameldemo.route;

import it.partec.cameldemo.aggregation.ArrayListAggregationStrategy;
import it.partec.cameldemo.dto.PaymentDto;
import it.partec.cameldemo.dto.PaymentExceptionDto;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.springframework.stereotype.Component;

@Component
public class ErrorRoute extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    from("direct:error")
        .routeId("error")
        .log("Il pagamento con id ${body.idPayment} non è stato inserito")
        .process(exchange -> {
          PaymentDto paymentDto = exchange.getMessage().getBody(PaymentDto.class);
          PaymentExceptionDto paymentExceptionDto = new PaymentExceptionDto(paymentDto,
              exchange.getMessage().getHeader("FailedBecause").toString().replace("\n", " "));
          exchange.getMessage().setBody(paymentExceptionDto);
        })
        .aggregate(constant(true), new ArrayListAggregationStrategy())
        .completionTimeout(10000)
        .marshal(new BindyCsvDataFormat(PaymentExceptionDto.class))
        .setHeader("CamelOverruleFileName", simple("/output${date:now:yyyyMMdd}.csv"))
        .to("file:output");
  }
}
