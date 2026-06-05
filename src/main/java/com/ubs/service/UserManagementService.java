package com.ubs.service;

import com.ubs.dto.user.UserCreateRequest;
import com.ubs.dto.user.UserResponse;
import com.ubs.dto.user.UserUpdateRequest;
import com.ubs.entity.User;
import com.ubs.exception.DuplicateResourceException;
import com.ubs.exception.ResourceNotFoundException;
import com.ubs.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserManagementService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UserResponse create(UserCreateRequest request) {
		if (userRepository.existsByUsername(request.username())) {
			throw new DuplicateResourceException("Username is already taken");
		}
		if (userRepository.existsByEmail(request.email())) {
			throw new DuplicateResourceException("Email is already registered");
		}

		User user = User.builder()
				.username(request.username().trim())
				.email(request.email().trim().toLowerCase())
				.password(passwordEncoder.encode(request.password()))
				.roles(request.roles())
				.build();

		return toResponse(userRepository.save(user));
	}

	@Transactional
	public UserResponse update(Long id, UserUpdateRequest request) {
		User user = findUserOrThrow(id);

		if (request.email() != null) {
			String email = request.email().trim().toLowerCase();
			if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
				throw new DuplicateResourceException("Email is already registered");
			}
			user.setEmail(email);
		}

		user.setRoles(request.roles());

		if (request.enabled() != null) {
			user.setEnabled(request.enabled());
		}
		if (request.accountNonLocked() != null) {
			user.setAccountNonLocked(request.accountNonLocked());
		}

		return toResponse(userRepository.save(user));
	}

	@Transactional(readOnly = true)
	public UserResponse getById(Long id) {
		return toResponse(findUserOrThrow(id));
	}

	@Transactional(readOnly = true)
	public Page<UserResponse> getAll(Pageable pageable) {
		return userRepository.findAll(pageable).map(this::toResponse);
	}

	@Transactional
	public void delete(Long id) {
		User user = findUserOrThrow(id);
		userRepository.delete(user);
	}

	private User findUserOrThrow(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getCustomer() != null ? user.getCustomer().getId() : null,
				user.getUsername(),
				user.getEmail(),
				user.getRoles(),
				user.isEnabled(),
				user.isAccountNonLocked(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}

}
