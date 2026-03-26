package com.shopmanagement.security;

public final class Permission {

    public static final String INVENTORY_READ = "inventory:read";
    public static final String INVENTORY_WRITE = "inventory:write";
    public static final String CATEGORY_READ = "category:read";
    public static final String CATEGORY_WRITE = "category:write";
    public static final String SUPPLIER_READ = "supplier:read";
    public static final String SUPPLIER_WRITE = "supplier:write";
    public static final String EMPLOYEE_READ = "employee:read";
    public static final String EMPLOYEE_WRITE = "employee:write";
    public static final String ORDER_CREATE = "order:create";
    public static final String ORDER_READ = "order:read";
    public static final String DASHBOARD_READ = "dashboard:read";

    private Permission() {
    }
}
