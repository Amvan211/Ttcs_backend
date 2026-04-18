package com.bookstores.service.impl;

import com.bookstores.DTO.BookDTO;
import com.bookstores.DTO.BookDetailDTO;
import com.bookstores.DTO.ReviewDTO;
import com.bookstores.common.DomainConstants;
import com.bookstores.entity.Book;
import com.bookstores.entity.Category;
import com.bookstores.entity.UserBehavior;
import com.bookstores.repository.BookRepository;
import com.bookstores.repository.CategoryRepository;
import com.bookstores.repository.ReviewRepository;
import com.bookstores.repository.UserBehaviorRepository;
import com.bookstores.service.BookService;
import com.bookstores.service.UserContextService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final UserBehaviorRepository userBehaviorRepository;
    private final UserContextService userContextService;

    public BookServiceImpl(
            BookRepository bookRepository,
            CategoryRepository categoryRepository,
            ReviewRepository reviewRepository,
            UserBehaviorRepository userBehaviorRepository,
            UserContextService userContextService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
        this.userBehaviorRepository = userBehaviorRepository;
        this.userContextService = userContextService;
    }

    @Override
    public List<BookDTO> search(String title, String author, Integer categoryId, String categoryName) {
        Integer resolvedCategoryId = categoryId;
        String cat = emptyToNull(categoryName);
        if (resolvedCategoryId == null && cat != null) {
            resolvedCategoryId =
                    categoryRepository.findByCategoryName(cat).map(Category::getId).orElse(null);
            if (resolvedCategoryId == null) {
                return List.of();
            }
        }
        return bookRepository.searchApproved(emptyToNull(title), emptyToNull(author), resolvedCategoryId).stream()
                .map(BookDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookDetailDTO getDetail(Integer id) {
        Book b = bookRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!isApprovedForDisplay(b)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        var reviews =
                reviewRepository.findByBook_IdOrderByReviewDateDesc(id).stream()
                        .map(ReviewDTO::fromEntity)
                        .toList();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            var user = userContextService.requireCurrentUser();
            userBehaviorRepository.save(
                    UserBehavior.builder()
                            .user(user)
                            .book(b)
                            .actionType(DomainConstants.BEHAVIOR_VIEW)
                            .actionTime(LocalDateTime.now())
                            .build());
        }
        return BookDetailDTO.from(b, reviews);
    }

    @Override
    public Map<String, Object> searchByImageStub(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Map.of(
                    "message",
                    "Chưa có file ảnh — stub search-image (tích hợp nhận diện ảnh bìa sau).",
                    "books",
                    List.<BookDTO>of());
        }
        return Map.of(
                "message",
                "Stub: tìm kiếm theo ảnh bìa chưa được triển khai. File nhận được: " + file.getOriginalFilename(),
                "books",
                List.<BookDTO>of());
    }

    private static boolean isApprovedForDisplay(Book b) {
        String a = b.getApprovalStatus();
        return a == null || DomainConstants.APPROVAL_APPROVED.equalsIgnoreCase(a);
    }

    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
