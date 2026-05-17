package com.bookstores.service;

import com.bookstores.DTO.BookDTO;
import com.bookstores.DTO.BookDetailDTO;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

    List<BookDTO> search(String title, String author, Integer categoryId, String categoryName);

    BookDetailDTO getDetail(Integer id);

    Map<String, Object> searchByImageStub(MultipartFile file);

    List<com.bookstores.DTO.ReviewDTO> getReviewsByBookId(Integer bookId);

    com.bookstores.DTO.ReviewDTO addReview(com.bookstores.DTO.ReviewUpsertRequest req, String username);
}
