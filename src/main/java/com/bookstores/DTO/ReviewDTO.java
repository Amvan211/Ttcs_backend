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
    private String reviewerFullName;
    private String reviewerAvatar;

    public static ReviewDTO fromEntity(Review r) {
        String fullName = r.getUser() != null ? r.getUser().getFullName() : null;
        String uname = r.getUser() != null ? r.getUser().getUsername() : null;
        return ReviewDTO.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .reviewDate(r.getReviewDate())
                .reviewerUsername(uname)
                .reviewerFullName(fullName != null && !fullName.isBlank() ? fullName : uname)
                .reviewerAvatar(r.getUser() != null ? r.getUser().getAvatarUrl() : null)
                .build();
    }
}
