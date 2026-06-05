package com.ubs.security;

import com.ubs.entity.Role;
import com.ubs.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails {

	private final Long id;
	private final Long customerId;
	private final String username;
	private final String email;
	private final String password;
	private final Set<Role> roles;
	private final boolean enabled;
	private final boolean accountNonLocked;

	public CustomUserDetails(User user) {
		this.id = user.getId();
		this.customerId = user.getCustomer() != null ? user.getCustomer().getId() : null;
		this.username = user.getUsername();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.roles = user.getRoles();
		this.enabled = user.isEnabled();
		this.accountNonLocked = user.isAccountNonLocked();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles.stream()
				.map(role -> new SimpleGrantedAuthority(role.name()))
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
