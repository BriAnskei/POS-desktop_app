package com.gierza_molases.molases_app.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DaoUtils {
	public static String toFtsPrefix(String search) {
		return Arrays.stream(search.trim().split("\\s+")).map(token -> token + "*").collect(Collectors.joining(" "));
	}

	public static int getGeneratedId(PreparedStatement ps) {
		try (ResultSet rs = ps.getGeneratedKeys()) {
			if (rs.next()) {
				return (int) rs.getLong(1);
			}
			throw new SQLException("Inserting customer failed, no ID obtained.");
		} catch (SQLException e) {
			throw new RuntimeException("Failed to retrieve generated ID", e);
		}
	}

	// SQLite error code 19 = constraint violation
	// This includes PRIMARY KEY and UNIQUE constraints
	public static boolean isUniqueConstraintViolation(SQLException e) {
		return e.getErrorCode() == 19; // SQLITE_CONSTRAINT
	}

	public static Double getNullableDouble(ResultSet rs, String column) throws SQLException {
		double value = rs.getDouble(column);
		return rs.wasNull() ? null : value;
	}

}
