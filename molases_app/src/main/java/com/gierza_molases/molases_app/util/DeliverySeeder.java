package com.gierza_molases.molases_app.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Seeder for generating 1 million delivery records with all related data.
 * 
 * This version integrates with the existing Database class.
 * 
 * Usage: - Run directly: DeliverySeeder.main(null) - Or from your application:
 * new DeliverySeeder().seed()
 */
public class DeliverySeeder {

	// ==================== CONFIGURATION ====================
	private static final int TARGET_DELIVERIES = 1_000_000;
	private static final int BATCH_SIZE = 5_000;
	private static final int DATE_RANGE_YEARS = 2;
	private static final double DELIVERED_PERCENTAGE = 0.85; // 85% delivered, 15% scheduled

	// Database metadata
	private static final int TOTAL_CUSTOMERS = 5000;
	private static final int TOTAL_PRODUCTS = 50;
	private static final int TOTAL_BRANCHES = 19999;

	// Expense categories
	private static final String[] EXPENSE_CATEGORIES = { "Gas", "Transportation", "Toll fees", "Meals", "Helper wages",
			"Vehicle maintenance", "Packaging", "Miscellaneous" };

	// Payment types
	private static final String[] PAYMENT_TYPES = { "Paid Cash", "Paid Cheque", "Partial", "Loan" };

	private final Connection conn;
	private final Random random;
	private final LocalDateTime startDate;
	private final LocalDateTime endDate;
	private final boolean manageConnection;

	// Statistics
	private long totalCustomerDeliveries = 0;
	private long totalBranchDeliveries = 0;
	private long totalProductDeliveries = 0;
	private long totalPayments = 0;
	private long totalPaymentHistory = 0;

	/**
	 * Constructor that uses the existing Database connection
	 */
	public DeliverySeeder() {
		this(Database.init(), false);
	}

	/**
	 * Constructor with custom connection (for testing)
	 */
	public DeliverySeeder(Connection conn, boolean manageConnection) {
		this.conn = conn;
		this.manageConnection = manageConnection;
		this.random = new Random();

		// Date range: last 2 years
		this.endDate = LocalDateTime.now();
		this.startDate = endDate.minusYears(DATE_RANGE_YEARS);

		System.out.println("=".repeat(60));
		System.out.println("DELIVERY SEEDER");
		System.out.println("=".repeat(60));
		System.out.println("Target deliveries: " + String.format("%,d", TARGET_DELIVERIES));
		System.out.println("Batch size: " + String.format("%,d", BATCH_SIZE));
		System.out.println("Date range: " + startDate.toLocalDate() + " to " + endDate.toLocalDate());
		System.out.println("Delivered percentage: " + (DELIVERED_PERCENTAGE * 100) + "%");
		System.out.println("=".repeat(60));
	}

	public void seed() throws SQLException {
		long startTime = System.currentTimeMillis();

		boolean originalAutoCommit = conn.getAutoCommit();

		try {
			conn.setAutoCommit(false);

			// Get the starting ID (after existing deliveries)
			int startingDeliveryId = getMaxDeliveryId() + 1;
			System.out.println("\nStarting from delivery ID: " + startingDeliveryId);

			// Generate deliveries in batches
			int deliveriesGenerated = 0;
			int batchNumber = 1;

			while (deliveriesGenerated < TARGET_DELIVERIES) {
				int batchSize = Math.min(BATCH_SIZE, TARGET_DELIVERIES - deliveriesGenerated);

				System.out.println("\n--- Batch " + batchNumber + " ---");
				System.out.println("Generating " + String.format("%,d", batchSize) + " deliveries...");

				generateDeliveryBatch(startingDeliveryId + deliveriesGenerated, batchSize);

				deliveriesGenerated += batchSize;
				batchNumber++;

				// Progress update
				double progress = (deliveriesGenerated * 100.0) / TARGET_DELIVERIES;
				System.out.printf("Progress: %,d / %,d (%.2f%%)\n", deliveriesGenerated, TARGET_DELIVERIES, progress);

				// Memory hint
				if (batchNumber % 10 == 0) {
					System.gc();
				}
			}

			conn.commit();

			// Final statistics
			long endTime = System.currentTimeMillis();
			long durationSeconds = (endTime - startTime) / 1000;

			System.out.println("\n" + "=".repeat(60));
			System.out.println("SEEDING COMPLETED SUCCESSFULLY");
			System.out.println("=".repeat(60));
			System.out.println("Total time: " + formatDuration(durationSeconds));
			System.out.println("\nRecords created:");
			System.out.println("  Deliveries:           " + String.format("%,d", deliveriesGenerated));
			System.out.println("  Customer deliveries:  " + String.format("%,d", totalCustomerDeliveries));
			System.out.println("  Branch deliveries:    " + String.format("%,d", totalBranchDeliveries));
			System.out.println("  Product deliveries:   " + String.format("%,d", totalProductDeliveries));
			System.out.println("  Customer payments:    " + String.format("%,d", totalPayments));
			System.out.println("  Payment history:      " + String.format("%,d", totalPaymentHistory));
			System.out.println("=".repeat(60));

		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}

	private void generateDeliveryBatch(int startId, int batchSize) throws SQLException {
		// Track products for financial calculations
		Map<Integer, ProductInfo> productCache = loadProductInfo();

		// Process each delivery individually to avoid foreign key issues
		for (int i = 0; i < batchSize; i++) {
			generateSingleDelivery(startId + i, productCache);
		}

		// Commit this batch
		conn.commit();
	}

	private void generateSingleDelivery(int deliveryId, Map<Integer, ProductInfo> productCache) throws SQLException {
		// Generate delivery data
		LocalDateTime createdAt = randomDateTime(startDate, endDate);
		LocalDateTime scheduleDate = createdAt.plusDays(random.nextInt(30));
		String status = random.nextDouble() < DELIVERED_PERCENTAGE ? "delivered" : "scheduled";
		String name = "Delivery-" + deliveryId;

		// Generate expenses
		Map<String, Double> expenses = generateExpenses();
		String expensesJson = mapToJson(expenses);
		double totalExpenses = expenses.values().stream().mapToDouble(Double::doubleValue).sum();

		// Generate customers for this delivery
		int numCustomers = randomWeighted(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
				new double[] { 0.05, 0.15, 0.20, 0.20, 0.15, 0.10, 0.08, 0.04, 0.02, 0.01 });

		Set<Integer> selectedCustomers = randomCustomerIds(numCustomers);

		// Financial accumulators - calculate first
		double totalGross = 0.0;
		double totalCapital = 0.0;
		int totalBranches = 0;

		// Pre-calculate all the data we need
		List<CustomerDeliveryPlan> customerPlans = new ArrayList<>();

		for (int customerId : selectedCustomers) {
			// Generate branches for this customer
			int numBranches = randomWeighted(new int[] { 1, 2, 3 }, new double[] { 0.70, 0.25, 0.05 });
			Set<Integer> customerBranchIds = getCustomerBranches(customerId, numBranches);
			totalBranches += customerBranchIds.size();

			double customerTotal = 0.0;
			List<BranchDeliveryPlan> branchPlans = new ArrayList<>();

			for (int branchId : customerBranchIds) {
				// Generate products for this branch
				int numProducts = randomWeighted(new int[] { 2, 3, 4, 5, 6, 7, 8 },
						new double[] { 0.15, 0.25, 0.25, 0.20, 0.10, 0.03, 0.02 });
				Set<Integer> productIds = randomProductIds(numProducts);

				List<ProductDeliveryPlan> productPlans = new ArrayList<>();

				for (int productId : productIds) {
					int quantity = 1 + random.nextInt(50); // 1-50 units

					ProductInfo product = productCache.get(productId);
					double gross = product.sellingPrice * quantity;
					double capital = product.capital * quantity;

					totalGross += gross;
					totalCapital += capital;
					customerTotal += gross;

					productPlans.add(new ProductDeliveryPlan(productId, quantity));
				}

				branchPlans.add(new BranchDeliveryPlan(branchId, productPlans));
			}

			customerPlans.add(new CustomerDeliveryPlan(customerId, customerTotal, branchPlans));
		}

		// Calculate final financials
		double grossProfit = totalGross - totalCapital;
		double netProfit = grossProfit - totalExpenses;

		// STEP 1: Insert delivery record FIRST (parent record must exist first)
		PreparedStatement deliveryStmt = conn
				.prepareStatement("INSERT INTO delivery (id, schedule_date, name, expenses, status, "
						+ "total_customers, total_branches, total_gross, total_capital, "
						+ "gross_profit, total_expenses, net_profit, created_at) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		deliveryStmt.setInt(1, deliveryId);
		deliveryStmt.setLong(2, scheduleDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		deliveryStmt.setString(3, name);
		deliveryStmt.setString(4, expensesJson);
		deliveryStmt.setString(5, status);
		deliveryStmt.setDouble(6, numCustomers);
		deliveryStmt.setDouble(7, totalBranches);
		deliveryStmt.setDouble(8, totalGross);
		deliveryStmt.setDouble(9, totalCapital);
		deliveryStmt.setDouble(10, grossProfit);
		deliveryStmt.setDouble(11, totalExpenses);
		deliveryStmt.setDouble(12, netProfit);
		deliveryStmt.setString(13, createdAt.toString());
		deliveryStmt.executeUpdate();
		deliveryStmt.close();

		// STEP 2: Now insert customer_delivery records (now that delivery exists)
		List<CustomerDeliveryData> customerDeliveryDataList = new ArrayList<>();

		for (CustomerDeliveryPlan customerPlan : customerPlans) {
			PreparedStatement customerDeliveryStmt = conn.prepareStatement(
					"INSERT INTO customer_delivery (customer_id, delivery_id, status) VALUES (?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);

			customerDeliveryStmt.setInt(1, customerPlan.customerId);
			customerDeliveryStmt.setInt(2, deliveryId);
			customerDeliveryStmt.setString(3, status);
			customerDeliveryStmt.executeUpdate();
			totalCustomerDeliveries++;

			// Get customer_delivery_id
			ResultSet cdKeys = customerDeliveryStmt.getGeneratedKeys();
			int customerDeliveryId = -1;
			if (cdKeys.next()) {
				customerDeliveryId = cdKeys.getInt(1);
			}
			cdKeys.close();
			customerDeliveryStmt.close();

			// STEP 3: Insert branch_delivery records
			for (BranchDeliveryPlan branchPlan : customerPlan.branchPlans) {
				PreparedStatement branchDeliveryStmt = conn.prepareStatement(
						"INSERT INTO branch_delivery (customer_delivery_id, branch_id, status) VALUES (?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);

				branchDeliveryStmt.setInt(1, customerDeliveryId);
				branchDeliveryStmt.setInt(2, branchPlan.branchId);
				branchDeliveryStmt.setString(3, status);
				branchDeliveryStmt.executeUpdate();
				totalBranchDeliveries++;

				// Get branch_delivery_id
				ResultSet bdKeys = branchDeliveryStmt.getGeneratedKeys();
				int branchDeliveryId = -1;
				if (bdKeys.next()) {
					branchDeliveryId = bdKeys.getInt(1);
				}
				bdKeys.close();
				branchDeliveryStmt.close();

				// STEP 4: Insert product_delivery records
				PreparedStatement productDeliveryStmt = conn.prepareStatement(
						"INSERT INTO product_delivery (branch_delivery_id, product_id, quantity) VALUES (?, ?, ?)");

				for (ProductDeliveryPlan productPlan : branchPlan.productPlans) {
					productDeliveryStmt.setInt(1, branchDeliveryId);
					productDeliveryStmt.setInt(2, productPlan.productId);
					productDeliveryStmt.setInt(3, productPlan.quantity);
					productDeliveryStmt.addBatch();
					totalProductDeliveries++;
				}

				productDeliveryStmt.executeBatch();
				productDeliveryStmt.close();
			}

			// Store customer delivery data for payments
			customerDeliveryDataList.add(new CustomerDeliveryData(customerDeliveryId, customerPlan.customerId,
					customerPlan.customerTotal, createdAt, status));
		}

		// STEP 5: Generate payments for each customer_delivery
		for (CustomerDeliveryData cd : customerDeliveryDataList) {
			generatePayments(cd);
		}
	}

	// Helper classes for planning
	private static class CustomerDeliveryPlan {
		final int customerId;
		final double customerTotal;
		final List<BranchDeliveryPlan> branchPlans;

		CustomerDeliveryPlan(int customerId, double customerTotal, List<BranchDeliveryPlan> branchPlans) {
			this.customerId = customerId;
			this.customerTotal = customerTotal;
			this.branchPlans = branchPlans;
		}
	}

	private static class BranchDeliveryPlan {
		final int branchId;
		final List<ProductDeliveryPlan> productPlans;

		BranchDeliveryPlan(int branchId, List<ProductDeliveryPlan> productPlans) {
			this.branchId = branchId;
			this.productPlans = productPlans;
		}
	}

	private static class ProductDeliveryPlan {
		final int productId;
		final int quantity;

		ProductDeliveryPlan(int productId, int quantity) {
			this.productId = productId;
			this.quantity = quantity;
		}
	}

	private void generatePayments(CustomerDeliveryData cd) throws SQLException {
		// Only delivered status gets payments
		if (!"delivered".equals(cd.status)) {
			return;
		}

		// Generate 1-4 payments per customer delivery
		int numPayments = 1 + random.nextInt(4);
		double remainingTotal = cd.total;

		for (int i = 0; i < numPayments && remainingTotal > 0.01; i++) {
			String paymentType = PAYMENT_TYPES[random.nextInt(PAYMENT_TYPES.length)];

			// Split total across payments
			double paymentTotal = (i == numPayments - 1) ? remainingTotal
					: remainingTotal * (0.2 + random.nextDouble() * 0.6);
			remainingTotal -= paymentTotal;

			// Determine payment status and total_payment
			String paymentStatus;
			double totalPayment;
			java.util.Date promiseToPay = null;

			switch (paymentType) {
			case "Paid Cash":
			case "Paid Cheque":
				paymentStatus = "complete";
				totalPayment = paymentTotal;
				break;

			case "Partial":
				paymentStatus = random.nextDouble() < 0.6 ? "complete" : "pending";
				totalPayment = paymentTotal * (0.3 + random.nextDouble() * 0.7);
				break;

			case "Loan":
				paymentStatus = "pending";
				totalPayment = 0.0;
				// Promise to pay in 7-60 days
				promiseToPay = java.util.Date
						.from(cd.createdAt.plusDays(7 + random.nextInt(54)).atZone(ZoneId.systemDefault()).toInstant());
				break;

			default:
				paymentStatus = "pending";
				totalPayment = 0.0;
			}

			// Insert payment
			PreparedStatement paymentStmt = conn
					.prepareStatement("INSERT INTO customer_payments (customer_id, customer_delivery_id, payment_type, "
							+ "status, notes, total, total_payment, promise_to_pay, created_at) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

			paymentStmt.setInt(1, cd.customerId);
			paymentStmt.setInt(2, cd.customerDeliveryId);
			paymentStmt.setString(3, paymentType);
			paymentStmt.setString(4, paymentStatus);
			paymentStmt.setString(5, null); // notes
			paymentStmt.setDouble(6, paymentTotal);
			paymentStmt.setDouble(7, totalPayment);

			if (promiseToPay != null) {
				paymentStmt.setDate(8, new java.sql.Date(promiseToPay.getTime()));
			} else {
				paymentStmt.setNull(8, Types.DATE);
			}

			paymentStmt.setString(9, cd.createdAt.toString());
			paymentStmt.executeUpdate();
			totalPayments++;

			// Get payment ID for history
			ResultSet pmtKeys = paymentStmt.getGeneratedKeys();
			int paymentId = -1;
			if (pmtKeys.next()) {
				paymentId = pmtKeys.getInt(1);
			}
			pmtKeys.close();
			paymentStmt.close();

			// Generate payment history for Partial payments
			if ("Partial".equals(paymentType) && totalPayment > 0) {
				int numHistoryRecords = 1 + random.nextInt(3); // 1-3 records
				double remainingPayment = totalPayment;

				PreparedStatement paymentHistoryStmt = conn.prepareStatement(
						"INSERT INTO payment_history (customer_payment_id, amount, created_at) VALUES (?, ?, ?)");

				for (int h = 0; h < numHistoryRecords && remainingPayment > 0.01; h++) {
					double historyAmount = (h == numHistoryRecords - 1) ? remainingPayment
							: remainingPayment * (0.2 + random.nextDouble() * 0.5);
					remainingPayment -= historyAmount;

					LocalDateTime historyDate = cd.createdAt.plusDays(h * 7 + random.nextInt(7));

					paymentHistoryStmt.setInt(1, paymentId);
					paymentHistoryStmt.setDouble(2, historyAmount);
					paymentHistoryStmt.setString(3, historyDate.toString());
					paymentHistoryStmt.addBatch();
					totalPaymentHistory++;
				}

				paymentHistoryStmt.executeBatch();
				paymentHistoryStmt.close();
			}
		}
	}

	private Map<String, Double> generateExpenses() {
		Map<String, Double> expenses = new HashMap<>();
		int numCategories = 2 + random.nextInt(5); // 2-6 categories

		for (int i = 0; i < numCategories; i++) {
			String category = EXPENSE_CATEGORIES[random.nextInt(EXPENSE_CATEGORIES.length)];
			double amount = 50 + random.nextDouble() * 2000; // ₱50 - ₱2050
			expenses.put(category, Math.round(amount * 100.0) / 100.0);
		}

		return expenses;
	}

	private String mapToJson(Map<String, Double> map) {
		StringBuilder json = new StringBuilder("{");
		boolean first = true;
		for (Map.Entry<String, Double> entry : map.entrySet()) {
			if (!first)
				json.append(",");
			json.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
			first = false;
		}
		json.append("}");
		return json.toString();
	}

	private Set<Integer> randomCustomerIds(int count) {
		Set<Integer> ids = new HashSet<>();
		while (ids.size() < count) {
			ids.add(1 + random.nextInt(TOTAL_CUSTOMERS));
		}
		return ids;
	}

	private Set<Integer> randomProductIds(int count) {
		Set<Integer> ids = new HashSet<>();
		while (ids.size() < count) {
			ids.add(1 + random.nextInt(TOTAL_PRODUCTS));
		}
		return ids;
	}

	private Set<Integer> getCustomerBranches(int customerId, int count) throws SQLException {
		// Get branches for this customer
		PreparedStatement stmt = conn.prepareStatement("SELECT id FROM branches WHERE customer_id = ? LIMIT ?");
		stmt.setInt(1, customerId);
		stmt.setInt(2, count * 2); // Get more than needed

		ResultSet rs = stmt.executeQuery();
		Set<Integer> branchIds = new HashSet<>();
		while (rs.next() && branchIds.size() < count) {
			branchIds.add(rs.getInt("id"));
		}
		rs.close();
		stmt.close();

		// If customer has no branches, use random branches
		if (branchIds.isEmpty()) {
			while (branchIds.size() < count) {
				branchIds.add(1 + random.nextInt(TOTAL_BRANCHES));
			}
		}

		return branchIds;
	}

	private Map<Integer, ProductInfo> loadProductInfo() throws SQLException {
		Map<Integer, ProductInfo> products = new HashMap<>();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT id, selling_price, capital FROM product");

		while (rs.next()) {
			int id = rs.getInt("id");
			double sellingPrice = rs.getDouble("selling_price");
			double capital = rs.getDouble("capital");
			products.put(id, new ProductInfo(sellingPrice, capital));
		}

		rs.close();
		stmt.close();
		return products;
	}

	private LocalDateTime randomDateTime(LocalDateTime start, LocalDateTime end) {
		long startEpoch = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		long endEpoch = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(randomEpoch), ZoneId.systemDefault());
	}

	private int randomWeighted(int[] values, double[] weights) {
		double random = this.random.nextDouble();
		double cumulative = 0.0;
		for (int i = 0; i < values.length; i++) {
			cumulative += weights[i];
			if (random <= cumulative) {
				return values[i];
			}
		}
		return values[values.length - 1];
	}

	private int getMaxDeliveryId() throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(id), 0) as max_id FROM delivery");
		int maxId = rs.next() ? rs.getInt("max_id") : 0;
		rs.close();
		stmt.close();
		return maxId;
	}

	private String formatDuration(long seconds) {
		long hours = seconds / 3600;
		long minutes = (seconds % 3600) / 60;
		long secs = seconds % 60;

		if (hours > 0) {
			return String.format("%dh %dm %ds", hours, minutes, secs);
		} else if (minutes > 0) {
			return String.format("%dm %ds", minutes, secs);
		} else {
			return String.format("%ds", secs);
		}
	}

	// ==================== HELPER CLASSES ====================

	private static class ProductInfo {
		final double sellingPrice;
		final double capital;

		ProductInfo(double sellingPrice, double capital) {
			this.sellingPrice = sellingPrice;
			this.capital = capital;
		}
	}

	private static class CustomerDeliveryData {
		final int customerDeliveryId;
		final int customerId;
		final double total;
		final LocalDateTime createdAt;
		final String status;

		CustomerDeliveryData(int customerDeliveryId, int customerId, double total, LocalDateTime createdAt,
				String status) {
			this.customerDeliveryId = customerDeliveryId;
			this.customerId = customerId;
			this.total = total;
			this.createdAt = createdAt;
			this.status = status;
		}
	}

	// ==================== MAIN ====================

	/**
	 * Main method - can be run directly without arguments
	 */
	public static void main(String[] args) {
		System.out.println("Initializing database connection...");

		DeliverySeeder seeder = null;

		try {
			seeder = new DeliverySeeder();
			seeder.seed();

			System.out.println("\n✓ Seeding completed successfully!");

		} catch (Exception e) {
			System.err.println("\n✗ ERROR: Seeding failed!");
			e.printStackTrace();
			System.exit(1);
		}
	}
}