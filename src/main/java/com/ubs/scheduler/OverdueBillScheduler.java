package com.ubs.scheduler;

import com.ubs.service.BillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OverdueBillScheduler {

	private static final Logger log = LoggerFactory.getLogger(OverdueBillScheduler.class);

	private final BillService billService;

	public OverdueBillScheduler(BillService billService) {
		this.billService = billService;
	}

	@Scheduled(cron = "${app.billing.overdue-cron:0 0 1 * * *}")
	public void processOverdueBills() {
		log.info("Running overdue bill processing job");
		billService.processOverdueBills();
	}

}
