package com.bookstores.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookApproveRequest {

    /** APPROVE hoặc REJECT */
    @NotBlank private String decision;
}
