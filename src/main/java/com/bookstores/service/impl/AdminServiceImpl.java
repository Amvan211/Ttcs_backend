package com.bookstores.service.impl;

import com.bookstores.DTO.AdminViewModels;
import com.bookstores.DTO.BookApproveRequest;
import com.bookstores.DTO.BookDTO;
import com.bookstores.DTO.UserDTO;
import com.bookstores.common.ApiRoleNames;
import com.bookstores.common.DomainConstants;
import com.bookstores.entity.Book;
import com.bookstores.entity.Order;
import com.bookstores.entity.OrderItem;
import com.bookstores.entity.Review;
import com.bookstores.entity.User;
import com.bookstores.repository.BookRepository;
import com.bookstores.repository.OrderItemRepository;
import com.bookstores.repository.OrderRepository;
import com.bookstores.repository.ReviewRepository;
import com.bookstores.repository.UserRepository;
import com.bookstores.service.AdminService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminServiceImpl implements AdminService {

    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;

    public AdminServiceImpl(
            UserRepository userRepository,
            BookRepository bookRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> listUsers() {
        return userRepository.findAll().stream().map(UserDTO::fromEntity).toList();
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
        String uname = r.getUser() != null ? r.getUser().getUsername() : "?";
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
}
