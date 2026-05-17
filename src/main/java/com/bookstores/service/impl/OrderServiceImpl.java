package com.bookstores.service.impl;

import com.bookstores.DTO.CreateOrderRequest;
import com.bookstores.DTO.OrderDTO;
import com.bookstores.common.DomainConstants;
import com.bookstores.entity.Book;
import com.bookstores.entity.Order;
import com.bookstores.entity.OrderItem;
import com.bookstores.entity.UserBehavior;
import com.bookstores.repository.BookRepository;
import com.bookstores.repository.OrderItemRepository;
import com.bookstores.repository.OrderRepository;
import com.bookstores.repository.UserBehaviorRepository;
import com.bookstores.service.OrderService;
import com.bookstores.service.UserContextService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bookstores.entity.UserVoucher;
import com.bookstores.entity.Voucher;
import com.bookstores.repository.UserVoucherRepository;
import com.bookstores.repository.VoucherRepository;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;
    private final UserBehaviorRepository userBehaviorRepository;
    private final UserContextService userContextService;
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            BookRepository bookRepository,
            UserBehaviorRepository userBehaviorRepository,
            UserContextService userContextService,
            VoucherRepository voucherRepository,
            UserVoucherRepository userVoucherRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.bookRepository = bookRepository;
        this.userBehaviorRepository = userBehaviorRepository;
        this.userContextService = userContextService;
        this.voucherRepository = voucherRepository;
        this.userVoucherRepository = userVoucherRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> history() {
        var user = userContextService.requireCurrentUser();
        return orderRepository.findByUser_IdOrderByOrderDateDesc(user.getId()).stream()
                .map(OrderDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO create(CreateOrderRequest req) {
        var user = userContextService.requireCurrentUser();
        if (DomainConstants.USER_LOCKED.equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account locked");
        }

        double total = 0;
        for (CreateOrderRequest.Line line : req.getItems()) {
            Book book = bookRepository.findById(line.getBookId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown book: " + line.getBookId()));
            if (!isSellable(book)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not available: " + book.getId());
            }
            int stock = book.getStockQuantity() == null ? 0 : book.getStockQuantity();
            if (stock < line.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for book: " + book.getId());
            }
            total += book.getPrice() * line.getQuantity();
        }

        Voucher appliedVoucher = null;
        UserVoucher appliedUserVoucher = null;

        if (req.getVoucherId() != null || (req.getVoucherCode() != null && !req.getVoucherCode().isBlank())) {
            Voucher v = null;
            if (req.getVoucherId() != null) {
                v = voucherRepository.findById(req.getVoucherId()).orElse(null);
            } else {
                v = voucherRepository.findByCode(req.getVoucherCode()).orElse(null);
            }

            if (v != null) {
                UserVoucher uv = userVoucherRepository.findByUser_IdAndVoucher_Id(user.getId(), v.getId()).orElse(null);
                if (uv != null && !uv.getIsUsed() && "ACTIVE".equals(v.getStatus())) {
                    if (v.getEndDate() == null || v.getEndDate().isAfter(LocalDateTime.now())) {
                        if (v.getMinOrderValue() == null || total >= v.getMinOrderValue()) {
                            appliedVoucher = v;
                            appliedUserVoucher = uv;
                            
                            double discount = 0;
                            if ("PERCENTAGE".equals(v.getDiscountType())) {
                                discount = total * (v.getDiscountValue() / 100.0);
                                if (v.getMaxDiscountAmount() != null && discount > v.getMaxDiscountAmount()) {
                                    discount = v.getMaxDiscountAmount();
                                }
                            } else {
                                discount = v.getDiscountValue();
                            }
                            
                            total -= discount;
                            if (total < 0) total = 0;
                        } else {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order total does not meet minimum value for voucher");
                        }
                    } else {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher expired");
                    }
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher invalid or already used");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher not found");
            }
        }

        Order order =
                Order.builder()
                        .user(user)
                        .orderDate(LocalDateTime.now())
                        .totalAmount(total)
                        .status(DomainConstants.ORDER_NEW)
                        .note(req.getNote())
                        .voucher(appliedVoucher)
                        .build();
        order = orderRepository.save(order);

        if (appliedUserVoucher != null) {
            appliedUserVoucher.setIsUsed(true);
            appliedUserVoucher.setUsedAt(LocalDateTime.now());
            userVoucherRepository.save(appliedUserVoucher);
            
            appliedVoucher.setUsedCount((appliedVoucher.getUsedCount() == null ? 0 : appliedVoucher.getUsedCount()) + 1);
            voucherRepository.save(appliedVoucher);
        }

        List<OrderItem> persisted = new ArrayList<>();
        for (CreateOrderRequest.Line line : req.getItems()) {
            Book book = bookRepository.findById(line.getBookId()).orElseThrow();
            int stock = book.getStockQuantity() == null ? 0 : book.getStockQuantity();
            book.setStockQuantity(stock - line.getQuantity());
            bookRepository.save(book);

            OrderItem oi =
                    OrderItem.builder()
                            .order(order)
                            .book(book)
                            .quantity(line.getQuantity())
                            .price(book.getPrice())
                            .build();
            persisted.add(orderItemRepository.save(oi));

            userBehaviorRepository.save(
                    UserBehavior.builder()
                            .user(user)
                            .book(book)
                            .actionType(DomainConstants.BEHAVIOR_PURCHASE)
                            .actionTime(LocalDateTime.now())
                            .build());
        }
        order.setOrderItems(persisted);
        return OrderDTO.fromEntity(order);
    }

    private static boolean isSellable(Book b) {
        String a = b.getApprovalStatus();
        boolean approved = a == null || DomainConstants.APPROVAL_APPROVED.equalsIgnoreCase(a);
        int stock = b.getStockQuantity() == null ? 0 : b.getStockQuantity();
        return approved && stock > 0;
    }
}
