package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    /**
     * Lấy toàn bộ địa chỉ của user — địa chỉ mặc định lên đầu.
     */
    @Query("""
            SELECT a FROM Address a
            WHERE a.user.id = :userId
            ORDER BY a.isDefault DESC, a.id ASC
            """)
    List<Address> findByUserIdOrderByIsDefaultDesc(@Param("userId") String userId);

    /**
     * Lấy địa chỉ mặc định của user.
     */
    Optional<Address> findByUserIdAndIsDefaultTrue(String userId);

    /**
     * Đếm số địa chỉ của user — dùng để check địa chỉ đầu tiên.
     */
    long countByUserId(String userId);

    /**
     * Reset tất cả isDefault = false cho user — gọi trước khi set địa chỉ mới làm mặc định.
     * @Modifying + @Transactional (ở service) để đảm bảo update được commit đúng.
     */
    @Modifying
    @Query("""
            UPDATE Address a SET a.isDefault = false
            WHERE a.user.id = :userId
            """)
    void clearDefaultByUserId(@Param("userId") String userId);
}
