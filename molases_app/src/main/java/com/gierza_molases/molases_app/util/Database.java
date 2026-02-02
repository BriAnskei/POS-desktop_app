package com.gierza_molases.molases_app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

	private static Connection conn;
	private static final String DB_URL = "jdbc:sqlite:database/molases.db";

	private Database() {
	};

	public static synchronized Connection init() {
		if (conn != null)
			return conn;

		try {
			conn = DriverManager.getConnection(DB_URL);

			try (Statement stmt = conn.createStatement()) {
				  stmt.execute("PRAGMA foreign_keys = ON;");
				
				stmt.execute("PRAGMA journal_mode = WAL;");
				stmt.execute("PRAGMA synchronous = NORMAL;");
				stmt.execute("PRAGMA busy_timeout = 5000;");
			}

			return conn;

		} catch (SQLException e) {
			throw new RuntimeException("Failed to initialize DB: ", e);
		}

	}

}
