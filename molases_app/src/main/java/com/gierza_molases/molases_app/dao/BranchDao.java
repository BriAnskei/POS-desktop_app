package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.util.DaoUtils;

public class BranchDao {

	private final Connection conn;

	// Create
	private static final String INSERT_BRANCH_SQL = """
			    INSERT INTO branches (customer_id, address, note)
			    VALUES (?, ?, ?)
			""";

	// Read
	// pagination control
	private static final String SELECT_BRANCHES_BASE = """
						    SELECT
			    b.id,
			    b.customer_id,
			    b.address,
			    b.note,
			    b.created_at,
			    c.display_name
			FROM branches b
			JOIN customer c ON c.id = b.customer_id
			WHERE b.id > ?
			ORDER BY b.id ASC
			LIMIT ?;

						""";

	// search with FTS
	private static final String SELECT_BRANCHES_BASE_SEARCH = """
				SELECT
			    b.id,
			    b.customer_id,
			    b.address,
			    b.note,
			    b.created_at,
			    c.display_name,
			    bm25(customer_fts) AS score
			FROM customer_fts
			JOIN customer c ON c.id = customer_fts.rowid
			JOIN branches b ON b.customer_id = c.id
			WHERE customer_fts MATCH ?
			  AND b.id > ?
			ORDER BY b.id ASC
			LIMIT ?;


					""";

	private static final String SELECT_BY_CUSTOMER_ID_SQL = """
			    SELECT id, customer_id, address, note, created_at
			    FROM branches
			    WHERE customer_id = ?
			    ORDER BY created_at ASC
			""";

	private static final String SELECT_BY_BRANCH_ID_SQL = """
			  SELECT *
			     FROM branches
			     WHERE id = ?
			""";

	// Update
	private static final String UPDATE_BRANCH_SQL = """
			    UPDATE branches
			    SET address = ?, note = ?
			    WHERE id = ?
			""";

	// Delete
	private static final String DELETE_BRANCH_SQL = """
			    DELETE FROM branches
			    WHERE id = ?
			""";

	public BranchDao(Connection conn) {
		this.conn = conn;

	}

	/**
	 * Insert single branch for a customer
	 */
	public void insertBranch(int customerId, Branch branch) {

		try (PreparedStatement ps = conn.prepareStatement(INSERT_BRANCH_SQL)) {
			ps.setInt(1, customerId);
			ps.setString(2, branch.getAddress());
			ps.setString(3, branch.getNote());

			ps.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("Failed to get DB connection", e);
		}
	}

	/**
	 * Insert multiple branches for a customer (batch insert)
	 */
	public void insertBranches(int customerId, List<Branch> branches, Connection conn) {

		if (branches == null || branches.isEmpty()) {
			return;
		}

		try (PreparedStatement ps = conn.prepareStatement(INSERT_BRANCH_SQL)) {

			conn.setAutoCommit(false); // transaction

			for (Branch branch : branches) {

				if (customerId <= 0) {
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

	public List<Branch> fetchNextPage(Long lastSeenBranchId, String customerNameFilter, int pageSize) {
		boolean hasSearch = customerNameFilter != null && !customerNameFilter.isBlank();

		return hasSearch ? this.fetchNextPageWithSearch(lastSeenBranchId, customerNameFilter, pageSize)
				: this.findNextPageNoSearch(lastSeenBranchId, pageSize);

	}

	/**
	 * Fetch all branches
	 */
	private List<Branch> findNextPageNoSearch(Long lastSeenBranchId, int pageSize) {

		List<Branch> branches = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(SELECT_BRANCHES_BASE)) {

			ps.setLong(1, lastSeenBranchId != null ? lastSeenBranchId : 0L);
			ps.setInt(2, pageSize);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					branches.add(mapRowToBranchOnJoin(rs));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch branches", e);
		}

		return branches;
	}

	private List<Branch> fetchNextPageWithSearch(Long lastSeenBranchId, String customerNameFilter, int pageSize) {

		List<Branch> branches = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(SELECT_BRANCHES_BASE_SEARCH)) {

			ps.setString(1, DaoUtils.toFtsPrefix(customerNameFilter));
			ps.setLong(2, lastSeenBranchId != null ? lastSeenBranchId : 0L);
			ps.setInt(3, pageSize);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					branches.add(mapRowToBranchOnJoin(rs));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch branches (FTS)", e);
		}

		return branches;
	}

	/**
	 * find branch by id
	 */
	public Branch findById(int branchId) {
		try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_BRANCH_ID_SQL)) {

			ps.setInt(1, branchId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRowToBranch(rs);
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch branch with ID: " + branchId, e);
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

		try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_CUSTOMER_ID_SQL)) {

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

	/**
	 * Update branch details
	 */
	public boolean updateBranch(int branchId, Branch branch) {

		if (branch == null || branchId <= 0) {
			throw new IllegalArgumentException("Valid branch is required for update");
		}

		try (PreparedStatement ps = conn.prepareStatement(UPDATE_BRANCH_SQL)) {

			ps.setString(1, branch.getAddress());
			ps.setString(2, branch.getNote());
			ps.setInt(3, branchId);

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			throw new RuntimeException("Failed to update branch with ID: " + branch.getId(), e);
		}
	}

	/**
	 * Delete branch by ID
	 * 
	 * @throws SQLException
	 */
	public boolean deleteBranch(int branchId) {

		if (branchId <= 0) {
			throw new IllegalArgumentException("Invalid branch ID");
		}

		try {

			try (PreparedStatement ps = conn.prepareStatement(DELETE_BRANCH_SQL)) {

				ps.setInt(1, branchId);
				return ps.executeUpdate() > 0;

			}
		} catch (SQLException err) {
			throw new RuntimeException("Failed to delete branch", err);
		}

	}

	// helper mapper
	private Branch mapRowToBranchOnJoin(ResultSet rs) throws Exception {
		return new Branch(rs.getInt("id"), rs.getInt("customer_id"), rs.getString("display_name"),
				rs.getString("address"), rs.getString("note"), rs.getString("created_at"));
	}

	private Branch mapRowToBranch(ResultSet rs) throws Exception {
		return new Branch(rs.getInt("id"), rs.getInt("customer_id"), null, rs.getString("address"),
				rs.getString("note"), rs.getString("created_at"));
	}

}
