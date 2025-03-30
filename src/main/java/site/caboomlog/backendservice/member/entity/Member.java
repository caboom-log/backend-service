package site.caboomlog.backendservice.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
@ToString
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mb_no")
    private Long mbNo;

    @Column(name = "mb_email")
    private String mbEmail;

    @Column(name = "mb_name")
    private String mbName;

    @Column(name = "mb_password")
    private String mbPassword;

    @Column(name = "mb_mobile")
    private String mbMobile;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "withdrawal_at")
    private LocalDateTime withdrawalAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private Member(Long mbNo, String mbEmail, String mbName, String mbPassword, String mbMobile,
                   LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime withdrawalAt) {
        this.mbNo = mbNo;
        this.mbEmail = mbEmail;
        this.mbName = mbName;
        this.mbPassword = mbPassword;
        this.mbMobile = mbMobile;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.withdrawalAt = withdrawalAt;
    }

    public static Member ofExistingMember(Long mbNo, String mbEmail, String mbName, String mbPassword, String mbMobile,
                                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Member(mbNo, mbEmail, mbName, mbPassword, mbMobile, createdAt, updatedAt, null);
    }

    public void update(String mbName, String mbMobile) {
        this.mbName = mbName;
        this.mbMobile = mbMobile;
    }

    public void updatePassword(String newPassword) {
        this.mbPassword = newPassword;
    }

    public void withdraw() {
        this.withdrawalAt = LocalDateTime.now();
    }
}
