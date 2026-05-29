package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.AddressRequest;
import com.huusang.demo.Dto.Response.AddressResponse;
import com.huusang.demo.Entity.Address;
import com.huusang.demo.Entity.User;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Repository.AddressRepository;
import com.huusang.demo.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AddressService {

    AddressRepository addressRepository;
    UserRepository    userRepository;

    // ═══════════════════════════════════════════════════════════════════════════
    // POST /api/v1/addresses — Thêm địa chỉ mới
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Thêm địa chỉ giao hàng mới cho user.
     * <p>
     * Logic:
     * - Nếu user CHƯA CÓ địa chỉ nào → ép buộc isDefault = true (dù request gửi false)
     * - Nếu user muốn set làm mặc định (isDefault = true) → reset tất cả địa chỉ cũ về false
     * - Nếu user không muốn set mặc định → giữ nguyên, lưu với isDefault = false
     */
    @Transactional
    public AddressResponse addAddress(String userEmail, AddressRequest request) {
        User user = resolveUser(userEmail);

        // Kiểm tra đây có phải địa chỉ đầu tiên không
        long existingCount = addressRepository.countByUserId(user.getId());
        boolean isFirstAddress = (existingCount == 0);

        // Xác định isDefault thật sự
        boolean shouldBeDefault = isFirstAddress || Boolean.TRUE.equals(request.getIsDefault());

        if (shouldBeDefault && !isFirstAddress) {
            // Có địa chỉ cũ + user muốn set mặc định → reset tất cả cũ về false
            addressRepository.clearDefaultByUserId(user.getId());
            log.info("Cleared old default address for userId={}", user.getId());
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .street(request.getStreet())
                .ward(request.getWard())
                .district(request.getDistrict())
                .province(request.getProvince())
                .wardCode(request.getWardCode())
                .districtId(request.getDistrictId())
                .provinceId(request.getProvinceId())
                .isDefault(shouldBeDefault)
                .build();

        Address saved = addressRepository.save(address);

        log.info("Address added: id={}, userId={}, isDefault={}", saved.getId(), user.getId(), shouldBeDefault);
        return toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GET /api/v1/addresses — Danh sách địa chỉ (mặc định lên đầu)
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(String userEmail) {
        User user = resolveUser(userEmail);
        return addressRepository
                .findByUserIdOrderByIsDefaultDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GET /api/v1/addresses/default — Lấy địa chỉ mặc định
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lấy địa chỉ mặc định của user — dùng để auto-fill trang Checkout.
     * Ném lỗi rõ ràng nếu user chưa có địa chỉ nào.
     */
    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress(String userEmail) {
        User user = resolveUser(userEmail);
        Address address = addressRepository
                .findByUserIdAndIsDefaultTrue(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NO_DEFAULT));
        return toResponse(address);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PUT /api/v1/addresses/{id} — Cập nhật địa chỉ
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public AddressResponse updateAddress(String userEmail, String addressId, AddressRequest request) {
        User user = resolveUser(userEmail);
        Address address = getOwnedAddress(addressId, user.getId());

        // Nếu muốn set làm mặc định → reset các địa chỉ khác
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.clearDefaultByUserId(user.getId());
        }

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setStreet(request.getStreet());
        address.setWard(request.getWard());
        address.setDistrict(request.getDistrict());
        address.setProvince(request.getProvince());
        address.setWardCode(request.getWardCode());
        address.setDistrictId(request.getDistrictId());
        address.setProvinceId(request.getProvinceId());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        Address saved = addressRepository.save(address);
        log.info("Address updated: id={}, userId={}", saved.getId(), user.getId());
        return toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PATCH /api/v1/addresses/{id}/set-default — Đặt làm mặc định
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public AddressResponse setDefault(String userEmail, String addressId) {
        User user = resolveUser(userEmail);
        Address address = getOwnedAddress(addressId, user.getId());

        // Reset tất cả địa chỉ cũ → set địa chỉ này làm mặc định
        addressRepository.clearDefaultByUserId(user.getId());
        address.setIsDefault(true);

        Address saved = addressRepository.save(address);
        log.info("Default address set: id={}, userId={}", saved.getId(), user.getId());
        return toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE /api/v1/addresses/{id} — Xóa địa chỉ
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public void deleteAddress(String userEmail, String addressId) {
        User user = resolveUser(userEmail);
        Address address = getOwnedAddress(addressId, user.getId());

        // Không cho xóa địa chỉ mặc định (tránh user mất địa chỉ mặc định)
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            throw new AppException(ErrorCode.ADDRESS_DEFAULT_CANNOT_DELETE);
        }

        addressRepository.delete(address);
        log.info("Address deleted: id={}, userId={}", addressId, user.getId());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * Lấy Address theo id + validate ownership.
     * Ném lỗi nếu không tìm thấy hoặc không phải của user này.
     */
    private Address getOwnedAddress(String addressId, String userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.ADDRESS_NOT_OWNED);
        }
        return address;
    }

    /** Map Entity → Response (thêm fullAddress tiện lợi). */
    private AddressResponse toResponse(Address a) {
        String fullAddress = String.join(", ", a.getStreet(), a.getWard(), a.getDistrict(), a.getProvince());
        return AddressResponse.builder()
                .id(a.getId())
                .fullName(a.getFullName())
                .phone(a.getPhone())
                .street(a.getStreet())
                .ward(a.getWard())
                .district(a.getDistrict())
                .province(a.getProvince())
                .fullAddress(fullAddress)
                .wardCode(a.getWardCode())
                .districtId(a.getDistrictId())
                .provinceId(a.getProvinceId())
                .isDefault(a.getIsDefault())
                .build();
    }
}
