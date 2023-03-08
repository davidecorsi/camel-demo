package it.partec.cameldemo.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.partec.cameldemo.dto.PaymentDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;

@Slf4j
public class KafkaDeserializer implements Deserializer<PaymentDto> {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public PaymentDto deserialize(String topic, byte[] data) {
    try {
      return objectMapper.readValue(new String(data, StandardCharsets.UTF_8), PaymentDto.class);
    } catch (Exception e) {
      log.error("Errore nella deserializzazione del messaggio kafka", e);
      return null;
    }
  }
}
