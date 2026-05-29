package com.huusang.demo.Service;

import com.huusang.demo.Configuration.GhnConfig;
import com.huusang.demo.Dto.Request.GhnFeeRequest;
import com.huusang.demo.Dto.Response.GhnFeeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

/**
 * Service tích hợp API GHN (Giao Hàng Nhanh) để tính phí vận chuyển.
 * <p>
 * Sử dụng {@link RestClient} của Spring Boot 3 (thay thế RestTemplate).
 * <p>
 * Logic phân loại dịch vụ:
 *   - service_type_id = 2 → Hàng nhẹ: kích thước lấy từ length/width/height/weight truyền vào
 *   - service_type_id = 5 → Hàng nặng: kích thước lấy từ items[].length/width/height/weight
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingService {

    private final GhnConfig ghnConfig;

    // RestClient được khởi tạo lazy khi lần đầu gọi — thread-safe vì RestClient immutable
    private volatile RestClient restClient;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Tính phí vận chuyển qua GHN API.
     * <p>
     * Logic tự động chọn service_type_id:
     * - Nếu {@code items} khác null và không rỗng → service_type_id = 5 (Hàng nặng)
     * - Ngược lại                                  → service_type_id = 2 (Hàng nhẹ)
     *
     * @param shopId          ID shop trên GHN — truyền vào Header "ShopId"
     * @param fromDistrictId  ID quận/huyện gửi hàng
     * @param fromWardCode    Mã phường/xã gửi hàng
     * @param toDistrictId    ID quận/huyện nhận hàng
     * @param toWardCode      Mã phường/xã nhận hàng
     * @param weight          Khối lượng tổng (gram)
     * @param length          Chiều dài gói hàng (cm) — dùng cho hàng nhẹ
     * @param width           Chiều rộng gói hàng (cm) — dùng cho hàng nhẹ
     * @param height          Chiều cao gói hàng (cm) — dùng cho hàng nhẹ
     * @param items           Danh sách sản phẩm (null hoặc rỗng = hàng nhẹ)
     * @return tổng phí vận chuyển (VNĐ)
     * @throws RuntimeException nếu GHN API trả lỗi hoặc mạng có vấn đề
     */
    public int calculateShippingFee(
            Integer shopId,
            Integer fromDistrictId,
            String fromWardCode,
            Integer toDistrictId,
            String toWardCode,
            Integer weight,
            Integer length,
            Integer width,
            Integer height,
            List<GhnFeeRequest.Item> items) {

        // ─── 1. Xác định service_type_id theo logic nghiệp vụ ──────────────────
        boolean isHeavy = (items != null && !items.isEmpty());
        int serviceTypeId = isHeavy ? 5 : 2;

        log.info("Calculating GHN shipping fee: shopId={}, serviceType={}, from={}/{}, to={}/{}, weight={}g",
                shopId, serviceTypeId, fromDistrictId, fromWardCode, toDistrictId, toWardCode, weight);

        // ─── 2. Build request body ──────────────────────────────────────────────
        GhnFeeRequest requestBody = GhnFeeRequest.builder()
                .serviceTypeId(serviceTypeId)
                .fromDistrictId(fromDistrictId)
                .fromWardCode(fromWardCode)
                .toDistrictId(toDistrictId)
                .toWardCode(toWardCode)
                .weight(weight)
                // Kích thước gói: luôn gửi để GHN có thể tính cả 2 chiều
                .length(length != null ? length : 1)
                .width(width != null ? width : 1)
                .height(height != null ? height : 1)
                .insuranceValue(0)
                .coupon(null)
                // items chỉ được gửi khi hàng nặng (NON_NULL sẽ bỏ qua nếu null)
                .items(isHeavy ? items : null)
                .build();

        // ─── 3. Gọi GHN API ─────────────────────────────────────────────────────
        try {
            GhnFeeResponse response = getRestClient()
                    .post()
                    .uri(ghnConfig.getFeeUrl())
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Token", ghnConfig.getToken())
                    .header("ShopId", String.valueOf(shopId))
                    .body(requestBody)
                    .retrieve()
                    // Xử lý lỗi 4xx (thường là sai tham số / shopId / district)
                    .onStatus(
                            status -> status.is4xxClientError(),
                            (req, res) -> {
                                String errorBody = new String(res.getBody().readAllBytes());
                                log.error("GHN API 4xx error: status={}, body={}", res.getStatusCode(), errorBody);
                                throw new RuntimeException(
                                        "GHN API lỗi " + res.getStatusCode().value() + ": " + errorBody
                                );
                            }
                    )
                    // Xử lý lỗi 5xx (GHN server down)
                    .onStatus(
                            status -> status.is5xxServerError(),
                            (req, res) -> {
                                log.error("GHN API 5xx server error: status={}", res.getStatusCode());
                                throw new RuntimeException("GHN server đang gặp sự cố, vui lòng thử lại sau");
                            }
                    )
                    .body(GhnFeeResponse.class);

            // ─── 4. Kiểm tra response hợp lệ ─────────────────────────────────
            if (response == null || !response.isSuccess()) {
                String msg = (response != null) ? response.getMessage() : "Null response";
                log.warn("GHN API returned non-success: {}", msg);
                throw new RuntimeException("GHN API không trả về kết quả hợp lệ: " + msg);
            }

            int totalFee = response.getTotalFee();
            log.info("GHN shipping fee calculated: {} VNĐ (serviceType={})", totalFee, serviceTypeId);
            return totalFee;

        } catch (RestClientResponseException e) {
            // Lỗi HTTP có status code (4xx/5xx) chưa được xử lý ở trên
            log.error("GHN API HTTP error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Không thể tính phí vận chuyển: GHN trả về lỗi " + e.getStatusCode().value(), e
            );

        } catch (RestClientException e) {
            // Lỗi mạng: timeout, không kết nối được GHN
            log.error("GHN API network error: {}", e.getMessage());
            throw new RuntimeException(
                    "Không thể kết nối đến GHN, vui lòng kiểm tra kết nối mạng", e
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER — Lazy init RestClient (thread-safe double-checked locking)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Khởi tạo RestClient lần đầu dùng đến.
     * RestClient của Spring Boot 3 là immutable sau khi build → an toàn khi dùng chung.
     */
    private RestClient getRestClient() {
        if (restClient == null) {
            synchronized (this) {
                if (restClient == null) {
                    restClient = RestClient.builder()
                            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .build();
                }
            }
        }
        return restClient;
    }
}
