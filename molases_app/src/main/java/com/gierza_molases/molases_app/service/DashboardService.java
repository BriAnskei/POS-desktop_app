package com.gierza_molases.molases_app.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.dao.DashboardDao;
import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.response.DashboardDataResponse;
import com.gierza_molases.molases_app.model.response.OperationSummary;
import com.gierza_molases.molases_app.model.response.RevenueSummary;

public class DashboardService {
	private DashboardDao dashboardDao;

	public DashboardService(DashboardDao dashboardDao) {
		this.dashboardDao = dashboardDao;
	}

	public DashboardDataResponse fetchDashBoardData(LocalDate from, LocalDate to) throws SQLException {

		RevenueSummary revenueSummary = fetchRevenueSummary(from, to);
		Map<String, BigDecimal> monthlyIncome = fetchMonthlyIncome(from, to);
		OperationSummary operationSummary = fetchOperationSummary(from, to);

		List<CustomerPayments> upcommingLoanPayments = fetchUpcommingLoanPayments();

		List<Delivery> upCommingDelivery = fetchUpCommingDelivery();

		return new DashboardDataResponse(revenueSummary, monthlyIncome, operationSummary, upcommingLoanPayments,
				upCommingDelivery);

	}

	public RevenueSummary fetchRevenueSummary(LocalDate from, LocalDate to) throws SQLException {
		return dashboardDao.getRevenueSummary(from, to);
	}

	public Map<String, BigDecimal> fetchMonthlyIncome(LocalDate from, LocalDate to) throws SQLException {
		return dashboardDao.getMonthlyIncomeThisYear();
	}

	public OperationSummary fetchOperationSummary(LocalDate from, LocalDate to) throws SQLException {
		return dashboardDao.getOperationSummary(from, to);
	}

	public List<CustomerPayments> fetchUpcommingLoanPayments() {
		return dashboardDao.fetchMonthlyLoanPaymentsIncludingOverdue();
	}

	public List<Delivery> fetchUpCommingDelivery() {
		return dashboardDao.fetchUpcomingDeliveriesThisAndNextMonth();
	}

}
