package com.bookstores.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PartnerRegisterRequest {

    @NotBlank private String storeName;

    private String address;

    private String description;
}
