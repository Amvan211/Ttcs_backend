package com.bookstores.controller;

import com.bookstores.DTO.BookDTO;
import com.bookstores.DTO.BookDetailDTO;
import com.bookstores.service.BookService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookDTO>> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(bookService.search(title, author, categoryId, category));
    }

    @PostMapping("/search-image")
    public ResponseEntity<Map<String, Object>> searchImage(@RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(bookService.searchByImageStub(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDetailDTO> detail(@PathVariable Integer id) {
        return ResponseEntity.ok(bookService.getDetail(id));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<com.bookstores.DTO.ReviewDTO>> getReviews(@PathVariable Integer id) {
        return ResponseEntity.ok(bookService.getReviewsByBookId(id));
    }

    @PostMapping("/{id}/reviews")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.bookstores.DTO.ReviewDTO> addReview(
            @PathVariable Integer id,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.bookstores.DTO.ReviewUpsertRequest req,
            org.springframework.security.core.Authentication authentication) {
        req.setBookId(id);
        return ResponseEntity.ok(bookService.addReview(req, authentication.getName()));
    }
}
