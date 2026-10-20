package com.bookstores.repository;

import com.bookstores.entity.UserBehavior;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Integer> {

    List<UserBehavior> findByUser_IdOrderByActionTimeDesc(Integer userId);

    List<UserBehavior> findByUser_IdAndActionTypeInOrderByActionTimeDesc(
            Integer userId, Collection<String> actionTypes);

    @Query(
            "SELECT ub.book.category.id FROM UserBehavior ub " +
            "WHERE ub.user.id = :userId AND ub.actionType IN ('PURCHASE', 'VIEW') " +
            "GROUP BY ub.book.category.id " +
            "ORDER BY COUNT(ub.id) DESC")
    List<Integer> findTopCategoryIdsByUserId(@Param("userId") Integer userId, Pageable pageable);
}

