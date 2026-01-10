package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.util.DaoUtils;

public class ProductAssociationDao {

	private final Connection conn;

	// CREATE QUERY
	private static final String INSERT_SQL = """
			    INSERT  INTO product_association (product_id, customer_id)
			    VALUES (?, ?)
			""";

	// READ QUERY
	private static final String FETCH_BY_ID_SQL = """
						    SELECT c.*
			FROM customer c
			JOIN product_association pa
			    ON pa.customer_id = c.id
			WHERE pa.product_id = ?;

						""";

	// DROP QUERY
	private static final String DELETE_SQL = """
			    DELETE FROM product_association
			    WHERE product_id = ?
			      AND customer_id = ?
			""";

	public ProductAssociationDao(Connection conn) {
		this.conn = conn;
	}

	public void insertAll(int productId, List<Integer> customerIds, Connection conn) {

		try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

			for (Integer customerId : customerIds) {
				ps.setInt(1, productId);
				ps.setInt(2, customerId);
				ps.addBatch();
			}

			ps.executeBatch();

		} catch (SQLException e) {
			throw new RuntimeException("Failed to insert product associations", e);
		}
	}

	public void insertAll(int productId, List<Integer> customerIds) {

		try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

			for (Integer customerId : customerIds) {
				ps.setInt(1, productId);
				ps.setInt(2, customerId);
				ps.addBatch();
			}

			ps.executeBatch();

		} catch (SQLException e) {
			if (DaoUtils.isUniqueConstraintViolation(e)) {
				throw new RuntimeException("Duplicate customer–product associations detected.");
			}
			throw new RuntimeException("Failed to insert product associations", e);
		}
	}

	public List<Customer> fetchAssociationByProductId(int productId) {
		List<Customer> customers = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(FETCH_BY_ID_SQL)) {

			ps.setInt(1, productId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Customer customer = mapRowToCustomer(rs);
					customers.add(customer);
				}
			}

			return customers;
		} catch (SQLException e) {
			if (DaoUtils.isUniqueConstraintViolation(e)) {
				throw new RuntimeException("Duplicate customer–product associations detected.");
			}
			throw new RuntimeException("Failed to fetch customers by product ID", e);
		}

	}

	public void removeAssociation(int productId, int customerId) {

		try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

			ps.setInt(1, productId);
			ps.setInt(2, customerId);

			ps.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("Failed to remove product-customer association", e);
		}
	}

// helper function
	private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
		return new Customer(rs.getInt("id"), rs.getString("type"), rs.getString("first_name"), rs.getString("mid_name"),
				rs.getString("last_name"), rs.getString("company_name"), rs.getString("contact_number"),
				rs.getString("address"), rs.getTimestamp("created_at").toLocalDateTime());
	}

}
