package com.ubs.service;

import com.ubs.entity.Bill;
import com.ubs.entity.Customer;
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
				NotificationType.BILL_APPROVED
		);
	}

	public void notifyPaymentConfirmed(Bill bill, Payment payment) {
		Customer customer = bill.getCustomer();

		notificationService.createSystemNotification(
				customer,
				"Payment Received - Bill Paid",
				"Payment of " + formatAmount(payment.getAmount()) + " received for bill "
						+ bill.getBillNumber() + ". Your bill is now fully paid. Thank you.",
				NotificationType.PAYMENT_CONFIRMATION
		);
	}

	public void notifyPartialPaymentReceived(Bill bill, Payment payment, BigDecimal remainingBalance) {
		Customer customer = bill.getCustomer();

		notificationService.createSystemNotification(
				customer,
				"Partial Payment Received - " + bill.getBillNumber(),
				"Payment of " + formatAmount(payment.getAmount()) + " received for bill "
						+ bill.getBillNumber() + ". Remaining balance: "
						+ formatAmount(remainingBalance) + ". Please pay the outstanding amount by "
						+ bill.getDueDate() + ".",
				NotificationType.PAYMENT_CONFIRMATION
		);
	}

	public void notifyPenaltyApplied(Bill bill, BigDecimal penaltyAmount, String reason) {
		if (penaltyAmount == null || penaltyAmount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}

		Customer customer = bill.getCustomer();
		notificationService.createSystemNotification(
				customer,
				"Late Payment Penalty Applied - " + bill.getBillNumber(),
				"A late payment penalty of " + formatAmount(penaltyAmount) + " has been applied "
						+ reason + ". Bill: " + bill.getBillNumber()
						+ ". Updated outstanding balance: " + formatAmount(bill.getBalance()) + ".",
				NotificationType.OVERDUE_NOTICE
		);
	}

	private String formatPeriod(int month, int year) {
		return String.format("%02d/%d", month, year);
	}

	private String formatAmount(BigDecimal amount) {
		return amount == null ? "0.00" : amount.toPlainString();
	}

}
