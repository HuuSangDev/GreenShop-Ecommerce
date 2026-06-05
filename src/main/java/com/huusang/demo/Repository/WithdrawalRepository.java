package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Withdrawal;
import com.huusang.demo.Enum.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, String> {
    List<Withdrawal> findByShopIdOrderByCreatedAtDesc(Long shopId);
    
    Page<Withdrawal> findByShopIsNull(Pageable pageable);
    
    long countByShopIsNullAndType(TransactionType type);
}
