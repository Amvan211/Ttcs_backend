package com.bookstores.controller;

import com.bookstores.DTO.CreateOrderRequest;
import com.bookstores.DTO.OrderDTO;
import com.bookstores.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bookstores.service.UserContextService;
import com.bookstores.repository.OrderRepository;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserContextService userContextService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, UserContextService userContextService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.userContextService = userContextService;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.create(request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<OrderDTO>> history() {
        return ResponseEntity.ok(orderService.history());
    }

    @GetMapping("/check-purchase")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.Map<String, Boolean>> checkPurchase(@org.springframework.web.bind.annotation.RequestParam Integer bookId) {
        var user = userContextService.requireCurrentUser();
        boolean hasPurchased = orderRepository.hasUserPurchasedBook(user.getId(), bookId);
        System.out.println("CHECK PURCHASE FOR USER " + user.getId() + " BOOK " + bookId + " -> " + hasPurchased);
        return ResponseEntity.ok(java.util.Map.of("canReview", hasPurchased));
    }
}
