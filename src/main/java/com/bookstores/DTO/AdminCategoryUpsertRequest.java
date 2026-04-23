package com.bookstores.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCategoryUpsertRequest {
    @NotBlank private String name;
}
