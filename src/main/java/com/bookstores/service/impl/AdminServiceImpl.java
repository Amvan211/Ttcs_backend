package com.bookstores.service.impl;

import com.bookstores.DTO.AdminViewModels;
import com.bookstores.DTO.AdminBookUpsertRequest;
import com.bookstores.DTO.AdminCategoryUpsertRequest;
import com.bookstores.DTO.AdminOrderUpsertRequest;
import com.bookstores.DTO.AdminUserUpsertRequest;
import com.bookstores.DTO.BookApproveRequest;
import com.bookstores.DTO.BookDTO;
import com.bookstores.DTO.CategoryListDTO;
import com.bookstores.DTO.OrderDTO;
import com.bookstores.DTO.UserDTO;
import com.bookstores.common.ApiRoleNames;
import com.bookstores.common.DomainConstants;
import com.bookstores.entity.Book;
import com.bookstores.entity.Category;
import com.bookstores.entity.Order;
import com.bookstores.entity.OrderItem;
import com.bookstores.entity.Partner;
import com.bookstores.entity.Review;
import com.bookstores.entity.Role;
import com.bookstores.entity.User;
import com.bookstores.repository.BookRepository;
import com.bookstores.repository.CategoryRepository;
import com.bookstores.repository.OrderItemRepository;
import com.bookstores.repository.OrderRepository;
import com.bookstores.repository.PartnerRepository;
import com.bookstores.repository.RoleRepository;
import com.bookstores.repository.ReviewRepository;
import com.bookstores.repository.UserRepository;
import com.bookstores.service.AdminService;
import com.bookstores.service.MailService;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminServiceImpl implements AdminService {

    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final PartnerRepository partnerRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AdminServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            CategoryRepository categoryRepository,
            PartnerRepository partnerRepository,
            BookRepository bookRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ReviewRepository reviewRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.partnerRepository = partnerRepository;
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> listUsers() {
        return userRepository.findAll().stream().map(UserDTO::fromEntity).toList();
    }

    @Override
    @Transactional
    public UserDTO createUser(AdminUserUpsertRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is required");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
        }
        if (userRepository.findByUsername(req.getUsername().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username taken");
        }
        User u = new User();
        applyUserUpsert(u, req, true);
        return UserDTO.fromEntity(userRepository.save(u));
    }

    @Override
    @Transactional
    public UserDTO updateUser(Integer id, AdminUserUpsertRequest req) {
        var u = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        applyUserUpsert(u, req, false);
        return UserDTO.fromEntity(userRepository.save(u));
    }

    @Override
    @Transactional
    public UserDTO lockUser(Integer id) {
        var u = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        u.setStatus(DomainConstants.USER_LOCKED);
        u = userRepository.save(u);
        return UserDTO.fromEntity(u);
    }

    @Override
    @Transactional
    public void deleteUser(Integer id) {
        var u = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        // Clean up all foreign key dependencies in safe cascading order
        userRepository.deleteChatHistories(id);
        userRepository.deleteUserBehaviorsByUserId(id);
        userRepository.deleteUserVouchersByUserId(id);
        userRepository.unlinkReviewsByUserId(id);
        userRepository.unlinkOrdersByUserId(id);
        userRepository.unlinkPartnerBooksByUserId(id);
        userRepository.deletePartnerByUserId(id);
        
        userRepository.delete(u);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryListDTO> listCategories() {
        return categoryRepository.findAll().stream().map(this::toCategoryRow).toList();
    }

    @Override
    @Transactional
    public CategoryListDTO createCategory(AdminCategoryUpsertRequest req) {
        String name = req.getName().trim();
        if (categoryRepository.findByCategoryName(name).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category exists");
        }
        Category c = Category.builder().categoryName(name).build();
        return toCategoryRow(categoryRepository.save(c));
    }

    @Override
    @Transactional
    public CategoryListDTO updateCategory(Integer id, AdminCategoryUpsertRequest req) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        c.setCategoryName(req.getName().trim());
        return toCategoryRow(categoryRepository.save(c));
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        long count = bookRepository.countByCategory_Id(id);
        if (count > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xóa danh mục này vì đang chứa sách.");
        }
        categoryRepository.delete(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> listBooks() {
        return bookRepository.findAll().stream().map(BookDTO::fromEntity).toList();
    }

    @Override
    @Transactional
    public BookDTO createBook(AdminBookUpsertRequest req) {
        Book b = new Book();
        applyBookUpsert(b, req, true);
        return BookDTO.fromEntity(bookRepository.save(b));
    }

    @Override
    @Transactional
    public BookDTO updateBook(Integer id, AdminBookUpsertRequest req) {
        Book b = bookRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        applyBookUpsert(b, req, false);
        return BookDTO.fromEntity(bookRepository.save(b));
    }

    @Override
    @Transactional
    public void deleteBook(Integer id) {
        Book b = bookRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookRepository.delete(b);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> pendingBooks() {
        return bookRepository.findByApprovalStatus(DomainConstants.APPROVAL_PENDING).stream()
                .map(BookDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public BookDTO approveBook(Integer id, BookApproveRequest req) {
        var b = bookRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String d = req.getDecision().trim();
        if (d.equalsIgnoreCase("APPROVE")) {
            b.setApprovalStatus(DomainConstants.APPROVAL_APPROVED);
        } else if (d.equalsIgnoreCase("REJECT")) {
            b.setApprovalStatus(DomainConstants.APPROVAL_REJECTED);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "decision must be APPROVE or REJECT");
        }
        b = bookRepository.save(b);
        return BookDTO.fromEntity(b);
    }

    @Override
    @Transactional
    public OrderDTO createOrder(AdminOrderUpsertRequest req) {
        User user =
                userRepository
                        .findById(req.getUserId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown user"));
        Order o = Order.builder()
                .user(user)
                .orderDate(java.time.LocalDateTime.now())
                .status(req.getStatus() == null || req.getStatus().isBlank() ? DomainConstants.ORDER_NEW : req.getStatus().trim())
                .note(req.getNote())
                .totalAmount(0d)
                .build();
        o = orderRepository.save(o);
        replaceOrderItems(o, req.getItems());
        return OrderDTO.fromEntity(o);
}

    @Override
    @Transactional
    public OrderDTO updateOrder(Integer id, AdminOrderUpsertRequest req) {
        Order o = orderRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown user"));
            o.setUser(user);
        }
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            o.setStatus(req.getStatus().trim());
        }
        if (req.getNote() != null) {
            o.setNote(req.getNote());
        }
        if (req.getItems() != null) {
            replaceOrderItems(o, req.getItems());
        } else {
            orderRepository.save(o);
        }
        return OrderDTO.fromEntity(o);
    }

    @Override
    @Transactional
    public void deleteOrder(Integer id) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không được phép xóa đơn hàng, chỉ có thể cập nhật trạng thái");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminViewModels.OrderRow> listOrdersForAdmin() {
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .map(this::toAdminOrderRow)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminViewModels.ReviewRow> listReviewsForAdmin() {
        return reviewRepository.findAll().stream()
                .sorted(Comparator.comparing(Review::getReviewDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .map(this::toAdminReviewRow)
                .toList();
    }

    @Override
    @Transactional
    public void deleteReview(Integer id) {
        Review r = reviewRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        reviewRepository.delete(r);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminViewModels.DashboardOverview dashboardOverview() {
        return new AdminViewModels.DashboardOverview(
                salesLast7Days(), recentOrdersForDashboard(5), topSellingBooks(3));
    }

    private List<AdminViewModels.SalesPoint> salesLast7Days() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        Map<LocalDate, Double> sums = new HashMap<>();
        for (Order o : orderRepository.findAll()) {
            if (o.getOrderDate() == null) {
                continue;
            }
            LocalDate d = o.getOrderDate().toLocalDate();
            if (!d.isBefore(start) && !d.isAfter(end)) {
                sums.merge(d, o.getTotalAmount() == null ? 0d : o.getTotalAmount(), Double::sum);
            }
        }
        List<AdminViewModels.SalesPoint> out = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            double t = sums.getOrDefault(d, 0d);
            out.add(new AdminViewModels.SalesPoint(viDayLabel(d), t));
        }
        return out;
    }

    private static String viDayLabel(LocalDate d) {
        return switch (d.getDayOfWeek()) {
            case MONDAY -> "T2";
            case TUESDAY -> "T3";
            case WEDNESDAY -> "T4";
            case THURSDAY -> "T5";
            case FRIDAY -> "T6";
            case SATURDAY -> "T7";
            case SUNDAY -> "CN";
        };
    }

    private List<AdminViewModels.OrderRow> recentOrdersForDashboard(int limit) {
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .limit(limit)
                .map(this::toAdminOrderRow)
                .toList();
    }

    private List<AdminViewModels.TopBook> topSellingBooks(int limit) {
        Map<Integer, Long> counts = new HashMap<>();
        Map<Integer, Book> books = new HashMap<>();
        for (OrderItem oi : orderItemRepository.findAll()) {
            Book b = oi.getBook();
            if (b == null) {
                continue;
            }
            books.put(b.getId(), b);
            int q = oi.getQuantity() == null ? 0 : oi.getQuantity();
            counts.merge(b.getId(), (long) q, Long::sum);
        }
        return counts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(
                        e -> {
                            Book b = books.get(e.getKey());
                            long sales = e.getValue();
                            return new AdminViewModels.TopBook(
                                    b.getId(),
                                    b.getTitle(),
                                    b.getAuthor() != null ? b.getAuthor() : "",
                                    sales,
                                    formatVnd(b.getPrice()),
                                    b.getCoverImageUrl() != null ? b.getCoverImageUrl() : "");
                        })
                .toList();
    }

    private AdminViewModels.OrderRow toAdminOrderRow(Order o) {
        User u = o.getUser();
        String customer = "";
        if (u != null) {
            String fn = u.getFullName();
            customer = (fn != null && !fn.isBlank()) ? fn : u.getUsername();
        } else {
            customer = "Người dùng đã xóa";
        }
        String dateStr =
                o.getOrderDate() != null ? o.getOrderDate().toLocalDate().format(ORDER_DATE) : "";
        return new AdminViewModels.OrderRow(
                "#ORD-" + o.getId(),
                customer,
                orderType(u),
                dateStr,
                formatVnd(o.getTotalAmount()),
                adminUiStatus(o.getStatus()),
                o.getNote() != null ? o.getNote() : "-");
    }

    private static String orderType(User u) {
        if (u == null || u.getRole() == null) {
            return "Individual Reader";
        }
        String r = ApiRoleNames.toApi(u.getRole().getRoleName());
        if (DomainConstants.ROLE_PARTNER.equalsIgnoreCase(r)) {
            return "Partner";
        }
        return "Individual Reader";
    }

    private static String adminUiStatus(String status) {
        if (status == null) {
            return "processing";
        }
        String s = status.trim();
        if (s.equalsIgnoreCase(DomainConstants.ORDER_DELIVERED)
                || "HOÀN TẤT".equalsIgnoreCase(s)
                || "Hoàn thành".equalsIgnoreCase(s)
                || "completed".equalsIgnoreCase(s)
                || "DELIVERED".equalsIgnoreCase(s)
                || "COMPLETED".equalsIgnoreCase(s)) {
            return "completed";
        }
        if (s.equalsIgnoreCase(DomainConstants.ORDER_CANCELLED)
                || "cancelled".equalsIgnoreCase(s)
                || "CANCELLED".equalsIgnoreCase(s)) {
            return "cancelled";
        }
        return "processing";
    }

    private AdminViewModels.ReviewRow toAdminReviewRow(Review r) {
        String uname = r.getUser() != null ? r.getUser().getUsername() : "Người dùng đã xóa";
        String initials = uname.length() >= 1 ? uname.substring(0, 1).toUpperCase(Locale.ROOT) : "?";
        String bookTitle = r.getBook() != null ? r.getBook().getTitle() : "";
        String dateStr =
                r.getReviewDate() != null ? r.getReviewDate().toLocalDate().format(ORDER_DATE) : "";
        return new AdminViewModels.ReviewRow(
                "#REV-" + r.getId(),
                uname,
                initials,
                bookTitle,
                r.getRating() != null ? r.getRating() : 0,
                r.getComment() != null ? r.getComment() : "",
                dateStr,
                "approved");
    }

    private static String formatVnd(Double amount) {
        if (amount == null) {
            return "0đ";
        }
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(new Locale("vi", "VN"));
        sym.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", sym);
        return df.format(Math.round(amount)) + "đ";
    }

    private CategoryListDTO toCategoryRow(Category c) {
        long n = bookRepository.countByCategory_Id(c.getId());
        return CategoryListDTO.builder()
                .categoryId(c.getId())
                .id(c.getCategoryName())
                .label(c.getCategoryName())
                .count(n + " Titles")
                .build();
    }

    private void applyUserUpsert(User user, AdminUserUpsertRequest req, boolean creating) {
        if (creating || req.getUsername() != null) {
            user.setUsername(req.getUsername().trim());
        }
        if (creating || req.getPassword() != null) {
            String raw = req.getPassword();
            if (raw == null || raw.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
            }
            user.setPassword(passwordEncoder.encode(raw));
        }
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getMail() != null) user.setMail(req.getMail());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());
        if (req.getStatus() != null && !req.getStatus().isBlank()) user.setStatus(req.getStatus().trim());
        if (creating && (user.getStatus() == null || user.getStatus().isBlank())) {
            user.setStatus(DomainConstants.USER_ACTIVE);
        }
        String roleName = req.getRoleName();
        if (roleName != null && !roleName.isBlank()) {
            user.setRole(resolveRole(roleName));
        } else if (creating && user.getRole() == null) {
            user.setRole(resolveRole(DomainConstants.ROLE_READER));
        }
    }

    private Role resolveRole(String roleName) {
        String normalized = roleName.trim().toUpperCase(Locale.ROOT);
        if ("READER".equals(normalized)) normalized = DomainConstants.ROLE_READER;
        if ("CUSTOMER".equals(normalized)) normalized = DomainConstants.ROLE_CUSTOMER;
        if ("ADMIN".equals(normalized)) normalized = DomainConstants.ROLE_ADMIN;
        if ("PARTNER".equals(normalized)) normalized = DomainConstants.ROLE_PARTNER;
        final String dbRole = normalized;
        return roleRepository.findByRoleName(dbRole)
                .or(() -> {
                    if (DomainConstants.ROLE_READER.equals(dbRole)) {
                        return roleRepository.findByRoleName(DomainConstants.ROLE_CUSTOMER);
                    }
                    return java.util.Optional.empty();
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role: " + roleName));
    }

    private void applyBookUpsert(Book book, AdminBookUpsertRequest req, boolean creating) {
        if ((creating || req.getTitle() != null) && (req.getTitle() == null || req.getTitle().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        if ((creating || req.getPrice() != null) && req.getPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "price is required");
        }
        if (creating || req.getTitle() != null) book.setTitle(req.getTitle().trim());
        if (req.getAuthor() != null) book.setAuthor(req.getAuthor());
        if (creating || req.getPrice() != null) book.setPrice(req.getPrice());
        if (req.getStockQuantity() != null) book.setStockQuantity(req.getStockQuantity());
        if (req.getDescription() != null) book.setDescription(req.getDescription());
        if (req.getCoverImageUrl() != null) book.setCoverImageUrl(req.getCoverImageUrl());
        if (req.getApprovalStatus() != null && !req.getApprovalStatus().isBlank()) {
            book.setApprovalStatus(req.getApprovalStatus().trim());
        } else if (creating && (book.getApprovalStatus() == null || book.getApprovalStatus().isBlank())) {
            book.setApprovalStatus(DomainConstants.APPROVAL_APPROVED);
        }
        if (req.getCategoryId() != null) {
            Category c = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown category"));
            book.setCategory(c);
        } else if (creating) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId is required");
        }
        if (req.getPartnerId() != null) {
            Partner p = partnerRepository.findById(req.getPartnerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown partner"));
            book.setPartner(p);
        }
    }

    private void replaceOrderItems(Order order, List<AdminOrderUpsertRequest.Line> items) {
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "items is required");
        }
        orderItemRepository.deleteByOrder_Id(order.getId());
        List<OrderItem> persisted = new ArrayList<>();
        double total = 0d;
        for (AdminOrderUpsertRequest.Line line : items) {
            if (line.getBookId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid line item");
            }
            Book b = bookRepository.findById(line.getBookId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown book"));
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .book(b)
                    .quantity(line.getQuantity())
                    .price(b.getPrice())
                    .build();
            persisted.add(orderItemRepository.save(oi));
            total += b.getPrice() * line.getQuantity();
        }
        order.setOrderItems(persisted);
        order.setTotalAmount(total);
        orderRepository.save(order);
    }
    @Override
    @Transactional(readOnly = true)
    public List<com.bookstores.DTO.PartnerDTO> getPendingPartners() {
        return partnerRepository.findAll().stream()
                .filter(p -> com.bookstores.common.DomainConstants.PARTNER_STORE_PENDING.equals(p.getStatus()))
                .map(com.bookstores.DTO.PartnerDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public com.bookstores.DTO.PartnerDTO approvePartner(Integer partnerId) {
        var partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found"));
        
        partner.setStatus("APPROVED");
        partner = partnerRepository.save(partner);
        
        var user = partner.getUser();
        if (user != null) {
            var partnerRole = roleRepository.findByRoleName(com.bookstores.common.DomainConstants.ROLE_PARTNER)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Missing role"));
            user.setRole(partnerRole);
            userRepository.save(user);
        }
        
        mailService.sendPartnerApprovalEmail(partner);
        
        return com.bookstores.DTO.PartnerDTO.fromEntity(partner);
    }
}
