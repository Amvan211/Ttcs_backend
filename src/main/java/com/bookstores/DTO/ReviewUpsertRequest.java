package com.bookstores.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewUpsertRequest {
    @NotNull(message = "bookId is required")
    private Integer bookId;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String comment;
}
