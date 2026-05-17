package com.bookstores.repository;

import com.bookstores.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByMail(String mail);

    @Modifying
    @Query(value = "DELETE FROM tblchat_history WHERE userID = :userId", nativeQuery = true)
    void deleteChatHistories(@Param("userId") Integer userId);

    @Modifying
    @Query(value = "DELETE FROM tbluserbehavior WHERE userID = :userId", nativeQuery = true)
    void deleteUserBehaviorsByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query(value = "DELETE FROM tbluser_voucher WHERE user_id = :userId", nativeQuery = true)
    void deleteUserVouchersByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query(value = "UPDATE tblreview SET userid = NULL WHERE userid = :userId", nativeQuery = true)
    void unlinkReviewsByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query(value = "UPDATE tblorder SET userID = NULL WHERE userID = :userId", nativeQuery = true)
    void unlinkOrdersByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query(value = "UPDATE tblbook SET partnerID = NULL WHERE partnerID = (SELECT id FROM tblpartner WHERE userID = :userId)", nativeQuery = true)
    void unlinkPartnerBooksByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query(value = "DELETE FROM tblpartner WHERE userID = :userId", nativeQuery = true)
    void deletePartnerByUserId(@Param("userId") Integer userId);
}
