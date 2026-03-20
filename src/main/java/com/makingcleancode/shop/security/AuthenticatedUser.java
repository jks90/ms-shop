package com.makingcleancode.shop.security;

public record AuthenticatedUser(
        Long userId,
        String email,
        String[] roles
) {
}
