package com.huusang.demo.Configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sepay")
public class SePayConfig {

    private String accountNumber;

    private String bankCode;

    private String template;
}
