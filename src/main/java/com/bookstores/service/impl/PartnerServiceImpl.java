package com.bookstores.service.impl;

import com.bookstores.DTO.BookDTO;
import com.bookstores.DTO.IncomeStat;
import com.bookstores.DTO.OrderDTO;
import com.bookstores.DTO.OrderStatusUpdateRequest;
import com.bookstores.DTO.PartnerBookRequest;
import com.bookstores.DTO.PartnerRegisterRequest;
import com.bookstores.common.DomainConstants;
import com.bookstores.entity.Book;
import com.bookstores.entity.Partner;
import com.bookstores.repository.BookRepository;
import com.bookstores.repository.CategoryRepository;
import com.bookstores.repository.OrderItemRepository;
import com.bookstores.repository.OrderRepository;
import com.bookstores.repository.PartnerRepository;
import com.bookstores.repository.RoleRepository;
import com.bookstores.repository.UserRepository;
import com.bookstores.service.PartnerService;
import com.bookstores.service.UserContextService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final UserContextService userContextService;

    public PartnerServiceImpl(
            PartnerRepository partnerRepository,
            RoleRepository roleRepository,
            CategoryRepository categoryRepository,
            BookRepository bookRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            UserContextService userContextService) {
        this.partnerRepository = partnerRepository;
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.userContextService = userContextService;
    }

    @Override
    @Transactional
    public Partner registerStore(PartnerRegisterRequest req) {
        var user = userContextService.requireCurrentUser();
        if (partnerRepository.findByUser_Id(user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Partner profile already exists");
        }
        Partner p =
                Partner.builder()
                        .storeName(req.getStoreName())
                        .address(req.getAddress())
                        .description(req.getDescription())
                        .status(DomainConstants.PARTNER_STORE_PENDING)
                        .user(user)
                        .build();
        p = partnerRepository.save(p);
        var partnerRole =
                roleRepository
                        .findByRoleName(DomainConstants.ROLE_PARTNER)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Missing role"));
        user.setRole(partnerRole);
        userRepository.save(user);
        return p;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> inventory() {
        Partner p = currentPartnerOrThrow();
        return bookRepository.findByPartner_Id(p.getId()).stream().map(BookDTO::fromEntity).toList();
    }

    @Override
    @Transactional
    public BookDTO addBook(PartnerBookRequest req) {
        Partner p = currentPartnerOrThrow();
        var cat =
                categoryRepository
                        .findById(req.getCategoryId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category"));
        Book b =
                Book.builder()
                        .title(req.getTitle())
                        .author(req.getAuthor())
                        .price(req.getPrice())
                        .stockQuantity(req.getStockQuantity())
                        .description(req.getDescription())
                        .coverImageUrl(req.getCoverImageUrl())
                        .category(cat)
                        .partner(p)
                        .approvalStatus(DomainConstants.APPROVAL_PENDING)
                        .build();
        b = bookRepository.save(b);
        return BookDTO.fromEntity(b);
    }

    @Override
    @Transactional
    public void deleteBook(Integer bookId) {
        Partner p = currentPartnerOrThrow();
        Book b = bookRepository.findById(bookId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        if (!b.getPartner().getId().equals(p.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete this book");
        }
        bookRepository.delete(b);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Integer orderId, OrderStatusUpdateRequest req) {
        Partner p = currentPartnerOrThrow();
        if (!orderRepository.existsByIdAndPartner(orderId, p.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for this partner");
        }
        var order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        order.setStatus(req.getStatus());
        orderRepository.save(order);
        return OrderDTO.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public IncomeStat stats() {
        Partner p = currentPartnerOrThrow();
        Double revenue = orderItemRepository.sumRevenueByPartner(p.getId());
        long orders = orderItemRepository.countDistinctOrdersByPartner(p.getId());
        return new IncomeStat(revenue == null ? 0d : revenue, orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> listOrdersForPartner() {
        Partner p = currentPartnerOrThrow();
        return orderRepository.findDistinctByPartnerBooks(p.getId()).stream()
                .map(OrderDTO::fromEntity)
                .toList();
    }

    private Partner currentPartnerOrThrow() {
        var user = userContextService.requireCurrentUser();
        return partnerRepository
                .findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a partner"));
    }
}
