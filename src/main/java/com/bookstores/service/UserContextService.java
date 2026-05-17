package com.bookstores.service;

import com.bookstores.entity.User;

public interface UserContextService {

    User requireCurrentUser();
    User getCurrentUser();
}
