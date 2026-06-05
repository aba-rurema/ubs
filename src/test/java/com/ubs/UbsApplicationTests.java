package com.ubs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:ubs_test;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"app.jwt.secret=test-jwt-secret-key-minimum-32-characters-long",
		"app.jwt.expiration-ms=3600000",
		"app.jwt.refresh-expiration-ms=604800000",
		"app.billing.vat-rate=18.0",
		"app.billing.penalty-rate=5.0",
		"app.billing.due-days=30",
		"app.billing.rates.water=500.00",
		"app.billing.rates.electricity=120.00",
		"app.billing.overdue-cron=0 0 1 * * *",
		"app.mail.enabled=false",
		"app.mail.from=dev@ubs.local",
		"app.mail.from-name=Utility Billing System",
		"app.password-reset.expiration-minutes=60",
		"app.password-reset.base-url=http://localhost:3000/reset-password",
		"app.otp.expiration-minutes=10",
		"app.otp.code-length=6"
})
class UbsApplicationTests {

	@Test
	void contextLoads() {
	}

}
