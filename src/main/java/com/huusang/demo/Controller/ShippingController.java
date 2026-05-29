package com.huusang.demo.Controller;


import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Entity.Shop;
import com.huusang.demo.Repository.ShopRepository;
import com.huusang.demo.Service.ShippingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller cung cấp API tính phí vận chuyển qua GHN.
 * <p>
 * Test endpoint không cần JWT (được permit trong SecurityConfig).
 */
@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShippingController {

    ShippingService shippingService;
    ShopRepository shopRepository;

    /**
     * GET /api/v1/shipping/test-checkout
     * <p>
     * Mô phỏng tính phí ship khi checkout — lấy thông tin kho từ DB của Shop,
     * sau đó gọi GHN API tính phí về địa chỉ của khách hàng.
     * <p>
     * Postman (không cần JWT):
     * GET http://localhost:8080/ecommerce/api/v1/shipping/test-checkout
     *   ?myShopId=1
     *   &toDistrictId=1820
     *   &toWardCode=030712
     *   &totalWeight=3000
     *
     * @param myShopId      ID shop trong DB của bạn (không phải GHN ShopId)
     * @param toDistrictId  ID quận/huyện nhà khách hàng (theo mã GHN)
     * @param toWardCode    Mã phường/xã nhà khách hàng (theo mã GHN)
     * @param totalWeight   Tổng khối lượng đơn hàng (gram)
     * @return phí vận chuyển (VNĐ)
     */
    @GetMapping("/test-checkout")
    public ApiResponse<Integer> testCheckout(
            @RequestParam Long myShopId,
            @RequestParam Integer toDistrictId,
            @RequestParam String toWardCode,
            @RequestParam Integer totalWeight) {

        // 1. Tìm Shop trong DB
        Shop shop = shopRepository.findById(myShopId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Shop có id=" + myShopId));

        // 2. Kiểm tra shop đã cấu hình thông tin GHN chưa
        if (shop.getGhnShopId() == null || shop.getDistrictId() == null || shop.getWardCode() == null) {
            throw new RuntimeException(
                    "Shop id=" + myShopId + " chưa cấu hình thông tin GHN (ghn_shop_id / district_id / ward_code). " +
                    "Vui lòng chạy script UPDATE trong MySQL Workbench trước."
            );
        }

        // 3. Lấy thông số GHN của Shop (địa chỉ kho gửi hàng)
        Integer ghnShopId     = shop.getGhnShopId();
        Integer fromDistrictId = shop.getDistrictId();
        String  fromWardCode   = shop.getWardCode();

        // 4. Gọi GHN API tính phí — Test hàng nhẹ (items = null → service_type_id = 2)
        Integer shippingFee = shippingService.calculateShippingFee(
                ghnShopId,
                fromDistrictId,
                fromWardCode,
                toDistrictId,
                toWardCode,
                totalWeight,
                30, 40, 20,  // kích thước mặc định (cm)
                null          // null = hàng nhẹ
        );

        return ApiResponse.<Integer>builder()
                .message("Tính phí vận chuyển thành công (GHN Hàng Nhẹ)")
                .result(shippingFee)
                .build();
    }
}
