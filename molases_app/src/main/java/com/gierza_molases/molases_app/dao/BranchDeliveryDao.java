package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.gierza_molases.molases_app.model.BranchDelivery;

public class BranchDeliveryDao {

	private Connection conn;

	// INSERT
	private final String INSERT_SQL = """
			    INSERT INTO branch_delivery
			    (customer_delivery_id, branch_id, product_id, quantity, status)
			    VALUES (?, ?, ?, ?, ?)
			""";

	public BranchDeliveryDao(Connection conn) {
		this.conn = conn;
	}

	public void insert(Connection conn, BranchDelivery bd) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
			ps.setInt(1, bd.getCustomerDeliveryId());
			ps.setInt(2, bd.getBranchId());
			ps.setInt(3, bd.getProductId());
			ps.setInt(4, bd.getQuantity());
			ps.setString(5, bd.getStatus());

			ps.executeUpdate();
		}
	}

}
