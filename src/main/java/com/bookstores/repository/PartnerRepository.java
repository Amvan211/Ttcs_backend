package com.bookstores.repository;

import com.bookstores.entity.Partner;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerRepository extends JpaRepository<Partner, Integer> {

    Optional<Partner> findByUser_Id(Integer userId);
}
