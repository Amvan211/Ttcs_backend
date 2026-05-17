package com.bookstores.DTO;

import com.bookstores.entity.Voucher;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoucherDTO {
    private Integer id;
    private String code;
    private String description;
    private String discountType;
    private Double discountValue;
    private Double minOrderValue;
    private Double maxDiscountAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static VoucherDTO fromEntity(Voucher v) {
        if (v == null) return null;
        return VoucherDTO.builder()
                .id(v.getId())
                .code(v.getCode())
                .description(v.getDescription())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .minOrderValue(v.getMinOrderValue())
                .maxDiscountAmount(v.getMaxDiscountAmount())
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .build();
    }
}
