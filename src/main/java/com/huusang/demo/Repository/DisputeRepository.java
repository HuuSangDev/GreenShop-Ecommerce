package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Dispute;
import com.huusang.demo.Enum.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, String> {

    /** Lấy tất cả dispute, tùy chọn lọc theo status */
    @Query("SELECT d FROM Dispute d WHERE (:status IS NULL OR d.status = :status) ORDER BY d.createdAt DESC")
    Page<Dispute> findAllFiltered(@Param("status") DisputeStatus status, Pageable pageable);

    /** Kiểm tra ShopOrder đã có dispute chưa */
    boolean existsByShopOrderId(Long shopOrderId);

    /** Đếm số dispute đang mở (để hiển thị trong overview dashboard) */
    long countByStatus(DisputeStatus status);
}
