package com.bookstores.controller;

import com.bookstores.DTO.VoucherDTO;
import com.bookstores.repository.UserVoucherRepository;
import com.bookstores.service.UserContextService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final UserVoucherRepository userVoucherRepository;
    private final UserContextService userContextService;

    public VoucherController(UserVoucherRepository userVoucherRepository, UserContextService userContextService) {
        this.userVoucherRepository = userVoucherRepository;
        this.userContextService = userContextService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<VoucherDTO>> getMyVouchers() {
        var user = userContextService.requireCurrentUser();
        List<VoucherDTO> vouchers = userVoucherRepository.findAvailableByUserId(user.getId())
                .stream()
                .map(uv -> VoucherDTO.fromEntity(uv.getVoucher()))
                .toList();
        return ResponseEntity.ok(vouchers);
    }
}
