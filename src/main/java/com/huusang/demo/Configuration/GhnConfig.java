package com.huusang.demo.Configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Bind cấu hình GHN từ application.yaml:
 *   ghn.api.fee-url  → feeUrl
 *   ghn.api.token    → token
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ghn.api")
public class GhnConfig {

    /** URL endpoint tính phí vận chuyển của GHN */
    private String feeUrl;

    /** Token xác thực từ GHN Merchant Dashboard */
    private String token;
}
