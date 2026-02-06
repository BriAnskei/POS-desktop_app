package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.BranchDelivery;

public class BranchDeliveryDao {

	private Connection conn;

	// INSERT
	private static final String INSERT = """
			    INSERT INTO branch_delivery (
			        customer_delivery_id, branch_id, status
			    ) VALUES (?, ?, ?)
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

	// UPDATE
	private static final String UPDATE_STATUSES_SQL = """
			     UPDATE branch_delivery
			      SET status = ?
			      WHERE id = ?
			""";

	// DELETE
	private static final String DELETE_SQL = """
			DELETE FROM branch_delivery WHERE id = ?
			""";

	public BranchDeliveryDao(Connection conn) {
		this.conn = conn;
	}

	public int insert(Connection conn, int customerDeliveryId, BranchDelivery branchDelivery) throws SQLException {

		try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

			ps.setInt(1, customerDeliveryId);
			ps.setInt(2, branchDelivery.getBranchId());
			ps.setString(3, branchDelivery.getStatus());

			ps.executeUpdate();

			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1); // generated ID
				} else {
					throw new SQLException("Creating branch_delivery failed, no ID obtained.");
				}
			}
		}
	}

	public void insertBatch(Connection conn, int customerDeliveryId, List<BranchDelivery> branchDeliveryList)
			throws SQLException {
		for (BranchDelivery bd : branchDeliveryList) {
			insert(conn, customerDeliveryId, bd);
		}
	}

	public List<BranchDelivery> findAllByCustomerDelivery(Connection conn, int customerDeliveryId) {
		List<BranchDelivery> branchDeliveries = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_CUSTOMER_DELIVERY_SQL)) {

			ps.setInt(1, customerDeliveryId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {

					BranchDelivery branchDelivery = mapToCustomerDelivery(rs);
					branchDeliveries.add(branchDelivery);
				}
			}

			return branchDeliveries;
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

	public void setBranchDeliverStatusBatch(Map<Integer, String> branchDeliveryStatuses, Connection conn)
			throws SQLException {
		if (branchDeliveryStatuses == null || branchDeliveryStatuses.isEmpty()) {
			return;
		}

		try (PreparedStatement ps = conn.prepareStatement(UPDATE_STATUSES_SQL)) {

			for (Map.Entry<Integer, String> entry : branchDeliveryStatuses.entrySet()) {
				int branchDeliveryId = entry.getKey();
				String status = entry.getValue();

				ps.setString(1, status);
				ps.setInt(2, branchDeliveryId);
				ps.addBatch();
			}

			ps.executeBatch();

		}
	}

	public void dropBatch(List<Integer> droppedIds, Connection conn) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

			for (int branchDeliverId : droppedIds) {
				ps.setInt(1, branchDeliverId);
				ps.addBatch();
			}

			ps.executeBatch();

		}
	}

	private BranchDelivery mapToCustomerDelivery(ResultSet rs) throws SQLException {
		return new BranchDelivery(rs.getInt("id"), rs.getInt("branch_id"), rs.getInt("customer_delivery_id"),
				rs.getString("status"));
	}

}
