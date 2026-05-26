package com.huusang.demo.Repository;

import com.huusang.demo.Entity.ShopWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopWalletRepository extends JpaRepository<ShopWallet, String> {

    Optional<ShopWallet> findByShopId(Long shopId);
}
