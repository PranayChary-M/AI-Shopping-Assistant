package com.trainee.reciptai.DTO;

import lombok.Data;

@Data
public class ShoppingItem {
    private String itemName;
    private int quantity;
    private double pricePerUnit;
    private double taxPerUnit;
}