package com.bookstores.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {

    /** Ví dụ: {@code Đang giao}, {@code Đã giao} */
    @NotBlank private String status;
}
