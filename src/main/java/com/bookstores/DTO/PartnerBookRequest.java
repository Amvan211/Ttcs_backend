package com.bookstores.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PartnerBookRequest {

    @NotBlank private String title;

    private String author;

    @NotNull @Min(0) private Double price;

    @NotNull @Min(0) private Integer stockQuantity;

    private String description;

    private String coverImageUrl;

    @NotNull private Integer categoryId;
}
