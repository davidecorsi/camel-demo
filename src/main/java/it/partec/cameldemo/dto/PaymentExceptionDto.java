package it.partec.cameldemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ";")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentExceptionDto {

  @DataField(pos = 1)
  private Long idPayment;
  @DataField(pos = 2)
  private String name;
  @DataField(pos = 3)
  private String surname;
  @DataField(pos = 4)
  private boolean processed;
  @DataField(pos = 5)
  private String exception;

  public PaymentExceptionDto(PaymentDto paymentDto, String exception) {
    this.idPayment = paymentDto.getIdPayment();
    this.name = paymentDto.getName();
    this.surname = paymentDto.getSurname();
    this.processed = paymentDto.isProcessed();
    this.exception = exception;
  }
}
