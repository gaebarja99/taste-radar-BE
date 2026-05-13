package com.tasteradar.domain.user.repository;

import com.tasteradar.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	/**
	 * {@link User} 의 {@code @SQLRestriction("is_deleted = false")} 때문에 일반 {@link #findByEmail} 은
	 * 소프트 삭제된 행을 찾지 못한다. 카카오 upsert 시 native 로 조회한다.
	 */
	@Query(value = "SELECT * FROM users WHERE email = :email LIMIT 1", nativeQuery = true)
	Optional<User> findByEmailIncludingDeleted(@Param("email") String email);
}
