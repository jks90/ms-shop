package com.makingcleancode.shop.service;

import com.makingcleancode.repository.UserRepository;
import com.makingcleancode.repository.entities.User;
import com.makingcleancode.users.dto.LoginDto;
import com.makingcleancode.users.dto.SignUpDto;
import com.makingcleancode.users.dto.TokenDto;
import com.makingcleancode.users.service.AuthService;
import com.makingcleancode.users.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decorates library AuthService to keep ms-shop specific data in sync.
 *
 * Requirement: when a user is created in users table, create its related
 * store_customer row in ms-shop domain.
 */
@Service
@Primary
@RequiredArgsConstructor
public class MsShopAuthService implements AuthService {

    private final AuthServiceImpl delegate;
    private final UserRepository userRepository;
    private final StoreCustomerProvisioningService storeCustomerProvisioningService;

    @Override
    @Transactional
    public void signup(SignUpDto signUpDto) {
        delegate.signup(signUpDto);

        User user = userRepository.findUserByUsername(signUpDto.getUsername())
                .orElseThrow(() -> new IllegalStateException("User created but not found by username"));

        storeCustomerProvisioningService.ensureCustomerForAuthUser(
                user.getId(),
                signUpDto.getEmail(),
                signUpDto.getUsername(),
                null
        );
    }

    @Override
    public TokenDto login(LoginDto loginDto) {
        return delegate.login(loginDto);
    }

    @Override
    public void confirmUser(String encodedToken) {
        delegate.confirmUser(encodedToken);
    }
}
