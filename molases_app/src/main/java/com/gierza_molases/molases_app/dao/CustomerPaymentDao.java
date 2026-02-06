package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
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

				String status;
				if ("Paid Cash".equals(payment.getPaymentType()) || "Paid Cheque".equals(payment.getPaymentType())) {
					status = "paid";
				} else {
					status = "pending"; // loan or partial
				}
				ps.setString(4, status);

				ps.setDouble(5, payment.getTotal());
				ps.setDouble(6, payment.getTotalPayment());

				// PROMISE TO PAY (loan only)
				if ("loan".equals(payment.getPaymentType())) {
					ps.setDate(7, new java.sql.Date(payment.getPromiseToPay().getTime()));
				} else {
					ps.setNull(7, Types.DATE);
				}

				ps.addBatch();
			}

			ps.executeBatch();
		}
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
