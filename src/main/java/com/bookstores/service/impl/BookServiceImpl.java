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
import com.bookstores.repository.OrderRepository;
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
    private final OrderRepository orderRepository;
    private final UserContextService userContextService;

    public BookServiceImpl(
            BookRepository bookRepository,
            CategoryRepository categoryRepository,
            ReviewRepository reviewRepository,
            UserBehaviorRepository userBehaviorRepository,
            OrderRepository orderRepository,
            UserContextService userContextService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
        this.userBehaviorRepository = userBehaviorRepository;
        this.orderRepository = orderRepository;
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

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByBookId(Integer bookId) {
        return reviewRepository.findByBook_IdOrderByReviewDateDesc(bookId).stream()
                .map(ReviewDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public ReviewDTO addReview(com.bookstores.DTO.ReviewUpsertRequest req, String username) {
        var user = userContextService.requireCurrentUser();
        boolean hasPurchased = orderRepository.hasUserPurchasedBook(user.getId(), req.getBookId());
        
        if (!hasPurchased) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn phải mua sách và đơn hàng phải được giao thành công mới có thể đánh giá.");
        }

        Book b = bookRepository.findById(req.getBookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

        com.bookstores.entity.Review r = com.bookstores.entity.Review.builder()
                .book(b)
                .user(user)
                .rating(req.getRating())
                .comment(req.getComment())
                .reviewDate(LocalDateTime.now())
                .build();
        return ReviewDTO.fromEntity(reviewRepository.save(r));
    }

    private static boolean isApprovedForDisplay(Book b) {
        String a = b.getApprovalStatus();
        return a == null || DomainConstants.APPROVAL_APPROVED.equalsIgnoreCase(a);
    }

    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
