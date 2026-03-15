package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.AuthRequest;
import com.huusang.demo.Dto.Request.LogoutRequest;
import com.huusang.demo.Dto.Request.RefreshRequest;
import com.huusang.demo.Dto.Response.AuthResponse;
import com.huusang.demo.Dto.Response.UserResponse;
import com.huusang.demo.Entity.InvalidatedToken;
import com.huusang.demo.Entity.RefreshToken;
import com.huusang.demo.Entity.User;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Mapper.UserMapper;
import com.huusang.demo.Repository.InvalidatedTokenRepository;
import com.huusang.demo.Repository.RefreshTokenRepository;
import com.huusang.demo.Repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class AuthService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    RefreshTokenRepository refreshTokenRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;



    public AuthResponse authenticate(AuthRequest request)
    {
        User user= userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        boolean authenticated=passwordEncoder.matches(request.getPassword(),user.getPassword());
        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        var token=generateToken(user);

        RefreshToken refreshToken= createRefreshToken(user.getId());

        var userResponse= userMapper.toUserResponse(user);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .authenticated(true)
                .user(userResponse)
                .build();

    }

    // Nhớ Inject InvalidatedTokenRepository và RefreshTokenRepository vào nhé

    public void logout(LogoutRequest request, String accessToken) {
        try {
            // 1. Phân tích cái Access Token (Dùng thư viện Nimbus bạn đang có)
            SignedJWT signToken = SignedJWT.parse(accessToken);

            String jwtId = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            // 2. Tống mã số thẻ này vào Sổ Đen
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jwtId)
                    .expiryTime(expiryTime)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);

            // 3. Xóa Refresh Token dưới DB để vĩnh viễn không đổi được thẻ mới nữa
            // (Bạn nhớ tạo hàm deleteByToken trong RefreshTokenRepository nhé)
            refreshTokenRepository.deleteByToken(request.getRefreshToken());

        } catch (Exception e) {
            log.info("Token đã không hợp lệ sẵn rồi, không cần xử lý thêm");
        }
    }

    public AuthResponse refreshToken(RefreshRequest request)
    {
        RefreshToken refreshToken= refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken); // Dọn rác
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Đuổi ra ngoài bắt đăng nhập lại!
        }

        User user= refreshToken.getUser();
        var newAccessToken=generateToken(user);
        UserResponse userResponse = userMapper.toUserResponse(user);
        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken.getToken()) // Trả lại cái Refresh Token cũ để lần sau xài tiếp
                .authenticated(true)
                .user(userResponse)
                .build();

    }

    public RefreshToken createRefreshToken(String userId) {
        User user = userRepository.findById(userId).orElseThrow();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString()) // Random 1 chuỗi ngẫu nhiên không ai đoán được
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS)) // Sống lâu tới 7 ngày
                .build();

        return refreshTokenRepository.save(refreshToken);
    }


    private String generateToken(User user)
    {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("sangledev.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(5, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope",buildScope(user) )
                .jwtID(UUID.randomUUID().toString())
                .build();


        //chuyen claim vao Payload
        Payload payload =new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject= new JWSObject(jwsHeader,payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();

        } catch (JOSEException e) {
            log.error("can't create token");
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        // 1. Kiểm tra xem user có danh sách role nào không
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {

            // 2. Lặp qua từng Role của User
            user.getRoles().forEach(role -> {

                stringJoiner.add("ROLE_" + role.getName());

                // 3. Lặp qua danh sách Permission của CÁI ROLE ĐÓ
                if (role.getPermissions() != null) {
                    role.getPermissions().forEach(permission -> {
                        // Nhét tên Permission vào (Để trần, không gắn ROLE_)
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }
        return stringJoiner.toString();
    }
}
