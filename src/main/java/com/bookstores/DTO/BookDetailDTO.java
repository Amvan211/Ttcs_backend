package com.bookstores.DTO;

import com.bookstores.entity.Book;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookDetailDTO {

    private Integer id;
    private String title;
    private String author;
    private Double price;
    private Integer stockQuantity;
    private String description;

    @JsonProperty("coverImage")
    private String coverImage;

    @JsonProperty("category")
    private String category;

    private String partnerStoreName;
    private List<ReviewDTO> reviews;

    public static BookDetailDTO from(Book b, List<ReviewDTO> reviews) {
        return BookDetailDTO.builder()
                .id(b.getId())
                .title(b.getTitle())
                .author(b.getAuthor())
                .price(b.getPrice())
                .stockQuantity(b.getStockQuantity())
                .description(b.getDescription())
                .coverImage(b.getCoverImageUrl())
                .category(b.getCategory() != null ? b.getCategory().getCategoryName() : null)
                .partnerStoreName(b.getPartner() != null ? b.getPartner().getStoreName() : null)
                .reviews(reviews)
                .build();
    }
}
