package com.gierza_molases.molases_app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.util.DaoUtils;

public class ProductDao {

	private Connection conn;

	public ProductDao(Connection conn) {
		this.conn = conn;
	}

	// CREATE
	private final String INSERT_SQL = """
						 INSERT INTO product (product_name, selling_price, capital)
			VALUES (?, ?, ?);
						""";

	// READ
	private final String SELECT_ALL_SQL = """
			     SELECT
			     p.id,
			     p.product_name,
			     p.selling_price,
			     p.capital,
			     p.created_at,
			     COUNT(pa.customer_id) AS associated_count
			 FROM product p
			 LEFT JOIN product_association pa
			     ON pa.product_id = p.id
			 WHERE (? IS NULL OR p.product_name LIKE ?)
			 GROUP BY p.id
			 ORDER BY p.created_at %s
			""";

	private final String FIND_BY_ID_SQL = """
			    SELECT
			        p.id,
			        p.product_name,
			        p.selling_price,
			        p.capital,
			        p.created_at,
			        COUNT(pa.customer_id) AS associated_count
			    FROM product p
			    LEFT JOIN product_association pa
			        ON pa.product_id = p.id
			    WHERE p.id = ?
			    GROUP BY p.id;
			""";

// UPDATE
	private final String UPDATE_PRODUCT_SQL = """
			UPDATE product
			SET
				 product_name = ?,
				 selling_price = ?,
				 capital = ?
			WHERE id = ?
						""";

// DELETE
	private final String DELETE_BY_ID_SQL = """
				     DELETE FROM product WHERE id = ?
			""";

	public int insert(Product product, Connection conn) {
		try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
			ps.setString(1, product.getName());
			ps.setDouble(2, product.getSellingPrice());
			ps.setDouble(3, product.getCapital());

			ps.executeUpdate();

			return DaoUtils.getGeneratedId(ps);
		} catch (Exception e) {
			throw new RuntimeException("Failed to insert customer", e);
		}

	}

	public Product findById(int productId) {
		try (PreparedStatement ps = conn.prepareStatement(FIND_BY_ID_SQL)) {

			ps.setInt(1, productId);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapRowToProduct(rs) : null;
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to find product by id: " + productId, e);
		}
	}

	public List<Product> findAll(String search, boolean newestFirst) {
		String order = newestFirst ? "DESC" : "ASC";
		String sql = String.format(SELECT_ALL_SQL, order);

		List<Product> products = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			if (search == null || search.isBlank()) {
				ps.setNull(1, java.sql.Types.VARCHAR);
				ps.setNull(2, java.sql.Types.VARCHAR);
			} else {
				ps.setString(1, search);
				ps.setString(2, "%" + search + "%");
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					products.add(mapRowToProduct(rs));
				}
			}

			return products;

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch products", e);
		}
	}

	public void update(Product product) {

		if (product.getId() <= 0) {
			throw new IllegalArgumentException("Customer ID is required");
		}

		try (PreparedStatement ps = conn.prepareStatement(UPDATE_PRODUCT_SQL)) {

			ps.setString(1, product.getName());
			ps.setDouble(2, product.getSellingPrice());
			ps.setDouble(3, product.getCapital());

			ps.setInt(4, product.getId());

			ps.executeUpdate();

		} catch (Exception e) {
			throw new RuntimeException("Failed to update individual customer", e);
		}

	}

	public void deleteById(int id) {
		if (id <= 0) {
			throw new IllegalArgumentException("Invalid customer ID");
		}

		try (PreparedStatement ps = conn.prepareStatement(DELETE_BY_ID_SQL)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (Exception e) {
			throw new RuntimeException("Failed to delete customer", e);
		}
	}

	// helper function
	private Product mapRowToProduct(ResultSet rs) throws SQLException {

		int id = rs.getInt("id");
		String name = rs.getString("product_name");
		double sellingPrice = rs.getDouble("selling_price");
		double capital = rs.getDouble("capital");

		LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

		int associatedCount = rs.getInt("associated_count");
		boolean isAssociated = associatedCount > 0;

		return new Product(id, name, sellingPrice, capital, createdAt, isAssociated, associatedCount);
	}

}
