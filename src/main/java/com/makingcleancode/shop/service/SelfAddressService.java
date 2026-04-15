package com.makingcleancode.shop.service;

import com.makingcleancode.shop.entity.CustomerAddress;
import com.makingcleancode.shop.entity.StoreCustomer;
import com.makingcleancode.shop.exception.NotFoundException;
import com.makingcleancode.shop.repository.CustomerAddressRepository;
import com.makingcleancode.shop.repository.StoreCustomerRepository;
import com.shop.v1.model.AddressDto;
import com.shop.v1.model.AddressRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SelfAddressService {

    private final StoreCustomerRepository storeCustomerRepository;
    private final CustomerAddressRepository customerAddressRepository;

    @Transactional(readOnly = true)
    public List<AddressDto> getSelfAddresses(Long authUserId) {
        StoreCustomer customer = findCustomer(authUserId);
        return customerAddressRepository.findAll().stream()
                .filter(a -> a.getCustomer().getId().equals(customer.getId()))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AddressDto createSelfAddress(Long authUserId, AddressRequestDto request) {
        StoreCustomer customer = findCustomer(authUserId);

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setType(request.getType() != null ? request.getType().getValue() : "SHIPPING");
        address.setRecipientName(request.getRecipientName());
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setPostalCode(request.getPostalCode());
        address.setCity(request.getCity());
        address.setStateRegion(request.getStateRegion());
        address.setCountryCode(request.getCountryCode());
        address.setPhone(request.getPhone());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        return toDto(customerAddressRepository.save(address));
    }

    @Transactional
    public AddressDto updateSelfAddress(Long authUserId, Long addressId, AddressRequestDto request) {
        StoreCustomer customer = findCustomer(authUserId);
        CustomerAddress address = customerAddressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new NotFoundException("Address not found: " + addressId));

        address.setRecipientName(request.getRecipientName());
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setPostalCode(request.getPostalCode());
        address.setCity(request.getCity());
        address.setStateRegion(request.getStateRegion());
        address.setCountryCode(request.getCountryCode());
        address.setPhone(request.getPhone());
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        return toDto(customerAddressRepository.save(address));
    }

    @Transactional
    public void deleteSelfAddress(Long authUserId, Long addressId) {
        StoreCustomer customer = findCustomer(authUserId);
        CustomerAddress address = customerAddressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new NotFoundException("Address not found: " + addressId));
        customerAddressRepository.delete(address);
    }

    private StoreCustomer findCustomer(Long authUserId) {
        return storeCustomerRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new NotFoundException("Customer not found for user " + authUserId));
    }

    private AddressDto toDto(CustomerAddress a) {
        return AddressDto.builder()
                .id(a.getId())
                .recipientName(a.getRecipientName())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .postalCode(a.getPostalCode())
                .city(a.getCity())
                .stateRegion(a.getStateRegion())
                .countryCode(a.getCountryCode())
                .phone(a.getPhone())
                .isDefault(a.getIsDefault())
                .build();
    }
}
