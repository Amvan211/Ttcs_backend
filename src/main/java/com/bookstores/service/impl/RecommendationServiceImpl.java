package com.bookstores.service.impl;

import com.bookstores.DTO.BookDTO;
import com.bookstores.common.DomainConstants;
import com.bookstores.entity.Book;
import com.bookstores.entity.UserBehavior;
import com.bookstores.repository.BookRepository;
import com.bookstores.repository.UserBehaviorRepository;
import com.bookstores.service.RecommendationService;
import com.bookstores.service.UserContextService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final UserBehaviorRepository userBehaviorRepository;
    private final BookRepository bookRepository;
    private final UserContextService userContextService;

    public RecommendationServiceImpl(
            UserBehaviorRepository userBehaviorRepository,
            BookRepository bookRepository,
            UserContextService userContextService) {
        this.userBehaviorRepository = userBehaviorRepository;
        this.bookRepository = bookRepository;
        this.userContextService = userContextService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> recommendForCurrentUser() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return new ArrayList<>();
        }
        var user = userContextService.requireCurrentUser();
        List<UserBehavior> behaviors =
                userBehaviorRepository.findByUser_IdAndActionTypeInOrderByActionTimeDesc(
                        user.getId(), List.of(DomainConstants.BEHAVIOR_VIEW, DomainConstants.BEHAVIOR_PURCHASE));

        Set<Integer> seenBookIds = new HashSet<>();
        Set<Integer> categoryIds = new HashSet<>();
        Set<String> authors = new HashSet<>();
        for (UserBehavior ub : behaviors) {
            if (ub.getBook() == null) {
                continue;
            }
            Book b = ub.getBook();
            seenBookIds.add(b.getId());
            if (b.getCategory() != null) {
                categoryIds.add(b.getCategory().getId());
            }
            if (b.getAuthor() != null && !b.getAuthor().isBlank()) {
                authors.add(b.getAuthor());
            }
        }

        Collection<Integer> excludeForQuery = notInGuard(seenBookIds);

        LinkedHashSet<BookDTO> out = new LinkedHashSet<>();
        if (!categoryIds.isEmpty()) {
            bookRepository.recommendByCategories(categoryIds, excludeForQuery).stream()
                    .map(BookDTO::fromEntity)
                    .forEach(out::add);
        }
        if (!authors.isEmpty()) {
            bookRepository.recommendByAuthors(authors, excludeForQuery).stream()
                    .map(BookDTO::fromEntity)
                    .forEach(out::add);
        }
        return new ArrayList<>(out).stream().limit(30).toList();
    }

    private static Collection<Integer> notInGuard(Set<Integer> seenBookIds) {
        if (seenBookIds.isEmpty()) {
            return List.of(-1);
        }
        return seenBookIds;
    }
}
