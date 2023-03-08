package it.partec.cameldemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ";")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

  @DataField(pos = 1)
  private Long idPayment;
  @DataField(pos = 2)
  private String name;
  @DataField(pos = 3)
  private String surname;
  @DataField(pos = 4)
  private boolean processed;
}
