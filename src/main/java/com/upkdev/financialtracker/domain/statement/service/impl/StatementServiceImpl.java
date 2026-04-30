package com.upkdev.financialtracker.domain.statement.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upkdev.financialtracker.domain.account.dao.AccountDao;
import com.upkdev.financialtracker.domain.account.dto.AccountStatementResponse;
import com.upkdev.financialtracker.domain.account.entity.AccountStatement;
import com.upkdev.financialtracker.domain.account.mapper.AccountMapper;
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
    private final AccountDao accountDao;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    public StatementServiceImpl(@Qualifier("ollamaRestTemplate") RestTemplate ollamaRestTemplate,
                                ObjectMapper objectMapper,
                                AccountDao accountDao) {
        this.ollamaRestTemplate = ollamaRestTemplate;
        this.objectMapper = objectMapper;
        this.accountDao = accountDao;
    }

    @Override
    public StatementUploadResponse processStatement(MultipartFile file) {
        return processStatement(file, null);
    }

    @Override
    public StatementUploadResponse processStatement(MultipartFile file, String accountTypeCode) {
        String pdfText = extractTextFromPdf(file);
        log.info("Extracted {} characters from PDF", pdfText.length());

        // Pre-process: strip running balance column before sending to AI
        String processedText = stripRunningBalances(pdfText);
        log.info("After balance stripping: {} characters", processedText.length());

        // If text is very long, process in chunks to avoid Ollama timeout
        List<ExtractedTransaction> all;
        if (processedText.length() > 8000) {
            log.info("Large PDF detected ({} chars), processing in chunks", processedText.length());
            all = extractInChunks(processedText, accountTypeCode);
        } else {
            all = extractTransactionsWithOllama(processedText, accountTypeCode);
        }

        // Post-process: flag amounts that look like running balances
        flagAnomalousAmounts(all);

        boolean isCreditCard = "CREDIT_CARD".equalsIgnoreCase(accountTypeCode);

        // Investments — tracked separately, never count as expense or income
        List<ExtractedTransaction> investments = all.stream()
                .filter(t -> "INVESTMENT".equalsIgnoreCase(t.getTransactionType()))
                .peek(t -> t.setDisplayType("INVESTMENT"))
                .collect(Collectors.toList());

        // Transfers = neutral transactions (CC payments from chequing, investment contributions, internal transfers)
        List<ExtractedTransaction> transfers = all.stream()
                .filter(t -> !"INVESTMENT".equalsIgnoreCase(t.getTransactionType())
                        && (Boolean.TRUE.equals(t.getIsTransfer())
                            || Boolean.TRUE.equals(t.getIsCreditCardPayment())))
                .peek(t -> t.setDisplayType("NEUTRAL"))
                .collect(Collectors.toList());

        // Expenses = debits that are not transfers and not investments
        List<ExtractedTransaction> expenses = all.stream()
                .filter(t -> "DEBIT".equalsIgnoreCase(t.getType())
                        && !Boolean.TRUE.equals(t.getIsTransfer())
                        && !Boolean.TRUE.equals(t.getIsCreditCardPayment())
                        && !"INVESTMENT".equalsIgnoreCase(t.getTransactionType()))
                .peek(t -> t.setDisplayType("EXPENSE"))
                .collect(Collectors.toList());

        // Income = credits that are not transfers and not on credit card and not investments
        List<ExtractedTransaction> income = all.stream()
                .filter(t -> "CREDIT".equalsIgnoreCase(t.getType())
                        && !isCreditCard
                        && !Boolean.TRUE.equals(t.getIsTransfer())
                        && !Boolean.TRUE.equals(t.getIsCreditCardPayment())
                        && !"INVESTMENT".equalsIgnoreCase(t.getTransactionType()))
                .peek(t -> t.setDisplayType("INCOME"))
                .collect(Collectors.toList());

        // CC payments specifically (subset of transfers, for display purposes)
        List<ExtractedTransaction> ccPayments = all.stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsCreditCardPayment())
                        || ("CREDIT".equalsIgnoreCase(t.getType()) && isCreditCard && !Boolean.TRUE.equals(t.getIsTransfer())))
                .collect(Collectors.toList());

        // Flat unified list (expenses + income + transfers/CC) — excludes investments (own section)
        List<ExtractedTransaction> transactions = new java.util.ArrayList<>();
        transactions.addAll(expenses);
        transactions.addAll(income);
        transactions.addAll(transfers);
        // Sort by date ascending
        transactions.sort(java.util.Comparator.comparing(
                t -> t.getDate() != null ? t.getDate() : ""));

        return StatementUploadResponse.builder()
                .transactionCount(all.size())
                .expenseCount(expenses.size())
                .incomeCount(income.size())
                .transferCount(transfers.size())
                .creditCardPaymentCount(ccPayments.size())
                .investmentCount(investments.size())
                .transactions(transactions)
                .investments(investments)
                .expenses(expenses)
                .income(income)
                .transfers(transfers)
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

    /**
     * Pre-processes raw PDFBox text before sending to the AI model.
     * Bank statements typically have a running balance as the LAST number on each transaction line.
     * PDFTextStripper flattens columns into a single stream, so the balance looks identical to
     * a transaction amount. This method strips trailing balance figures from transaction lines
     * to prevent the model from confusing them with actual amounts.
     *
     * Pattern matched: lines ending with  ...  $1,234.56  $5,678.90
     * The last money-like token is removed; the second-to-last is the real transaction amount.
     */
    private String stripRunningBalances(String rawText) {
        // Match lines that end with two consecutive money patterns (amount then balance)
        // e.g. "Jan 5   Netflix   15.99   4,588.81"  →  "Jan 5   Netflix   15.99"
        // Money pattern: optional $ or -, digits, optional comma-groups, optional decimal
        String moneyPattern = "\\$?-?[\\d,]+(?:\\.\\d{2})?";
        // Line ends with: whitespace + money + whitespace + money (balance)
        java.util.regex.Pattern balanceAtEnd = java.util.regex.Pattern.compile(
            "(" + moneyPattern + ")\\s+(" + moneyPattern + ")\\s*$");

        StringBuilder cleaned = new StringBuilder();
        for (String line : rawText.split("\n")) {
            java.util.regex.Matcher m = balanceAtEnd.matcher(line);
            if (m.find()) {
                // Only strip if both numbers look like plausible amounts (> 0.00)
                String first  = m.group(1).replaceAll("[,$]", "");
                String second = m.group(2).replaceAll("[,$]", "");
                try {
                    double a = Double.parseDouble(first);
                    double b = Double.parseDouble(second);
                    // Heuristic: if second >> first it's more likely a running balance
                    // Also strip if second looks like a large account balance (> $500 and > 5x first)
                    if (a > 0 && b > 0 && (b > 500 || b > a * 3)) {
                        // Remove the trailing balance (second match)
                        cleaned.append(line, 0, m.start(2)).append("\n");
                        continue;
                    }
                } catch (NumberFormatException ignored) {}
            }
            cleaned.append(line).append("\n");
        }
        return cleaned.toString();
    }

    /**
     * Post-extraction anomaly detection.
     * Flags transactions whose amount is suspiciously large compared to the median —
     * a strong signal that the model picked up a running balance instead of a transaction.
     */
    private void flagAnomalousAmounts(List<ExtractedTransaction> transactions) {
        if (transactions.size() < 3) return;

        List<Double> amounts = transactions.stream()
                .filter(t -> t.getAmount() != null)
                .map(t -> t.getAmount().doubleValue())
                .sorted()
                .collect(Collectors.toList());

        // Median
        double median = amounts.get(amounts.size() / 2);
        // P90 — top 10% threshold
        double p90 = amounts.get((int)(amounts.size() * 0.9));

        for (ExtractedTransaction tx : transactions) {
            if (tx.getAmount() == null) continue;
            double amt = tx.getAmount().doubleValue();
            // Flag if: amount > 10x median AND > $1000 (likely a running balance)
            if (amt > median * 10 && amt > 1000) {
                tx.setConfidenceLow(true);
                tx.setConfidenceNote(String.format(
                    "Amount $%.2f is unusually large (median: $%.2f) — may be a running balance. Please verify.",
                    amt, median));
            }
            // Also flag if it looks exactly like a round thousand (running balances often are)
            else if (amt > p90 && amt % 100 == 0 && amt > 2000) {
                tx.setConfidenceLow(true);
                tx.setConfidenceNote(String.format(
                    "Amount $%.0f is a round number larger than 90%% of transactions — verify this is not a balance.",
                    amt));
            }
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

    private List<ExtractedTransaction> extractInChunks(String pdfText, String accountTypeCode) {
        // Prefer page boundaries (\f = form feed inserted by PDFTextStripper between pages)
        // Fall back to newline-based chunking if no page breaks found
        String[] pages = pdfText.split("\f");
        List<String> chunks = new ArrayList<>();

        if (pages.length > 1) {
            // Group pages into batches that stay under 6000 chars
            StringBuilder batch = new StringBuilder();
            for (String page : pages) {
                if (batch.length() + page.length() > 6000 && batch.length() > 0) {
                    chunks.add(batch.toString());
                    batch = new StringBuilder();
                }
                batch.append(page).append("\n");
            }
            if (batch.length() > 0) chunks.add(batch.toString());
        } else {
            // No page breaks — fall back to newline-boundary chunking
            int chunkSize = 6000;
            int start = 0;
            while (start < pdfText.length()) {
                int end = Math.min(start + chunkSize, pdfText.length());
                if (end < pdfText.length()) {
                    int nl = pdfText.lastIndexOf('\n', end);
                    if (nl > start + 1000) end = nl;
                }
                chunks.add(pdfText.substring(start, end));
                start = end;
            }
        }

        List<ExtractedTransaction> all = new ArrayList<>();
        int chunkNum = 1;
        for (String chunk : chunks) {
            log.info("Processing chunk {} of {} ({} chars)", chunkNum, chunks.size(), chunk.length());
            try {
                List<ExtractedTransaction> chunkResult = extractTransactionsWithOllama(chunk, accountTypeCode);
                all.addAll(chunkResult);
            } catch (Exception e) {
                log.warn("Chunk {} failed: {}", chunkNum, e.getMessage());
            }
            chunkNum++;
        }

        // Deduplicate by date+description+amount (override equals via key string)
        Map<String, ExtractedTransaction> seen = new LinkedHashMap<>();
        for (ExtractedTransaction tx : all) {
            if (tx.getDate() == null || tx.getAmount() == null) continue;
            String key = tx.getDate() + "|"
                    + (tx.getDescription() != null ? tx.getDescription().trim().toLowerCase() : "") + "|"
                    + tx.getAmount().toPlainString();
            seen.putIfAbsent(key, tx);
        }
        return new ArrayList<>(seen.values());
    }

    private String buildExtractionPrompt(String pdfText, String accountTypeCode) {
        boolean isCreditCard = "CREDIT_CARD".equalsIgnoreCase(accountTypeCode);
        boolean isInvestment = "INVESTMENT".equalsIgnoreCase(accountTypeCode);

        String accountContext;
        if (isCreditCard) {
            accountContext = "This is a CREDIT CARD statement. Purchases/charges are DEBIT (expenses). Payments made TO the card are credits — mark those as isCreditCardPayment=true. They are NOT income.";
        } else if (isInvestment) {
            accountContext = "This is an INVESTMENT account statement. Contributions/deposits are transfers IN (isTransfer=true). Withdrawals are transfers OUT (isTransfer=true). Dividends or interest are CREDIT (income). Fees are DEBIT (expense).";
        } else {
            accountContext = "This is a bank account statement. Outgoing transactions are DEBIT (expenses). Incoming transactions like salary are CREDIT (income). Payments to credit cards, transfers to savings/investment accounts, and internal transfers must be marked isTransfer=true — they are NEUTRAL.";
        }

        return "You are a precise financial data extraction assistant. Extract every transaction from the bank statement below.\n\n"
                + "CRITICAL RULE — READ THIS FIRST:\n"
                + "Bank statements have a RUNNING BALANCE column on the right side of each row.\n"
                + "The running balance is NOT a transaction amount. NEVER use it as the amount field.\n"
                + "Each transaction row has this structure:\n"
                + "  [date]  [description]  [transaction amount]  [running balance]\n"
                + "Example:\n"
                + "  Jan 5   Netflix          15.99   4,588.81\n"
                + "  Jan 6   Grocery Store    45.20   4,543.61\n"
                + "Correct: amount=15.99 (NOT 4588.81), amount=45.20 (NOT 4543.61)\n"
                + "The transaction amount is always SMALLER. The running balance is always LARGER and changes each row.\n\n"
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
                + "      \"transactionType\": \"EXPENSE\",\n"
                + "      \"isCreditCardPayment\": false,\n"
                + "      \"isTransfer\": false\n"
                + "    }\n"
                + "  ]\n"
                + "}\n\n"
                + "Field rules:\n"
                + "- amount: MUST be the transaction amount only — the smaller number on the row. NEVER the running balance.\n"
                + "- amount must always be a positive number\n"
                + "- type: \"DEBIT\" for money going out, \"CREDIT\" for money coming in\n"
                + "- date: YYYY-MM-DD format only\n"
                + "- suggestedCategory for DEBIT: FOOD, TRANSPORT, UTILITIES, SUBSCRIPTIONS, ENTERTAINMENT, TRAVEL, HEALTH, INVESTMENT, OTHER\n"
                + "- suggestedCategory for CREDIT: SALARY, FREELANCE, REFUND, TRANSFER, OTHER\n"
                + "- transactionType: EXPENSE for purchases/bills, INCOME for salary/deposits, INVESTMENT for brokerage/investment contributions, TRANSFER for CC payments or account transfers, CC_PAYMENT for payments received by a credit card\n"
                + "- isCreditCardPayment: true ONLY for credits received by a credit card\n"
                + "- isTransfer: true for CC payments from chequing, transfers to savings/investment, internal moves — these are NEUTRAL\n"
                + "- Extract EVERY transaction row. Do not skip any.\n\n"
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

    @Override
    public void markMonthUploaded(Long accountId, int year, int month, int transactionCount) {
        var existing = accountDao.findStatement(accountId, year, month);
        AccountStatement stmt;
        if (existing.isPresent()) {
            stmt = existing.get();
            stmt.setStatus("UPLOADED");
            stmt.setTransactionCount(transactionCount);
            stmt.setUploadedAt(java.time.LocalDateTime.now());
        } else {
            stmt = AccountStatement.builder()
                    .accountId(accountId)
                    .statementYear(year)
                    .statementMonth(month)
                    .status("UPLOADED")
                    .transactionCount(transactionCount)
                    .uploadedAt(java.time.LocalDateTime.now())
                    .build();
        }
        accountDao.saveStatement(stmt);
    }

    @Override
    public AccountStatementResponse getStatementStatus(Long accountId, int year, int month) {
        return accountDao.findStatement(accountId, year, month)
                .map(AccountMapper::toStatementResponse)
                .orElse(null);
    }
}
