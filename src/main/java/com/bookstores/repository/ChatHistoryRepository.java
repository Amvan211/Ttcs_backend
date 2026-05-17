package com.bookstores.repository;

import com.bookstores.entity.ChatHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Integer> {
    List<ChatHistory> findByUserIdOrderBySentAtAsc(Integer userId);
}
