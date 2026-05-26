package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Withdrawal;
import com.huusang.demo.Enum.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, String> {

    List<Withdrawal> findByShopIdOrderByRequestedAtDesc(Long shopId);

    List<Withdrawal> findByStatus(WithdrawalStatus status);
}
