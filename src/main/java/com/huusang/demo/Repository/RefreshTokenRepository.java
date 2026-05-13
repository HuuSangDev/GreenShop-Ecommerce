package com.huusang.demo.Repository;

import com.huusang.demo.Entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,String> {
    Optional<RefreshToken> findByToken(String token);

    // Dùng khi người dùng bấm Đăng xuất -> Xóa thẻ này đi
    void deleteByToken(String token);
}
