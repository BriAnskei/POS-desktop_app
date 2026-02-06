package com.gierza_molases.molases_app.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class DatabaseSeeder {

	private static final int TOTAL_CUSTOMERS = 5000;
	private static final Random RANDOM = new Random();

	private DatabaseSeeder() {
	}

	public static void seed() {

		try {
			Connection conn = Database.init();
			conn.setAutoCommit(false); // 🔥 speed boost

			seedCustomers(conn);
			seedBranches(conn);

			conn.commit();
			System.out.println("✅ Database seeding finished");

		} catch (Exception e) {
			throw new RuntimeException("Failed to seed database", e);
		}
	}

	// --------------------------------------------------
	// CUSTOMERS
	// --------------------------------------------------
	private static void seedCustomers(Connection conn) throws SQLException {

		String sql = """
				    INSERT INTO customer (
				        first_name,
				        mid_name,
				        last_name,
				        company_name,
				        display_name,
				        type,
				        contact_number,
				        address
				    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			for (int i = 1; i <= TOTAL_CUSTOMERS; i++) {

				String firstName = "First" + i;
				String lastName = "Last" + i;

				ps.setString(1, firstName);
				ps.setString(2, null);
				ps.setString(3, lastName);
				ps.setString(4, null);
				ps.setString(5, firstName + " " + lastName);
				ps.setString(6, "individual");
				ps.setString(7, randomPhone());
				ps.setString(8, "Address #" + i);

				ps.addBatch();

				if (i % 500 == 0) {
					ps.executeBatch();
				}
			}

			ps.executeBatch();
			System.out.println("✔ Customers seeded");
		}
	}

	// --------------------------------------------------
	// BRANCHES (1–7 PER CUSTOMER)
	// --------------------------------------------------
	private static void seedBranches(Connection conn) throws SQLException {

		String sql = """
				    INSERT INTO branches (
				        customer_id,
				        address,
				        note
				    ) VALUES (?, ?, ?)
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT id FROM customer")) {

			while (rs.next()) {
				int customerId = rs.getInt("id");
				int branchCount = RANDOM.nextInt(7) + 1; // 1–7

				for (int i = 1; i <= branchCount; i++) {
					ps.setInt(1, customerId);
					ps.setString(2, "Branch " + i + " Address for customer " + customerId);
					ps.setString(3, "Auto-generated branch");
					ps.addBatch();
				}
			}

			ps.executeBatch();
			System.out.println("✔ Branches seeded");
		}
	}

	// --------------------------------------------------
	// UTIL
	// --------------------------------------------------
	private static String randomPhone() {
		return "09" + (100_000_000 + RANDOM.nextInt(900_000_000));
	}

	public static void main(String[] args) {
		DatabaseInitializer.init();
		DatabaseSeeder.seed();
	}

}
