package com.bookstores.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatAIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final com.bookstores.repository.UserBehaviorRepository userBehaviorRepository;
    private final com.bookstores.repository.BookRepository bookRepository;

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    @Value("${ai.gemini.api-url}")
    private String apiUrl;

    @Value("${ai.gemini.system-prompt}")
    private String systemPrompt;

    public String getGeminiResponse(List<com.bookstores.entity.ChatHistory> history, Integer userId) {
        String url = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();

        // Lấy danh sách sách bán chạy và đánh giá tốt làm dữ liệu thống kê cho AI
        String statsContext = "";
        try {
            List<com.bookstores.entity.Book> topSelling = bookRepository.findTopSellingBooks(org.springframework.data.domain.PageRequest.of(0, 5));
            if (topSelling != null && !topSelling.isEmpty()) {
                String sellingTitles = topSelling.stream()
                        .map(b -> b.getTitle() + " (Tác giả: " + (b.getAuthor() != null ? b.getAuthor() : "Chưa rõ") + " - Giá: " + Math.round(b.getPrice()) + "đ)")
                        .collect(java.util.stream.Collectors.joining(", "));
                statsContext += "\n[Thống kê hệ thống - SÁCH ĐƯỢC MUA NHIỀU NHẤT / BÁN CHẠY NHẤT: " + sellingTitles + "]";
            }
            
            List<com.bookstores.entity.Book> topRated = bookRepository.findTopRatedBooks(org.springframework.data.domain.PageRequest.of(0, 5));
            if (topRated != null && !topRated.isEmpty()) {
                String ratedTitles = topRated.stream()
                        .map(b -> b.getTitle() + " (Tác giả: " + (b.getAuthor() != null ? b.getAuthor() : "Chưa rõ") + " - Giá: " + Math.round(b.getPrice()) + "đ)")
                        .collect(java.util.stream.Collectors.joining(", "));
                statsContext += "\n[Thống kê hệ thống - SÁCH CÓ ĐÁNH GIÁ TỐT NHẤT: " + ratedTitles + "]";
            }
        } catch (Exception e) {
            log.warn("Lỗi khi lấy thông tin thống kê sách cho chatbot: ", e);
        }

        String ragContext = "";
        if (userId != null) {
            try {
                List<Integer> topCategories = userBehaviorRepository.findTopCategoryIdsByUserId(userId, org.springframework.data.domain.PageRequest.of(0, 1));
                if (topCategories != null && !topCategories.isEmpty()) {
                    Integer topCategoryId = topCategories.get(0);
                    List<com.bookstores.entity.Book> books = bookRepository.findTop3ByCategory_IdAndStockQuantityGreaterThanOrderByIdDesc(topCategoryId, 0);
                    if (books != null && !books.isEmpty()) {
                        String categoryName = books.get(0).getCategory().getCategoryName();
                        String bookTitles = books.stream().map(com.bookstores.entity.Book::getTitle).collect(java.util.stream.Collectors.joining(", "));
                        ragContext = "\n\n[Thông tin hệ thống: Người dùng này thường quan tâm sách thể loại " + categoryName + ". Sách gợi ý hiện có là: " + bookTitles + ". Hãy khéo léo dùng thông tin này để tư vấn nếu họ nhờ gợi ý sách]";
                    }
                }
            } catch (Exception e) {
                log.warn("Lỗi khi lấy RAG context: ", e);
            }
        }

        // 1. System Instruction
        // LƯU Ý: Để dùng systemInstruction, hãy đảm bảo URL trong application.yml 
        // kết thúc bằng /v1beta/models/gemini-1.5-flash-latest:generateContent
        Map<String, Object> systemInstruction = new HashMap<>();
        List<Map<String, Object>> sysPartsList = new ArrayList<>();
        Map<String, Object> sysPart = new HashMap<>();
        sysPart.put("text", systemPrompt + statsContext + ragContext);
        sysPartsList.add(sysPart);
        systemInstruction.put("parts", sysPartsList);
        requestBody.put("systemInstruction", systemInstruction);

        // 2. Contents (Lịch sử Chat)
        List<Map<String, Object>> contents = new ArrayList<>();
        
        String currentRole = null;
        StringBuilder currentText = new StringBuilder();

        for (com.bookstores.entity.ChatHistory h : history) {
            String role = h.getSender() == com.bookstores.entity.ChatHistory.Sender.BOT ? "model" : "user";
            String text = h.getMessage() != null ? h.getMessage() : "";

            if (currentRole == null) {
                currentRole = role;
                currentText.append(text);
            } else if (currentRole.equals(role)) {
                currentText.append("\n").append(text);
            } else {
                Map<String, Object> contentItem = new HashMap<>();
                contentItem.put("role", currentRole);
                List<Map<String, Object>> parts = new ArrayList<>();
                Map<String, Object> textPart = new HashMap<>();
                textPart.put("text", currentText.toString());
                parts.add(textPart);
                contentItem.put("parts", parts);
                contents.add(contentItem);

                currentRole = role;
                currentText = new StringBuilder(text);
            }
        }

        // Đẩy tin nhắn cuối cùng vào mảng
        if (currentRole != null) {
            Map<String, Object> contentItem = new HashMap<>();
            contentItem.put("role", currentRole);
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", currentText.toString());
            parts.add(textPart);
            contentItem.put("parts", parts);
            contents.add(contentItem);
        }

        // Đảm bảo tin nhắn đầu tiên phải là của user (nếu mảng bắt đầu bằng model, ta bỏ nó đi)
        if (!contents.isEmpty() && "model".equals(contents.get(0).get("role"))) {
            contents.remove(0);
        }

        // Đảm bảo tin nhắn cuối cùng bắt buộc phải là của user
        if (!contents.isEmpty() && "model".equals(contents.get(contents.size() - 1).get("role"))) {
            // Nếu cuối cùng là model, thêm một câu user giả định để tránh lỗi
            Map<String, Object> contentItem = new HashMap<>();
            contentItem.put("role", "user");
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", "Tiếp tục");
            parts.add(textPart);
            contentItem.put("parts", parts);
            contents.add(contentItem);
        }

        requestBody.put("contents", contents);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return parseGeminiResponse(response.getBody());
            } else {
                log.error("Gemini API error: {}", response.getStatusCode());
                return "Xin lỗi, tôi đang gặp lỗi kết nối đến AI. Hãy thử lại sau nhé!";
            }
        } catch (org.springframework.web.client.RestClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            System.err.println("\n\u001B[31m=== LỖI TỪ GEMINI API ===\u001B[0m");
            System.err.println("\u001B[31mStatus Code: " + e.getStatusCode() + "\u001B[0m");
            System.err.println("\u001B[31mResponse Body: " + errorBody + "\u001B[0m");
            System.err.println("\u001B[31m=========================\u001B[0m\n");
            
            log.error("Gemini API HTTP Error: {} - {}", e.getStatusCode(), errorBody);
            return "Xin lỗi, tôi đang gặp trục trặc kỹ thuật. Hãy thử lại sau nhé!";
        } catch (Exception e) {
            log.error("Exception calling Gemini API", e);
            try {
                java.nio.file.Files.writeString(java.nio.file.Paths.get("d:/Studying/ttcs/back_end/ai_error.txt"), e.toString() + "\n" + java.util.Arrays.toString(e.getStackTrace()));
            } catch (Exception ignored) {}
            return "Xin lỗi, tôi đang gặp trục trặc kỹ thuật. Hãy thử lại sau nhé! Lỗi: " + e.getMessage();
        }
    }

    private String parseGeminiResponse(String responseBody) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode candidatesNode = rootNode.path("candidates");
        if (candidatesNode.isArray() && candidatesNode.size() > 0) {
            JsonNode firstCandidate = candidatesNode.get(0);
            JsonNode contentNode = firstCandidate.path("content");
            JsonNode partsNode = contentNode.path("parts");
            if (partsNode.isArray() && partsNode.size() > 0) {
                return partsNode.get(0).path("text").asText();
            }
        }
        return "Xin lỗi, tôi không thể hiểu được câu trả lời từ hệ thống.";
    }
}
