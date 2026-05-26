package com.huusang.demo.Entity;

import com.huusang.demo.Enum.ShopApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "shop_applications")
public class ShopApplication {

    @Id
    String id;

    @PrePersist
    protected void onCreate() {
        this.id = UUID.randomUUID().toString();
        this.submittedAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "shop_name", nullable = false)
    String shopName;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "tax_code")
    String taxCode;

    @Column(name = "tax_address")
    String taxAddress;

    @Column(name = "tax_full_name")
    String taxFullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ShopApplicationStatus status = ShopApplicationStatus.PENDING;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    String rejectReason;

    @Column(name = "submitted_at")
    LocalDateTime submittedAt;

    @Column(name = "resolved_at")
    LocalDateTime resolvedAt;
}
