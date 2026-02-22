package com.trainee.reciptai.Service;

import com.azure.ai.formrecognizer.documentanalysis.*;
import com.azure.ai.formrecognizer.documentanalysis.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import com.trainee.reciptai.DTO.ReceiptItemDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReceiptService {

    @Value("${azure.document.intelligence.key}")
    private String key;

    @Value("${azure.document.intelligence.endpoint}")
    private String endpoint;

    public List<ReceiptItemDTO> extractItemsFromReceipt(MultipartFile file) throws IOException {
        // 1. Re-initialize Client
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .buildClient();

        // 2. Prepare Data (Restored byte array logic to avoid length error)
        byte[] fileBytes = file.getBytes();
        BinaryData receiptData = BinaryData.fromBytes(fileBytes);

        // 3. Start Analysis
        SyncPoller<OperationResult, AnalyzeResult> analyzeReceiptPoller =
                client.beginAnalyzeDocument("prebuilt-receipt", receiptData);

        AnalyzeResult receiptResult = analyzeReceiptPoller.getFinalResult();
        List<ReceiptItemDTO> itemsFound = new ArrayList<>();

        // 4. Extract logic
        for (AnalyzedDocument document : receiptResult.getDocuments()) {
            Map<String, DocumentField> fields = document.getFields();
            DocumentField itemsField = fields.get("Items");

            if (itemsField != null && DocumentFieldType.LIST == itemsField.getType()) {
                for (DocumentField itemField : itemsField.getValueAsList()) {
                    Map<String, DocumentField> itemFields = itemField.getValueAsMap();
                    ReceiptItemDTO item = new ReceiptItemDTO();

                    // Mapping AI fields with safety checks
                    item.setName(itemFields.get("Description") != null ? itemFields.get("Description").getValueAsString() : "Unknown Item");
                    item.setQuantity(itemFields.get("Quantity") != null ? itemFields.get("Quantity").getValueAsDouble().intValue() : 1);
                    item.setPricePerUnit(itemFields.get("Price") != null ? itemFields.get("Price").getValueAsDouble() : 0.0);
                    item.setTotalPrice(itemFields.get("TotalPrice") != null ? itemFields.get("TotalPrice").getValueAsDouble() : 0.0);
                    // Add tax extraction if your DTO supports it
                    item.setTax(itemFields.get("Tax") != null ? itemFields.get("Tax").getValueAsDouble() : 0.0);

                    itemsFound.add(item);
                }
            }
        }
        return itemsFound;
    }
}