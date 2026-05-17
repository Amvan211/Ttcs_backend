package com.bookstores.repository;

import com.bookstores.entity.UserVoucher;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {
    
    @Query("SELECT uv FROM UserVoucher uv JOIN FETCH uv.voucher WHERE uv.user.id = :userId AND uv.isUsed = false AND uv.voucher.status = 'ACTIVE' AND (uv.voucher.endDate IS NULL OR uv.voucher.endDate > CURRENT_TIMESTAMP)")
    List<UserVoucher> findAvailableByUserId(Integer userId);

    Optional<UserVoucher> findByUser_IdAndVoucher_Id(Integer userId, Integer voucherId);
}
