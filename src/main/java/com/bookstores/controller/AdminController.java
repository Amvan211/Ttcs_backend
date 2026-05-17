package com.bookstores.controller;

import com.bookstores.DTO.AdminBookUpsertRequest;
import com.bookstores.DTO.AdminCategoryUpsertRequest;
import com.bookstores.DTO.AdminOrderUpsertRequest;
import com.bookstores.DTO.AdminUserUpsertRequest;
import com.bookstores.DTO.AdminViewModels;
import com.bookstores.DTO.BookApproveRequest;
import com.bookstores.DTO.BookDTO;
import com.bookstores.DTO.CategoryListDTO;
import com.bookstores.DTO.OrderDTO;
import com.bookstores.DTO.UserDTO;
import com.bookstores.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard/overview")
    public ResponseEntity<AdminViewModels.DashboardOverview> dashboardOverview() {
        return ResponseEntity.ok(adminService.dashboardOverview());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<AdminViewModels.OrderRow>> orders() {
        return ResponseEntity.ok(adminService.listOrdersForAdmin());
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<AdminViewModels.ReviewRow>> reviews() {
        return ResponseEntity.ok(adminService.listReviewsForAdmin());
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer id) {
        adminService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> users() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody AdminUserUpsertRequest request) {
        return ResponseEntity.ok(adminService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Integer id, @Valid @RequestBody AdminUserUpsertRequest request) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    @PutMapping("/users/{id}/lock")
    public ResponseEntity<UserDTO> lock(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.lockUser(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryListDTO>> categories() {
        return ResponseEntity.ok(adminService.listCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryListDTO> createCategory(
            @Valid @RequestBody AdminCategoryUpsertRequest request) {
        return ResponseEntity.ok(adminService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryListDTO> updateCategory(
            @PathVariable Integer id, @Valid @RequestBody AdminCategoryUpsertRequest request) {
        return ResponseEntity.ok(adminService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        adminService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/books")
    public ResponseEntity<List<BookDTO>> books() {
        return ResponseEntity.ok(adminService.listBooks());
    }

    @PostMapping("/books")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody AdminBookUpsertRequest request) {
        return ResponseEntity.ok(adminService.createBook(request));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<BookDTO> updateBook(
            @PathVariable Integer id, @Valid @RequestBody AdminBookUpsertRequest request) {
        return ResponseEntity.ok(adminService.updateBook(id, request));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer id) {
        adminService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/books/pending")
    public ResponseEntity<List<BookDTO>> pendingBooks() {
        return ResponseEntity.ok(adminService.pendingBooks());
    }

    @PutMapping("/books/{id}/approve")
    public ResponseEntity<BookDTO> approve(
            @PathVariable Integer id, @Valid @RequestBody BookApproveRequest request) {
        return ResponseEntity.ok(adminService.approveBook(id, request));
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody AdminOrderUpsertRequest request) {
        return ResponseEntity.ok(adminService.createOrder(request));
    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<OrderDTO> updateOrder(
            @PathVariable Integer id, @Valid @RequestBody AdminOrderUpsertRequest request) {
        return ResponseEntity.ok(adminService.updateOrder(id, request));
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        adminService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/partners/pending")
    public ResponseEntity<List<com.bookstores.DTO.PartnerDTO>> pendingPartners() {
        return ResponseEntity.ok(adminService.getPendingPartners());
    }

    @PutMapping("/partners/{id}/approve")
    public ResponseEntity<com.bookstores.DTO.PartnerDTO> approvePartner(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.approvePartner(id));
    }
}
