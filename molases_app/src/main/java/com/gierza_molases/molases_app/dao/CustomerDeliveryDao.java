package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.gierza_molases.molases_app.model.CustomerDelivery;
import com.gierza_molases.molases_app.util.DaoUtils;

public class CustomerDeliveryDao {

	private Connection conn;

	private final String INSERT_SQL = """
			    INSERT INTO customer_delivery (customer_id, delivery_id)
			    VALUES (?, ?)
			""";

	public CustomerDeliveryDao(Connection conn) {
		this.conn = conn;
	}

	public int insert(Connection conn, CustomerDelivery cd) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

			ps.setInt(1, cd.getCustomerId());
			ps.setInt(2, cd.getDeliveryId());

			ps.executeUpdate();

			return DaoUtils.getGeneratedId(ps);
		}

	}

}
