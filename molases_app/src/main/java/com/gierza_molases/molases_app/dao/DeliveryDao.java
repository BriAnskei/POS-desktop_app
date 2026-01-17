package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.gierza_molases.molases_app.model.Delivery;

public class DeliveryDao {
	private final Connection conn;

	// INSERTS
	private static final String INSERT_DELIVERY_SQL = """
			   INSERT INTO delivery (
			         schedule_date, name, expenses, status,
			         total_gross, total_capital, gross_profit,
			         total_expenses, net_profit
			     ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
			""";

	public DeliveryDao(Connection conn) {
		this.conn = conn;
	}

	public int insert(Connection conn, Delivery delivery) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(INSERT_DELIVERY_SQL, Statement.RETURN_GENERATED_KEYS)) {

			ps.setObject(1, delivery.getScheduleDate());
			ps.setString(2, delivery.getName());
			ps.setString(3, delivery.getExpensesAsJson()); // JSON string
			ps.setString(4, "scheduled");

			ps.setDouble(5, delivery.getTotalGross());
			ps.setDouble(6, delivery.getTotalCapital());
			ps.setDouble(7, delivery.getGrossProfit());
			ps.setDouble(8, delivery.getTotalExpenses());
			ps.setDouble(9, delivery.getNetProfit());

			ps.executeUpdate();

			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		throw new SQLException("Failed to insert delivery");
	}
}
