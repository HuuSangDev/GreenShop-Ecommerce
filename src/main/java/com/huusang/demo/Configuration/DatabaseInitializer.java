package com.huusang.demo.Configuration;

import com.huusang.demo.Entity.Permission;
import com.huusang.demo.Entity.Role;
import com.huusang.demo.Entity.User;
import com.huusang.demo.Enum.RoleName;
import com.huusang.demo.Repository.PermissionRepository;
import com.huusang.demo.Repository.RoleRepository;
import com.huusang.demo.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DatabaseInitializer implements ApplicationRunner {

    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    UserRepository userRepository;

    @Override
    @Transactional // Đảm bảo nếu lỗi thì rollback toàn bộ, không bị rác DB
    public void run(ApplicationArguments args) throws Exception {

        log.info("--- BẮT ĐẦU KHỞI TẠO DỮ LIỆU BẢO MẬT ---");

        // ======================================================
        // BƯỚC 1: TẠO TOÀN BỘ PERMISSION (NẾU CHƯA CÓ)
        // ======================================================
        String[][] allPermissions = {
                // Nhóm Buyer
                {"VIEW_PRODUCT", "Xem sản phẩm"},
                {"CREATE_ORDER", "Đặt hàng"},
                {"VIEW_MY_ORDER", "Xem đơn hàng cá nhân"},
                {"CREATE_REVIEW", "Đánh giá sản phẩm"},

                // Nhóm Seller
                {"CREATE_PRODUCT", "Đăng bán sản phẩm"},
                {"UPDATE_PRODUCT", "Sửa sản phẩm"},
                {"DELETE_PRODUCT", "Xóa sản phẩm"},
                {"VIEW_SHOP_ORDERS", "Xem đơn hàng của shop"},
                {"UPDATE_ORDER_STATUS", "Cập nhật trạng thái đơn hàng"},

                // Nhóm Admin
                {"MANAGE_USER", "Quản lý tài khoản"},
                {"VERIFY_SELLER", "Duyệt gian hàng"},
                {"MANAGE_CATEGORY", "Quản lý danh mục"},
                {"MODERATE_PRODUCT", "Kiểm duyệt sản phẩm vi phạm"}
        };

        for (String[] p : allPermissions) {
            if (!permissionRepository.existsById(p[0])) {
                permissionRepository.save(Permission.builder()
                        .name(p[0])
                        .description(p[1])
                        .build());
            }
        }
        log.info(">> Đã bơm xong danh sách Permission!");

        // ======================================================
        // BƯỚC 2: TẠO ROLE VÀ GẮN PERMISSION VÀO ROLE
        // ======================================================
        if (roleRepository.count() == 0) {

            // 1. Phân loại quyền cho BUYER
            List<String> buyerPermNames = List.of("VIEW_PRODUCT", "CREATE_ORDER", "VIEW_MY_ORDER", "CREATE_REVIEW");
            Set<Permission> buyerPerms = new HashSet<>(permissionRepository.findAllById(buyerPermNames));

            Role buyerRole = Role.builder()
                    .name(RoleName.BUYER.name())
                    .description("Khách hàng mua sắm")
                    .permissions(buyerPerms) // GẮN QUYỀN VÀO ĐÂY
                    .build();

            // 2. Phân loại quyền cho SELLER (Seller có quyền của chính họ + quyền của Buyer)
            List<String> sellerPermNames = List.of(
                    "VIEW_PRODUCT", "CREATE_ORDER", "VIEW_MY_ORDER", "CREATE_REVIEW", // Quyền Buyer
                    "CREATE_PRODUCT", "UPDATE_PRODUCT", "DELETE_PRODUCT", "VIEW_SHOP_ORDERS", "UPDATE_ORDER_STATUS" // Quyền Seller
            );
            Set<Permission> sellerPerms = new HashSet<>(permissionRepository.findAllById(sellerPermNames));

            Role sellerRole = Role.builder()
                    .name(RoleName.SELLER.name())
                    .description("Chủ shop bán hàng")
                    .permissions(sellerPerms) // GẮN QUYỀN VÀO ĐÂY
                    .build();

            // 3. Phân loại quyền cho ADMIN
            List<String> adminPermNames = List.of("MANAGE_USER", "VERIFY_SELLER", "MANAGE_CATEGORY", "MODERATE_PRODUCT");
            Set<Permission> adminPerms = new HashSet<>(permissionRepository.findAllById(adminPermNames));

            Role adminRole = Role.builder()
                    .name(RoleName.ADMIN.name())
                    .description("Quản trị viên toàn hệ thống")
                    .permissions(adminPerms) // GẮN QUYỀN VÀO ĐÂY
                    .build();

            // Lưu cả 3 Role xuống Database
            roleRepository.saveAll(List.of(buyerRole, sellerRole, adminRole));
            log.info(">> Đã khởi tạo thành công 3 Role kèm theo Quyền hạn tương ứng!");
        }

        // ======================================================
        // BƯỚC 3: TẠO TÀI KHOẢN ADMIN MẶC ĐỊNH (NẾU CHƯA CÓ)
        // ======================================================
        String adminEmail = "admin@gmail.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            Role adminRole = roleRepository.findById(RoleName.ADMIN.name())
                    .orElseThrow(() -> new RuntimeException("Role ADMIN chưa được khởi tạo!"));

            User adminUser = User.builder()
                    .username("admin")
                    .email(adminEmail)
                    .password(encoder.encode("admin"))
                    .fullName("Administrator")
                    .active(true)
                    .roles(new HashSet<>(List.of(adminRole)))
                    .build();

            userRepository.save(adminUser);
            log.info(">> Đã tạo tài khoản Admin mặc định: {} / admin", adminEmail);
        } else {
            log.info(">> Tài khoản Admin đã tồn tại, bỏ qua.");
        }

        log.info("--- HOÀN TẤT KHỞI TẠO ---");
    }
}