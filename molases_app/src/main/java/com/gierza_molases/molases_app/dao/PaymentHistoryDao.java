package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.gierza_molases.molases_app.model.PaymentHistory;

public class PaymentHistoryDao {

	private final Connection conn;

	// ===== SQL
	private static final String INSERT_SQL = """
			    INSERT INTO payment_history (customer_payment_id, amount)
			    VALUES (?, ?)
			""";

	private static final String FETCH_BY_CUSTOMER_PAYMENT_ID = """
			    SELECT id, customer_payment_id, amount, created_at
			    FROM payment_history
			    WHERE customer_payment_id = ?
			    ORDER BY created_at ASC
			""";

	private static final String UPDATE_AMOUNT_SQL = """
			    UPDATE payment_history
			    SET amount = ?
			    WHERE id = ?
			""";

	private static final String DELETE_SQL = """
			    DELETE FROM payment_history
			    WHERE id = ?
			""";

	public PaymentHistoryDao(Connection conn) {
		this.conn = conn;
	}

	// ===== CREATE
	public void insert(int customerPaymentId, PaymentHistory paymentHistory) throws SQLException {
		insert(customerPaymentId, paymentHistory, conn);
	}

	public void insert(int customerPaymentId, PaymentHistory paymentHistory, Connection conn) throws SQLException {

		try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
			ps.setInt(1, customerPaymentId);
			ps.setDouble(2, paymentHistory.getAmount());
			ps.executeUpdate();
		}
	}

	// ===== READ
	public List<PaymentHistory> fetchByCustomerPaymentId(int customerPaymentId, Connection conn) throws SQLException {

		List<PaymentHistory> histories = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(FETCH_BY_CUSTOMER_PAYMENT_ID)) {
			ps.setInt(1, customerPaymentId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					PaymentHistory ph = new PaymentHistory();
					ph.setId(rs.getInt("id"));
					ph.setCustomerPaymentId(rs.getInt("customer_payment_id"));
					ph.setAmount(rs.getDouble("amount"));
					ph.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));

					histories.add(ph);
				}
			}
		}

		return histories;
	}

	// ===== UPDATE
	public void updateAmount(int id, double newAmount) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(UPDATE_AMOUNT_SQL)) {
			ps.setDouble(1, newAmount);
			ps.setInt(2, id);
			ps.executeUpdate();
		}
	}

	// ===== DELETE
	public void delete(int id) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
			ps.setInt(1, id);
			ps.executeUpdate();
		}
	}
}
