package com.bookstores.DTO;

import com.bookstores.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Integer bookId;
    private String bookTitle;
    private Integer quantity;
    private Double unitPrice;
    private Double lineTotal;

    public static OrderItemDTO fromEntity(OrderItem oi) {
        double line = oi.getPrice() * oi.getQuantity();
        return OrderItemDTO.builder()
                .bookId(oi.getBook().getId())
                .bookTitle(oi.getBook().getTitle())
                .quantity(oi.getQuantity())
                .unitPrice(oi.getPrice())
                .lineTotal(line)
                .build();
    }
}
