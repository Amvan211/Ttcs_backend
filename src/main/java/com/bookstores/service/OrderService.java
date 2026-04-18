package com.bookstores.service;

import com.bookstores.DTO.CreateOrderRequest;
import com.bookstores.DTO.OrderDTO;
import java.util.List;

public interface OrderService {

    List<OrderDTO> history();

    OrderDTO create(CreateOrderRequest req);
}
