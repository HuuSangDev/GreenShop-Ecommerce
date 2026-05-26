package com.huusang.demo.Repository;

import com.huusang.demo.Entity.ShopApplication;
import com.huusang.demo.Enum.ShopApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopApplicationRepository extends JpaRepository<ShopApplication, String> {

    // Tìm đơn của 1 user
    List<ShopApplication> findByUserId(String userId);

    // Tìm đơn PENDING của 1 user (ngăn nộp 2 đơn cùng lúc)
    Optional<ShopApplication> findByUserIdAndStatus(String userId, ShopApplicationStatus status);

    // Lấy tất cả đơn theo trạng thái (Admin filter)
    List<ShopApplication> findByStatus(ShopApplicationStatus status);
}
