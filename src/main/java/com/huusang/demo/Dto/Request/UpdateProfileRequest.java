package com.huusang.demo.Dto.Request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProfileRequest {
    String fullName;
    String phoneNumber;
    String address;
}
