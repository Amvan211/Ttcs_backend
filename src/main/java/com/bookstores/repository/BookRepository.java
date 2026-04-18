package com.bookstores.repository;

import com.bookstores.entity.Book;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Integer> {

    List<Book> findByApprovalStatus(String approvalStatus);

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
}
