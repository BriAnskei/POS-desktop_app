package com.gierza_molases.molases_app.util;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionHelper {

	@FunctionalInterface
	public interface TransactionCallback {
		void execute(Connection conn) throws Exception;
	}

	public static synchronized void executeInTransaction(TransactionCallback callback) {
		Connection conn = Database.init();
		boolean originalAutoCommit = false;

		try {
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			callback.execute(conn);

			conn.commit();
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				throw new RuntimeException("Rollback failed", ex);
			}
			throw new RuntimeException("Transaction failed", e);
		} finally {
			try {
				conn.setAutoCommit(originalAutoCommit);
			} catch (SQLException e) {
				System.err.println("Warning: Failed to restore auto-commit: " + e.getMessage());
			}
		}
	}
}