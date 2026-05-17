package com.bookstores.DTO;

import com.bookstores.entity.Partner;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerDTO {
    private Integer id;
    private String storeName;
    private String address;
    private String description;
    private String status;
    private String username;
    private String fullName;
    private String email;

    public static PartnerDTO fromEntity(Partner p) {
        if (p == null) return null;
        return PartnerDTO.builder()
                .id(p.getId())
                .storeName(p.getStoreName())
                .address(p.getAddress())
                .description(p.getDescription())
                .status(p.getStatus())
                .username(p.getUser() != null ? p.getUser().getUsername() : null)
                .fullName(p.getUser() != null ? p.getUser().getFullName() : null)
                .email(p.getUser() != null ? p.getUser().getMail() : null)
                .build();
    }
}
