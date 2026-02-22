package com.trainee.reciptai.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptItemDTO {
    private String name;
    private Integer quantity;
    private Double pricePerUnit;
    private Double totalPrice;
    private Double tax;
}