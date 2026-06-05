package com.ubs.config;

import com.ubs.entity.Customer;
import com.ubs.entity.CustomerStatus;
import com.ubs.entity.Meter;
import com.ubs.entity.MeterStatus;
import com.ubs.entity.MeterType;
import com.ubs.entity.Role;
import com.ubs.entity.Tariff;
import com.ubs.entity.User;
import com.ubs.repository.CustomerRepository;
import com.ubs.repository.MeterRepository;
import com.ubs.repository.TariffRepository;
import com.ubs.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final MeterRepository meterRepository;
	private final TariffRepository tariffRepository;
	private final PasswordEncoder passwordEncoder;

	public DataSeeder(UserRepository userRepository,
					  CustomerRepository customerRepository,
					  MeterRepository meterRepository,
					  TariffRepository tariffRepository,
					  PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.customerRepository = customerRepository;
		this.meterRepository = meterRepository;
		this.tariffRepository = tariffRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		seedTariffs();
		seedStaffUsers();
		seedDemoCustomer();
		log.info("Demo data ready — login via Swagger: admin/Admin@123, operator/Operator@123, finance/Finance@123, customer/Customer@123");
	}

	private void seedTariffs() {
		seedTariffIfMissing(MeterType.WATER, new BigDecimal("500.00"), new BigDecimal("1000.00"),
				new BigDecimal("18.00"), new BigDecimal("5.00"), "Water supply tariff");
		seedTariffIfMissing(MeterType.ELECTRICITY, new BigDecimal("120.00"), new BigDecimal("2000.00"),
				new BigDecimal("18.00"), new BigDecimal("5.00"), "Electricity supply tariff");
	}

	private void seedTariffIfMissing(MeterType meterType, BigDecimal unitRate, BigDecimal fixedCharges,
									 BigDecimal vatPercentage, BigDecimal penaltyPercentage, String description) {
		if (tariffRepository.existsByMeterType(meterType)) {
			return;
		}
		tariffRepository.save(Tariff.builder()
				.meterType(meterType)
				.unitRate(unitRate)
				.fixedCharges(fixedCharges)
				.vatPercentage(vatPercentage)
				.penaltyPercentage(penaltyPercentage)
				.description(description)
				.active(true)
				.build());
	}

	private void seedStaffUsers() {
		seedUserIfMissing("admin", "admin@ubs.local", "Admin@123", Set.of(Role.ROLE_ADMIN), null);
		seedUserIfMissing("operator", "operator@ubs.local", "Operator@123", Set.of(Role.ROLE_OPERATOR), null);
		seedUserIfMissing("finance", "finance@ubs.local", "Finance@123", Set.of(Role.ROLE_FINANCE), null);
	}

	private void seedDemoCustomer() {
		if (userRepository.existsByUsername("customer")) {
			return;
		}

		Customer customer = customerRepository.save(Customer.builder()
				.fullNames("Jane Customer")
				.nationalId("1199880011223344")
				.email("customer@ubs.local")
				.phone("+250788999888")
				.address("KG 456 St, Kigali, Rwanda")
				.status(CustomerStatus.ACTIVE)
				.build());

		seedUserIfMissing("customer", "customer@ubs.local", "Customer@123", Set.of(Role.ROLE_CUSTOMER), customer);

		if (!meterRepository.existsByMeterNumber("WTR-001")) {
			meterRepository.save(Meter.builder()
					.meterNumber("WTR-001")
					.meterType(MeterType.WATER)
					.installationDate(LocalDate.of(2024, 1, 15))
					.status(MeterStatus.ACTIVE)
					.customer(customer)
					.build());
		}

		if (!meterRepository.existsByMeterNumber("ELC-001")) {
			meterRepository.save(Meter.builder()
					.meterNumber("ELC-001")
					.meterType(MeterType.ELECTRICITY)
					.installationDate(LocalDate.of(2024, 1, 15))
					.status(MeterStatus.ACTIVE)
					.customer(customer)
					.build());
		}
	}

	private void seedUserIfMissing(String username, String email, String rawPassword, Set<Role> roles, Customer customer) {
		if (userRepository.existsByUsername(username)) {
			return;
		}
		userRepository.save(User.builder()
				.username(username)
				.email(email)
				.password(passwordEncoder.encode(rawPassword))
				.roles(roles)
				.customer(customer)
				.build());
	}

}
