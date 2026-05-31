package com.huusang.demo.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewStatsResponse {
    BigDecimal averageRating;
    Integer totalReviews;
    Map<Integer, Integer> ratingDistribution;
}
