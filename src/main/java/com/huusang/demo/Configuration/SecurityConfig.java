package com.huusang.demo.Configuration;

import com.huusang.demo.Repository.InvalidatedTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SecurityConfig {

    final InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String signerkey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/users/register", "auth/**").permitAll()
                        // Category tree public — ai cũng xem được
                        .requestMatchers("/api/v1/categories/tree").permitAll()
                        .requestMatchers("/api/v1/categories/{id}").permitAll()
                        // SePay webhook — gọi từ server SePay, không có JWT user
                        .requestMatchers("/api/v1/payments/sepay/webhook").permitAll()
                        // Thông tin public của shop — khách xem không cần đăng nhập
                        .requestMatchers("GET", "/api/v1/shops/{id}").permitAll()
                        // Review list + summary public — ai cũng xem được
                        .requestMatchers("/api/v1/reviews/products/**").permitAll()
                        // Ảnh tĩnh local — phục vụ qua /images/**, không cần JWT
                        .requestMatchers("/images/**").permitAll()
                        .anyRequest().authenticated());
        // xác thực( authentication)
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder())
                .jwtAuthenticationConverter(jwtAuthenticationConverter()))

        );
        // Tắt CSRF nếu cần
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    JwtDecoder jwtDecoder() {
        // giải mã jwt
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerkey.getBytes(), "HS512");

        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
        return token -> {

            Jwt jwt = nimbusJwtDecoder.decode(token);
            String jwtId = jwt.getId();

            if (invalidatedTokenRepository.existsById(jwtId)) {

                throw new BadJwtException("Thẻ này đã bị Đăng xuất / Vô hiệu hóa!");
            }

            // Mọi thứ hoàn hảo, cho phép thẻ đi qua cửa bảo vệ!
            return jwt;
        };
    }

    // mo xe token , tim den payload -> moc chuoi quyen
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;

    }

}
