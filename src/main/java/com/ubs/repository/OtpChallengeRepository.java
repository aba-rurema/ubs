package com.ubs.repository;

import com.ubs.entity.OtpChallenge;
import com.ubs.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

	Optional<OtpChallenge> findByIdAndUsedFalse(Long id);

	@Modifying
	@Query("UPDATE OtpChallenge c SET c.used = true WHERE c.email = :email AND c.purpose = :purpose AND c.used = false")
	void invalidateActiveByEmailAndPurpose(@Param("email") String email, @Param("purpose") OtpPurpose purpose);

	@Modifying
	@Query("UPDATE OtpChallenge c SET c.used = true WHERE c.user.id = :userId AND c.purpose = :purpose AND c.used = false")
	void invalidateActiveByUserAndPurpose(@Param("userId") Long userId, @Param("purpose") OtpPurpose purpose);

}
