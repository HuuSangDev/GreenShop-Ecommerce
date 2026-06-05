package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.UserCreationRequest;
import com.huusang.demo.Dto.Response.UserResponse;
import com.huusang.demo.Entity.Role;
import com.huusang.demo.Entity.User;
import com.huusang.demo.Enum.RoleName;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Mapper.UserMapper;
import com.huusang.demo.Repository.RoleRepository;
import com.huusang.demo.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;
import com.huusang.demo.Dto.Request.UpdateProfileRequest;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    FileStorageService fileStorageService;

    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        // Có thể mở rộng thêm setAddress nếu Entity User có address. Hiện tại User.java không có address.

        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public UserResponse uploadAvatar(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String avatarUrl = fileStorageService.storeFile(file, "avatars");

        // Xóa ảnh cũ
        if (user.getAvatar() != null) {
            fileStorageService.deleteFile(user.getAvatar());
        }

        user.setAvatar(avatarUrl);
        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public UserResponse createUser(UserCreationRequest request)
    {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.USER_EXISTED);

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber()))
            throw new AppException(ErrorCode.PHONE_NUMBER_EXISTED);

        User user=userMapper.toUser(request);
        String randomUserName= UUID.randomUUID().toString().replace("-","").substring(0, 8);
        user.setUsername("user_" + randomUserName);


        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles= new HashSet<>();
        Role defaultRole= roleRepository.getReferenceById(RoleName.BUYER.name());
        roles.add(defaultRole);
        user.setRoles(roles);
        user.setActive(true);

        //notification and mail ....
        user= userRepository.save(user);
        return userMapper.toUserResponse(user);

    }

    public UserResponse addRoletoUser(String userId, String roleName )
    {
        User user=userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.ID_USER_NOT_FOUND));

        Role role= roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NAME_NOT_FOUND));
        user.getRoles().add(role);
        user=userRepository.save(user);

        return userMapper.toUserResponse(user);
    }


    public List<UserResponse> getUsers()
    {
        //lay toan bo User goc
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> userMapper.toUserResponse(user) )
                .toList();
    }

    // 3. Hàm Lấy 1 User theo ID
    public UserResponse getUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }


    public UserResponse lockUser(String userId)
    {
        User user=userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.ID_USER_NOT_FOUND));
        user.setActive(!user.isActive());
        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.isActive()) {
            throw new AppException(ErrorCode.USER_MUST_BE_LOCKED);
        }
        userRepository.delete(user);
    }
}
