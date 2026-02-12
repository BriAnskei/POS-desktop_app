package com.gierza_molases.molases_app.UiController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.context.DashboardState;
import com.gierza_molases.molases_app.context.DashboardState.UpcomingDelivery;
import com.gierza_molases.molases_app.context.DashboardState.UpcomingLoanPayment;
import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.response.DashboardDataResponse;
import com.gierza_molases.molases_app.model.response.OperationSummary;
import com.gierza_molases.molases_app.model.response.RevenueSummary;
import com.gierza_molases.molases_app.service.DashboardService;

/**
 * Controller for the Dashboard page.
 *
 * Register in AppContext: public static final DashboardController
 * dashboardController = new DashboardController(new DashboardState());
 */
public class DashboardController {

	private final DashboardService service;
	private final DashboardState state;

	public DashboardController(DashboardService service, DashboardState state) {
		this.service = service;
		this.state = state;
	}

	public DashboardState getState() {
		return state;
	}

	// =========================================================================
	// Public API
	// =========================================================================

	/**
	 * Initial / full load. Uses whatever filterFrom/filterTo is already set in
	 * state (null = no filter = current month for financial cards).
	 */
	public void loadDashboard(Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private DashboardDataResponse dashboardDataResponse;
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					LocalDate from = state.getFilterFrom();
					LocalDate to = state.getFilterTo();

					// Fetch the complete dashboard data from service
					dashboardDataResponse = service.fetchDashBoardData(from, to);

				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to load dashboard: " + error.getMessage());
					}
				} else {
					// Store the response in state
					state.setDashboardData(dashboardDataResponse);

					// Map and populate state from response
					populateStateFromResponse(dashboardDataResponse);

					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}

	/**
	 * Apply a new date filter and reload. Stores the filter in state so the UI can
	 * read it back (e.g. to keep date-pickers in sync after a refresh).
	 */
	public void applyFilter(LocalDate from, LocalDate to, Runnable onSuccess, Consumer<String> onError) {
		state.setFilterFrom(from);
		state.setFilterTo(to);
		state.resetData();
		loadDashboard(onSuccess, onError);
	}

	/**
	 * Clear the date filter and reload with all-time / current-month defaults.
	 */
	public void clearFilter(Runnable onSuccess, Consumer<String> onError) {
		state.setFilterFrom(null);
		state.setFilterTo(null);
		state.resetData();
		loadDashboard(onSuccess, onError);
	}

	/** Full reset + reload (used by the Refresh button). */
	public void refresh(Runnable onSuccess, Consumer<String> onError) {
		state.resetData();
		loadDashboard(onSuccess, onError);
	}

	// =========================================================================
	// Private mapping methods
	// =========================================================================

	/**
	 * Populate state from the dashboard response
	 */
	private void populateStateFromResponse(DashboardDataResponse response) {
		if (response == null)
			return;

		// ── Revenue Summary ────────────────────────────────────────────────
		RevenueSummary revenueSummary = response.getRevenueSummary();
		if (revenueSummary != null) {
			state.setTotalRevenueMonth(BigDecimal.valueOf(revenueSummary.getTotalRevenue()));
			state.setExpensesMonth(BigDecimal.valueOf(revenueSummary.getTotalExpenses()));
			state.setNetProfitMonth(BigDecimal.valueOf(revenueSummary.getNetProfit()));
		}

		// ── Operation Summary ──────────────────────────────────────────────
		OperationSummary operationSummary = response.getOperationSummary();
		if (operationSummary != null) {
			state.setTotalDeliveries(operationSummary.getTotalDeliveries());
			state.setPendingDeliveries(operationSummary.getPendingDeliveries());
			state.setTotalCustomers(operationSummary.getTotalCustomers());
			state.setTotalProducts(operationSummary.getTotalProducts());
		}

		// ── Monthly Income Chart ───────────────────────────────────────────
		if (response.getMonthlyIncome() != null) {
			state.setMonthlyIncome(response.getMonthlyIncome());
		}

		// ── Upcoming Loan Payments ─────────────────────────────────────────
		List<CustomerPayments> loanPayments = response.getUpcommingDueLoanPayments();
		if (loanPayments != null) {
			state.setUpcomingLoanPayments(mapToUpcomingLoanPayments(loanPayments));
		}

		// ── Upcoming Deliveries ────────────────────────────────────────────
		List<Delivery> deliveries = response.getUpComingDelivery();
		if (deliveries != null) {
			state.setUpcomingDeliveries(mapToUpcomingDeliveries(deliveries));
		}

		// TODO: Add these to your service response if needed
		// state.setTotalRevenueAllTime(...);
		// state.setPendingPaymentsTotal(...);
		// state.setPaymentsThisMonth(...);
		// state.setTotalBranches(...);
	}

	/**
	 * Map CustomerPayments to UpcomingLoanPayment (state inner class)
	 */
	private List<UpcomingLoanPayment> mapToUpcomingLoanPayments(List<CustomerPayments> payments) {
		List<UpcomingLoanPayment> result = new ArrayList<>();

		for (CustomerPayments payment : payments) {
			// Calculate amount due (total - totalPayment)
			BigDecimal amountDue = BigDecimal.valueOf(payment.getTotal())
					.subtract(BigDecimal.valueOf(payment.getTotalPayment()));

			// Convert Date to LocalDate
			// Handle both java.sql.Date and java.util.Date
			LocalDate dueDate = null;
			if (payment.getPromiseToPay() != null) {
				if (payment.getPromiseToPay() instanceof java.sql.Date) {
					// java.sql.Date can be converted directly to LocalDate
					dueDate = ((java.sql.Date) payment.getPromiseToPay()).toLocalDate();
				} else {
					// java.util.Date uses toInstant()
					dueDate = payment.getPromiseToPay().toInstant().atZone(java.time.ZoneId.systemDefault())
							.toLocalDate();
				}
			}

			// Determine status based on due date
			String status = determinePaymentStatus(dueDate);

			result.add(new UpcomingLoanPayment(payment.getId(), payment.getCustomerName(), payment.getDeliveryName(),
					amountDue, dueDate, status));
		}

		return result;
	}

	/**
	 * Map Delivery to UpcomingDelivery (state inner class)
	 */
	private List<UpcomingDelivery> mapToUpcomingDeliveries(List<Delivery> deliveries) {
		List<UpcomingDelivery> result = new ArrayList<>();

		for (Delivery delivery : deliveries) {
			// Convert LocalDateTime to LocalDate
			LocalDate scheduledDate = delivery.getScheduleDate() != null ? delivery.getScheduleDate().toLocalDate()
					: null;

			// Note: You'll need to add customer and branch info to the Delivery model
			// or enhance your service to fetch this data via JOINs
			String customerName = "N/A"; // TODO: Add to Delivery model
			String branchName = "N/A"; // TODO: Add to Delivery model

			result.add(new UpcomingDelivery(delivery.getId(), delivery.getName(), customerName, branchName,
					scheduledDate, capitalizeStatus(delivery.getStatus())));
		}

		return result;
	}

	/**
	 * Determine payment status based on due date
	 */
	private String determinePaymentStatus(LocalDate dueDate) {
		if (dueDate == null) {
			return "Unknown";
		}

		LocalDate today = LocalDate.now();

		if (dueDate.isBefore(today)) {
			return "Overdue";
		} else if (dueDate.isEqual(today) || dueDate.isBefore(today.plusDays(7))) {
			return "Due Soon";
		} else {
			return "Upcoming";
		}
	}

	/**
	 * Capitalize status string
	 */
	private String capitalizeStatus(String status) {
		if (status == null || status.isEmpty()) {
			return "";
		}
		return status.substring(0, 1).toUpperCase() + status.substring(1);
	}
}