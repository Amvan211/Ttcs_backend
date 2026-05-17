package com.bookstores.repository;

import com.bookstores.entity.Book;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Integer> {

    List<Book> findByApprovalStatus(String approvalStatus);

    List<Book> findTop3ByApprovalStatusOrderByIdDesc(String approvalStatus);

    List<Book> findTop3ByCategory_IdAndStockQuantityGreaterThanOrderByIdDesc(Integer categoryId, Integer stockQuantity);

    List<Book> findByPartner_Id(Integer partnerId);

    long countByCategory_Id(Integer categoryId);

    @Query(
            """
            SELECT b FROM Book b
            WHERE (b.approvalStatus = 'APPROVED' OR b.approvalStatus IS NULL)
            AND (:title IS NULL OR :title = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
            AND (:author IS NULL OR :author = '' OR (b.author IS NOT NULL AND LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))))
            AND (:categoryId IS NULL OR b.category.id = :categoryId)
            """)
    List<Book> searchApproved(
            @Param("title") String title, @Param("author") String author, @Param("categoryId") Integer categoryId);

    @Query(
            """
            SELECT b FROM Book b
            WHERE (b.approvalStatus = 'APPROVED' OR b.approvalStatus IS NULL)
            AND b.category.id IN :categoryIds
            AND b.id NOT IN :excludeIds
            """)
    List<Book> recommendByCategories(
            @Param("categoryIds") Collection<Integer> categoryIds, @Param("excludeIds") Collection<Integer> excludeIds);

    @Query(
            """
            SELECT b FROM Book b
            WHERE (b.approvalStatus = 'APPROVED' OR b.approvalStatus IS NULL)
            AND b.author IN :authors
            AND b.id NOT IN :excludeIds
            """)
    List<Book> recommendByAuthors(
            @Param("authors") Collection<String> authors, @Param("excludeIds") Collection<Integer> excludeIds);

    @Query(
            """
            SELECT b FROM Book b
            LEFT JOIN OrderItem oi ON oi.book.id = b.id
            WHERE b.approvalStatus = 'APPROVED' OR b.approvalStatus IS NULL
            GROUP BY b
            ORDER BY SUM(COALESCE(oi.quantity, 0)) DESC, b.id DESC
            """)
    List<Book> findTopSellingBooks(org.springframework.data.domain.Pageable pageable);

    @Query(
            """
            SELECT b FROM Book b
            INNER JOIN Review r ON r.book.id = b.id
            WHERE b.approvalStatus = 'APPROVED' OR b.approvalStatus IS NULL
            GROUP BY b
            ORDER BY AVG(r.rating) DESC, COUNT(r.id) DESC, b.id DESC
            """)
    List<Book> findTopRatedBooks(org.springframework.data.domain.Pageable pageable);
}
