package com.bookstores.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeStat {

    private Double totalRevenue;
    private Long orderCount;
}
