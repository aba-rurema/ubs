package com.ubs.service;

import com.ubs.entity.Bill;
import com.ubs.entity.Customer;
import com.ubs.entity.NotificationChannel;
import com.ubs.entity.NotificationType;
import com.ubs.entity.Payment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BillingNotificationService {

	private final NotificationService notificationService;

	public BillingNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public void notifyBillApproved(Bill bill) {
		Customer customer = bill.getCustomer();
		String period = formatPeriod(bill.getBillingMonth(), bill.getBillingYear());
		String meterLabel = bill.getMeter().getMeterType().name().toLowerCase();

		notificationService.createSystemNotification(
				customer,
				"Bill Approved - " + period,
				"Your " + meterLabel + " bill " + bill.getBillNumber()
						+ " for " + period + " has been approved. Amount due: "
						+ formatAmount(bill.getTotalAmount()) + ". Due date: " + bill.getDueDate() + ".",
				NotificationType.BILL_APPROVED,
				NotificationChannel.EMAIL
		);
	}

	public void notifyPaymentConfirmed(Bill bill, Payment payment) {
		Customer customer = bill.getCustomer();

		notificationService.createSystemNotification(
				customer,
				"Payment Received - Bill Paid",
				"Payment of " + formatAmount(payment.getAmount()) + " received for bill "
						+ bill.getBillNumber() + ". Your bill is now fully paid. Thank you.",
				NotificationType.PAYMENT_CONFIRMATION,
				NotificationChannel.EMAIL
		);
	}

	public void notifyOverdue(Bill bill) {
		Customer customer = bill.getCustomer();
		String period = formatPeriod(bill.getBillingMonth(), bill.getBillingYear());

		notificationService.createSystemNotification(
				customer,
				"Overdue Bill - " + period,
				"Your bill " + bill.getBillNumber() + " for " + period
						+ " is overdue. Outstanding balance: " + formatAmount(bill.getBalance())
						+ ". A penalty has been applied per your tariff.",
				NotificationType.OVERDUE_NOTICE,
				NotificationChannel.EMAIL
		);
	}

	private String formatPeriod(int month, int year) {
		return String.format("%02d/%d", month, year);
	}

	private String formatAmount(BigDecimal amount) {
		return amount == null ? "0.00" : amount.toPlainString();
	}

}
