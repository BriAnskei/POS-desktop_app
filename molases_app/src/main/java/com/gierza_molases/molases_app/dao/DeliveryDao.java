package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

	private static final String SELECT_DELIVERY_SQL = """
			   SELECT * FROM delivery
			   WHERE id = ?
			""";

	// UPDATES
	private static final String UPDATE_DELIVERY_SQL = """
			UPDATE delivery
						SET
						    name = ?,
						    expenses = ?,
						    total_customers = ?,
						    total_branches = ?,
						    total_gross = ?,
						    total_capital = ?,
						    gross_profit = ?,
						    total_expenses = ?,
						    net_profit = ?
						WHERE id = ?

									""";

	private static final String MARK_AS_DELIVERED_SQL = """
			   UPDATE delivery SET  status = 'delivered' WHERE id = ?
			""";

	// DELETE
	private static final String DELETE_DELIVERY_SQL = """
			   DELETE FROM delivery
			   WHERE id = ?
			""";

	public DeliveryDao(Connection conn) {
		this.conn = conn;
	}

	public int insert(Connection conn, Delivery delivery) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(INSERT_DELIVERY_SQL, Statement.RETURN_GENERATED_KEYS)) {

			long scheduleEpochMillis = delivery.getScheduleDate().atZone(ZoneId.systemDefault()).toInstant()
					.toEpochMilli();

			ps.setLong(1, scheduleEpochMillis);

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
				LocalDate startDate = startAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

				long startOfDay = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

				ps.setLong(i++, startOfDay);
			}

			if (hasEnd) {
				LocalDate endDate = endAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

				long endOfDay = endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

				ps.setLong(i++, endOfDay);
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

	public Delivery findById(Connection conn, int deliveryId) {

		if (deliveryId <= 0) {
			throw new IllegalArgumentException("Invalid branch ID");
		}

		try (PreparedStatement ps = conn.prepareStatement(SELECT_DELIVERY_SQL)) {

			ps.setInt(1, deliveryId);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					throw new IllegalStateException("Delivery does not exist.");
				}

				return this.mapRowToDelivery(rs);
			}

		} catch (Exception err) {
			throw new RuntimeException("Failed to delete branch", err);
		}

	}

	public void update(Delivery delivery, Connection conn) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(UPDATE_DELIVERY_SQL)) {

			ps.setString(1, delivery.getName());
			ps.setString(2, delivery.getExpensesAsJson());
			ps.setInt(3, delivery.getTotalCustomers());
			ps.setInt(4, delivery.getTotalBranches());

			ps.setDouble(5, delivery.getTotalGross());
			ps.setDouble(6, delivery.getTotalCapital());
			ps.setDouble(7, delivery.getGrossProfit());
			ps.setDouble(8, delivery.getTotalExpenses());
			ps.setDouble(9, delivery.getNetProfit());

			ps.setInt(10, delivery.getId());

			ps.executeUpdate();
		}
	}

	public void markDeliveryAsDelivered(Delivery delivery, Connection conn) {
		try (PreparedStatement ps = conn.prepareStatement(MARK_AS_DELIVERED_SQL)) {

			ps.setInt(1, delivery.getId());

			int res = ps.executeUpdate();

			if (res == 0) {
				throw new RuntimeException("delivery id not found" + delivery.getId());
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to delete branch", e);
		}
	}

	public void deleteDelivery(int deliveryId) {
		if (deliveryId <= 0) {
			throw new IllegalArgumentException("Invalid branch ID");
		}

		try {

			try (PreparedStatement ps = conn.prepareStatement(DELETE_DELIVERY_SQL)) {

				ps.setInt(1, deliveryId);

				if (ps.executeUpdate() == 0) {
					throw new IllegalStateException("Delivery does not exist.");
				}

			}
		} catch (SQLException err) {
			throw new RuntimeException("Failed to delete branch", err);
		}

	}

	// HELPER FUNCTIONS
	private Delivery mapRowToDelivery(ResultSet rs) throws Exception {

		Integer id = rs.getInt("id");

		long epochMillis = rs.getLong("schedule_date");

		LocalDateTime scheduleDate = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();

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
