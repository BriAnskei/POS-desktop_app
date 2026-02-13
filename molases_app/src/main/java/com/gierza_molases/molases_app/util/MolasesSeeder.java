package com.gierza_molases.molases_app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-performance database seeder for Molases delivery system Generates
 * millions of delivery records with proper relationships
 */
public class MolasesSeeder {

	// Database configuration
	private static final String DB_URL = "jdbc:sqlite:database/molases.db";
	private static final int BATCH_SIZE = 1000;
	private static final int THREAD_COUNT = 4;

	// Target records
	private static final int TARGET_DELIVERIES = 100_000;

	// ID ranges from existing data
	private static final int MIN_CUSTOMER_ID = 1;
	private static final int MAX_CUSTOMER_ID = 5000;
	private static final int MIN_PRODUCT_ID = 1;
	private static final int MAX_PRODUCT_ID = 50;
	private static final int MIN_BRANCH_ID = 1;
	private static final int MAX_BRANCH_ID = 20068;

	// Date range (5 years)
	private static final LocalDateTime START_DATE = LocalDateTime.now().minusYears(5);
	private static final LocalDateTime END_DATE = LocalDateTime.now();
	private static final LocalDateTime RECENT_CUTOFF = LocalDateTime.now().minusDays(7);

	// Status probabilities
	private static final double DELIVERED_PROB_OLD = 0.95;
	private static final double DELIVERED_PROB_RECENT = 0.30;

	// Payment types
	private static enum PaymentType {
		PAID_CASH(0.50, "Paid Cash"), PAID_CHEQUE(0.20, "Paid Cheque"), PARTIAL(0.20, "Partial"), LOAN(0.10, "Loan");

		final double probability;
		final String value;

		PaymentType(double prob, String val) {
			this.probability = prob;
			this.value = val;
		}
	}

	private final Random random = new Random();
	private final AtomicInteger deliveryCounter = new AtomicInteger(0);
	private final AtomicInteger customerDeliveryCounter = new AtomicInteger(0);
	private final AtomicInteger branchDeliveryCounter = new AtomicInteger(0);
	private final AtomicInteger productDeliveryCounter = new AtomicInteger(0);
	private final AtomicInteger paymentCounter = new AtomicInteger(0);

	// Cached product data for calculations
	private List<Product> products = new ArrayList<>();
	private Map<Integer, List<Integer>> customerBranches = new HashMap<>();

	public static void main(String[] args) {
		MolasesSeeder seeder = new MolasesSeeder();

		System.out.println("=".repeat(80));
		System.out.println("MOLASES DATABASE SEEDER");
		System.out.println("=".repeat(80));
		System.out.println("Database: " + DB_URL);
		System.out.println("Target: " + TARGET_DELIVERIES + " deliveries");
		System.out.println("Threads: " + THREAD_COUNT);
		System.out.println("Date Range: " + START_DATE.toLocalDate() + " to " + END_DATE.toLocalDate());
		System.out.println("=".repeat(80));

		// Check if database file exists
		java.io.File dbFile = new java.io.File("database/molases.db");
		if (!dbFile.exists()) {
			System.err.println("\nERROR: Database file not found at: database/molases.db");
			System.err.println("Please ensure:");
			System.err.println("  1. The 'database' folder exists");
			System.err.println("  2. The 'molases.db' file is in the database folder");
			System.err.println("  3. You're running the seeder from the project root directory");
			System.exit(1);
		}

		try {
			seeder.seed();
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void seed() throws Exception {
		long startTime = System.currentTimeMillis();

		// Step 1: Load existing data
		System.out.println("\n[1/3] Loading existing data...");
		loadExistingData();

		// Step 2: Prepare database
		System.out.println("\n[2/3] Preparing database...");
		prepareDatabase();

		// Step 3: Generate deliveries in parallel
		System.out.println("\n[3/3] Generating deliveries...");
		generateDeliveries();

		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime) / 1000;

		// Print statistics
		printStatistics(duration);
	}

	private void loadExistingData() throws SQLException {
		try (Connection conn = DriverManager.getConnection(DB_URL)) {
			// Load products with pricing
			String sql = "SELECT id, product_name, selling_price, capital FROM product";
			try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
				while (rs.next()) {
					products.add(new Product(rs.getInt("id"), rs.getString("product_name"),
							rs.getDouble("selling_price"), rs.getDouble("capital")));
				}
			}
			System.out.println("  ✓ Loaded " + products.size() + " products");

			// Load customer-branch mappings
			sql = "SELECT customer_id, id FROM branches ORDER BY customer_id";
			try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
				while (rs.next()) {
					int customerId = rs.getInt("customer_id");
					int branchId = rs.getInt("id");
					customerBranches.computeIfAbsent(customerId, k -> new ArrayList<>()).add(branchId);
				}
			}
			System.out.println("  ✓ Loaded branch mappings for " + customerBranches.size() + " customers");
		}
	}

	private void prepareDatabase() throws SQLException {
		try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {

			// Note: Database is already in WAL mode (set by Database.init())
			// We disable foreign keys and triggers temporarily for faster inserts
			stmt.execute("PRAGMA foreign_keys = OFF");

			// Disable triggers to prevent delivery_daily_counts constraint errors
			// (Triggers expect text dates, but we're storing epoch milliseconds)
			stmt.execute("DROP TRIGGER IF EXISTS delivery_ai");
			stmt.execute("DROP TRIGGER IF EXISTS delivery_ad");
			stmt.execute("DROP TRIGGER IF EXISTS delivery_au");

			// Increase cache for better performance
			stmt.execute("PRAGMA cache_size = -64000"); // 64MB cache

			// Set busy timeout to handle concurrent access
			stmt.execute("PRAGMA busy_timeout = 30000"); // 30 seconds

			System.out.println("  ✓ Database optimized for bulk inserts");
			System.out.println("  ✓ Triggers temporarily disabled");
		}
	}

	private void generateDeliveries() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

		int deliveriesPerThread = TARGET_DELIVERIES / THREAD_COUNT;

		for (int t = 0; t < THREAD_COUNT; t++) {
			final int threadId = t;
			final int startDelivery = t * deliveriesPerThread;
			final int endDelivery = (t == THREAD_COUNT - 1) ? TARGET_DELIVERIES : (t + 1) * deliveriesPerThread;

			executor.submit(() -> {
				try {
					generateDeliveriesForThread(threadId, startDelivery, endDelivery);
				} catch (Exception e) {
					System.err.println("Thread " + threadId + " failed: " + e.getMessage());
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		// Progress monitor
		Thread monitor = new Thread(() -> {
			try {
				while (!latch.await(2, TimeUnit.SECONDS)) {
					int current = deliveryCounter.get();
					double progress = (current * 100.0) / TARGET_DELIVERIES;
					System.out.printf("  Progress: %d / %d deliveries (%.1f%%)%n", current, TARGET_DELIVERIES,
							progress);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		monitor.setDaemon(true);
		monitor.start();

		latch.await();
		executor.shutdown();

		// Re-enable foreign keys and recreate triggers
		try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {

			stmt.execute("PRAGMA foreign_keys = ON");

			System.out.println("\n  ✓ Recreating triggers...");

			// Recreate delivery triggers for delivery_daily_counts
			stmt.execute("CREATE TRIGGER IF NOT EXISTS delivery_ai " + "AFTER INSERT ON delivery " + "BEGIN "
					+ "  INSERT INTO delivery_daily_counts(counter_date, status, total_rows) "
					+ "  VALUES (DATE(NEW.created_at / 1000, 'unixepoch', 'localtime'), NEW.status, 1) "
					+ "  ON CONFLICT(counter_date, status) " + "  DO UPDATE SET total_rows = total_rows + 1; " + "END");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS delivery_ad " + "AFTER DELETE ON delivery " + "BEGIN "
					+ "  UPDATE delivery_daily_counts " + "  SET total_rows = total_rows - 1 "
					+ "  WHERE counter_date = DATE(OLD.created_at / 1000, 'unixepoch', 'localtime') "
					+ "    AND status = OLD.status; " + "END");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS delivery_au " + "AFTER UPDATE OF status, created_at ON delivery "
					+ "BEGIN " + "  UPDATE delivery_daily_counts " + "  SET total_rows = total_rows - 1 "
					+ "  WHERE counter_date = DATE(OLD.created_at / 1000, 'unixepoch', 'localtime') "
					+ "    AND status = OLD.status; "
					+ "  INSERT INTO delivery_daily_counts(counter_date, status, total_rows) "
					+ "  VALUES (DATE(NEW.created_at / 1000, 'unixepoch', 'localtime'), NEW.status, 1) "
					+ "  ON CONFLICT(counter_date, status) " + "  DO UPDATE SET total_rows = total_rows + 1; " + "END");

			System.out.println("  ✓ Triggers recreated");

			// Populate delivery_daily_counts from existing data
			System.out.println("  ✓ Populating delivery_daily_counts...");
			stmt.execute("INSERT INTO delivery_daily_counts(counter_date, status, total_rows) " + "SELECT "
					+ "  DATE(created_at / 1000, 'unixepoch', 'localtime') as counter_date, " + "  status, "
					+ "  COUNT(*) as total_rows " + "FROM delivery " + "GROUP BY counter_date, status "
					+ "ON CONFLICT(counter_date, status) " + "DO UPDATE SET total_rows = excluded.total_rows");

			System.out.println("  ✓ Database constraints and triggers re-enabled");
		}
	}

	private void generateDeliveriesForThread(int threadId, int start, int end) throws SQLException {
		Connection conn = DriverManager.getConnection(DB_URL);
		conn.setAutoCommit(false);

		// Prepare statements
		PreparedStatement deliveryStmt = conn
				.prepareStatement("INSERT INTO delivery (schedule_date, name, expenses, status, "
						+ "total_customers, total_branches, total_gross, total_capital, "
						+ "gross_profit, total_expenses, net_profit, created_at) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

		PreparedStatement customerDeliveryStmt = conn.prepareStatement(
				"INSERT INTO customer_delivery (customer_id, delivery_id, status) " + "VALUES (?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);

		PreparedStatement branchDeliveryStmt = conn.prepareStatement(
				"INSERT INTO branch_delivery (customer_delivery_id, branch_id, status) " + "VALUES (?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);

		PreparedStatement productDeliveryStmt = conn.prepareStatement(
				"INSERT INTO product_delivery (branch_delivery_id, product_id, quantity) " + "VALUES (?, ?, ?)");

		PreparedStatement paymentStmt = conn
				.prepareStatement("INSERT INTO customer_payments (customer_id, customer_delivery_id, "
						+ "payment_type, status, notes, total, total_payment, promise_to_pay, created_at) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

		PreparedStatement paymentHistoryStmt = conn.prepareStatement(
				"INSERT INTO payment_history (customer_payment_id, amount, created_at) " + "VALUES (?, ?, ?)");

		try {
			for (int i = start; i < end; i++) {
				generateSingleDelivery(deliveryStmt, customerDeliveryStmt, branchDeliveryStmt, productDeliveryStmt,
						paymentStmt, paymentHistoryStmt);

				if ((i - start + 1) % BATCH_SIZE == 0) {
					conn.commit();
				}

				deliveryCounter.incrementAndGet();
			}

			conn.commit();

		} finally {
			deliveryStmt.close();
			customerDeliveryStmt.close();
			branchDeliveryStmt.close();
			productDeliveryStmt.close();
			paymentStmt.close();
			paymentHistoryStmt.close();
			conn.close();
		}
	}

	private void generateSingleDelivery(PreparedStatement deliveryStmt, PreparedStatement customerDeliveryStmt,
			PreparedStatement branchDeliveryStmt, PreparedStatement productDeliveryStmt, PreparedStatement paymentStmt,
			PreparedStatement paymentHistoryStmt) throws SQLException {

		// Generate delivery date
		LocalDateTime deliveryDate = randomDateBetween(START_DATE, END_DATE);
		LocalDateTime createdAt = deliveryDate.minusDays(random.nextInt(7));

		// Determine status based on date
		boolean isRecent = deliveryDate.isAfter(RECENT_CUTOFF);
		double deliveredProb = isRecent ? DELIVERED_PROB_RECENT : DELIVERED_PROB_OLD;
		String deliveryStatus = random.nextDouble() < deliveredProb ? "delivered" : "scheduled";

		// Generate delivery name
		String deliveryName = "Delivery-" + deliveryDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
				+ String.format("%05d", random.nextInt(100000));

		// Select customers for this delivery (5-15 customers)
		int numCustomers = 5 + random.nextInt(11);
		Set<Integer> selectedCustomers = new HashSet<>();
		while (selectedCustomers.size() < numCustomers) {
			selectedCustomers.add(MIN_CUSTOMER_ID + random.nextInt(MAX_CUSTOMER_ID - MIN_CUSTOMER_ID + 1));
		}

		// Tracking totals
		double totalGross = 0;
		double totalCapital = 0;
		int totalBranches = 0;
		List<CustomerDeliveryData> customerDeliveries = new ArrayList<>();

		// Process each customer
		for (int customerId : selectedCustomers) {
			// Get branches for this customer (1-4 branches)
			List<Integer> availableBranches = customerBranches.get(customerId);
			if (availableBranches == null || availableBranches.isEmpty()) {
				// Customer has no branches, skip
				continue;
			}

			int numBranches = Math.min(1 + random.nextInt(4), availableBranches.size());
			List<Integer> selectedBranches = new ArrayList<>();
			List<Integer> branchCopy = new ArrayList<>(availableBranches);
			Collections.shuffle(branchCopy);
			for (int i = 0; i < numBranches; i++) {
				selectedBranches.add(branchCopy.get(i));
			}

			CustomerDeliveryData custData = new CustomerDeliveryData();
			custData.customerId = customerId;
			custData.status = deliveryStatus;

			// Process each branch
			for (int branchId : selectedBranches) {
				BranchDeliveryData branchData = new BranchDeliveryData();
				branchData.branchId = branchId;
				branchData.status = deliveryStatus;

				// Select products for this branch (1-8 products)
				int numProducts = 1 + random.nextInt(8);
				Set<Integer> selectedProducts = new HashSet<>();
				while (selectedProducts.size() < numProducts) {
					selectedProducts.add(random.nextInt(products.size()));
				}

				// Process each product
				for (int productIdx : selectedProducts) {
					Product product = products.get(productIdx);
					int quantity = 1 + random.nextInt(500);

					ProductDeliveryData prodData = new ProductDeliveryData();
					prodData.productId = product.id;
					prodData.quantity = quantity;

					double itemGross = product.sellingPrice * quantity;
					double itemCapital = product.capital * quantity;

					branchData.gross += itemGross;
					branchData.capital += itemCapital;
					branchData.products.add(prodData);

					totalGross += itemGross;
					totalCapital += itemCapital;
				}

				custData.branches.add(branchData);
				totalBranches++;
			}

			customerDeliveries.add(custData);
		}

		// Calculate delivery totals
		double grossProfit = totalGross - totalCapital;
		double expenses = totalGross * (0.05 + random.nextDouble() * 0.05); // 5-10% of gross
		double netProfit = grossProfit - expenses;

		// Generate expenses JSON in the format your app expects
		// Format: {"item1": amount1, "item2": amount2, ...}
		String expensesJson = generateExpensesJson(expenses);

		// Insert delivery record
		// Store dates as epoch milliseconds (your mapper expects this)
		deliveryStmt.setLong(1, deliveryDate.atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
		deliveryStmt.setString(2, deliveryName);
		deliveryStmt.setString(3, expensesJson);
		deliveryStmt.setString(4, deliveryStatus);
		deliveryStmt.setDouble(5, customerDeliveries.size());
		deliveryStmt.setDouble(6, totalBranches);
		deliveryStmt.setDouble(7, totalGross);
		deliveryStmt.setDouble(8, totalCapital);
		deliveryStmt.setDouble(9, grossProfit);
		deliveryStmt.setDouble(10, expenses);
		deliveryStmt.setDouble(11, netProfit);
		deliveryStmt.setLong(12, createdAt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
		deliveryStmt.executeUpdate();

		ResultSet deliveryKeys = deliveryStmt.getGeneratedKeys();
		deliveryKeys.next();
		int deliveryId = deliveryKeys.getInt(1);

		// Insert customer_delivery records
		for (CustomerDeliveryData custData : customerDeliveries) {
			customerDeliveryStmt.setInt(1, custData.customerId);
			customerDeliveryStmt.setInt(2, deliveryId);
			customerDeliveryStmt.setString(3, custData.status);
			customerDeliveryStmt.executeUpdate();

			ResultSet custDelKeys = customerDeliveryStmt.getGeneratedKeys();
			custDelKeys.next();
			int customerDeliveryId = custDelKeys.getInt(1);
			custData.customerDeliveryId = customerDeliveryId;

			customerDeliveryCounter.incrementAndGet();

			// Insert branch_delivery records
			for (BranchDeliveryData branchData : custData.branches) {
				branchDeliveryStmt.setInt(1, customerDeliveryId);
				branchDeliveryStmt.setInt(2, branchData.branchId);
				branchDeliveryStmt.setString(3, branchData.status);
				branchDeliveryStmt.executeUpdate();

				ResultSet branchDelKeys = branchDeliveryStmt.getGeneratedKeys();
				branchDelKeys.next();
				int branchDeliveryId = branchDelKeys.getInt(1);

				branchDeliveryCounter.incrementAndGet();

				// Insert product_delivery records
				for (ProductDeliveryData prodData : branchData.products) {
					productDeliveryStmt.setInt(1, branchDeliveryId);
					productDeliveryStmt.setInt(2, prodData.productId);
					productDeliveryStmt.setInt(3, prodData.quantity);
					productDeliveryStmt.addBatch();

					productDeliveryCounter.incrementAndGet();
				}
				productDeliveryStmt.executeBatch();
			}

			// Insert payment record for this customer (sum all their branches)
			if (deliveryStatus.equals("delivered")) {
				double customerTotal = 0;
				for (BranchDeliveryData bd : custData.branches) {
					customerTotal += bd.gross;
				}
				generatePayment(custData, customerTotal, paymentStmt, paymentHistoryStmt, createdAt);
			}
		}
	}

	private void generatePayment(CustomerDeliveryData custData, double total, PreparedStatement paymentStmt,
			PreparedStatement paymentHistoryStmt, LocalDateTime baseDate) throws SQLException {

		PaymentType paymentType = selectPaymentType();
		String status;
		double totalPayment;
		String promiseToPay = null;

		switch (paymentType) {
		case PAID_CASH:
		case PAID_CHEQUE:
			status = "complete";
			totalPayment = total;
			break;

		case PARTIAL:
			// 80% complete, 20% pending
			status = random.nextDouble() < 0.80 ? "complete" : "pending";
			if (status.equals("complete")) {
				totalPayment = total;
			} else {
				// Paid 60-95% of total
				totalPayment = total * (0.60 + random.nextDouble() * 0.35);
			}
			break;

		case LOAN:
			// 60% complete, 40% pending
			status = random.nextDouble() < 0.60 ? "complete" : "pending";
			if (status.equals("complete")) {
				totalPayment = total;
			} else {
				// Paid 0-50% of total
				totalPayment = total * (random.nextDouble() * 0.50);
			}
			// Promise to pay in 7-90 days
			promiseToPay = baseDate.plusDays(7 + random.nextInt(84)).format(DateTimeFormatter.ISO_LOCAL_DATE);
			break;

		default:
			status = "complete";
			totalPayment = total;
		}

		// Insert payment record
		paymentStmt.setInt(1, custData.customerId);
		paymentStmt.setInt(2, custData.customerDeliveryId);
		paymentStmt.setString(3, paymentType.value);
		paymentStmt.setString(4, status);
		paymentStmt.setString(5, "Auto-generated payment");
		paymentStmt.setDouble(6, total);
		paymentStmt.setDouble(7, totalPayment);
		paymentStmt.setString(8, promiseToPay);
		// Store timestamp as epoch milliseconds
		paymentStmt.setLong(9, baseDate.atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
		paymentStmt.executeUpdate();

		ResultSet paymentKeys = paymentStmt.getGeneratedKeys();
		paymentKeys.next();
		int paymentId = paymentKeys.getInt(1);

		paymentCounter.incrementAndGet();

		// Generate payment history for PARTIAL and LOAN types
		if (paymentType == PaymentType.PARTIAL || paymentType == PaymentType.LOAN) {
			int numPayments = status.equals("complete") ? (2 + random.nextInt(4)) : (1 + random.nextInt(3));

			double remaining = totalPayment;
			LocalDateTime paymentDate = baseDate;

			for (int i = 0; i < numPayments; i++) {
				double amount;
				if (i == numPayments - 1) {
					// Last payment gets the remainder
					amount = remaining;
				} else {
					// Random portion of remaining
					amount = remaining * (0.20 + random.nextDouble() * 0.40);
					remaining -= amount;
				}

				paymentHistoryStmt.setInt(1, paymentId);
				paymentHistoryStmt.setDouble(2, amount);
				// Store timestamp as epoch milliseconds
				paymentHistoryStmt.setLong(3, paymentDate.atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
				paymentHistoryStmt.addBatch();

				// Next payment 1-30 days later
				paymentDate = paymentDate.plusDays(1 + random.nextInt(30));
			}

			paymentHistoryStmt.executeBatch();
		}
	}

	private PaymentType selectPaymentType() {
		double rand = random.nextDouble();
		double cumulative = 0;

		for (PaymentType type : PaymentType.values()) {
			cumulative += type.probability;
			if (rand <= cumulative) {
				return type;
			}
		}

		return PaymentType.PAID_CASH;
	}

	private LocalDateTime randomDateBetween(LocalDateTime start, LocalDateTime end) {
		long startSeconds = start.toEpochSecond(ZoneOffset.UTC);
		long endSeconds = end.toEpochSecond(ZoneOffset.UTC);
		long randomSeconds = startSeconds + (long) (random.nextDouble() * (endSeconds - startSeconds));
		return LocalDateTime.ofEpochSecond(randomSeconds, 0, ZoneOffset.UTC);
	}

	/**
	 * Generate expenses in JSON format: {"category1": amount1, "category2":
	 * amount2, ...} This matches the format expected by
	 * Delivery.parseExpensesJson()
	 */
	private String generateExpensesJson(double totalExpenses) {
		// Generate 2-5 expense categories
		int numCategories = 2 + random.nextInt(4);
		String[] categories = { "Gas", "Toll", "Meals", "Maintenance", "Parking", "Misc" };

		StringBuilder json = new StringBuilder("{");
		double remaining = totalExpenses;

		for (int i = 0; i < numCategories; i++) {
			String category = categories[random.nextInt(categories.length)];
			double amount;

			if (i == numCategories - 1) {
				// Last category gets the remainder
				amount = remaining;
			} else {
				// Random portion
				amount = remaining * (0.10 + random.nextDouble() * 0.40);
				remaining -= amount;
			}

			if (i > 0)
				json.append(",");
			json.append("\"").append(category).append("\":");
			json.append(String.format("%.2f", amount));
		}

		json.append("}");
		return json.toString();
	}

	private void printStatistics(long durationSeconds) {
		System.out.println("\n" + "=".repeat(80));
		System.out.println("SEEDING COMPLETE!");
		System.out.println("=".repeat(80));
		System.out.println("Duration: " + durationSeconds + " seconds");
		System.out.println("\nRecords generated:");
		System.out.println("  Deliveries:          " + String.format("%,d", deliveryCounter.get()));
		System.out.println("  Customer Deliveries: " + String.format("%,d", customerDeliveryCounter.get()));
		System.out.println("  Branch Deliveries:   " + String.format("%,d", branchDeliveryCounter.get()));
		System.out.println("  Product Deliveries:  " + String.format("%,d", productDeliveryCounter.get()));
		System.out.println("  Payments:            " + String.format("%,d", paymentCounter.get()));

		int totalRecords = deliveryCounter.get() + customerDeliveryCounter.get() + branchDeliveryCounter.get()
				+ productDeliveryCounter.get() + paymentCounter.get();

		System.out.println("  ---");
		System.out.println("  TOTAL:               " + String.format("%,d", totalRecords));

		if (durationSeconds > 0) {
			long recordsPerSecond = totalRecords / durationSeconds;
			System.out.println("\nPerformance: " + String.format("%,d", recordsPerSecond) + " records/second");
		}
		System.out.println("=".repeat(80));
	}

	// Helper classes
	static class Product {
		int id;
		String name;
		double sellingPrice;
		double capital;

		Product(int id, String name, double sellingPrice, double capital) {
			this.id = id;
			this.name = name;
			this.sellingPrice = sellingPrice;
			this.capital = capital;
		}
	}

	static class CustomerDeliveryData {
		int customerId;
		int customerDeliveryId;
		String status;
		List<BranchDeliveryData> branches = new ArrayList<>();
	}

	static class BranchDeliveryData {
		int branchId;
		String status;
		double gross;
		double capital;
		List<ProductDeliveryData> products = new ArrayList<>();
	}

	static class ProductDeliveryData {
		int productId;
		int quantity;
	}
}