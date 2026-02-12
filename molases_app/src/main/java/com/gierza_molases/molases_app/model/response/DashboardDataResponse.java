package com.gierza_molases.molases_app.model.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.model.Delivery;

public class DashboardDataResponse {
	private RevenueSummary revenueSummary;
	private OperationSummary operationSummary;

	private Map<String, BigDecimal> monthlyIncome;
	private List<CustomerPayments> upcommingDueLoanPayments; // contains 'loan' payments that with in this month or over
																// due

	private List<Delivery> upcommingDelivery; // upcomming delivery for this month to month

	public DashboardDataResponse(RevenueSummary revenueSummary, Map<String, BigDecimal> monthlyIncome,
			OperationSummary operationSummary, List<CustomerPayments> upcommingDueLoanPayments,
			List<Delivery> upcommingDelivery) {
		this.revenueSummary = revenueSummary;
		this.monthlyIncome = monthlyIncome;
		this.operationSummary = operationSummary;
		this.upcommingDueLoanPayments = upcommingDueLoanPayments;
		this.upcommingDelivery = upcommingDelivery;
	}

	public RevenueSummary getRevenueSummary() {
		return revenueSummary;
	}

	public void setRevenueSummary(RevenueSummary revenueSummary) {
		this.revenueSummary = revenueSummary;
	}

	public Map<String, BigDecimal> getMonthlyIncome() {
		return monthlyIncome;
	}

	public OperationSummary getOperationSummary() {
		return operationSummary;
	}

	public List<CustomerPayments> getUpcommingDueLoanPayments() {
		return upcommingDueLoanPayments;
	}

	public List<Delivery> getUpComingDelivery() {
		return upcommingDelivery;
	}

}
