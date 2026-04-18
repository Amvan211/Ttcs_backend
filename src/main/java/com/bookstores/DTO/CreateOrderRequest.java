package com.bookstores.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class CreateOrderRequest {

    private String note;

    @NotEmpty @Valid private List<Line> items;

    @Data
    public static class Line {

        @NotNull private Integer bookId;

        @NotNull @Min(1) private Integer quantity;
    }
}
