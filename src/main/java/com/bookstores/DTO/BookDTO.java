package com.bookstores.DTO;

import com.bookstores.entity.Book;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookDTO {

    private Integer id;
    private String title;
    private String author;
    private Double price;

    @JsonProperty("coverImage")
    private String coverImage;

    @JsonProperty("category")
    private String category;

    private Integer stock;
    private String approvalStatus;

    public static BookDTO fromEntity(Book b) {
        if (b == null) {
            return null;
        }
        return BookDTO.builder()
                .id(b.getId())
                .title(b.getTitle())
                .author(b.getAuthor())
                .price(b.getPrice())
                .coverImage(b.getCoverImageUrl())
                .category(b.getCategory() != null ? b.getCategory().getCategoryName() : null)
                .stock(b.getStockQuantity())
                .approvalStatus(b.getApprovalStatus())
                .build();
    }
}
