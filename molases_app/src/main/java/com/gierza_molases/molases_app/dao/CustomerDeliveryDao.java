package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.gierza_molases.molases_app.model.CustomerDelivery;

public class CustomerDeliveryDao {

	private Connection conn;

	private final String INSERT_SQL = """
			  INSERT INTO customer_delivery (customer_id, delivery_id)
			     VALUES (?, ?)
			""";

	// READ
	private final String SELECT_BY_DELEIVERY_SQL = """
			 	SELECT * FROM customer_delivery WHERE delivery_id = ?
			""";

	private final String SELECT_BY_ID_SQL = """
			 	SELECT * FROM customer_delivery WHERE id = ?
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

	public List<CustomerDelivery> findAllByDeliveryId(Connection conn, int deliveryId) {
		List<CustomerDelivery> customerDelivery = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_DELEIVERY_SQL)) {

			ps.setInt(1, deliveryId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					customerDelivery.add(mapToCustomerDelivery(rs));
				}
			}

			return customerDelivery;
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch with this deliveryId " + deliveryId, e);
		}
	}

	public CustomerDelivery findById(Connection conn, int id) {
		try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapToCustomerDelivery(rs);
				} else {
					throw new RuntimeException("Cannot find customer delivery with this ID: " + id);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch branch with ID: " + id, e);
		}
	}

	private CustomerDelivery mapToCustomerDelivery(ResultSet rs) throws SQLException {
		return new CustomerDelivery(rs.getInt("id"), rs.getInt("customer_id"), rs.getInt("delivery_id"));
	}

}
