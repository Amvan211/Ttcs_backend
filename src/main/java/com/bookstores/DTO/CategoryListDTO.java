package com.bookstores.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Khám phá front_end: id/label theo tên danh mục, count dạng "N Titles". */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryListDTO {

    /** Khóa DB — dùng cho partner thêm sách (PartnerBookRequest.categoryId). */
    private Integer categoryId;

    private String id;
    private String label;
    private String count;
}
