package com.bookstores.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String message;
    private BookInfo book;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookInfo {
        private Integer id;
        private String title;
        private String coverImageUrl;
        private Double price;
    }
}
