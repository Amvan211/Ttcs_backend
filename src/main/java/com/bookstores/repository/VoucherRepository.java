package com.bookstores.repository;

import com.bookstores.entity.Voucher;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Optional<Voucher> findByCode(String code);
}
