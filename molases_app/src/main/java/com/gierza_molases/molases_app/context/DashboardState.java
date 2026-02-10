package com.gierza_molases.molases_app.context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * State holder for the Dashboard page.
 */
public class DashboardState {

	// ── Date filter (set by the UI, read by the controller) ──────────────────
	private LocalDate filterFrom = null;
	private LocalDate filterTo = null;

	// ── Summary metric cards ──────────────────────────────────────────────────
	private int totalDeliveries = 0;
	private int pendingDeliveries = 0;
	private int totalCustomers = 0;
	private int totalBranches = 0;
	private int totalProducts = 0;

	// Financial cards (scoped to the selected date range / current month)
	private BigDecimal totalRevenueMonth = BigDecimal.ZERO;
	private BigDecimal netProfitMonth = BigDecimal.ZERO;
	private BigDecimal expensesMonth = BigDecimal.ZERO;

	// All-time totals (always shown regardless of filter)
	private BigDecimal totalRevenueAllTime = BigDecimal.ZERO;
	private BigDecimal pendingPaymentsTotal = BigDecimal.ZERO;
	private int paymentsThisMonth = 0;

	// ── Monthly income chart data ─────────────────────────────────────────────
	// Key = "Jan 2025", Value = revenue BigDecimal (last 12 months, ordered)
	private Map<String, BigDecimal> monthlyIncome = new LinkedHashMap<>();

	// ── Upcoming due loan payments ────────────────────────────────────────────
	private List<UpcomingLoanPayment> upcomingLoanPayments = new ArrayList<>();

	// ── Upcoming deliveries ───────────────────────────────────────────────────
	private List<UpcomingDelivery> upcomingDeliveries = new ArrayList<>();

	// ── Recent activity (small feed at the bottom) ────────────────────────────
	private List<RecentDelivery> recentDeliveries = new ArrayList<>();
	private List<RecentPayment> recentPayments = new ArrayList<>();

	// =========================================================================
	// Inner data classes
	// =========================================================================

	public static class UpcomingLoanPayment {
		private final int id;
		private final String customerName;
		private final String deliveryName;
		private final BigDecimal amountDue;
		private final LocalDate dueDate;
		private final String status; // e.g. "Due Soon", "Overdue"

		public UpcomingLoanPayment(int id, String customerName, String deliveryName, BigDecimal amountDue,
				LocalDate dueDate, String status) {
			this.id = id;
			this.customerName = customerName;
			this.deliveryName = deliveryName;
			this.amountDue = amountDue;
			this.dueDate = dueDate;
			this.status = status;
		}

		public int getId() {
			return id;
		}

		public String getCustomerName() {
			return customerName;
		}

		public String getDeliveryName() {
			return deliveryName;
		}

		public BigDecimal getAmountDue() {
			return amountDue;
		}

		public LocalDate getDueDate() {
			return dueDate;
		}

		public String getStatus() {
			return status;
		}
	}

	public static class UpcomingDelivery {
		private final int id;
		private final String deliveryName;
		private final String customerName;
		private final String branchName;
		private final LocalDate scheduledDate;
		private final String status; // e.g. "Scheduled", "In Transit"

		public UpcomingDelivery(int id, String deliveryName, String customerName, String branchName,
				LocalDate scheduledDate, String status) {
			this.id = id;
			this.deliveryName = deliveryName;
			this.customerName = customerName;
			this.branchName = branchName;
			this.scheduledDate = scheduledDate;
			this.status = status;
		}

		public int getId() {
			return id;
		}

		public String getDeliveryName() {
			return deliveryName;
		}

		public String getCustomerName() {
			return customerName;
		}

		public String getBranchName() {
			return branchName;
		}

		public LocalDate getScheduledDate() {
			return scheduledDate;
		}

		public String getStatus() {
			return status;
		}
	}

	public static class RecentDelivery {
		private final int id;
		private final String deliveryName;
		private final String customerName;
		private final String branchName;
		private final String status;
		private final String date;

		public RecentDelivery(int id, String deliveryName, String customerName, String branchName, String status,
				String date) {
			this.id = id;
			this.deliveryName = deliveryName;
			this.customerName = customerName;
			this.branchName = branchName;
			this.status = status;
			this.date = date;
		}

		public int getId() {
			return id;
		}

		public String getDeliveryName() {
			return deliveryName;
		}

		public String getCustomerName() {
			return customerName;
		}

		public String getBranchName() {
			return branchName;
		}

		public String getStatus() {
			return status;
		}

		public String getDate() {
			return date;
		}
	}

	public static class RecentPayment {
		private final int id;
		private final String customerName;
		private final String deliveryName;
		private final BigDecimal amount;
		private final String paymentType;
		private final String status;
		private final String date;

		public RecentPayment(int id, String customerName, String deliveryName, BigDecimal amount, String paymentType,
				String status, String date) {
			this.id = id;
			this.customerName = customerName;
			this.deliveryName = deliveryName;
			this.amount = amount;
			this.paymentType = paymentType;
			this.status = status;
			this.date = date;
		}

		public int getId() {
			return id;
		}

		public String getCustomerName() {
			return customerName;
		}

		public String getDeliveryName() {
			return deliveryName;
		}

		public BigDecimal getAmount() {
			return amount;
		}

		public String getPaymentType() {
			return paymentType;
		}

		public String getStatus() {
			return status;
		}

		public String getDate() {
			return date;
		}
	}

	// =========================================================================
	// Getters & Setters
	// =========================================================================

	// Date filter
	public LocalDate getFilterFrom() {
		return filterFrom;
	}

	public void setFilterFrom(LocalDate v) {
		this.filterFrom = v;
	}

	public LocalDate getFilterTo() {
		return filterTo;
	}

	public void setFilterTo(LocalDate v) {
		this.filterTo = v;
	}

	// Counts
	public int getTotalDeliveries() {
		return totalDeliveries;
	}

	public void setTotalDeliveries(int v) {
		this.totalDeliveries = v;
	}

	public int getPendingDeliveries() {
		return pendingDeliveries;
	}

	public void setPendingDeliveries(int v) {
		this.pendingDeliveries = v;
	}

	public int getTotalCustomers() {
		return totalCustomers;
	}

	public void setTotalCustomers(int v) {
		this.totalCustomers = v;
	}

	public int getTotalBranches() {
		return totalBranches;
	}

	public void setTotalBranches(int v) {
		this.totalBranches = v;
	}

	public int getTotalProducts() {
		return totalProducts;
	}

	public void setTotalProducts(int v) {
		this.totalProducts = v;
	}

	public int getPaymentsThisMonth() {
		return paymentsThisMonth;
	}

	public void setPaymentsThisMonth(int v) {
		this.paymentsThisMonth = v;
	}

	// Financial
	public BigDecimal getTotalRevenueMonth() {
		return totalRevenueMonth;
	}

	public void setTotalRevenueMonth(BigDecimal v) {
		this.totalRevenueMonth = v;
	}

	public BigDecimal getNetProfitMonth() {
		return netProfitMonth;
	}

	public void setNetProfitMonth(BigDecimal v) {
		this.netProfitMonth = v;
	}

	public BigDecimal getExpensesMonth() {
		return expensesMonth;
	}

	public void setExpensesMonth(BigDecimal v) {
		this.expensesMonth = v;
	}

	public BigDecimal getTotalRevenueAllTime() {
		return totalRevenueAllTime;
	}

	public void setTotalRevenueAllTime(BigDecimal v) {
		this.totalRevenueAllTime = v;
	}

	public BigDecimal getPendingPaymentsTotal() {
		return pendingPaymentsTotal;
	}

	public void setPendingPaymentsTotal(BigDecimal v) {
		this.pendingPaymentsTotal = v;
	}

	// Chart
	public Map<String, BigDecimal> getMonthlyIncome() {
		return monthlyIncome;
	}

	public void setMonthlyIncome(Map<String, BigDecimal> m) {
		this.monthlyIncome = m;
	}

	// Upcoming
	public List<UpcomingLoanPayment> getUpcomingLoanPayments() {
		return upcomingLoanPayments;
	}

	public void setUpcomingLoanPayments(List<UpcomingLoanPayment> l) {
		this.upcomingLoanPayments = l;
	}

	public List<UpcomingDelivery> getUpcomingDeliveries() {
		return upcomingDeliveries;
	}

	public void setUpcomingDeliveries(List<UpcomingDelivery> l) {
		this.upcomingDeliveries = l;
	}

	// Recent
	public List<RecentDelivery> getRecentDeliveries() {
		return recentDeliveries;
	}

	public void setRecentDeliveries(List<RecentDelivery> l) {
		this.recentDeliveries = l;
	}

	public List<RecentPayment> getRecentPayments() {
		return recentPayments;
	}

	public void setRecentPayments(List<RecentPayment> l) {
		this.recentPayments = l;
	}

	// =========================================================================
	// Reset
	// =========================================================================

	public void reset() {
		filterFrom = null;
		filterTo = null;
		totalDeliveries = 0;
		pendingDeliveries = 0;
		totalCustomers = 0;
		totalBranches = 0;
		totalProducts = 0;
		totalRevenueMonth = BigDecimal.ZERO;
		netProfitMonth = BigDecimal.ZERO;
		expensesMonth = BigDecimal.ZERO;
		totalRevenueAllTime = BigDecimal.ZERO;
		pendingPaymentsTotal = BigDecimal.ZERO;
		paymentsThisMonth = 0;
		monthlyIncome = new LinkedHashMap<>();
		upcomingLoanPayments = new ArrayList<>();
		upcomingDeliveries = new ArrayList<>();
		recentDeliveries = new ArrayList<>();
		recentPayments = new ArrayList<>();
	}

	/** Partial reset — keeps filter dates but clears all loaded data */
	public void resetData() {
		totalDeliveries = 0;
		pendingDeliveries = 0;
		totalCustomers = 0;
		totalBranches = 0;
		totalProducts = 0;
		totalRevenueMonth = BigDecimal.ZERO;
		netProfitMonth = BigDecimal.ZERO;
		expensesMonth = BigDecimal.ZERO;
		totalRevenueAllTime = BigDecimal.ZERO;
		pendingPaymentsTotal = BigDecimal.ZERO;
		paymentsThisMonth = 0;
		monthlyIncome = new LinkedHashMap<>();
		upcomingLoanPayments = new ArrayList<>();
		upcomingDeliveries = new ArrayList<>();
		recentDeliveries = new ArrayList<>();
		recentPayments = new ArrayList<>();
	}
}