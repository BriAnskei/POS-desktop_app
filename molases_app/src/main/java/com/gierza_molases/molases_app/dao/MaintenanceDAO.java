package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

import com.gierza_molases.molases_app.model.response.DeletionPreview;

public class MaintenanceDAO {

	private final Connection connection;

	// SQLite DATETIME format
	private static final DateTimeFormatter SQLITE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public MaintenanceDAO(Connection connection) {
		this.connection = Objects.requireNonNull(connection);
	}

	private String getFormattedCutoff(int yearsOld) {
		LocalDateTime cutoff = LocalDateTime.now().minusYears(yearsOld);
		return cutoff.format(SQLITE_FORMATTER);
	}

	/**
	 * Preview how many deliveries would be deleted
	 */
	public int previewDeletion(int yearsOld) throws SQLException {

		String sql = """
				SELECT COUNT(DISTINCT d.id) as count
				FROM delivery d
				WHERE d.status = 'delivered' AND d.created_at < ?
				AND NOT EXISTS (
				    SELECT 1
				    FROM customer_delivery cd
				    JOIN customer_payments cp
				        ON cp.customer_delivery_id = cd.id
				    WHERE cd.delivery_id = d.id
				    AND cp.status = 'pending'
				)
				AND EXISTS (
				    SELECT 1
				    FROM customer_delivery cd
				    JOIN customer_payments cp
				        ON cp.customer_delivery_id = cd.id
				    WHERE cd.delivery_id = d.id
				)
				""";

		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, getFormattedCutoff(yearsOld));

			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() ? rs.getInt("count") : 0;
			}
		}
	}

	/**
	 * Delete old completed deliveries
	 */
	public int deleteOldCompletedDeliveries(int yearsOld, IntConsumer progressCallback) throws SQLException {

		String selectSql = """
				SELECT d.id
				FROM delivery d
					WHERE d.status = 'delivered' AND d.created_at < ?
				AND NOT EXISTS (
				    SELECT 1
				    FROM customer_delivery cd
				    JOIN customer_payments cp
				        ON cp.customer_delivery_id = cd.id
				    WHERE cd.delivery_id = d.id
				    AND cp.status = 'pending'
				)
				AND EXISTS (
				    SELECT 1
				    FROM customer_delivery cd
				    JOIN customer_payments cp
				        ON cp.customer_delivery_id = cd.id
				    WHERE cd.delivery_id = d.id
				)
				""";

		List<Integer> idsToDelete = new ArrayList<>();

		try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {

			selectStmt.setString(1, getFormattedCutoff(yearsOld));

			try (ResultSet rs = selectStmt.executeQuery()) {
				while (rs.next()) {
					idsToDelete.add(rs.getInt("id"));
				}
			}
		}

		if (idsToDelete.isEmpty()) {
			return 0;
		}

		boolean originalAutoCommit = connection.getAutoCommit();
		int deletedCount = 0;

		try {
			connection.setAutoCommit(false);

			String deleteSql = "DELETE FROM delivery WHERE id = ?";
			try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {

				for (int id : idsToDelete) {
					deleteStmt.setInt(1, id);
					deleteStmt.addBatch();
				}

				int[] results = deleteStmt.executeBatch();

				for (int result : results) {
					if (result > 0) {
						deletedCount++;
					}

					if (progressCallback != null) {
						int progress = (deletedCount * 100) / idsToDelete.size();
						progressCallback.accept(progress);
					}
				}
			}

			connection.commit();

		} catch (SQLException e) {
			connection.rollback();
			throw e;

		} finally {
			connection.setAutoCommit(originalAutoCommit);
		}

		return deletedCount;
	}

	/**
	 * Detailed preview
	 */
	public DeletionPreview getDetailedPreview(int yearsOld) throws SQLException {

		DeletionPreview preview = new DeletionPreview();

		String sql = """
				SELECT
				    COUNT(DISTINCT d.id) as delivery_count,
				    COUNT(DISTINCT cd.id) as customer_delivery_count,
				    COUNT(DISTINCT bd.id) as branch_delivery_count,
				    COUNT(DISTINCT pd.id) as product_delivery_count,
				    COUNT(DISTINCT cp.id) as payment_count,
				    COUNT(DISTINCT ph.id) as payment_history_count
				FROM delivery d
				LEFT JOIN customer_delivery cd
				    ON cd.delivery_id = d.id
				LEFT JOIN branch_delivery bd
				    ON bd.customer_delivery_id = cd.id
				LEFT JOIN product_delivery pd
				    ON pd.branch_delivery_id = bd.id
				LEFT JOIN customer_payments cp
				    ON cp.customer_delivery_id = cd.id
				LEFT JOIN payment_history ph
				    ON ph.customer_payment_id = cp.id
				WHERE d.status = 'delivered' AND d.created_at < ?
				AND NOT EXISTS (
				    SELECT 1
				    FROM customer_delivery cd2
				    JOIN customer_payments cp2
				        ON cp2.customer_delivery_id = cd2.id
				    WHERE cd2.delivery_id = d.id
				    AND cp2.status = 'pending'
				)
				AND EXISTS (
				    SELECT 1
				    FROM customer_delivery cd3
				    JOIN customer_payments cp3
				        ON cp3.customer_delivery_id = cd3.id
				    WHERE cd3.delivery_id = d.id
				)
				""";

		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

			pstmt.setString(1, getFormattedCutoff(yearsOld));

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					preview.deliveryCount = rs.getInt("delivery_count");
					preview.customerDeliveryCount = rs.getInt("customer_delivery_count");
					preview.branchDeliveryCount = rs.getInt("branch_delivery_count");
					preview.productDeliveryCount = rs.getInt("product_delivery_count");
					preview.paymentCount = rs.getInt("payment_count");
					preview.paymentHistoryCount = rs.getInt("payment_history_count");
				}
			}
		}

		return preview;
	}
}
