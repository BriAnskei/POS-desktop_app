package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

	// REACD

	private static final String SELECT_BY_CUSTOMER_DELIVERY_SQL = """
			 SELECT * FROM  branch_delivery WHERE customer_delivery_id = ?
			""";

	private static final String CHECK_BRANCH_DELIVERY_SQL = """
			    SELECT EXISTS (
			        SELECT 1
			        FROM branch_delivery
			        WHERE branch_id = ?
			    )
			""";

	public BranchDeliveryDao(Connection conn) {
		this.conn = conn;
	}

	public void insertAll(int customerDeliveryId, List<BranchDelivery> list) {
		try {
			insertAll(conn, customerDeliveryId, list);
		} catch (SQLException e) {
			throw new RuntimeException("Error inserting branch deliveries", e);
		}
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

	public List<BranchDelivery> findAllByCustomerDelivery(Connection conn, int customerDeliveryId) {
		List<BranchDelivery> branchDelivery = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_CUSTOMER_DELIVERY_SQL)) {

			ps.setInt(1, customerDeliveryId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					branchDelivery.add(mapToCustomerDelivery(rs));
				}
			}

			return branchDelivery;
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch with this customer delivery id " + customerDeliveryId, e);
		}
	}

	public boolean hasDelivery(int branchId) {
		try (PreparedStatement ps = conn.prepareStatement(CHECK_BRANCH_DELIVERY_SQL)) {
			ps.setInt(1, branchId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException err) {
			throw new RuntimeException("Failed to check data for branchDelivery", err);
		}

		return false;
	}

	private BranchDelivery mapToCustomerDelivery(ResultSet rs) throws SQLException {
		return new BranchDelivery(rs.getInt("id"), rs.getInt("branch_id"), rs.getInt("customer_delivery_id"),
				rs.getInt("product_id"), rs.getInt("quantity"), rs.getString("status"));
	}

}
