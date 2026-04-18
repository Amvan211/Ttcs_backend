package com.bookstores.DTO;

import com.bookstores.entity.Review;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private Integer id;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewDate;
    private String reviewerUsername;

    public static ReviewDTO fromEntity(Review r) {
        return ReviewDTO.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .reviewDate(r.getReviewDate())
                .reviewerUsername(r.getUser() != null ? r.getUser().getUsername() : null)
                .build();
    }
}
