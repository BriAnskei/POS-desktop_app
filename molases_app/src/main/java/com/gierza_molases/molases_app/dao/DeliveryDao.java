package com.gierza_molases.molases_app.dao;

import java.sql.Connection;

public class DeliveryDao {
	private final Connection conn;

	// INSERTS
	private static final String INSERT_DELIVERY_SQL = """
			  SELECT *
			  FROM customer
			  ORDER BY created_at %s
			  LIMIT ? OFFSET ?
			""";

	public DeliveryDao(Connection conn) {
		this.conn = conn;
	}

}
