package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.util.DaoUtils;

public class DeliveryDao {
	private final Connection conn;

	// INSERTS
	private static final String INSERT_DELIVERY_SQL = """
			   INSERT INTO delivery (
			         schedule_date, name, expenses, status, total_customers, total_branches,
			         total_gross, total_capital, gross_profit,
			         total_expenses, net_profit
			     ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
			""";

	// READ
	private static final String SELECT_DELIVERIES_NEWEST_BASE = """
			    SELECT *
			    FROM delivery
			    WHERE id < ?
			    ORDER BY id DESC
			    LIMIT ?;
			""";

	public DeliveryDao(Connection conn) {
		this.conn = conn;
	}

	public int insert(Connection conn, Delivery delivery) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(INSERT_DELIVERY_SQL, Statement.RETURN_GENERATED_KEYS)) {

			ps.setTimestamp(1, Timestamp.valueOf(delivery.getScheduleDate()));
			ps.setString(2, delivery.getName());
			ps.setString(3, delivery.getExpensesAsJson()); // JSON string
			ps.setString(4, "scheduled");

			ps.setInt(5, delivery.getTotalCustomers());
			ps.setInt(6, delivery.getTotalBranches());

			ps.setDouble(7, delivery.getTotalGross());
			ps.setDouble(8, delivery.getTotalCapital());
			ps.setDouble(9, delivery.getGrossProfit());
			ps.setDouble(10, delivery.getTotalExpenses());
			ps.setDouble(11, delivery.getNetProfit());

			ps.executeUpdate();

			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		throw new SQLException("Failed to insert delivery");
	}

	public List<Delivery> fetchNextPageNewest(Long lastSeenDeliveryId, String search, Date startAt, Date endAt,
			int pageSize) {
		boolean hasName = search != null && !search.isBlank();
		boolean hasStart = startAt != null;
		boolean hasEnd = endAt != null;

		return (hasName || hasStart || hasEnd)
				? fetchNewestWithFilters(lastSeenDeliveryId, search, startAt, endAt, pageSize)
				: fetchNewestNoFilters(lastSeenDeliveryId, pageSize);
	}

	private List<Delivery> fetchNewestNoFilters(Long lastSeenDeliveryId, int pageSize) {
		List<Delivery> deliveries = new ArrayList<>();

		long cursor = (lastSeenDeliveryId != null) ? lastSeenDeliveryId : Long.MAX_VALUE;

		try (PreparedStatement ps = conn.prepareStatement(SELECT_DELIVERIES_NEWEST_BASE)) {

			ps.setLong(1, cursor);
			ps.setInt(2, pageSize);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					deliveries.add(mapRowToDelivery(rs));
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch newest deliveries", e);
		}

		return deliveries;
	}

	private List<Delivery> fetchNewestWithFilters(Long lastSeenDeliveryId, String search, Date startAt, Date endAt,
			int pageSize) {
		List<Delivery> deliveries = new ArrayList<>();

		boolean hasName = search != null && !search.isBlank();
		boolean hasStart = startAt != null;
		boolean hasEnd = endAt != null;

		String sql = buildFilteredNewestQuery(hasName, hasStart, hasEnd);

		long cursor = (lastSeenDeliveryId != null) ? lastSeenDeliveryId : Long.MAX_VALUE;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			int i = 1;
			ps.setLong(i++, cursor);

			if (hasName) {
				ps.setString(i++, search + "%");
			}
			if (hasStart) {
				ps.setTimestamp(i++, new Timestamp(startAt.getTime()));
			}
			if (hasEnd) {
				ps.setTimestamp(i++, new Timestamp(endAt.getTime()));
			}

			ps.setInt(i, pageSize);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					deliveries.add(mapRowToDelivery(rs));
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch newest deliveries (filtered)", e);
		}

		return deliveries;
	}

	// HELPER FUNCTIONS
	private Delivery mapRowToDelivery(ResultSet rs) throws Exception {

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

		return new Delivery(id, scheduleDate, name, expenses, status, totalGross, totalCapital, grossProfit, netProfit,
				createdAt, totalCustomers, totalBranches);
	}

	private static String buildFilteredNewestQuery(boolean hasName, boolean hasStart, boolean hasEnd) {
		StringBuilder sql = new StringBuilder("""
				    SELECT *
				    FROM delivery
				    WHERE id < ?
				""");

		if (hasName) {
			sql.append(" AND name LIKE ?");
		}
		if (hasStart) {
			sql.append(" AND schedule_date >= ?");
		}
		if (hasEnd) {
			sql.append(" AND schedule_date <= ?");
		}

		sql.append(" ORDER BY id DESC LIMIT ?");

		return sql.toString();
	}

}
