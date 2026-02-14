package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthDao {
	private final Connection conn;

	public AuthDao(Connection conn) {
		this.conn = conn;
	}

	public boolean validateLogin(String inputUserName, String inputPassword) throws SQLException, SecurityException {

		// Just select the single user row
		String sql = """
				    SELECT user_name, password
				    FROM "user"
				    LIMIT 1
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

			if (!rs.next()) {

				throw new SQLException("No user found in the database");
			}

			String storedUserName = rs.getString("user_name");
			String storedPassword = rs.getString("password");

			if (!storedUserName.equals(inputUserName)) {
				throw new SecurityException("Invalid username");
			}

			if (!storedPassword.equals(inputPassword)) {
				throw new SecurityException("Invalid password");
			}

			return true; // both matched
		}
	}

	public boolean verifyPasswordForAdmin(String inputPassword) {

		// Again, just select the single user row
		String sql = """
				    SELECT 1
				    FROM "user"
				    WHERE password = ?
				    LIMIT 1
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, inputPassword);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next(); // true if password matches
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
