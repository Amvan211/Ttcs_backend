package com.bookstores.service;

import com.bookstores.entity.Book;
import com.bookstores.entity.ChatHistory;
import com.bookstores.repository.BookRepository;
import com.bookstores.repository.UserBehaviorRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatAIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserBehaviorRepository userBehaviorRepository;
    private final BookRepository bookRepository;

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    @Value("${ai.gemini.api-url}")
    private String apiUrl;

    @Value("${ai.gemini.system-prompt}")
    private String systemPrompt;

    public String getGeminiResponse(List<ChatHistory> history, Integer userId) {
        String url = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();

        // Lấy danh sách sách bán chạy và đánh giá tốt làm dữ liệu thống kê cho AI
        String statsContext = "";
        try {
            List<Book> topSelling = bookRepository.findTopSellingBooks(PageRequest.of(0, 5));
            if (topSelling != null && !topSelling.isEmpty()) {
                String sellingTitles = topSelling.stream()
                        .map(b -> b.getTitle() + " (Tác giả: " + (b.getAuthor() != null ? b.getAuthor() : "Chưa rõ") + " - Giá: " + Math.round(b.getPrice()) + "đ)")
                        .collect(Collectors.joining(", "));
                statsContext += "\n[Thống kê hệ thống - SÁCH ĐƯỢC MUA NHIỀU NHẤT / BÁN CHẠY NHẤT: " + sellingTitles + "]";
            }
            
            List<Book> topRated = bookRepository.findTopRatedBooks(PageRequest.of(0, 5));
            if (topRated != null && !topRated.isEmpty()) {
                String ratedTitles = topRated.stream()
                        .map(b -> b.getTitle() + " (Tác giả: " + (b.getAuthor() != null ? b.getAuthor() : "Chưa rõ") + " - Giá: " + Math.round(b.getPrice()) + "đ)")
                        .collect(Collectors.joining(", "));
                statsContext += "\n[Thống kê hệ thống - SÁCH CÓ ĐÁNH GIÁ TỐT NHẤT: " + ratedTitles + "]";
            }
        } catch (Exception e) {
            log.warn("Lỗi khi lấy thông tin thống kê sách cho chatbot: ", e);
        }

        String ragContext = "";
        if (userId != null) {
            try {
                List<Integer> topCategories = userBehaviorRepository.findTopCategoryIdsByUserId(userId, PageRequest.of(0, 1));
                if (topCategories != null && !topCategories.isEmpty()) {
                    Integer topCategoryId = topCategories.get(0);
                    List<Book> books = bookRepository.findTop3ByCategory_IdAndStockQuantityGreaterThanOrderByIdDesc(topCategoryId, 0);
                    if (books != null && !books.isEmpty()) {
                        String categoryName = books.get(0).getCategory().getCategoryName();
                        String bookTitles = books.stream().map(Book::getTitle).collect(Collectors.joining(", "));
                        ragContext = "\n\n[Thông tin hệ thống: Người dùng này thường quan tâm sách thể loại " + categoryName + ". Sách gợi ý hiện có là: " + bookTitles + ". Hãy khéo léo dùng thông tin này để tư vấn nếu họ nhờ gợi ý sách]";
                    }
                }
            } catch (Exception e) {
                log.warn("Lỗi khi lấy RAG context: ", e);
            }
        }

        // 1. System Instruction
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

        for (ChatHistory h : history) {
            String role = h.getSender() == ChatHistory.Sender.BOT ? "model" : "user";
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
        } catch (RestClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("Gemini API HTTP Error: {} - {}", e.getStatusCode(), errorBody);
            return "Xin lỗi, tôi đang gặp trục trặc kỹ thuật. Hãy thử lại sau nhé!";
        } catch (Exception e) {
            log.error("Exception calling Gemini API: {}", e.getMessage(), e);
            return "Xin lỗi, tôi đang gặp trục trặc kỹ thuật. Hãy thử lại sau nhé!";
        }
    }

    private String parseGeminiResponse(String responseBody) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode candidatesNode = rootNode.path("candidates");
        if (candidatesNode.isArray() && !candidatesNode.isEmpty()) {
            JsonNode firstCandidate = candidatesNode.get(0);
            JsonNode contentNode = firstCandidate.path("content");
            JsonNode partsNode = contentNode.path("parts");
            if (partsNode.isArray() && !partsNode.isEmpty()) {
                return partsNode.get(0).path("text").asText();
            }
        }
        return "Xin lỗi, tôi không thể hiểu được câu trả lời từ hệ thống.";
    }
}
