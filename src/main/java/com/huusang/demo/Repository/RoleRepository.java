package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Role;
import com.huusang.demo.Enum.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByName(String name);

}
