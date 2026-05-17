package com.bookstores.repository;

import com.bookstores.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUser_IdOrderByOrderDateDesc(Integer userId);

    @Query(
            """
            SELECT DISTINCT o FROM Order o
            JOIN o.orderItems oi
            JOIN oi.book b
            WHERE b.partner.id = :partnerId
            ORDER BY o.orderDate DESC
            """)
    List<Order> findDistinctByPartnerBooks(@Param("partnerId") Integer partnerId);

    @Query(
            """
            SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
            FROM Order o JOIN o.orderItems oi JOIN oi.book b
            WHERE o.id = :orderId AND b.partner.id = :partnerId
            """)
    boolean existsByIdAndPartner(@Param("orderId") Integer orderId, @Param("partnerId") Integer partnerId);

    @Query(
            """
            SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
            FROM Order o JOIN o.orderItems oi
            WHERE o.user.id = :userId AND oi.book.id = :bookId AND LOWER(o.status) IN ('completed', 'delivered', 'hoàn tất', 'hoàn thành', 'đã giao')
            """)
    boolean hasUserPurchasedBook(@Param("userId") Integer userId, @Param("bookId") Integer bookId);
}
