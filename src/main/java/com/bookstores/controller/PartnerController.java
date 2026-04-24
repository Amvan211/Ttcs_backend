package com.bookstores.controller;

import com.bookstores.DTO.BookDTO;
import com.bookstores.DTO.IncomeStat;
import com.bookstores.DTO.OrderDTO;
import com.bookstores.DTO.OrderStatusUpdateRequest;
import com.bookstores.DTO.PartnerBookRequest;
import com.bookstores.DTO.PartnerRegisterRequest;
import com.bookstores.entity.Partner;
import com.bookstores.service.PartnerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/partner")
public class PartnerController {

    private final PartnerService partnerService;

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @PostMapping("/register")
    public ResponseEntity<Partner> register(@Valid @RequestBody PartnerRegisterRequest request) {
        return ResponseEntity.ok(partnerService.registerStore(request));
    }

    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> partnerOrders() {
        return ResponseEntity.ok(partnerService.listOrdersForPartner());
    }

    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/inventory")
    public ResponseEntity<List<BookDTO>> inventory() {
        return ResponseEntity.ok(partnerService.inventory());
    }

    @PreAuthorize("hasRole('PARTNER')")
    @PostMapping("/books")
    public ResponseEntity<BookDTO> addBook(@Valid @RequestBody PartnerBookRequest request) {
        return ResponseEntity.ok(partnerService.addBook(request));
    }

    @PreAuthorize("hasRole('PARTNER')")
    @PutMapping("/books/{id}")
    public ResponseEntity<BookDTO> updateBook(
            @PathVariable Integer id, @Valid @RequestBody PartnerBookRequest request) {
        return ResponseEntity.ok(partnerService.updateBook(id, request));
    }

    @PreAuthorize("hasRole('PARTNER')")
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Integer id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(partnerService.updateOrderStatus(id, request));
    }

    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/stats")
    public ResponseEntity<IncomeStat> stats() {
        return ResponseEntity.ok(partnerService.stats());
    }

    @PreAuthorize("hasRole('PARTNER')")
    @DeleteMapping("/books/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Integer id) {
        partnerService.deleteBook(id);
        return ResponseEntity.ok("Đã xóa sách thành công!");
    }
}
