package com.bookstores.service.impl;

import com.bookstores.DTO.CategoryListDTO;
import com.bookstores.entity.Category;
import com.bookstores.repository.BookRepository;
import com.bookstores.repository.CategoryRepository;
import com.bookstores.service.CategoryService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public List<CategoryListDTO> listForExplore() {
        return categoryRepository.findAll().stream().map(this::toExploreRow).toList();
    }

    private CategoryListDTO toExploreRow(Category c) {
        long n = bookRepository.countByCategory_Id(c.getId());
        return CategoryListDTO.builder()
                .categoryId(c.getId())
                .id(c.getCategoryName())
                .label(c.getCategoryName())
                .count(n + " Titles")
                .build();
    }
}
