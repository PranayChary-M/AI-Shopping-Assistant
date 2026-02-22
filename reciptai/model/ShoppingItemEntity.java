package com.trainee.reciptai.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "shopping_list")
@Data
public class ShoppingItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "item_name")
    private String itemName;
    private int quantity;
    private double pricePerUnit;
    private double taxPerUnit;
    private double totalAmount; // This is what your service calculates
    private boolean isChecked = false; // Default state for new items
}