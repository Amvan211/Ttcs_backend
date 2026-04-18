package com.bookstores.common;

/**
 * Maps persisted role names to the names exposed in JWT and API responses (aligned with the
 * front-end: READER, PARTNER, ADMIN). Legacy DB rows may still use CUSTOMER.
 */
public final class ApiRoleNames {

    private ApiRoleNames() {}

    public static String toApi(String dbRoleName) {
        if (dbRoleName == null || dbRoleName.isBlank()) {
            return DomainConstants.ROLE_READER;
        }
        if (DomainConstants.ROLE_CUSTOMER.equalsIgnoreCase(dbRoleName)) {
            return DomainConstants.ROLE_READER;
        }
        return dbRoleName;
    }
}
