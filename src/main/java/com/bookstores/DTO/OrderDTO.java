package com.bookstores.DTO;

import com.bookstores.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Integer id;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private String note;
    /** Tên hiển thị khách — dùng cho danh sách đơn đối tác (front_end store orders). */
    private String customerName;
    private List<OrderItemDTO> items;

    public static OrderDTO fromEntity(Order o) {
        List<OrderItemDTO> lines =
                o.getOrderItems() == null
                        ? List.of()
                        : o.getOrderItems().stream().map(OrderItemDTO::fromEntity).toList();
        String display = null;
        if (o.getUser() != null) {
            String fn = o.getUser().getFullName();
            display = (fn != null && !fn.isBlank()) ? fn : o.getUser().getUsername();
        }
        return OrderDTO.builder()
                .id(o.getId())
                .orderDate(o.getOrderDate())
                .totalAmount(o.getTotalAmount())
                .status(o.getStatus())
                .note(o.getNote())
                .customerName(display)
                .items(lines)
                .build();
    }
}
