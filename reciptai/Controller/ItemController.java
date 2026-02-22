package com.trainee.reciptai.Controller;

import com.trainee.reciptai.DTO.ReceiptItemDTO;
import com.trainee.reciptai.DTO.ShoppingItem;
import com.trainee.reciptai.Service.ReceiptService;
import com.trainee.reciptai.Service.SpeechService;
import com.trainee.reciptai.model.ShoppingItemEntity;
import com.trainee.reciptai.Service.ItemsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemsService itemsService;
    private final SpeechService speechService;
    private final ReceiptService receiptService;

    public ItemController(ItemsService itemsService, SpeechService speechService, ReceiptService receiptService) {
        this.itemsService = itemsService;
        this.speechService = speechService;
        this.receiptService = receiptService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createItem(@RequestBody ShoppingItem dto) {
        return itemsService.receiveItems(dto);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearData() {
        return itemsService.clearAllData();
    }

    @GetMapping("/all")
    public List<ShoppingItemEntity> getAllItems() {
        return itemsService.getAllItems();
    }

    @PostMapping("/voice-add")
    public ResponseEntity<String> addByVoice() throws Exception {
        String voiceItemName = speechService.captureItemByVoice();

        if (voiceItemName.startsWith("Error")) {
            return ResponseEntity.badRequest().body(voiceItemName);
        }

        ShoppingItem voiceDto = new ShoppingItem();
        voiceDto.setItemName(voiceItemName);
        voiceDto.setQuantity(1);
        voiceDto.setPricePerUnit(0.0);
        voiceDto.setTaxPerUnit(0.0);

        return itemsService.receiveItems(voiceDto);
    }

    /**
     * UPDATED: Returns List<ReceiptItemDTO> for cleaner JSON in Postman.
     * Use this to update existing "checked" status and prices on your list.
     */
    @PostMapping("/upload-receipt")
    public ResponseEntity<List<ReceiptItemDTO>> uploadReceipt(@RequestParam("file") MultipartFile file) throws Exception {
        List<ReceiptItemDTO> itemsFromAI = receiptService.extractItemsFromReceipt(file);

        for (ReceiptItemDTO aiItem : itemsFromAI) {
            itemsService.updateItemFromReceipt(aiItem);
        }

        return ResponseEntity.ok(itemsFromAI);
    }

    @GetMapping("/summary")
    public ResponseEntity<String> getBudgetSummary() {
        Double totalSpent = itemsService.calculateTotalMonthlyExpenditure();

        String report = String.format(
                "--- Smart Shopping Monthly Report ---\n" +
                        "Total Amount Spent: ₹%.2f\n" +
                        "Status: All receipt data processed and synced to Azure PostgreSQL.",
                totalSpent
        );

        return ResponseEntity.ok(report);
    }

    /**
     * Use this to ignore your list and just save EVERYTHING the AI sees as new rows.
     */
    @PostMapping("/upload-receipt-full")
    public ResponseEntity<List<ShoppingItemEntity>> uploadReceiptFull(@RequestParam("file") MultipartFile file) throws Exception {
        List<ReceiptItemDTO> extractedDtos = receiptService.extractItemsFromReceipt(file);
        List<ShoppingItemEntity> savedEntries = new ArrayList<>();

        for (ReceiptItemDTO dto : extractedDtos) {
            ShoppingItemEntity entity = itemsService.saveExtractedItem(dto);
            savedEntries.add(entity);
        }

        return ResponseEntity.ok(savedEntries);
    }
}