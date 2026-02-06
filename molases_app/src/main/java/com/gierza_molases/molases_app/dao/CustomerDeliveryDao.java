package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.CustomerDelivery;

public class CustomerDeliveryDao {

	private Connection conn;

	// CREATE
	private final String INSERT_SQL = """
			  INSERT INTO customer_delivery (customer_id, delivery_id, status)
			     VALUES (?, ?, ?)
			""";

	// READ
	private final String SELECT_BY_DELEIVERY_SQL = """
			 	SELECT * FROM customer_delivery WHERE delivery_id = ?
			""";

	private final String SELECT_BY_ID_SQL = """
			 	SELECT * FROM customer_delivery WHERE id = ?
			""";

	// UPDATE
	private static final String SET_STATUS_SQL = """
			    UPDATE customer_delivery
			    SET status = ?
			    WHERE id = ?
			""";

	// DELETE
	private final String DELETE_SQL = """
			 	DELETE FROM customer_delivery WHERE id = ?
			""";

	public CustomerDeliveryDao(Connection conn) {
		this.conn = conn;
	}

	public int insert(Connection conn, CustomerDelivery cd, boolean isNewDelivery) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

			ps.setInt(1, cd.getCustomerId());
			ps.setInt(2, cd.getDeliveryId());
			ps.setString(3, isNewDelivery ? "scheduled" : "delivered");
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

	public void setStatusesBatch(Map<Integer, String> customerDeliveryStatuses, Connection conn) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SET_STATUS_SQL)) {
			for (Map.Entry<Integer, String> entry : customerDeliveryStatuses.entrySet()) {
				int id = entry.getKey();
				String status = entry.getValue();

				ps.setString(1, status);
				ps.setInt(2, id);

				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	public void dropBatch(List<Integer> customerDeliveryIds, Connection conn) throws SQLException {

		try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

			for (int customerDeliverId : customerDeliveryIds) {
				ps.setInt(1, customerDeliverId);

				ps.addBatch();
			}

			ps.executeBatch();

		}
	}

	// Mapper function
	private CustomerDelivery mapToCustomerDelivery(ResultSet rs) throws SQLException {
		return new CustomerDelivery(rs.getInt("id"), rs.getInt("customer_id"), rs.getInt("delivery_id"),
				rs.getString("status"));
	}

}
