package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.gierza_molases.molases_app.model.CustomerPayments;

public class CustomerPaymentDao {
	private Connection conn;

	// INSERT
	private static final String INSERT = """
			    INSERT INTO customer_payments (
			        customer_id,
			        customer_delivery_id,
			        payment_type,
			        status,
			        total,
			        total_payment,
			        promise_to_pay
			    ) VALUES (?, ?, ?, ?, ?, ?, ?)
			""";

	// READ
	private static final String SELECT_PAYMENTS_BASE = """
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

			        c.display_name        AS customer_name,

			        d.id				  AS deliver_id,
			        d.name                AS delivery_name,
			        d.schedule_date       AS delivery_date
			    FROM customer_payments cp
			    JOIN customer c ON c.id = cp.customer_id
			    LEFT JOIN customer_delivery cd ON cd.id = cp.customer_delivery_id
			    LEFT JOIN delivery d ON d.id = cd.delivery_id
			    WHERE cp.id < ?
			    ORDER BY cp.id DESC
			    LIMIT ?;
			""";

	private static final String SELECT_PAYMENTS_BASE_SEARCH = """
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

			        c.display_name  AS customer_name,


			        d.id				  AS deliver_id,
			        d.name          AS delivery_name,
			        d.schedule_date AS delivery_date
			    FROM customer_payments cp
			    JOIN customer c ON c.id = cp.customer_id
			    LEFT JOIN customer_delivery cd ON cd.id = cp.customer_delivery_id
			    LEFT JOIN delivery d ON d.id = cd.delivery_id
			    WHERE cp.id < ?
			      AND (
			            ? IS NULL
			            OR c.display_name LIKE ?
			            OR d.name LIKE ?
			          )
			      AND (? IS NULL OR cp.payment_type = ?)
			      AND (? IS NULL OR cp.status = ?)
			      AND (
			            ? IS NULL
			            OR (d.schedule_date IS NOT NULL AND d.schedule_date >= ?)
			          )
			      AND (
			            ? IS NULL
			            OR (d.schedule_date IS NOT NULL AND d.schedule_date < ?)
			          )
			    ORDER BY cp.id DESC
			    LIMIT ?;
			""";

	private static final String SELECT_BY_CUSTOMER_DELIVERY_ID = """
			    SELECT
			        id,
			        customer_id,
			        customer_delivery_id,
			        payment_type,
			        status,
			        total,
			        total_payment,
			        promise_to_pay,
			        created_at
			    FROM customer_payments
			    WHERE customer_delivery_id = ?
			""";

	private static final String SELECT_BY_ID = """
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


			       c.display_name  AS customer_name,


			         d.id				  AS deliver_id,
			       d.name          AS delivery_name,
			       d.schedule_date AS delivery_date
			   FROM customer_payments cp
			   JOIN customer c ON c.id = cp.customer_id
			   LEFT JOIN customer_delivery cd ON cd.id = cp.customer_delivery_id
			   LEFT JOIN delivery d ON d.id = cd.delivery_id
			   WHERE cp.id = ?
			""";

	// UPDATE
	private final String UPDATE_TYPE_SQL = """
			UPDATE customer_payments SET payment_type = ? WHERE id = ?
			""";
	private final String UPDATE_STATUS_SQL = """
			UPDATE customer_payments SET status = ? WHERE id = ?
			""";

	private final String UPDATE_PROMISE_TO_PAY = """
			UPDATE customer_payments SET promise_to_pay = ? WHERE id = ?
			""";

	private final String UPDATE_TOTAL_PAYMEN = """
			UPDATE customer_payments SET total_payment = ? WHERE id = ?
			""";

	private final String SET_NOTES = """
			UPDATE customer_payments SET notes = ? WHERE id = ?
			""";

	public CustomerPaymentDao(Connection conn) {
		this.conn = conn;
	}

	public int insert(CustomerPayments customerPayment, Connection conn) throws SQLException {

		try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

			ps.setInt(1, customerPayment.getCustomerId());
			ps.setInt(2, customerPayment.getCustomerDeliveryId());
			ps.setString(3, customerPayment.getPaymentType());
			ps.setString(4, customerPayment.getStatus());
			ps.setDouble(5, customerPayment.getTotal());
			ps.setDouble(6, customerPayment.getTotalPayment());

			// PROMISE TO PAY (loan only)
			if ("loan".equalsIgnoreCase(customerPayment.getPaymentType())) {
				ps.setDate(7, new java.sql.Date(customerPayment.getPromiseToPay().getTime()));
			} else {
				ps.setNull(7, Types.DATE);
			}
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		throw new SQLException("Failed to insert customer_delivery");

	}

	public List<CustomerPayments> fetchNextPage(Long lastSeenPaymentId, String search, String paymentType,
			String status, LocalDateTime fromDate, LocalDateTime toDate, int pageSize) {
		boolean hasFilter = (search != null && !search.isBlank()) || paymentType != null || status != null
				|| fromDate != null || toDate != null;
		return hasFilter
				? fetchNextPageWithSearch(lastSeenPaymentId, search, paymentType, status, fromDate, toDate, pageSize)
				: fetchNextPageNoSearch(lastSeenPaymentId, pageSize);
	}

	private List<CustomerPayments> fetchNextPageNoSearch(Long lastSeenPaymentId, int pageSize) {
		List<CustomerPayments> payments = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(SELECT_PAYMENTS_BASE)) {

			long cursor = lastSeenPaymentId != null ? lastSeenPaymentId : Long.MAX_VALUE;

			int i = 1;
			ps.setLong(i++, cursor);

			ps.setInt(i++, pageSize);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					payments.add(mapRow(rs));
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch payments", e);
		}

		return payments;
	}

	private List<CustomerPayments> fetchNextPageWithSearch(Long lastSeenPaymentId, String search, String paymentType,
			String status, LocalDateTime fromDate, LocalDateTime toDate, int pageSize) {

		List<CustomerPayments> payments = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(SELECT_PAYMENTS_BASE_SEARCH)) {

			long cursor = lastSeenPaymentId != null ? lastSeenPaymentId : Long.MAX_VALUE;

			// search
			String likeSearch = (search == null || search.isBlank()) ? null : "%" + search.trim() + "%";

			int i = 1;
			ps.setLong(i++, cursor);

			// SEARCH (3 params!)
			ps.setString(i++, likeSearch); // ? IS NULL
			ps.setString(i++, likeSearch); // c.display_name LIKE ?
			ps.setString(i++, likeSearch); // d.name LIKE ?

			ps.setString(i++, paymentType);
			ps.setString(i++, paymentType);

			String statusLow = status == null || status.isBlank() ? null : status.toLowerCase();

			ps.setString(i++, statusLow);
			ps.setString(i++, statusLow);

			Timestamp fromTs = fromDate == null ? null : Timestamp.valueOf(fromDate);
			Timestamp toTs = toDate == null ? null : Timestamp.valueOf(toDate.plusDays(1));

			ps.setTimestamp(i++, fromTs);
			ps.setTimestamp(i++, fromTs);

			ps.setTimestamp(i++, toTs);
			ps.setTimestamp(i++, toTs);

			ps.setInt(i, pageSize);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					payments.add(mapRow(rs));
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch payments (search)", e);
		}

		return payments;
	}

	public CustomerPayments findById(int customerPaymentId) throws SQLException {
		return findById(customerPaymentId, conn);
	}

	public CustomerPayments findById(int customerPaymentId, Connection conn) throws SQLException {

		try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

			ps.setInt(1, customerPaymentId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					CustomerPayments cp = mapRow(rs);
					return cp;
				}
			}
		}

		throw new SQLException("No customer_payment found for id = " + customerPaymentId);
	}

	public CustomerPayments findByCustomerDeliveryId(int customerDeliveryId, Connection conn) throws SQLException {

		try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_CUSTOMER_DELIVERY_ID)) {

			ps.setInt(1, customerDeliveryId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapCustomerPayments(rs);
				}
			}
		}

		throw new SQLException("No customer_payment found for customer_delivery_id = " + customerDeliveryId);
	}

	public void setNotes(int id, String notes) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SET_NOTES)) {
			ps.setString(1, notes);
			ps.setInt(2, id);

			ps.executeUpdate();
		}
	}

	// increase the total_paid based on the new amount
	public void updateForNewPayment(int id, double paymentAmount, Connection conn) throws SQLException {
		CustomerPayments customerPayment = this.findById(id, conn);

		if (customerPayment == null) {
			throw new SQLException("Failed in updateForNewPayment: Cannot find customer oayment with this id");
		}

		double currentAmount = customerPayment.getTotalPayment();
		// add the old one to new payment
		double updatedValue = currentAmount + paymentAmount;
		updateTotalPayment(id, updatedValue, conn);
	}

	public void updateTotalPayment(int id, double amount, Connection conn) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(UPDATE_TOTAL_PAYMEN)) {

			ps.setDouble(1, amount);
			ps.setInt(2, id);

			ps.executeUpdate();
		}

	}

	public void updateType(int id, String newType, Connection conn) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(UPDATE_TYPE_SQL)) {
			ps.setString(1, newType);
			ps.setInt(2, id);
			ps.executeUpdate();

		}
	}

	public void updatePromiseToPay(int id, Date newPromiseToPay) throws SQLException {
		updatePromiseToPay(id, newPromiseToPay, conn);
	}

	public void updatePromiseToPay(int id, Date newPromiseToPay, Connection conn) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(UPDATE_PROMISE_TO_PAY)) {

			if (newPromiseToPay != null) {
				ps.setDate(1, new java.sql.Date(newPromiseToPay.getTime()));
			} else {
				ps.setNull(1, java.sql.Types.DATE);
			}

			ps.setInt(2, id);
			ps.executeUpdate();
		}
	}

	public void updateStatusBasedOnTotalPayment(int id, Connection conn) throws SQLException {
		CustomerPayments cp = findById(id, conn);

		boolean isFullyPaid = cp.getTotal() == cp.getTotalPayment();

		if (isFullyPaid) {
			updateStatus(id, "complete", conn);
		} else {
			updateStatus(id, "pending", conn);
		}

	}

	public void updateStatus(int id, String newStatus, Connection conn) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS_SQL)) {
			ps.setString(1, newStatus);
			ps.setInt(2, id);
			ps.executeUpdate();

		}
	}

	// Utility functions

	/*
	 * Complete property map with addition properties
	 */
	private CustomerPayments mapRow(ResultSet rs) {
		try {
			return new CustomerPayments(rs.getInt("id"), rs.getInt("customer_id"), rs.getInt("customer_delivery_id"),
					rs.getString("payment_type"), rs.getString("status"), rs.getString("notes"), rs.getDouble("total"),
					rs.getDouble("total_payment"), rs.getDate("promise_to_pay"),
					rs.getTimestamp("created_at").toLocalDateTime(),

					// JOIN fields
					rs.getInt("deliver_id"), rs.getString("customer_name"), rs.getString("delivery_name"),
					rs.getDate("delivery_date"));
		} catch (SQLException err) {
			System.err.println("Failed mapping: " + err);
			throw new RuntimeException("Error mapping CustomerPayments row", err);
		}
	}

	/*
	 * raw data map for delivery detials functionality
	 */
	private CustomerPayments mapCustomerPayments(ResultSet rs) throws SQLException {

		Integer id = rs.getInt("id");
		if (rs.wasNull()) {
			id = null;
		}

		int customerId = rs.getInt("customer_id");
		int customerDeliveryId = rs.getInt("customer_delivery_id");

		String paymentType = rs.getString("payment_type");
		String status = rs.getString("status");

		double total = rs.getDouble("total");
		double totalPayment = rs.getDouble("total_payment");

		Date promiseToPay = rs.getDate("promise_to_pay");

		Timestamp createdAtTs = rs.getTimestamp("created_at");
		LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : null;

		return new CustomerPayments(id, customerId, customerDeliveryId, paymentType, status, total, totalPayment,
				promiseToPay, createdAt);
	}

}
