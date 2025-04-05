package site.caboomlog.backendservice.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.role.entity.Role;

public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByRoleId(String roleId);
}
