package com.upkdev.financialtracker.domain.statement.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upkdev.financialtracker.domain.statement.dto.ExtractedTransaction;
import com.upkdev.financialtracker.domain.statement.dto.StatementUploadResponse;
import com.upkdev.financialtracker.domain.statement.service.StatementService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatementServiceImpl implements StatementService {

    private static final Logger log = LoggerFactory.getLogger(StatementServiceImpl.class);

    private final RestTemplate ollamaRestTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    public StatementServiceImpl(@Qualifier("ollamaRestTemplate") RestTemplate ollamaRestTemplate,
                                ObjectMapper objectMapper) {
        this.ollamaRestTemplate = ollamaRestTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public StatementUploadResponse processStatement(MultipartFile file) {
        return processStatement(file, null);
    }

    @Override
    public StatementUploadResponse processStatement(MultipartFile file, String accountTypeCode) {
        String pdfText = extractTextFromPdf(file);
        log.info("Extracted {} characters from PDF", pdfText.length());

        List<ExtractedTransaction> all = extractTransactionsWithOllama(pdfText, accountTypeCode);

        boolean isCreditCard = "CREDIT_CARD".equalsIgnoreCase(accountTypeCode);

        List<ExtractedTransaction> expenses = all.stream()
                .filter(t -> "DEBIT".equalsIgnoreCase(t.getType()) && !Boolean.TRUE.equals(t.getIsCreditCardPayment()))
                .collect(Collectors.toList());

        List<ExtractedTransaction> income = all.stream()
                .filter(t -> "CREDIT".equalsIgnoreCase(t.getType()) && !isCreditCard && !Boolean.TRUE.equals(t.getIsCreditCardPayment()))
                .collect(Collectors.toList());

        List<ExtractedTransaction> ccPayments = all.stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsCreditCardPayment()) || ("CREDIT".equalsIgnoreCase(t.getType()) && isCreditCard))
                .collect(Collectors.toList());

        return StatementUploadResponse.builder()
                .transactionCount(all.size())
                .expenseCount(expenses.size())
                .incomeCount(income.size())
                .creditCardPaymentCount(ccPayments.size())
                .expenses(expenses)
                .income(income)
                .creditCardPayments(ccPayments)
                .rawTextPreview(pdfText.length() > 500 ? pdfText.substring(0, 500) : pdfText)
                .modelUsed(ollamaModel)
                .build();
    }

    private String extractTextFromPdf(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            try (var document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }

    private List<ExtractedTransaction> extractTransactionsWithOllama(String pdfText, String accountTypeCode) {
        String prompt = buildExtractionPrompt(pdfText, accountTypeCode);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaModel);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("format", "json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = ollamaRestTemplate.postForEntity(
                    ollamaBaseUrl + "/api/generate", request, String.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseOllamaResponse(response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to call Ollama: {}", e.getMessage());
            throw new RuntimeException("Failed to extract transactions from AI model: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private String buildExtractionPrompt(String pdfText, String accountTypeCode) {
        boolean isCreditCard = "CREDIT_CARD".equalsIgnoreCase(accountTypeCode);
        String accountContext = isCreditCard
                ? "This is a CREDIT CARD statement. Purchases/charges are DEBIT. Payments made TO the card are credits — mark those as isCreditCardPayment=true, NOT as income."
                : "This is a bank account statement. Outgoing transactions are DEBIT (expenses). Incoming transactions like salary, transfers, refunds are CREDIT (income).";

        return "You are a financial data extraction assistant. Extract all transactions from the bank statement text below.\n\n"
                + "Account context: " + accountContext + "\n\n"
                + "Return ONLY a valid JSON object in this exact format, nothing else:\n"
                + "{\n"
                + "  \"transactions\": [\n"
                + "    {\n"
                + "      \"date\": \"YYYY-MM-DD\",\n"
                + "      \"description\": \"transaction description\",\n"
                + "      \"amount\": 12.34,\n"
                + "      \"type\": \"DEBIT\",\n"
                + "      \"suggestedCategory\": \"FOOD\",\n"
                + "      \"isCreditCardPayment\": false\n"
                + "    }\n"
                + "  ]\n"
                + "}\n\n"
                + "Rules:\n"
                + "- type must be either \"DEBIT\" or \"CREDIT\"\n"
                + "- suggestedCategory for DEBIT: FOOD, TRANSPORT, UTILITIES, SUBSCRIPTIONS, ENTERTAINMENT, TRAVEL, HEALTH, OTHER\n"
                + "- suggestedCategory for CREDIT: SALARY, FREELANCE, REFUND, TRANSFER, OTHER\n"
                + "- isCreditCardPayment: true only for credit card payment credits (not purchases)\n"
                + "- amount must be a positive number\n"
                + "- date must be in YYYY-MM-DD format\n"
                + "- Extract ALL transactions\n\n"
                + "Bank statement text:\n" + pdfText;
    }

    private List<ExtractedTransaction> parseOllamaResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String responseText = root.path("response").asText().trim();
            if (responseText.startsWith("```")) {
                responseText = responseText.replaceAll("```json\\n?", "").replaceAll("```\\n?", "").trim();
            }
            JsonNode parsed = objectMapper.readTree(responseText);
            JsonNode transactionsNode = parsed.path("transactions");
            if (transactionsNode.isArray()) {
                return objectMapper.convertValue(transactionsNode, new TypeReference<List<ExtractedTransaction>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to parse Ollama response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse AI model response: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }
}
