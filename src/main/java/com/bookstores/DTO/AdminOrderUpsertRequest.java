package com.bookstores.DTO;

import java.util.List;
import lombok.Data;

@Data
public class AdminOrderUpsertRequest {
    private Integer userId;
    private String status;
    private String note;
    private List<Line> items;

    @Data
    public static class Line {
        private Integer bookId;
        private Integer quantity;
    }
}
