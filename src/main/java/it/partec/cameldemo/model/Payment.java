package it.partec.cameldemo.model;

import it.partec.cameldemo.dto.PaymentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Payment implements Serializable {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "id_payment")
  private Long idPayment;

  @Column(name = "name")
  private String name;

  @Column(name = "surname")
  private String surname;

  public Payment(PaymentDto paymentDto) {
    this.id = paymentDto.getIdPayment();
    this.name = paymentDto.getName();
    this.surname = paymentDto.getSurname();
  }
}
