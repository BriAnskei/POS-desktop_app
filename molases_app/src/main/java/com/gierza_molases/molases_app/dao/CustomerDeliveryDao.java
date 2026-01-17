package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerDeliveryDao {

	private Connection conn;

	private final String INSERT_SQL = """
			  INSERT INTO customer_delivery (customer_id, delivery_id)
			     VALUES (?, ?)
			""";

	public CustomerDeliveryDao(Connection conn) {
		this.conn = conn;
	}

	public int insert(Connection conn, int customerId, int deliveryId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

			ps.setInt(1, customerId);
			ps.setInt(2, deliveryId);
			ps.executeUpdate();

			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		throw new SQLException("Failed to insert customer_delivery");
	}

}
