package com.huusang.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_product_id",   columnList = "product_id"),
        @Index(name = "idx_review_user_id",      columnList = "user_id"),
        @Index(name = "idx_review_order_item_id",columnList = "order_item_id"),
        @Index(name = "idx_review_is_deleted",   columnList = "is_deleted")
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    // Ràng buộc: mỗi order_item chỉ được review 1 lần (unique)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    OrderItem orderItem;

    @Column(nullable = false)
    Integer rating; // 1–5

    @Column(columnDefinition = "TEXT")
    String comment;

    // Danh sách URL ảnh review (lưu dạng JSON string hoặc separate table)
    @ElementCollection
    @CollectionTable(name = "review_images", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url", nullable = false)
    @Builder.Default
    List<String> imageUrls = new ArrayList<>();

    // ─── Seller reply ────────────────────────────────────────────────────────
    @Column(name = "seller_reply", columnDefinition = "TEXT")
    String sellerReply;

    @Column(name = "seller_replied_at")
    LocalDateTime sellerRepliedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_by_shop_id")
    Shop repliedByShop;

    // ─── Soft delete ─────────────────────────────────────────────────────────
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    Boolean isDeleted = false;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by_admin_id")
    User deletedByAdmin;

    @Column(name = "delete_reason", length = 500)
    String deleteReason;

    // ─── Timestamps ──────────────────────────────────────────────────────────
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
