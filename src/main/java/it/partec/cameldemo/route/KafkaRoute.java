package it.partec.cameldemo.route;

import it.partec.cameldemo.dto.PaymentDto;
import it.partec.cameldemo.model.Payment;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.sql.SQLIntegrityConstraintViolationException;

@Component
public class KafkaRoute extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    onException(SQLIntegrityConstraintViolationException.class)
        .log("Il pagamento con id ${header.idPayment} è gia stato inserito")
        .handled(true);

    errorHandler(new DeadLetterChannelBuilder("direct:error")
        .useOriginalMessage()
        .onPrepareFailure(exchange -> {
          Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
          exchange.getIn().setHeader("FailedBecause", cause.getMessage());
        })
        .maximumRedeliveries(3)
        .redeliveryDelay(1000));

    from("kafka:payment?groupId=test")
        .routeId("kafkaRoute")
        .log("processamento del messaggio ${body}")
        .setHeader("idPayment", simple("${body.idPayment}"))
        .process(exchange -> {
          PaymentDto paymentDto = exchange.getMessage().getBody(PaymentDto.class);
          exchange.getMessage().setBody(new Payment(paymentDto));
        })
        .to("jpa:it.partec.cameldemo.model.Payment?entityType=java.util.ArrayList&persistenceUnit=mysql");
  }
}
