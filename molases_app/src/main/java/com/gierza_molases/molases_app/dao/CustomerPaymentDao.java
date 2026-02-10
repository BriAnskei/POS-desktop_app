package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
			       cp.total,
			       cp.total_payment,
			       cp.promise_to_pay,
			       cp.created_at,

			       c.display_name  AS customer_name,
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
			UPDATE customer_payments SET total_payment = ? WHERE id = ?
			""";

	public CustomerPaymentDao(Connection conn) {
		this.conn = conn;
	}

	public void insertAll(List<CustomerPayments> customerPayments, Connection conn) throws SQLException {
		if (customerPayments == null || customerPayments.isEmpty()) {
			return;
		}

		try (PreparedStatement ps = conn.prepareStatement(INSERT)) {

			for (CustomerPayments payment : customerPayments) {

				ps.setInt(1, payment.getCustomerId());
				ps.setInt(2, payment.getCustomerDeliveryId());
				ps.setString(3, payment.getPaymentType());

				ps.setString(4, payment.getStatus());

				ps.setDouble(5, payment.getTotal());
				ps.setDouble(6, payment.getTotalPayment());

				// PROMISE TO PAY (loan only)
				if ("loan".equals(payment.getPaymentType().toLowerCase())) {
					ps.setDate(7, new java.sql.Date(payment.getPromiseToPay().getTime()));
				} else {
					ps.setNull(7, Types.DATE);
				}

				ps.addBatch();
			}

			ps.executeBatch();
		}
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

		try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

			ps.setInt(1, customerPaymentId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		}

		throw new SQLException("No customer_payment found for customer_delivery_id = " + customerPaymentId);
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

	/*
	 * Used directly in the view page, changing from 'loan' to 'partial' or vice
	 * versa.
	 * 
	 * promiseToPay will be filled only when the tyoe update us changed from partial
	 * to loan
	 */
	public void updateType(int id, String newType, Date promiseToPay) throws SQLException {
		CustomerPayments customerPayments = findById(id);
		String originalType = customerPayments.getPaymentType();

		boolean fromLoanToPartial = "loan".equals(newType) && "partial".equals(originalType);
		boolean fromPartialToLoan = "partial".equals(newType) && "loan".equals(originalType);

		try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_CUSTOMER_DELIVERY_ID)) {

			ps.setString(1, newType);

			// set promise to pay on Loan updates
			if (fromPartialToLoan) {
				ps.setDate(2, new java.sql.Date(promiseToPay.getTime()));
			} else if (fromLoanToPartial) {
				ps.setNull(2, Types.DATE);
			} else {
				throw new SQLException("Invalid status action");
			}

			ps.setInt(3, id);

			ps.executeUpdate();
		}
	}

	public void updatePromiseToPay(int id, Date newPromiseToPay) {

	}

	// Utility functions

	/*
	 * Complete property map with addition properties
	 */
	private CustomerPayments mapRow(ResultSet rs) throws SQLException {

		return new CustomerPayments(rs.getInt("id"), rs.getInt("customer_id"), rs.getInt("customer_delivery_id"),
				rs.getString("payment_type"), rs.getString("status"), rs.getDouble("total"),
				rs.getDouble("total_payment"), rs.getDate("promise_to_pay"),
				rs.getTimestamp("created_at").toLocalDateTime(),

				// JOIN fields

				rs.getInt("deliver_id"), rs.getString("customer_name"), rs.getString("delivery_name"),
				rs.getDate("delivery_date"));
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
