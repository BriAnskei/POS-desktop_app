package com.gierza_molases.molases_app.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.response.OperationSummary;
import com.gierza_molases.molases_app.model.response.RevenueSummary;
import com.gierza_molases.molases_app.util.DaoUtils;

public class DashboardDao {

	private Connection conn;

	private static final DateTimeFormatter SQLITE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private static final String SELECT_REVENUE_SUM_BASE_SQL = """
			    SELECT
			        SUM(total_gross) AS total_revenue,
			        SUM(total_expenses) AS total_expenses,
			        SUM(net_profit) AS net_profit
			    FROM delivery
			""";

	private static final String SELECT_MONTHLY_INCOME_SQL = """
			    WITH RECURSIVE months(m) AS (
			        SELECT date('now', 'start of year')
			        UNION ALL
			        SELECT date(m, '+1 month')
			        FROM months
			        WHERE m < date('now', 'start of year', '+11 months')
			    )
			    SELECT
			        strftime('%Y-%m', m) AS month,
			        COALESCE(SUM(ph.amount), 0) AS monthly_income
			    FROM months
			    LEFT JOIN payment_history ph
			        ON strftime('%Y-%m', ph.created_at) = strftime('%Y-%m', m)
			    LEFT JOIN customer_payments cp
			        ON cp.id = ph.customer_payment_id
			    GROUP BY month
			    ORDER BY month
			""";

	private static final String SELECT_UPCOMMING_LOAN = """
					SELECT
			    cp.id,
			    cp.customer_id,
			    cp.customer_delivery_id,
			    cp.payment_type,
			    cp.status,
			    cp.notes,
			    cp.total,
			    cp.total_payment,
			    cp.promise_to_pay,
			    cp.created_at,

			    d.id AS delivery_id,
			    c.display_name AS customer_name,
			    d.name AS delivery_name,
			    d.schedule_date AS delivery_date
			FROM customer_payments cp
			JOIN customer c
			    ON c.id = cp.customer_id
			LEFT JOIN customer_delivery cd
			    ON cd.id = cp.customer_delivery_id
			LEFT JOIN delivery d
			    ON d.id = cd.delivery_id
			WHERE cp.payment_type = 'Loan'
			  AND cp.promise_to_pay IS NOT NULL
			  AND cp.total_payment < cp.total
			  AND (cp.status IS NULL OR cp.status != 'complete')
			  AND (
			        date(cp.promise_to_pay) < date('now')                 -- overdue
			        OR
			        strftime('%Y-%m', cp.promise_to_pay) = strftime('%Y-%m', 'now') -- this month
			      )
			ORDER BY date(cp.promise_to_pay) ASC;

					""";

	private static final String SELECT_TOTAL_DELIVERIES_BASE_SQL = """
			    SELECT COALESCE(SUM(total_rows), 0)
			    FROM delivery_daily_counts
			""";

	private static final String SELECT_PENDING_DELIVERIES_BASE_SQL = """
			    SELECT COALESCE(SUM(total_rows), 0)
			    FROM delivery_daily_counts
			    WHERE status = 'scheduled'
			""";

	private static final String SELECT_TOTAL_CUSTOMERS_SQL = """
			   SELECT COUNT(*) FROM customer
			""";

	private static final String SELECT_TOTAL_PRODUCTS_SQL = "SELECT COUNT(*) FROM product";

	private static final String SELECT_UPCOMING_DELIVERIES_SQL = """
			    SELECT
			        d.id,
			        d.name,
			        d.schedule_date,
			        d.status,
			        d.total_customers,
			        d.total_branches,
			        d.total_gross,
			        d.total_capital,
			        d.gross_profit,
			        d.total_expenses,
			        d.net_profit,
			        d.created_at
			    FROM delivery d
			    WHERE d.schedule_date >= (strftime('%s', 'now', 'start of month') * 1000)
			      AND d.schedule_date <  (strftime('%s', 'now', 'start of month', '+2 months') * 1000)
			      AND d.status = 'scheduled'
			    ORDER BY d.schedule_date ASC
			""";

	public DashboardDao(Connection conn) {
		this.conn = conn;
	}

	public RevenueSummary getRevenueSummary(LocalDate from, LocalDate to) throws SQLException {

		String dateCondition = "";

		if (from != null && to != null) {
			dateCondition = " WHERE created_at BETWEEN ? AND ? ";
		} else if (from != null) {
			dateCondition = " WHERE created_at >= ? ";
		} else if (to != null) {
			dateCondition = " WHERE created_at <= ? ";
		}

		String sql = SELECT_REVENUE_SUM_BASE_SQL + dateCondition;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			setDateParams(ps, from, to);

			try (ResultSet rs = ps.executeQuery()) {

				if (rs.next()) {

					double totalRevenue = rs.getDouble("total_revenue");
					double totalExpenses = rs.getDouble("total_expenses");
					double netProfit = rs.getDouble("net_profit");

					return new RevenueSummary(totalRevenue, totalExpenses, netProfit);
				}
			}
		}

		System.err.println("Failed to fetch revenue summary");
		return new RevenueSummary(0, 0, 0);
	}

	public Map<String, BigDecimal> getMonthlyIncomeThisYear() throws SQLException {

		Map<String, BigDecimal> map = new LinkedHashMap<>();

		try (PreparedStatement ps = conn.prepareStatement(SELECT_MONTHLY_INCOME_SQL);
				ResultSet rs = ps.executeQuery()) {

			DateTimeFormatter uiFmt = DateTimeFormatter.ofPattern("MMM yy");

			while (rs.next()) {

				String monthStr = rs.getString("month"); // yyyy-MM
				BigDecimal income = rs.getBigDecimal("monthly_income");

				LocalDate parsedMonth = LocalDate.parse(monthStr + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));

				map.put(parsedMonth.format(uiFmt), income);
			}
		}

		return map;
	}

	public OperationSummary getOperationSummary(LocalDate from, LocalDate to) throws SQLException {
		return getOperationSummary(conn, from, to);
	}

	public OperationSummary getOperationSummary(Connection conn, LocalDate from, LocalDate to) throws SQLException {

		StringBuilder totalSql = new StringBuilder(SELECT_TOTAL_DELIVERIES_BASE_SQL);
		StringBuilder pendingSql = new StringBuilder(SELECT_PENDING_DELIVERIES_BASE_SQL);

		// total deliveries (no WHERE initially)
		appendDateFilter(totalSql, from, to, false);

		// pending deliveries (already has WHERE status = 'scheduled')
		appendDateFilter(pendingSql, from, to, true);

		int totalDeliveries;
		int pendingDeliveries;
		int totalCustomers;
		int totalProducts;

		try (PreparedStatement ps = conn.prepareStatement(totalSql.toString())) {
			setDateParams(ps, from, to);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				totalDeliveries = rs.getInt(1);
			}
		}

		try (PreparedStatement ps = conn.prepareStatement(pendingSql.toString())) {
			setDateParams(ps, from, to);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				pendingDeliveries = rs.getInt(1);
			}
		}

		try (PreparedStatement ps = conn.prepareStatement(SELECT_TOTAL_CUSTOMERS_SQL);
				ResultSet rs = ps.executeQuery()) {
			rs.next();
			totalCustomers = rs.getInt(1);
		}

		try (PreparedStatement ps = conn.prepareStatement(SELECT_TOTAL_PRODUCTS_SQL);
				ResultSet rs = ps.executeQuery()) {
			rs.next();
			totalProducts = rs.getInt(1);
		}

		return new OperationSummary(totalDeliveries, pendingDeliveries, totalCustomers, totalProducts);
	}

	public List<CustomerPayments> fetchMonthlyLoanPaymentsIncludingOverdue() {
		List<CustomerPayments> list = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(SELECT_UPCOMMING_LOAN); ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				CustomerPayments payment = new CustomerPayments(rs.getInt("id"), rs.getInt("customer_id"),
						rs.getInt("customer_delivery_id"), rs.getString("payment_type"), rs.getString("status"),
						rs.getString("notes"), rs.getDouble("total"), rs.getDouble("total_payment"),
						rs.getDate("promise_to_pay"), rs.getTimestamp("created_at").toLocalDateTime(),
						rs.getInt("delivery_id"), rs.getString("customer_name"), rs.getString("delivery_name"),
						rs.getDate("delivery_date"));

				list.add(payment);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return list;
	}

	public List<Delivery> fetchUpcomingDeliveriesThisAndNextMonth() {
		List<Delivery> list = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(SELECT_UPCOMING_DELIVERIES_SQL);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				Integer id = rs.getInt("id");

				LocalDateTime scheduleDate = rs.getTimestamp("schedule_date").toLocalDateTime();

				String name = rs.getString("name");

				// expenses JSON (nullable)
				String expensesJson = null;
				try {
					expensesJson = rs.getString("expenses");
				} catch (SQLException ignore) {
					// expenses column may not be selected in list queries
				}

				Map<String, Double> expenses = Delivery.parseExpensesJson(expensesJson);

				String status = rs.getString("status");

				// numeric fields (nullable-safe)
				Double totalGross = DaoUtils.getNullableDouble(rs, "total_gross");
				Double totalCapital = DaoUtils.getNullableDouble(rs, "total_capital");
				Double grossProfit = DaoUtils.getNullableDouble(rs, "gross_profit");
				Double netProfit = DaoUtils.getNullableDouble(rs, "net_profit");

				LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

				int totalCustomers = rs.getInt("total_customers");
				int totalBranches = rs.getInt("total_branches");

				Delivery delivery = new Delivery(id, scheduleDate, name, expenses, status, totalGross, totalCapital,
						grossProfit, netProfit, createdAt, totalCustomers, totalBranches);

				list.add(delivery);
			}

		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch upcoming deliveries", e);
		}

		return list;
	}

	// --------------helper
	private void setDateParams(PreparedStatement ps, LocalDate from, LocalDate to) throws SQLException {

		int index = 1;

		if (from != null && to != null) {

			ps.setString(index++, from.atStartOfDay().format(SQLITE_FORMAT));

			ps.setString(index, to.atTime(LocalTime.MAX).format(SQLITE_FORMAT));

		} else if (from != null) {

			ps.setString(index, from.atStartOfDay().format(SQLITE_FORMAT));

		} else if (to != null) {

			ps.setString(index, to.atTime(LocalTime.MAX).format(SQLITE_FORMAT));
		}
	}

	private void appendDateFilter(StringBuilder sql, LocalDate from, LocalDate to, boolean hasWhere) {
		if (from == null && to == null) {
			return;
		}

		sql.append(hasWhere ? " AND " : " WHERE ");

		if (from != null && to != null) {
			sql.append("counter_date BETWEEN ? AND ?");
		} else if (from != null) {
			sql.append("counter_date >= ?");
		} else {
			sql.append("counter_date <= ?");
		}
	}

}
