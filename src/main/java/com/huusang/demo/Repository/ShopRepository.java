package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Shop;
import com.huusang.demo.Entity.User;
import com.huusang.demo.Enum.ShopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    // Tìm shop theo owner
    Optional<Shop> findByOwner(User owner);

    // Tìm shop theo owner ID (UUID)
    Optional<Shop> findByOwnerId(String ownerId);

    // Tìm shop theo owner Email — dùng khi JWT subject là email
    Optional<Shop> findByOwnerEmail(String email);

    // Admin lọc shop theo trạng thái
    List<Shop> findByStatus(ShopStatus status);

    // Kiểm tra user đã có shop chưa
    boolean existsByOwnerId(String ownerId);
}
