package com.bookstores.repository;

import com.bookstores.entity.UserBehavior;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Integer> {

    List<UserBehavior> findByUser_IdOrderByActionTimeDesc(Integer userId);

    List<UserBehavior> findByUser_IdAndActionTypeInOrderByActionTimeDesc(
            Integer userId, Collection<String> actionTypes);

    @org.springframework.data.jpa.repository.Query(
            "SELECT ub.book.category.id FROM UserBehavior ub " +
            "WHERE ub.user.id = :userId AND ub.actionType IN ('PURCHASE', 'VIEW') " +
            "GROUP BY ub.book.category.id " +
            "ORDER BY COUNT(ub.id) DESC")
    List<Integer> findTopCategoryIdsByUserId(@org.springframework.data.repository.query.Param("userId") Integer userId, org.springframework.data.domain.Pageable pageable);
}
