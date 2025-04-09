package site.caboomlog.backendservice.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMbUuid(String mbUuid);
    Optional<Member> findByMbEmail(String mbEmail);
}
