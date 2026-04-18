package com.bookstores.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bookstores.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByCategoryName(String categoryName);
}
