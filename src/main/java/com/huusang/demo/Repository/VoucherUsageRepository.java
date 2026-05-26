package com.huusang.demo.Repository;

import com.huusang.demo.Entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, String> {

    boolean existsByVoucherIdAndUserId(String voucherId, String userId);
}