package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.util.DaoUtils;

public class CustomerDao {

	private final Connection conn;

	private static final String INSERT_INDIVIDUAL_SQL = "INSERT INTO customer (type, first_name, mid_name, last_name, display_name,  contact_number, address) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

	private static final String INSERT_COMPANY_SQL = "INSERT INTO customer (type, company_name, display_name,  contact_number, address) "
			+ "VALUES (?, ?, ?, ?, ?)";

	// fetcher
	private static final String SELECT_ALL_SQL = """
			  SELECT *
			  FROM customer
			  ORDER BY created_at %s
			  LIMIT ? OFFSET ?
			""";
	// search ranking
	private static final String SELECT_ALL_SQL_MATCH = """
			  SELECT c.*, bm25(customer_fts) AS score
			  FROM customer_fts
			  JOIN customer c ON c.id = customer_fts.rowid
			  WHERE customer_fts MATCH ?
			  ORDER BY score, c.created_at %s
			  LIMIT ? OFFSET ?
			""";

	private static final String SELECT_BY_ID_SQL = """
			  SELECT * FROM customer WHERE id = ?
			""";

	private static final String SELECT_LIMIT_20_MATCH = """
			SELECT c.* FROM customer_fts JOIN customer c on c.id = customer_fts.rowid WHERE customer_fts MATCH ?
			ORDER BY bm25(customer_fts) LIMIT 20
			""";

	private static final String SELECT_LIMIT_20 = """
			    SELECT *
			    FROM customer
			    ORDER BY created_at DESC
			    LIMIT 20
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

	public CustomerDao(Connection conn) {
		this.conn = conn;
	}

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

			return DaoUtils.getGeneratedId(ps);
		} catch (Exception e) {
			throw new RuntimeException("Failed to insert customer", e);
		}
	}

	public int insertAsCompany(Customer customer, Connection conn) {
		try (PreparedStatement ps = conn.prepareStatement(INSERT_COMPANY_SQL)) {
			ps.setString(1, customer.getType());
			ps.setString(2, customer.getCompanyName());
			ps.setString(3, customer.getDisplayName());
			ps.setString(4, customer.getContactNumber());
			ps.setString(5, customer.getAddress());

			ps.executeUpdate();

			return DaoUtils.getGeneratedId(ps);

		} catch (Exception e) {
			throw new RuntimeException("Failed to insert customer", e);
		}
	}

	public List<Customer> selectAll(int page, int pageSize, String search, String sortOrder // "ASC" or "DESC"
	) {
		List<Customer> customers = new ArrayList<>();

		int offset = (page - 1) * pageSize;
		boolean hasSearch = search != null && !search.isBlank();

		String sql = hasSearch ? SELECT_ALL_SQL_MATCH : SELECT_ALL_SQL;

		try (PreparedStatement ps = conn.prepareStatement(sql.formatted(sortOrder))) {

			int idx = 1;

			if (hasSearch) {
				ps.setString(idx++, DaoUtils.toFtsPrefix(search));
			}

			ps.setInt(idx++, pageSize);
			ps.setInt(idx, offset);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				customers.add(mapRowToCustomer(rs));
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch customers", e);
		}

		return customers;

	}

	public List<Customer> fetch20Customer(String search) {
		List<Customer> customer = new ArrayList<>();

		boolean hasSearch = search != null && !search.isBlank();
		String sql = hasSearch ? SELECT_LIMIT_20_MATCH : SELECT_LIMIT_20;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			if (hasSearch) {
				ps.setString(1, DaoUtils.toFtsPrefix(search));
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					customer.add(mapRowToCustomer(rs));
				}
			}

			return customer;

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch 20 customer", e);
		}

	}

	public Customer findById(int branchId) {
		try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {

			ps.setInt(1, branchId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRowToCustomer(rs);
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch branch with ID: " + branchId, e);
		}
	}

	public int getTotalCount(String search) {
		try (PreparedStatement ps = conn.prepareStatement(COUNT_TOTAL_SQL)) {

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

		try (PreparedStatement ps = conn.prepareStatement(UPDATE_INDIVIDUAL_SQL)) {

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

		try (PreparedStatement ps = conn.prepareStatement(UPDATE_COMPANY_SQL)) {

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

		try (PreparedStatement ps = conn.prepareStatement(DELETE_BY_ID_SQL)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (Exception e) {
			throw new RuntimeException("Failed to delete customer", e);
		}
	}

	// helper function

	private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
		return new Customer(rs.getInt("id"), rs.getString("type"), rs.getString("first_name"), rs.getString("mid_name"),
				rs.getString("last_name"), rs.getString("company_name"), rs.getString("contact_number"),
				rs.getString("address"), rs.getTimestamp("created_at").toLocalDateTime());
	}

}
