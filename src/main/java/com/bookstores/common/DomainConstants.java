package com.bookstores.common;

public final class DomainConstants {

    /** Persisted name for end-user accounts (legacy rows may still read CUSTOMER). */
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    /** API / JWT role name aligned with the front-end (READER). */
    public static final String ROLE_READER = "READER";

    public static final String ROLE_PARTNER = "PARTNER";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String USER_ACTIVE = "ACTIVE";
    public static final String USER_LOCKED = "LOCKED";

    public static final String APPROVAL_PENDING = "PENDING";
    public static final String APPROVAL_APPROVED = "APPROVED";
    public static final String APPROVAL_REJECTED = "REJECTED";

    /** Trạng thái đơn — khớp UI cửa hàng đối tác / khách (front_end). */
    public static final String ORDER_NEW = "Mới";

    public static final String ORDER_PROCESSING = "Đang xử lý";
    public static final String ORDER_SHIPPING = "Đang giao";
    public static final String ORDER_DELIVERED = "Đã giao";
    public static final String ORDER_CANCELLED = "Đã hủy";

    /** Legacy value; prefer {@link #ORDER_NEW}. */
    public static final String ORDER_PENDING = "PENDING";

    public static final String BEHAVIOR_VIEW = "VIEW";
    public static final String BEHAVIOR_PURCHASE = "PURCHASE";

    public static final String PARTNER_STORE_PENDING = "PENDING";
    public static final String PARTNER_STORE_ACTIVE = "ACTIVE";

    private DomainConstants() {}
}
