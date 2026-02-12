package com.gierza_molases.molases_app.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.gierza_molases.molases_app.model.Delivery;

public class DatabaseSeeder {

	private static final int TOTAL_CUSTOMERS = 5000;
	private static final Random RANDOM = new Random();

	// Add new constants
	private static final int TOTAL_PRODUCTS = 50;

	// Payment types
	private static final String[] PAYMENT_TYPES = { "Paid Cash", "Paid Cheque", "Partial", "Loan" };

	// Delivery statuses
	private static final String[] DELIVERY_STATUSES = { "scheduled", "delivered" };

	// Customer delivery statuses
	private static final String[] CUSTOMER_DELIVERY_STATUSES = { "delivered", "cancelled" };

	private DatabaseSeeder() {
	}

	public static void seed() {

		try {
			Connection conn = Database.init();
			conn.setAutoCommit(false); // 🔥 speed boost

			seedCustomers(conn);
			seedBranches(conn);

			seedProducts(conn);
			seedDeliveries(conn);

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

	// -----------------------------
	// SEED PRODUCTS
	// -----------------------------
	private static void seedProducts(Connection conn) throws SQLException {
		String sql = "INSERT INTO product (product_name, selling_price, capital) VALUES (?, ?, ?)";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			for (int i = 1; i <= TOTAL_PRODUCTS; i++) {
				ps.setString(1, "Product " + i);
				ps.setDouble(2, 100 + RANDOM.nextInt(900)); // selling price 100-1000
				ps.setDouble(3, 50 + RANDOM.nextInt(200)); // capital 50-250
				ps.addBatch();
			}
			ps.executeBatch();
			System.out.println("✔ Products seeded");
		}
	}

	// -----------------------------
	// SEED DELIVERIES, CUSTOMER_DELIVERY, BRANCH_DELIVERY, PRODUCT_DELIVERY,
	// PAYMENTS
	// -----------------------------
	private static void seedDeliveries(Connection conn) throws SQLException {

		// 1️⃣ Get all customers who have at least one branch
		List<Integer> customerIdsWithBranches = new ArrayList<>();
		try (Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT DISTINCT customer_id FROM branches")) {
			while (rs.next()) {
				customerIdsWithBranches.add(rs.getInt("customer_id"));
			}
		}

		if (customerIdsWithBranches.isEmpty()) {
			System.out.println("No customers with branches. Cannot seed deliveries.");
			return;
		}

		// 2️⃣ Get all product IDs
		List<Integer> productIds = new ArrayList<>();
		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id FROM product")) {
			while (rs.next()) {
				productIds.add(rs.getInt("id"));
			}
		}

		if (productIds.isEmpty()) {
			System.out.println("No products found. Cannot seed deliveries.");
			return;
		}

		String insertDelivery = """
				    INSERT INTO delivery (schedule_date, name, expenses, status, total_customers, total_branches,
				                          total_gross, total_capital, gross_profit, total_expenses, net_profit)
				    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";

		String insertCustomerDelivery = "INSERT INTO customer_delivery (customer_id, delivery_id, status) VALUES (?, ?, ?)";
		String insertBranchDelivery = "INSERT INTO branch_delivery (customer_delivery_id, branch_id, status) VALUES (?, ?, ?)";
		String insertProductDelivery = "INSERT INTO product_delivery (branch_delivery_id, product_id, quantity) VALUES (?, ?, ?)";
		String insertCustomerPayments = "INSERT INTO customer_payments (customer_id, customer_delivery_id, payment_type, status, total, total_payment, promise_to_pay) VALUES (?, ?, ?, ?, ?, ?, ?)";
		String insertPaymentHistory = "INSERT INTO payment_history (customer_payment_id, amount, created_at) VALUES (?, ?, ?)";

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDate startOfYear = LocalDate.now().withDayOfYear(1);

		try (PreparedStatement psDelivery = conn.prepareStatement(insertDelivery, Statement.RETURN_GENERATED_KEYS);
				PreparedStatement psCustDel = conn.prepareStatement(insertCustomerDelivery,
						Statement.RETURN_GENERATED_KEYS);
				PreparedStatement psBranchDel = conn.prepareStatement(insertBranchDelivery,
						Statement.RETURN_GENERATED_KEYS);
				PreparedStatement psProdDel = conn.prepareStatement(insertProductDelivery);
				PreparedStatement psCustPay = conn.prepareStatement(insertCustomerPayments,
						Statement.RETURN_GENERATED_KEYS);
				PreparedStatement psPayHist = conn.prepareStatement(insertPaymentHistory)) {

			for (int monthOffset = 0; monthOffset < 12; monthOffset++) {
				LocalDate deliveryDate = startOfYear.plusMonths(monthOffset);

				// Pick random customer with branches
				int customerId = customerIdsWithBranches.get(RANDOM.nextInt(customerIdsWithBranches.size()));

				// Get a random branch for this customer
				List<Integer> customerBranches = new ArrayList<>();
				try (PreparedStatement psBranchList = conn
						.prepareStatement("SELECT id FROM branches WHERE customer_id = ?")) {
					psBranchList.setInt(1, customerId);
					try (ResultSet rsBranch = psBranchList.executeQuery()) {
						while (rsBranch.next()) {
							customerBranches.add(rsBranch.getInt("id"));
						}
					}
				}
				int branchId = customerBranches.get(RANDOM.nextInt(customerBranches.size()));

				// Prepare delivery expenses as Map -> JSON
				Map<String, Double> expensesMap = new HashMap<>();
				expensesMap.put("transport", 50.0);
				expensesMap.put("fuel", 20.0);
				Delivery tempDelivery = new Delivery(deliveryDate.atStartOfDay(), "Delivery " + (monthOffset + 1),
						expensesMap, 1, 1, 500.0, 300.0, 200.0, 150.0);

				String expensesJson = tempDelivery.getExpensesAsJson();

				// 🔥 Randomly pick delivery status
				String deliveryStatus = DELIVERY_STATUSES[RANDOM.nextInt(DELIVERY_STATUSES.length)];

				// 3️⃣ Insert delivery
				psDelivery.setString(1, deliveryDate.atStartOfDay().format(dtf));
				psDelivery.setString(2, tempDelivery.getName());
				psDelivery.setString(3, expensesJson); // JSON expenses
				psDelivery.setString(4, deliveryStatus); // 🔥 random status
				psDelivery.setInt(5, tempDelivery.getTotalCustomers());
				psDelivery.setInt(6, tempDelivery.getTotalBranches());
				psDelivery.setDouble(7, tempDelivery.getTotalGross());
				psDelivery.setDouble(8, tempDelivery.getTotalCapital());
				psDelivery.setDouble(9, tempDelivery.getGrossProfit());
				psDelivery.setDouble(10, tempDelivery.getTotalExpenses());
				psDelivery.setDouble(11, tempDelivery.getNetProfit());
				psDelivery.executeUpdate();

				ResultSet rsDelivery = psDelivery.getGeneratedKeys();
				rsDelivery.next();
				int deliveryId = rsDelivery.getInt(1);

				// 🔥 Randomly pick customer_delivery status
				String customerDeliveryStatus = CUSTOMER_DELIVERY_STATUSES[RANDOM.nextInt(CUSTOMER_DELIVERY_STATUSES.length)];

				// 4️⃣ Insert customer_delivery
				psCustDel.setInt(1, customerId);
				psCustDel.setInt(2, deliveryId);
				psCustDel.setString(3, customerDeliveryStatus); // 🔥 random status
				psCustDel.executeUpdate();

				ResultSet rsCustDel = psCustDel.getGeneratedKeys();
				rsCustDel.next();
				int customerDeliveryId = rsCustDel.getInt(1);

				// 5️⃣ Insert branch_delivery
				psBranchDel.setInt(1, customerDeliveryId);
				psBranchDel.setInt(2, branchId);
				psBranchDel.setString(3, "delivered"); // 🔥 always delivered for seeding
				psBranchDel.executeUpdate();

				ResultSet rsBranchDel = psBranchDel.getGeneratedKeys();
				rsBranchDel.next();
				int branchDeliveryId = rsBranchDel.getInt(1);

				// 6️⃣ Insert product_delivery (1–3 random products)
				int productCount = RANDOM.nextInt(3) + 1;
				for (int i = 0; i < productCount; i++) {
					int productId = productIds.get(RANDOM.nextInt(productIds.size()));
					int qty = RANDOM.nextInt(10) + 1;
					psProdDel.setInt(1, branchDeliveryId);
					psProdDel.setInt(2, productId);
					psProdDel.setInt(3, qty);
					psProdDel.addBatch();
				}
				psProdDel.executeBatch();

				// 🔥 7️⃣ Insert customer_payment with random payment type
				String paymentType = PAYMENT_TYPES[RANDOM.nextInt(PAYMENT_TYPES.length)];
				double total = 500.0;
				double totalPayment;
				String paymentStatus;
				String promiseToPay = null;

				switch (paymentType) {
					case "Paid Cash":
					case "Paid Cheque":
						// Full payment
						totalPayment = total;
						paymentStatus = "complete";
						break;

					case "Partial":
						// Random 40-80% payment
						totalPayment = total * (0.4 + RANDOM.nextDouble() * 0.4);
						paymentStatus = "pending";
						break;

					case "Loan":
						// No payment yet
						totalPayment = 0;
						paymentStatus = "pending";
						// Promise to pay: 15-60 days from delivery (with time component)
						int daysOffset = 15 + RANDOM.nextInt(46);
						promiseToPay = deliveryDate.plusDays(daysOffset).atStartOfDay().format(dtf);
						break;

					default:
						throw new IllegalStateException("Unknown payment type: " + paymentType);
				}

				psCustPay.setInt(1, customerId);
				psCustPay.setInt(2, customerDeliveryId);
				psCustPay.setString(3, paymentType);
				psCustPay.setString(4, paymentStatus);
				psCustPay.setDouble(5, total);
				psCustPay.setDouble(6, totalPayment);
				psCustPay.setString(7, promiseToPay); // null for non-loan types
				psCustPay.executeUpdate();

				ResultSet rsCustPay = psCustPay.getGeneratedKeys();
				rsCustPay.next();
				int customerPaymentId = rsCustPay.getInt(1);

				// 🔥 8️⃣ Insert payment_history ONLY for non-Loan types
				if (!paymentType.equals("Loan")) {
					psPayHist.setInt(1, customerPaymentId);
					psPayHist.setDouble(2, totalPayment);
					// 🔥 Set created_at to match delivery month (so Monthly Income chart spreads across 12 months)
					psPayHist.setString(3, deliveryDate.atStartOfDay().format(dtf));
					psPayHist.executeUpdate();
				}
			}

			System.out.println("✔ Deliveries seeded with varied payment types and statuses");
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