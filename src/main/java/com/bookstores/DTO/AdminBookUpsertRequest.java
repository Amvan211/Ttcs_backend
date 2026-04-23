package com.bookstores.DTO;

import lombok.Data;

@Data
public class AdminBookUpsertRequest {
    private String title;
    private String author;
    private Double price;
    private Integer stockQuantity;
    private String description;
    private String coverImageUrl;
    private Integer categoryId;
    private Integer partnerId;
    private String approvalStatus;
}
