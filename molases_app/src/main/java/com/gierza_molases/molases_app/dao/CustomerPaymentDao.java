package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.gierza_molases.molases_app.model.CustomerPayments;

public class CustomerPaymentDao {
	private Connection conn;

	// INSERT
	private static final String INSERT = """
				   INSERT INTO customer_payments (customer_id, customer_delivery_id, payment_type,
			total, total_payment, balance, note) VALUES (?, ?, ?, ?, ?, ?, ?)
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
				ps.setDouble(4, payment.getTotal());
				ps.setDouble(5, payment.getTotalPayment());
				ps.setDouble(6, payment.getBalance());
				ps.setString(7, payment.getNote());

				ps.addBatch();
			}
			ps.executeBatch();
		}

	}

}
