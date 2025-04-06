package site.caboomlog.backendservice.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByMbNo(Long mbNo);

    Member findByMbUuid(String mbUuid);
}
