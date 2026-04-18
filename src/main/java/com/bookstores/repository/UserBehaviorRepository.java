package com.bookstores.repository;

import com.bookstores.entity.UserBehavior;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Integer> {

    List<UserBehavior> findByUser_IdOrderByActionTimeDesc(Integer userId);

    List<UserBehavior> findByUser_IdAndActionTypeInOrderByActionTimeDesc(
            Integer userId, Collection<String> actionTypes);
}
