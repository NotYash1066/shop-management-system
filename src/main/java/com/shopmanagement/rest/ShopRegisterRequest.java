package com.shopmanagement.rest;

import lombok.Data;

@Data
public class ShopRegisterRequest {
    private String username;
    private String email;
    private String password;
    private String shopName;
}
