package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Shop;
import com.huusang.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    
    // Tìm shop theo owner
    Optional<Shop> findByOwner(User owner);
    
    // Tìm shop theo owner ID
    Optional<Shop> findByOwnerId(String ownerId);
}
