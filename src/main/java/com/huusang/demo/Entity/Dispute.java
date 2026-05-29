package com.huusang.demo.Entity;

import com.huusang.demo.Enum.DisputeStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bảng disputes — lưu các khiếu nại của người mua về một shop order.
 * Mỗi ShopOrder chỉ được tạo tối đa 1 dispute (OneToOne).
 *
 * Khi Admin phán quyết:
 *  - RESOLVED_BUYER_WIN  → logic service sẽ trừ ShopWallet.balance (hoàn tiền ngầm định).
 *  - RESOLVED_SELLER_WIN → không cần thêm thao tác tài chính.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "disputes")
public class Dispute {

    @Id
    String id;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = DisputeStatus.OPEN;
    }

    /** ShopOrder bị khiếu nại — mỗi ShopOrder chỉ có tối đa 1 dispute */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_order_id", nullable = false, unique = true)
    ShopOrder shopOrder;

    /** Người mua gửi khiếu nại */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    User buyer;

    /** Lý do khiếu nại từ người mua */
    @Column(columnDefinition = "TEXT", nullable = false)
    String reason;

    /** Trạng thái xử lý của dispute */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    DisputeStatus status;

    /** Ghi chú phán quyết từ Admin */
    @Column(name = "admin_note", columnDefinition = "TEXT")
    String adminNote;

    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @Column(name = "resolved_at")
    LocalDateTime resolvedAt;
}
