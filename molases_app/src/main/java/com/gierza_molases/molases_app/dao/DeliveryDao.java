package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;

import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.util.DaoUtils;

public class DeliveryDao {
	private final Connection conn;

	// INSERTS
	private static final String INSERT_DELIVERY_SQL = """
			  INSERT INTO delivery
			         (schedule_date, name, expenses, status, total_gross, overAll_profit, overAll_capital)
			         VALUES (?, ?, ?, ?, ?, ?, ?)
			""";

	public DeliveryDao(Connection conn) {
		this.conn = conn;
	}

	public int insert(Connection conn, Delivery delivery) throws SQLException {

		try (PreparedStatement ps = conn.prepareStatement(INSERT_DELIVERY_SQL, Statement.RETURN_GENERATED_KEYS)) {

			ps.setString(1, delivery.getScheduleDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			ps.setString(2, delivery.getName());
			ps.setString(3, delivery.getExpensesAsJson());
			ps.setString(4, delivery.getStatus());
			ps.setDouble(5, delivery.getTotalGross());
			ps.setDouble(6, delivery.getOverAllProfit());
			ps.setDouble(7, delivery.getOverAllCapital());

			ps.executeUpdate();

			return DaoUtils.getGeneratedId(ps);
		}

	}

}
