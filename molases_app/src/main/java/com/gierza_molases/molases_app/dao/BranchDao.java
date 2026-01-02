package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.util.DatabaseUtil;

public class BranchDao {

	// Create
	private static final String INSERT_BRANCH_SQL = """
			    INSERT INTO branches (customer_id, address, note)
			    VALUES (?, ?, ?)
			""";

	// Read
	private static final String SELECT_BY_CUSTOMER_ID_SQL = """
			    SELECT id, customer_id, address, note, created_at
			    FROM branches
			    WHERE customer_id = ?
			    ORDER BY created_at ASC
			""";

	/**
	 * Insert multiple branches for a customer (batch insert)
	 */
	public void insertBranches(long customerId, List<Branch> branches, Connection conn) {

		if (branches == null || branches.isEmpty()) {
			return;
		}

		try (PreparedStatement ps = conn.prepareStatement(INSERT_BRANCH_SQL)) {

			conn.setAutoCommit(false); // transaction

			for (Branch branch : branches) {

				if (branch.getCustomerId() <= 0) {
					throw new IllegalArgumentException("Customer ID is required for branch");
				}

				ps.setInt(1, customerId);
				ps.setString(2, branch.getAddress());
				ps.setString(3, branch.getNote());
				ps.addBatch();
			}

			ps.executeBatch();
			conn.commit();

		} catch (Exception e) {
			throw new RuntimeException("Failed to insert branches", e);
		}
	}

	/**
	 * Fetch all branches belonging to a specific customer
	 */
	public List<Branch> findAllByCustomerId(int customerId) {

		if (customerId <= 0) {
			throw new IllegalArgumentException("Invalid customer ID");
		}

		List<Branch> branches = new ArrayList<>();

		try (Connection conn = DatabaseUtil.getConnection();
				PreparedStatement ps = conn.prepareStatement(SELECT_BY_CUSTOMER_ID_SQL)) {

			ps.setInt(1, customerId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					branches.add(mapRowToBranch(rs));
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch branches for customer ID: " + customerId, e);
		}

		return branches;
	}

	// helper mapper
	private Branch mapRowToBranch(ResultSet rs) throws Exception {
		return new Branch(rs.getInt("id"), rs.getInt("customer_id"), rs.getString("address"), rs.getString("note"),
				rs.getString("created_at"));
	}
}
