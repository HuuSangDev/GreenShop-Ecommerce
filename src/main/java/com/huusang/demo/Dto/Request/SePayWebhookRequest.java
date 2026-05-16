package com.huusang.demo.Dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Payload mà SePay gửi về webhook khi có giao dịch thành công.
 * Tham khảo: https://docs.sepay.vn/webhook.html
 * <p>
 * Các field chính cần match:
 * - transferAmount  : số tiền thực nhận (so sánh với payment.amount)
 * - content         : nội dung chuyển khoản (= transactionRef "ORDER_{id}")
 * - transactionDate : thời gian giao dịch
 * - referenceCode   : mã giao dịch nội bộ của SePay
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SePayWebhookRequest {

    @JsonProperty("id")
    Long id;

    @JsonProperty("gateway")
    String gateway;

    @JsonProperty("transactionDate")
    String transactionDate;

    @JsonProperty("accountNumber")
    String accountNumber;

    @JsonProperty("content")
    String content;                 // = transactionRef ("ORDER_15")

    @JsonProperty("transferAmount")
    BigDecimal transferAmount;

    @JsonProperty("referenceCode")
    String referenceCode;           // Mã giao dịch từ SePay

    @JsonProperty("description")
    String description;
}
