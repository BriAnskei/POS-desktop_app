package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.util.DatabaseUtil;

public class CustomerDao {

	private static final String INSERT_INDIVIDUAL_SQL = "INSERT INTO customer (type, first_name, mid_name, last_name, display_name,  contact_number, address) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

	private static final String INSERT_COMPANY_SQL = "INSERT INTO customer (type, company_name, display_name,  contact_number, address) "
			+ "VALUES (?, ?, ?, ?, ?)";

	// fetcher
	private static final String SELECT_ALL_SQL = """
			    SELECT *
			    FROM customer
			    WHERE (? IS NULL OR display_name LIKE ?)
			    ORDER BY created_at %s
			    LIMIT ? OFFSET ?
			""";

	private static final String COUNT_TOTAL_SQL = """
			    SELECT COUNT(*)
			    FROM customer
			    WHERE (? IS NULL OR display_name LIKE ?)
			""";

	// UPDATE
	private static final String UPDATE_INDIVIDUAL_SQL = """
			    UPDATE customer
			    SET
			        first_name = ?,
			        mid_name   = ?,
			        last_name  = ?,
			        display_name = ?,
			        contact_number = ?,
			        address = ?
			    WHERE id = ? AND type = 'individual'
			""";

	private static final String UPDATE_COMPANY_SQL = """
			    UPDATE customer
			    SET
			        company_name = ?,
			        display_name = ?,
			        contact_number = ?,
			        address = ?
			    WHERE id = ? AND type = 'company'
			""";

	// DELETE
	private static final String DELETE_BY_ID_SQL = """
			    DELETE FROM customer WHERE id = ?
			""";

	public int insertAsIndividual(Customer customer, Connection conn) {
		try (PreparedStatement ps = conn.prepareStatement(INSERT_INDIVIDUAL_SQL)) {
			ps.setString(1, customer.getType());
			ps.setString(2, customer.getFirstName());
			ps.setString(3, customer.getMidName());
			ps.setString(4, customer.getLastName());
			ps.setString(5, customer.getDisplayName());
			ps.setString(6, customer.getContactNumber());
			ps.setString(7, customer.getAddress());

			ps.executeUpdate();

			return this.getGeneratedId(ps);
		} catch (Exception e) {
			throw new RuntimeException("Failed to insert customer", e);
		}
	}

	public long insertAsCompany(Customer customer, Connection conn) {
		try (PreparedStatement ps = conn.prepareStatement(INSERT_COMPANY_SQL)) {
			ps.setString(1, customer.getType());
			ps.setString(2, customer.getCompanyName());
			ps.setString(3, customer.getDisplayName());
			ps.setString(4, customer.getContactNumber());
			ps.setString(5, customer.getAddress());

			ps.executeUpdate();

			return this.getGeneratedId(ps);

		} catch (Exception e) {
			throw new RuntimeException("Failed to insert customer", e);
		}
	}

	public List<Customer> selectAll(int page, int pageSize, String search, String sortOrder // "ASC" or "DESC"
	) {
		List<Customer> customers = new ArrayList<>();

		int offset = (page - 1) * pageSize;
		String sql = SELECT_ALL_SQL.formatted(sortOrder);

		try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			if (search == null || search.isBlank()) {
				ps.setNull(1, java.sql.Types.VARCHAR);
				ps.setNull(2, java.sql.Types.VARCHAR);
			} else {
				ps.setString(1, search);
				ps.setString(2, "%" + search + "%");
			}

			ps.setInt(3, pageSize);
			ps.setInt(4, offset);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				customers.add(mapRowToCustomer(rs));
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch customers", e);
		}

		return customers;

	}

	public int getTotalCount(String search) {
		try (Connection conn = DatabaseUtil.getConnection();
				PreparedStatement ps = conn.prepareStatement(COUNT_TOTAL_SQL)) {

			if (search == null || search.isBlank()) {
				ps.setNull(1, java.sql.Types.VARCHAR);
				ps.setNull(2, java.sql.Types.VARCHAR);
			} else {
				ps.setString(1, search);
				ps.setString(2, search + "%"); // index-friendly
			}

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {

					return rs.getInt(1);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to count customers", e);
		}

		return 0;
	}

	public void updateIndividual(Customer customer) {

		if (customer.getId() <= 0) {
			throw new IllegalArgumentException("Customer ID is required");
		}

		try (Connection conn = DatabaseUtil.getConnection();
				PreparedStatement ps = conn.prepareStatement(UPDATE_INDIVIDUAL_SQL)) {

			ps.setString(1, customer.getFirstName());
			ps.setString(2, customer.getMidName());
			ps.setString(3, customer.getLastName());
			ps.setString(4, customer.getDisplayName());
			ps.setString(5, customer.getContactNumber());
			ps.setString(6, customer.getAddress());
			ps.setInt(7, customer.getId());

			ps.executeUpdate();

		} catch (Exception e) {
			throw new RuntimeException("Failed to update individual customer", e);
		}
	}

	public void updateCompany(Customer customer) {

		if (customer.getId() <= 0) {
			throw new IllegalArgumentException("Customer ID is required");
		}

		try (Connection conn = DatabaseUtil.getConnection();
				PreparedStatement ps = conn.prepareStatement(UPDATE_COMPANY_SQL)) {

			ps.setString(1, customer.getCompanyName());
			ps.setString(2, customer.getDisplayName());
			ps.setString(3, customer.getContactNumber());
			ps.setString(4, customer.getAddress());
			ps.setInt(5, customer.getId());

			ps.executeUpdate();

		} catch (Exception e) {
			throw new RuntimeException("Failed to update company customer", e);
		}
	}

	public void deleteById(int id) {

		if (id <= 0) {
			throw new IllegalArgumentException("Invalid customer ID");
		}

		try (Connection conn = DatabaseUtil.getConnection();
				PreparedStatement ps = conn.prepareStatement(DELETE_BY_ID_SQL)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (Exception e) {
			throw new RuntimeException("Failed to delete customer", e);
		}
	}

	// helper function
	private int getGeneratedId(PreparedStatement ps) {
		try (ResultSet rs = ps.getGeneratedKeys()) {
			if (rs.next()) {
				return (int) rs.getLong(1);
			}
			throw new SQLException("Inserting customer failed, no ID obtained.");
		} catch (SQLException e) {
			throw new RuntimeException("Failed to retrieve generated ID", e);
		}
	}

	private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
		return new Customer(rs.getInt("id"), rs.getString("type"), rs.getString("first_name"), rs.getString("mid_name"),
				rs.getString("last_name"), rs.getString("company_name"), rs.getString("contact_number"),
				rs.getString("address"), rs.getTimestamp("created_at").toLocalDateTime());
	}

}
