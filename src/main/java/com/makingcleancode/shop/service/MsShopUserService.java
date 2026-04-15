package com.makingcleancode.shop.service;

import com.makingcleancode.users.dto.UserDto;
import com.makingcleancode.users.dto.UserListDto;
import com.makingcleancode.users.dto.UserUpdateDto;
import com.makingcleancode.users.service.UserService;
import com.makingcleancode.users.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
@RequiredArgsConstructor
public class MsShopUserService implements UserService {

    private final UserServiceImpl delegate;
    private final StoreCustomerProvisioningService storeCustomerProvisioningService;

    @Override
    public UserDto getSelf() {
        return delegate.getSelf();
    }

    @Override
    public UserDto updateSelf(UserUpdateDto updateDto) {
        return delegate.updateSelf(updateDto);
    }

    @Override
    public UserListDto getAllUsers() {
        return delegate.getAllUsers();
    }

    @Override
    @Transactional
    public UserDto createUser(UserUpdateDto createDto) {
        UserDto created = delegate.createUser(createDto);
        if (created != null && created.getId() != null) {
            String email = created.getDetail() != null ? created.getDetail().getEmail() : createDto.getDetailEmail();
            String name = created.getDetail() != null ? created.getDetail().getName() : createDto.getName();
            String phone = created.getDetail() != null ? created.getDetail().getPhone() : createDto.getPhone();
            storeCustomerProvisioningService.ensureCustomerForAuthUser(created.getId(), email, name, phone);
        }
        return created;
    }

    @Override
    public UserDto getUserById(Long id) {
        return delegate.getUserById(id);
    }

    @Override
    public UserDto updateUserById(Long id, UserUpdateDto updateDto) {
        return delegate.updateUserById(id, updateDto);
    }

    @Override
    public void deleteUserById(Long id) {
        delegate.deleteUserById(id);
    }
}
