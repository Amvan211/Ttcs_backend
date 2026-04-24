package com.bookstores.service;

import com.bookstores.DTO.BookDTO;
import com.bookstores.DTO.IncomeStat;
import com.bookstores.DTO.OrderDTO;
import com.bookstores.DTO.OrderStatusUpdateRequest;
import com.bookstores.DTO.PartnerBookRequest;
import com.bookstores.DTO.PartnerRegisterRequest;
import com.bookstores.entity.Partner;
import java.util.List;

public interface PartnerService {

    Partner registerStore(PartnerRegisterRequest req);

    List<BookDTO> inventory();

    BookDTO addBook(PartnerBookRequest req);

    BookDTO updateBook(Integer bookId, PartnerBookRequest req);

    OrderDTO updateOrderStatus(Integer orderId, OrderStatusUpdateRequest req);

    IncomeStat stats();

    void deleteBook(Integer bookId);

    List<OrderDTO> listOrdersForPartner();
}
