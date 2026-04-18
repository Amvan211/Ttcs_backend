package com.bookstores.service;

import com.bookstores.DTO.BookDTO;
import java.util.List;

public interface RecommendationService {

    List<BookDTO> recommendForCurrentUser();
}
