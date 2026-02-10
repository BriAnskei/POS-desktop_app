package com.gierza_molases.molases_app.UiController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import com.gierza_molases.molases_app.context.DashboardState;
import com.gierza_molases.molases_app.context.DashboardState.RecentDelivery;
import com.gierza_molases.molases_app.context.DashboardState.RecentPayment;
import com.gierza_molases.molases_app.context.DashboardState.UpcomingDelivery;
import com.gierza_molases.molases_app.context.DashboardState.UpcomingLoanPayment;

/**
 * Controller for the Dashboard page.
 *
 * Register in AppContext: public static final DashboardController
 * dashboardController = new DashboardController(new DashboardState());
 */
public class DashboardController {

	private final DashboardState state;

	public DashboardController(DashboardState state) {
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
		new Thread(() -> {
			try {
				LocalDate from = state.getFilterFrom();
				LocalDate to = state.getFilterTo();

				// ── Static counts (not affected by date filter) ───────────────
				int totalDeliveries = fetchTotalDeliveries();
				int pendingDeliveries = fetchPendingDeliveries();
				int totalCustomers = fetchTotalCustomers();
				int totalBranches = fetchTotalBranches();
				int totalProducts = fetchTotalProducts();
				int paymentsThisMonth = fetchPaymentsThisMonth();

				// ── Financial cards (scoped to date range) ────────────────────
				BigDecimal totalRevenueMonth = fetchTotalRevenue(from, to);
				BigDecimal expensesMonth = fetchExpenses(from, to);
				BigDecimal netProfitMonth = totalRevenueMonth.subtract(expensesMonth);
				BigDecimal totalRevenueAllTime = fetchTotalRevenueAllTime();
				BigDecimal pendingPayments = fetchPendingPayments();

				// ── Monthly income chart (last 12 months, ignores date filter) ─
				Map<String, BigDecimal> monthlyIncome = fetchMonthlyIncome();

				// ── Upcoming sections (ignores date filter) ───────────────────
				List<UpcomingLoanPayment> upcomingLoans = fetchUpcomingLoanPayments(7);
				List<UpcomingDelivery> upcomingDeliveries = fetchUpcomingDeliveries(7);

				// ── Recent activity (scoped to date range) ────────────────────
				List<RecentDelivery> recentDeliveries = fetchRecentDeliveries(5, from, to);
				List<RecentPayment> recentPayments = fetchRecentPayments(5, from, to);

				SwingUtilities.invokeLater(() -> {
					state.setTotalDeliveries(totalDeliveries);
					state.setPendingDeliveries(pendingDeliveries);
					state.setTotalCustomers(totalCustomers);
					state.setTotalBranches(totalBranches);
					state.setTotalProducts(totalProducts);
					state.setPaymentsThisMonth(paymentsThisMonth);
					state.setTotalRevenueMonth(totalRevenueMonth);
					state.setExpensesMonth(expensesMonth);
					state.setNetProfitMonth(netProfitMonth);
					state.setTotalRevenueAllTime(totalRevenueAllTime);
					state.setPendingPaymentsTotal(pendingPayments);
					state.setMonthlyIncome(monthlyIncome);
					state.setUpcomingLoanPayments(upcomingLoans);
					state.setUpcomingDeliveries(upcomingDeliveries);
					state.setRecentDeliveries(recentDeliveries);
					state.setRecentPayments(recentPayments);

					if (onSuccess != null)
						onSuccess.run();
				});

			} catch (Exception e) {
				e.printStackTrace();
				SwingUtilities.invokeLater(() -> {
					if (onError != null)
						onError.accept("Failed to load dashboard: " + e.getMessage());
				});
			}
		}, "DashboardController-load").start();
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
	// Private fetch helpers — MOCK DATA
	// Replace each body with your real DAO / service call.
	// The LocalDate parameters are passed through so your DAO can use them.
	// =========================================================================

	private int fetchTotalDeliveries() {
		simulateDelay();
		// TODO: return AppContext.deliveryRepository.countAll();
		return 142;
	}

	private int fetchPendingDeliveries() {
		// TODO: return AppContext.deliveryRepository.countByStatus("pending");
		return 18;
	}

	private int fetchTotalCustomers() {
		// TODO: return AppContext.customerRepository.countAll();
		return 57;
	}

	private int fetchTotalBranches() {
		// TODO: return AppContext.branchRepository.countAll();
		return 5;
	}

	private int fetchTotalProducts() {
		// TODO: return AppContext.productRepository.countAll();
		return 23;
	}

	private int fetchPaymentsThisMonth() {
		// TODO: return AppContext.customerPaymentRepository.countThisMonth();
		return 34;
	}

	/**
	 * Revenue for the given date range. If from/to are null, defaults to current
	 * month. TODO: return
	 * AppContext.customerPaymentRepository.sumRevenueBetween(from, to);
	 */
	private BigDecimal fetchTotalRevenue(LocalDate from, LocalDate to) {
		// Mock: vary slightly based on whether a filter is active
		if (from != null || to != null) {
			return new BigDecimal("385400.00");
		}
		return new BigDecimal("428750.00");
	}

	/**
	 * Expenses for the given date range. TODO: return
	 * AppContext.expenseRepository.sumBetween(from, to);
	 */
	private BigDecimal fetchExpenses(LocalDate from, LocalDate to) {
		if (from != null || to != null) {
			return new BigDecimal("112300.00");
		}
		return new BigDecimal("134200.00");
	}

	private BigDecimal fetchTotalRevenueAllTime() {
		// TODO: return AppContext.customerPaymentRepository.sumAllCompleted();
		return new BigDecimal("1284750.00");
	}

	private BigDecimal fetchPendingPayments() {
		// TODO: return AppContext.customerPaymentRepository.sumPending();
		return new BigDecimal("237600.50");
	}

	/** Last 12 months of monthly revenue, ordered oldest → newest. */
	private Map<String, BigDecimal> fetchMonthlyIncome() {
		// TODO: return AppContext.customerPaymentRepository.monthlyRevenueLast12();
		Map<String, BigDecimal> map = new LinkedHashMap<>();
		LocalDate now = LocalDate.now();
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yy");

		// Realistic-looking mock values that trend upward
		BigDecimal[] values = { new BigDecimal("210000"), new BigDecimal("185000"), new BigDecimal("245000"),
				new BigDecimal("198000"), new BigDecimal("275000"), new BigDecimal("310000"), new BigDecimal("289000"),
				new BigDecimal("325000"), new BigDecimal("298000"), new BigDecimal("356000"), new BigDecimal("380000"),
				new BigDecimal("428750") };

		for (int i = 11; i >= 0; i--) {
			LocalDate month = now.minusMonths(i);
			map.put(month.format(fmt), values[11 - i]);
		}
		return map;
	}

	/** Upcoming loan payments due within the next N days. */
	private List<UpcomingLoanPayment> fetchUpcomingLoanPayments(int limit) {
		// TODO: return AppContext.customerPaymentRepository.findUpcomingLoans(limit);
		List<UpcomingLoanPayment> list = new ArrayList<>();
		LocalDate today = LocalDate.now();

		list.add(new UpcomingLoanPayment(301, "Cruz Enterprises", "DEL-2025-002", new BigDecimal("120000.00"),
				today.plusDays(2), "Due Soon"));
		list.add(new UpcomingLoanPayment(302, "Garcia Feeds", "DEL-2025-004", new BigDecimal("89200.00"),
				today.plusDays(3), "Due Soon"));
		list.add(new UpcomingLoanPayment(303, "Reyes Trading", "DEL-2025-003", new BigDecimal("67500.50"),
				today.plusDays(5), "Due Soon"));
		list.add(new UpcomingLoanPayment(304, "Dela Cruz Agri", "DEL-2025-008", new BigDecimal("45000.00"),
				today.minusDays(1), "Overdue"));
		list.add(new UpcomingLoanPayment(305, "Mendoza Supply Co.", "DEL-2025-006", new BigDecimal("210000.00"),
				today.plusDays(7), "Due Soon"));
		list.add(new UpcomingLoanPayment(306, "Tan Enterprises", "DEL-2025-010", new BigDecimal("55000.00"),
				today.minusDays(3), "Overdue"));
		list.add(new UpcomingLoanPayment(307, "Villanueva Farm", "DEL-2025-012", new BigDecimal("98000.00"),
				today.plusDays(9), "Due Soon"));

		return list.subList(0, Math.min(limit, list.size()));
	}

	/** Upcoming scheduled deliveries. */
	private List<UpcomingDelivery> fetchUpcomingDeliveries(int limit) {
		// TODO: return AppContext.deliveryRepository.findUpcoming(limit);
		List<UpcomingDelivery> list = new ArrayList<>();
		LocalDate today = LocalDate.now();

		list.add(new UpcomingDelivery(401, "DEL-2025-015", "Santos Farm", "Davao Branch", today.plusDays(1),
				"Scheduled"));
		list.add(new UpcomingDelivery(402, "DEL-2025-016", "Lim Agri Supply", "Cebu Branch", today.plusDays(1),
				"In Transit"));
		list.add(new UpcomingDelivery(403, "DEL-2025-017", "Cruz Enterprises", "Manila Branch", today.plusDays(2),
				"Scheduled"));
		list.add(new UpcomingDelivery(404, "DEL-2025-018", "Buenaventura Co.", "Davao Branch", today.plusDays(3),
				"Scheduled"));
		list.add(new UpcomingDelivery(405, "DEL-2025-019", "Reyes Trading", "Cebu Branch", today.plusDays(4),
				"Scheduled"));
		list.add(new UpcomingDelivery(406, "DEL-2025-020", "Flores Feeds", "Manila Branch", today.plusDays(5),
				"Scheduled"));
		list.add(new UpcomingDelivery(407, "DEL-2025-021", "Garcia Feeds", "Davao Branch", today.plusDays(6),
				"In Transit"));

		return list.subList(0, Math.min(limit, list.size()));
	}

	private List<RecentDelivery> fetchRecentDeliveries(int limit, LocalDate from, LocalDate to) {
		// TODO: return AppContext.deliveryRepository.findRecent(limit, from, to);
		List<RecentDelivery> list = new ArrayList<>();
		list.add(new RecentDelivery(1001, "DEL-2025-001", "Santos Farm", "Davao Branch", "complete", "Feb 09, 2025"));
		list.add(
				new RecentDelivery(1002, "DEL-2025-002", "Cruz Enterprises", "Cebu Branch", "pending", "Feb 09, 2025"));
		list.add(
				new RecentDelivery(1003, "DEL-2025-003", "Reyes Trading", "Manila Branch", "complete", "Feb 08, 2025"));
		list.add(new RecentDelivery(1004, "DEL-2025-004", "Garcia Feeds", "Davao Branch", "pending", "Feb 08, 2025"));
		list.add(
				new RecentDelivery(1005, "DEL-2025-005", "Lim Agri Supply", "Cebu Branch", "complete", "Feb 07, 2025"));
		return list.subList(0, Math.min(limit, list.size()));
	}

	private List<RecentPayment> fetchRecentPayments(int limit, LocalDate from, LocalDate to) {
		// TODO: return AppContext.customerPaymentRepository.findRecent(limit, from,
		// to);
		List<RecentPayment> list = new ArrayList<>();
		list.add(new RecentPayment(201, "Santos Farm", "DEL-2025-001", new BigDecimal("45000.00"), "Paid Cash",
				"complete", "Feb 09, 2025"));
		list.add(new RecentPayment(202, "Cruz Enterprises", "DEL-2025-002", new BigDecimal("120000.00"), "Paid Cheque",
				"pending", "Feb 09, 2025"));
		list.add(new RecentPayment(203, "Reyes Trading", "DEL-2025-003", new BigDecimal("67500.50"), "Partial",
				"pending", "Feb 08, 2025"));
		list.add(new RecentPayment(204, "Garcia Feeds", "DEL-2025-004", new BigDecimal("89200.00"), "Loan", "pending",
				"Feb 08, 2025"));
		list.add(new RecentPayment(205, "Lim Agri Supply", "DEL-2025-005", new BigDecimal("32750.00"), "Paid Cash",
				"complete", "Feb 07, 2025"));
		return list.subList(0, Math.min(limit, list.size()));
	}

	private void simulateDelay() {
		try {
			Thread.sleep(700);
		} catch (InterruptedException ignored) {
		}
	}
}