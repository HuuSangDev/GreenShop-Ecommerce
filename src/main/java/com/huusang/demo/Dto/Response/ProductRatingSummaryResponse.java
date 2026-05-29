package com.huusang.demo.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductRatingSummaryResponse {

    Long       productId;
    String     productName;
    BigDecimal averageRating;   // Làm tròn 1 chữ số thập phân, ví dụ: 4.3
    Integer    totalReviews;

    // Số lượng từng mức sao: key = 1..5, value = count
    Map<Integer, Long>   starCounts;

    // Phần trăm từng mức sao: key = 1..5, value = 0.0–100.0
    Map<Integer, Double> starPercentages;
}
