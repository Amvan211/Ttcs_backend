package com.bookstores.controller;

import com.bookstores.DTO.ChatRequest;
import com.bookstores.DTO.ChatResponse;
import com.bookstores.entity.Book;
import com.bookstores.entity.ChatHistory;
import com.bookstores.entity.User;
import com.bookstores.repository.BookRepository;
import com.bookstores.repository.ChatHistoryRepository;
import com.bookstores.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatHistoryRepository chatHistoryRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final com.bookstores.service.ChatAIService chatAIService;

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }

        // Save User Message
        ChatHistory userMessage = ChatHistory.builder()
                .user(user)
                .message(request.getMessage())
                .sender(ChatHistory.Sender.USER)
                .sentAt(LocalDateTime.now())
                .build();
        chatHistoryRepository.save(userMessage);

        // Process Bot Response via AI or hardcoded logic
        String botMessageText;
        ChatResponse.BookInfo bookInfo = null;
        String msgLower = request.getMessage().toLowerCase().trim();

        if (msgLower.equals("xin chào")) {
            if (user != null && user.getFullName() != null) {
                botMessageText = "Chào " + user.getFullName() + ", tôi có thể giúp gì cho bạn?";
            } else {
                botMessageText = "Chào bạn, tôi có thể giúp gì cho bạn?";
            }
        } else if (msgLower.contains("sách mới")) {
            List<Book> topBooks = bookRepository.findTop3ByApprovalStatusOrderByIdDesc("APPROVED");
            if (topBooks.isEmpty()) {
                botMessageText = "Hiện tại chưa có sách mới nào.";
            } else {
                StringBuilder sb = new StringBuilder("Dưới đây là các sách mới nhất:\n");
                for (int i = 0; i < topBooks.size(); i++) {
                    Book b = topBooks.get(i);
                    sb.append(i + 1).append(". ").append(b.getTitle()).append(" - ").append(Math.round(b.getPrice())).append("đ\n");
                }
                botMessageText = sb.toString().trim();
            }
        } else {
            List<ChatHistory> history = new ArrayList<>();
            if (user != null) {
                history.addAll(chatHistoryRepository.findByUserIdOrderBySentAtAsc(user.getId()));
                if (history.stream().noneMatch(h -> h.getId() != null && h.getId().equals(userMessage.getId()))) {
                    history.add(userMessage);
                }
            } else {
                history.add(userMessage);
            }
            botMessageText = chatAIService.getGeminiResponse(history, user != null ? user.getId() : null);
            
            // Try to find if a book title is mentioned in the AI's response or User's request
            String lowerBotMessage = botMessageText.toLowerCase();
            String lowerUserMessage = request.getMessage().toLowerCase();
            List<Book> allBooks = bookRepository.findAll();
            
            Optional<Book> matchedBook = allBooks.stream()
                    .filter(b -> lowerBotMessage.contains(b.getTitle().toLowerCase()) 
                              || lowerUserMessage.contains(b.getTitle().toLowerCase()))
                    .findFirst();

            if (matchedBook.isPresent()) {
                Book book = matchedBook.get();
                bookInfo = ChatResponse.BookInfo.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .coverImageUrl(book.getCoverImageUrl())
                        .price(book.getPrice())
                        .build();
            }
        }

        // Save Bot Message
        ChatHistory botMessage = ChatHistory.builder()
                .user(user)
                .message(botMessageText)
                .sender(ChatHistory.Sender.BOT)
                .sentAt(LocalDateTime.now())
                .build();
        chatHistoryRepository.save(botMessage);

        ChatResponse response = ChatResponse.builder()
                .message(botMessageText)
                .book(bookInfo)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{userID}")
    public ResponseEntity<List<ChatHistory>> getHistory(@PathVariable Integer userID) {
        List<ChatHistory> history = chatHistoryRepository.findByUserIdOrderBySentAtAsc(userID);
        return ResponseEntity.ok(history);
    }
}
