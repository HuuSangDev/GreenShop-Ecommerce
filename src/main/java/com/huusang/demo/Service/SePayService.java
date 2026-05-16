package com.huusang.demo.Service;

import com.huusang.demo.Configuration.SePayConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
@Service
@RequiredArgsConstructor
public class SePayService {
    private final SePayConfig sePayConfig;

    public String generateQrUrl(
            BigDecimal amount,
            String content
    ) {

        return UriComponentsBuilder
                .fromHttpUrl("https://qr.sepay.vn/img")
                .queryParam("acc", sePayConfig.getAccountNumber())
                .queryParam("bank", sePayConfig.getBankCode())
                .queryParam("amount", amount.longValue())
                .queryParam("des", content)
                .queryParam("template", sePayConfig.getTemplate())
                .build()
                .toUriString();
    }
}
