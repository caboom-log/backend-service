package site.caboomlog.backendservice.role.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class Role {

    @Id
    @Column(name = "role_id")
    private String roleId;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "role_description")
    private String roleDescription;

    private Role(String roleId, String roleName, String roleDescription) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleDescription = roleDescription;
    }

    public static Role ofNewRole(String roleId, String roleName, String roleDescription) {
        return new Role(roleId, roleName, roleDescription);
    }
}
