package com.ubs.repository;

import com.ubs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.customer WHERE u.username = :username")
	Optional<User> findByUsername(@Param("username") String username);

	Optional<User> findByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.customer WHERE u.username = :identifier OR u.email = :identifier")
	Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

}
