package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.gierza_molases.molases_app.model.ProductDelivery;

public class ProductDeliveryDao {
	private final Connection conn;

	// INSERT
	private static final String INSERT_SQL = """
				INSERT INTO product_delivery (
					branch_delivery_id,
					product_id,
					quantity
				) VALUES (?, ?, ?)
			""";

	private static final String FETCH_BY_BRANCH_DELIVERY_ID = """
						SELECT * FROM product_delivery WHERE branch_delivery_id = ?
			""";

	public ProductDeliveryDao(Connection conn) {
		this.conn = conn;
	}

	public void insertAll(int branchDeliveryId, List<ProductDelivery> list, Connection conn) throws Exception {

		System.out.println("Inserting products:");
		for (ProductDelivery pd : list) {
			System.out.println(pd);
		}

		try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

			for (ProductDelivery pd : list) {
				ps.setInt(1, branchDeliveryId);
				ps.setInt(2, pd.getProductId());
				ps.setInt(3, pd.getQuantity());
				ps.addBatch();
			}

			ps.executeBatch();
		}
	}

	public List<ProductDelivery> fetchProductDeliverisByBranchDeliveryId(int branchDeliveryId, Connection conn)
			throws Exception {
		List<ProductDelivery> productDelivery = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(FETCH_BY_BRANCH_DELIVERY_ID)) {
			ps.setInt(1, branchDeliveryId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					productDelivery.add(mapProductDelivery(rs));
				}
			}

		}
		return productDelivery;
	}

// Mapper function
	private ProductDelivery mapProductDelivery(ResultSet rs) throws SQLException {
		return new ProductDelivery(rs.getInt("id"), rs.getInt("branch_delivery_id"), rs.getInt("product_id"),
				rs.getInt("quantity"));
	}

}
