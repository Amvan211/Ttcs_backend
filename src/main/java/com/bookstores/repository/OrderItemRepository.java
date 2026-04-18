package com.bookstores.repository;

import com.bookstores.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    @Query(
            """
            SELECT COALESCE(SUM(oi.price * oi.quantity), 0)
            FROM OrderItem oi
            WHERE oi.book.partner.id = :partnerId
              AND oi.order.status IN (
                  'Đã giao', 'DELIVERED', 'COMPLETED', 'completed', 'HOÀN TẤT', 'Hoàn thành')
            """)
    Double sumRevenueByPartner(@Param("partnerId") Integer partnerId);

    @Query(
            """
            SELECT COUNT(DISTINCT oi.order.id)
            FROM OrderItem oi
            WHERE oi.book.partner.id = :partnerId
              AND oi.order.status IN (
                  'Đã giao', 'DELIVERED', 'COMPLETED', 'completed', 'HOÀN TẤT', 'Hoàn thành')
            """)
    long countDistinctOrdersByPartner(@Param("partnerId") Integer partnerId);
}
