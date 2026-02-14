package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import com.gierza_molases.molases_app.model.response.DeletionPreview;

/**
 * Data Access Object for Maintenance operations Handles deletion of old
 * completed delivery records
 */
public class MaintenanceDAO {

	private Connection connection;

	public MaintenanceDAO(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Preview how many deliveries would be deleted
	 * 
	 * @param yearsOld Number of years to look back
	 * @return Count of deliveries that would be deleted
	 * @throws SQLException
	 */
	public int previewDeletion(int yearsOld) throws SQLException {
		LocalDate cutoffDate = LocalDate.now().minusYears(yearsOld);

		String sql = """
				SELECT COUNT(DISTINCT d.id) as count
				FROM delivery d
				WHERE d.created_at < ?
				AND NOT EXISTS (
				    -- Exclude deliveries with any pending payments
				    SELECT 1
				    FROM customer_delivery cd
				    JOIN customer_payments cp ON cp.customer_delivery_id = cd.id
				    WHERE cd.delivery_id = d.id
				    AND cp.status = 'pending'
				)
				AND EXISTS (
				    -- Only include deliveries that have at least one payment
				    SELECT 1
				    FROM customer_delivery cd
				    JOIN customer_payments cp ON cp.customer_delivery_id = cd.id
				    WHERE cd.delivery_id = d.id
				)
				""";

		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, cutoffDate.toString());

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("count");
				}
			}
		}

		return 0;
	}

	/**
	 * Delete old completed deliveries and all related records
	 * 
	 * @param yearsOld Number of years to look back
	 * @return Number of deliveries deleted
	 * @throws SQLException
	 */
	public int deleteOldCompletedDeliveries(int yearsOld, IntConsumer progressCallback) throws SQLException {

		// Use LocalDateTime instead of LocalDate
		LocalDateTime cutoffDateTime = LocalDateTime.now().minusYears(yearsOld);

		int deletedCount = 0;
		boolean autoCommit = connection.getAutoCommit();

		try {
			connection.setAutoCommit(false);

			String selectSql = """
					    SELECT d.id
					    FROM delivery d
					    WHERE d.created_at < ?
					    AND NOT EXISTS (
					        SELECT 1
					        FROM customer_delivery cd
					        JOIN customer_payments cp ON cp.customer_delivery_id = cd.id
					        WHERE cd.delivery_id = d.id
					        AND cp.status = 'pending'
					    )
					    AND EXISTS (
					        SELECT 1
					        FROM customer_delivery cd
					        JOIN customer_payments cp ON cp.customer_delivery_id = cd.id
					        WHERE cd.delivery_id = d.id
					    )
					""";

			List<Integer> idsToDelete = new ArrayList<>();

			try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {

				// pass full datetime string
				selectStmt.setString(1, cutoffDateTime.toString());

				try (ResultSet rs = selectStmt.executeQuery()) {
					while (rs.next()) {
						idsToDelete.add(rs.getInt("id"));
					}
				}
			}

			int total = idsToDelete.size();

			if (total > 0) {
				String deleteSql = "DELETE FROM delivery WHERE id = ?";

				try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
					for (int deliveryId : idsToDelete) {
						deleteStmt.setInt(1, deliveryId);
						deleteStmt.executeUpdate();
						deletedCount++;

						if (progressCallback != null) {
							int progress = (deletedCount * 100) / total;
							progressCallback.accept(progress);
						}
					}
				}
			}

			connection.commit();

		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException rollbackEx) {
				rollbackEx.printStackTrace();
			}
			throw e;

		} finally {
			connection.setAutoCommit(autoCommit);
		}

		return deletedCount;
	}

	/**
	 * Get detailed information about what will be deleted
	 * 
	 * @param yearsOld Number of years to look back
	 * @return DeletionPreview object with detailed counts
	 * @throws SQLException
	 */
	public DeletionPreview getDetailedPreview(int yearsOld) throws SQLException {
		LocalDate cutoffDate = LocalDate.now().minusYears(yearsOld);
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
				LEFT JOIN customer_delivery cd ON cd.delivery_id = d.id
				LEFT JOIN branch_delivery bd ON bd.customer_delivery_id = cd.id
				LEFT JOIN product_delivery pd ON pd.branch_delivery_id = bd.id
				LEFT JOIN customer_payments cp ON cp.customer_delivery_id = cd.id
				LEFT JOIN payment_history ph ON ph.customer_payment_id = cp.id
				WHERE d.created_at < ?
				AND NOT EXISTS (
				    SELECT 1
				    FROM customer_delivery cd2
				    JOIN customer_payments cp2 ON cp2.customer_delivery_id = cd2.id
				    WHERE cd2.delivery_id = d.id
				    AND cp2.status = 'pending'
				)
				AND EXISTS (
				    SELECT 1
				    FROM customer_delivery cd3
				    JOIN customer_payments cp3 ON cp3.customer_delivery_id = cd3.id
				    WHERE cd3.delivery_id = d.id
				)
				""";

		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, cutoffDate.toString());

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