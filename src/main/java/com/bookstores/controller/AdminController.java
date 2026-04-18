package com.bookstores.controller;

import com.bookstores.DTO.AdminViewModels;
import com.bookstores.DTO.BookApproveRequest;
import com.bookstores.DTO.BookDTO;
import com.bookstores.DTO.UserDTO;
import com.bookstores.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> users() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PutMapping("/users/{id}/lock")
    public ResponseEntity<UserDTO> lock(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.lockUser(id));
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
}
