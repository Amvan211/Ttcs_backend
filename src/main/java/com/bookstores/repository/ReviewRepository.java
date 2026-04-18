package com.bookstores.repository;

import com.bookstores.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByBook_IdOrderByReviewDateDesc(Integer bookId);
}
