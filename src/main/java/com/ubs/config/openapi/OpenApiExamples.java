package com.ubs.config.openapi;

/**
 * Example JSON payloads displayed in Swagger UI for requests and responses.
 */
public final class OpenApiExamples {

	public static final String EXAMPLE_UUID = "1";

	public static final String LOGIN_REQUEST = """
			{
			  "usernameOrEmail": "john.doe",
			  "password": "Password123"
			}
			""";

	public static final String VERIFY_OTP_REQUEST = """
			{
			  "sessionId": 1,
			  "otpCode": "123456"
			}
			""";

	public static final String OTP_CHALLENGE_RESPONSE = """
			{
			  "sessionId": 1,
			  "message": "A 6-digit verification code has been sent to your email. Use it to sign in.",
			  "maskedEmail": "j*****@example.com",
			  "expiresInMinutes": 10,
			  "purpose": "LOGIN"
			}
			""";

	public static final String REGISTER_REQUEST = """
			{
			  "username": "john.doe",
			  "email": "john.doe@example.com",
			  "password": "Password123",
			  "fullNames": "John Doe",
			  "nationalId": "1199880022334455",
			  "phone": "+250788123456",
			  "address": "KG 123 St, Kigali, Rwanda"
			}
			""";

	public static final String AUTH_RESPONSE = """
			{
			  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
			  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
			  "tokenType": "Bearer",
			  "expiresIn": 3600000,
			  "user": {
			    "id": 1,
			    "username": "john.doe",
			    "email": "john.doe@example.com",
			    "roles": ["ROLE_CUSTOMER"]
			  }
			}
			""";

	public static final String CUSTOMER_CREATE_REQUEST = """
			{
			  "fullNames": "John Doe",
			  "nationalId": "1199880022334455",
			  "email": "john.doe@example.com",
			  "phone": "+250788123456",
			  "address": "KG 123 St, Kigali, Rwanda",
			  "status": "ACTIVE"
			}
			""";

	public static final String CUSTOMER_RESPONSE = """
			{
			  "id": 1,
			  "fullNames": "John Doe",
			  "nationalId": "1199880022334455",
			  "email": "john.doe@example.com",
			  "phone": "+250788123456",
			  "address": "KG 123 St, Kigali, Rwanda",
			  "status": "ACTIVE",
			  "createdAt": "2024-06-01T10:00:00Z",
			  "updatedAt": "2024-06-01T10:00:00Z"
			}
			""";

	public static final String METER_READING_CREATE_REQUEST = """
			{
			  "meterId": 1,
			  "currentReading": 1250.500,
			  "readingMonth": 6,
			  "readingYear": 2024,
			  "readingDate": "2024-06-28"
			}
			""";

	public static final String BILL_GENERATE_REQUEST = """
			{
			  "meterReadingId": 1
			}
			""";

	public static final String BILL_RESPONSE = """
			{
			  "id": 1,
			  "billNumber": "BILL-202406-00001",
			  "customerId": 1,
			  "customerFullNames": "John Doe",
			  "meterId": 1,
			  "meterNumber": "WM-2024-001",
			  "meterType": "WATER",
			  "meterReadingId": 1,
			  "consumption": 70.250,
			  "unitRate": 500.00,
			  "baseAmount": 35125.00,
			  "vatAmount": 6322.50,
			  "penaltyAmount": 0.00,
			  "totalAmount": 41447.50,
			  "amountPaid": 0.00,
			  "balance": 41447.50,
			  "status": "PENDING",
			  "billingMonth": 6,
			  "billingYear": 2024,
			  "dueDate": "2024-07-28",
			  "createdAt": "2024-06-29T10:00:00Z",
			  "updatedAt": "2024-06-29T10:00:00Z"
			}
			""";

	public static final String PAYMENT_CREATE_REQUEST = """
			{
			  "billId": 1,
			  "amount": 10000.00,
			  "payFullBalance": false,
			  "paymentMethod": "MOBILE_MONEY",
			  "paymentDate": "2024-07-15",
			  "notes": "Partial payment - first installment"
			}
			""";

	public static final String PAYMENT_RESPONSE = """
			{
			  "id": 1,
			  "paymentReference": "PAY-2024-000001",
			  "billId": 1,
			  "billNumber": "BILL-202406-00001",
			  "customerId": 1,
			  "customerFullNames": "John Doe",
			  "amount": 10000.00,
			  "paymentMethod": "MOBILE_MONEY",
			  "paymentType": "PARTIAL",
			  "paymentDate": "2024-07-15",
			  "balanceBefore": 41447.50,
			  "balanceAfter": 31447.50,
			  "billBalance": 31447.50,
			  "billStatus": "PARTIALLY_PAID",
			  "notes": "Partial payment - first installment",
			  "createdAt": "2024-07-15T14:30:00Z"
			}
			""";

	public static final String ERROR_RESPONSE = """
			{
			  "timestamp": "2024-06-29T10:00:00Z",
			  "status": 400,
			  "error": "Validation Failed",
			  "message": "fullNames: Full names are required",
			  "path": "/api/customers"
			}
			""";

	private OpenApiExamples() {
	}

}
