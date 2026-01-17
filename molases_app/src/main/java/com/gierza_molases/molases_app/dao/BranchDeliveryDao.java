package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.gierza_molases.molases_app.model.BranchDelivery;

public class BranchDeliveryDao {

	private Connection conn;

	// INSERT
	private static final String INSERT = """
			    INSERT INTO branch_delivery (
			        customer_delivery_id, branch_id, product_id, quantity, status
			    ) VALUES (?, ?, ?, ?, ?)
			""";

	public BranchDeliveryDao(Connection conn) {
		this.conn = conn;
	}

	public void insertAll(Connection conn, int customerDeliveryId, List<BranchDelivery> list) throws SQLException {

		try (PreparedStatement ps = conn.prepareStatement(INSERT)) {

			for (BranchDelivery bd : list) {
				ps.setInt(1, customerDeliveryId);
				ps.setInt(2, bd.getBranchId());
				ps.setInt(3, bd.getProductId());
				ps.setInt(4, bd.getQuantity());
				ps.setString(5, bd.getStatus());
				ps.addBatch();
			}

			ps.executeBatch();
		}
	}

}
