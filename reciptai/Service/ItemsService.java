package com.trainee.reciptai.Service;

import com.trainee.reciptai.DTO.ReceiptItemDTO;
import com.trainee.reciptai.DTO.ShoppingItem;
import com.trainee.reciptai.model.ShoppingItemEntity;
import com.trainee.reciptai.Repository.ItemsRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemsService {

    private final ItemsRepo itemsRepo;

    public ItemsService(ItemsRepo itemsRepo) {
        this.itemsRepo = itemsRepo;
    }

    /**
     * Sprint 1: Manual/Voice Entry logic
     */
    public ResponseEntity<String> receiveItems(ShoppingItem dto) {
        ShoppingItemEntity entity = new ShoppingItemEntity();
        entity.setItemName(dto.getItemName());
        entity.setQuantity(dto.getQuantity());
        entity.setPricePerUnit(dto.getPricePerUnit());
        entity.setTaxPerUnit(dto.getTaxPerUnit());
        entity.setChecked(false); // Initial plan is unchecked

        double total = (dto.getQuantity() * dto.getPricePerUnit()) + dto.getTaxPerUnit();
        entity.setTotalAmount(total);

        itemsRepo.save(entity);
        return new ResponseEntity<>("Success: Item saved to Azure shopping list!", HttpStatus.CREATED);
    }

    public List<ShoppingItemEntity> getAllItems() {
        return itemsRepo.findAll();
    }

    /**
     * Sprint 2/3: Cross-check logic (Fuzzy match by name)
     */
    public void markItemAsChecked(String aiItemName) {
        itemsRepo.findByItemNameContainingIgnoreCase(aiItemName).ifPresent(entity -> {
            entity.setChecked(true);
            itemsRepo.save(entity);
            System.out.println("Auto-checked item: " + entity.getItemName());
        });
    }

    /**
     * Sprint 3: Detailed Update from Receipt
     * Updates an existing planned item with actual prices from AI.
     */
    public void updateItemFromReceipt(ReceiptItemDTO aiItem) {
        itemsRepo.findByItemNameContainingIgnoreCase(aiItem.getName()).ifPresent(entity -> {
            entity.setChecked(true);
            entity.setQuantity(aiItem.getQuantity());
            entity.setPricePerUnit(aiItem.getPricePerUnit());
            entity.setTaxPerUnit(aiItem.getTax());

            // Use the AI extracted line total for the final amount
            entity.setTotalAmount(aiItem.getTotalPrice());

            itemsRepo.save(entity);
            System.out.println("Updated pricing for: " + entity.getItemName());
        });
    }

    /**
     * Sprint 4: Full Receipt Import
     * Saves a brand new entry for every item found on a receipt.
     */
    public ShoppingItemEntity saveExtractedItem(ReceiptItemDTO dto) {
        ShoppingItemEntity entity = new ShoppingItemEntity();
        entity.setItemName(dto.getName());
        entity.setQuantity(dto.getQuantity());
        entity.setPricePerUnit(dto.getPricePerUnit());
        entity.setTotalAmount(dto.getTotalPrice());
        entity.setTaxPerUnit(dto.getTax());
        entity.setChecked(true); // Receipt items are confirmed purchases

        return itemsRepo.save(entity);
    }

    /**
     * Sprint 4: Financial Summary (Rupee Standard)
     */
    public Double calculateTotalMonthlyExpenditure() {
        return itemsRepo.findAll().stream()
                .filter(ShoppingItemEntity::isChecked)
                .mapToDouble(ShoppingItemEntity::getTotalAmount)
                .sum();
    }

    /**
     * Developer Tool: Clear Database
     */
    public ResponseEntity<String> clearAllData() {
        itemsRepo.deleteAll();
        return new ResponseEntity<>("All shopping data has been cleared successfully.", HttpStatus.OK);
    }
}