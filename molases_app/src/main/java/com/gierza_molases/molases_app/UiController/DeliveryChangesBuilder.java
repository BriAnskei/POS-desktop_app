package com.gierza_molases.molases_app.UiController;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.context.DeliveryDetailsState;
import com.gierza_molases.molases_app.dto.delivery.BranchDeliveryChanges;
import com.gierza_molases.molases_app.dto.delivery.CustomerBranchDelivery;
import com.gierza_molases.molases_app.dto.delivery.CustomerDeliveryChanges;
import com.gierza_molases.molases_app.dto.delivery.DeliveryChanges;
import com.gierza_molases.molases_app.dto.delivery.DeliveryStatusChanges;
import com.gierza_molases.molases_app.dto.delivery.ProductDeliveryChanges;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.BranchDelivery;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.CustomerDelivery;
import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.model.ProductDelivery;
import com.gierza_molases.molases_app.model.ProductWithQuantity;

/**
 * Builds DeliveryChanges DTO from DeliveryDetailsState Handles conversion from
 * UI domain objects to database delivery objects
 */
public class DeliveryChangesBuilder {

	private final DeliveryDetailsState state;

	// Lookup maps for finding IDs from raw data
	private Map<Integer, CustomerDelivery> customerIdToCustomerDelivery;
	private Map<Integer, Map<Integer, BranchDelivery>> customerIdToBranchDeliveries; // customerId -> (branchId ->
																						// BranchDelivery)
	private Map<Integer, Map<Integer, ProductDelivery>> branchDeliveryIdToProductDeliveries; // branchDeliveryId ->
																								// (productId ->
																								// ProductDelivery)

	public DeliveryChangesBuilder(DeliveryDetailsState state) {
		this.state = state;
		buildLookupMaps();
	}

	/**
	 * Build lookup maps from raw CustomerBranchDelivery data This allows us to find
	 * delivery IDs from domain objects
	 */
	private void buildLookupMaps() {
		customerIdToCustomerDelivery = new HashMap<>();
		customerIdToBranchDeliveries = new HashMap<>();
		branchDeliveryIdToProductDeliveries = new HashMap<>();

		List<CustomerBranchDelivery> rawData = state.getRawCustomerBranchDeliveries();

		if (rawData == null) {
			return;
		}

		for (CustomerBranchDelivery cbd : rawData) {
			CustomerDelivery cd = cbd.getCustomerDelivery();
			int customerId = cd.getCustomerId();

			// Map customerId -> CustomerDelivery
			customerIdToCustomerDelivery.put(customerId, cd);

			// Map customerId -> (branchId -> BranchDelivery)
			customerIdToBranchDeliveries.putIfAbsent(customerId, new HashMap<>());

			Map<BranchDelivery, List<ProductDelivery>> branches = cbd.getBranches();
			if (branches != null) {
				for (Map.Entry<BranchDelivery, List<ProductDelivery>> entry : branches.entrySet()) {
					BranchDelivery bd = entry.getKey();
					List<ProductDelivery> products = entry.getValue();

					customerIdToBranchDeliveries.get(customerId).put(bd.getBranchId(), bd);

					// Map branchDeliveryId -> (productId -> ProductDelivery)
					branchDeliveryIdToProductDeliveries.putIfAbsent(bd.getId(), new HashMap<>());

					if (products != null) {
						for (ProductDelivery pd : products) {
							branchDeliveryIdToProductDeliveries.get(bd.getId()).put(pd.getProductId(), pd);
						}
					}
				}
			}
		}
	}

	/**
	 * Build complete DeliveryChanges DTO
	 */
	public DeliveryChanges build() {
		CustomerDeliveryChanges customerDeliveryChanges = buildCustomerDeliveryChanges();
		BranchDeliveryChanges branchDeliveryChanges = buildBranchDeliveryChanges();
		DeliveryStatusChanges deliveryStatusChanges = buildDeliveryStatusChanges();
		ProductDeliveryChanges productDeliveryChanges = buildProductDeliveryChanges();
		List<CustomerPayments> customerPayments = buildCustomerPayments();

		return new DeliveryChanges(customerDeliveryChanges, branchDeliveryChanges, deliveryStatusChanges,
				productDeliveryChanges, customerPayments);
	}

	/**
	 * Build CustomerDeliveryChanges
	 */
	private CustomerDeliveryChanges buildCustomerDeliveryChanges() {
		List<CustomerBranchDelivery> addedDeliveries = new ArrayList<>();

		// Process newly added customers from additionalCustomerDelivery
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> additionalCustomers = state
				.getAdditionalCustomerDelivery();

		if (additionalCustomers != null) {
			for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> entry : additionalCustomers.entrySet()) {
				Customer customer = entry.getKey();
				Map<Branch, List<ProductWithQuantity>> branches = entry.getValue();

				// Create CustomerDelivery (without ID since it's new)
				CustomerDelivery cd = new CustomerDelivery(customer.getId(), state.getDelivery().getId());

				// Build branch deliveries and product deliveries
				Map<BranchDelivery, List<ProductDelivery>> branchDeliveryMap = new HashMap<>();

				for (Map.Entry<Branch, List<ProductWithQuantity>> branchEntry : branches.entrySet()) {
					Branch branch = branchEntry.getKey();
					List<ProductWithQuantity> products = branchEntry.getValue();

					// Create BranchDelivery (without ID since it's new, customerDeliveryId will be
					// set by service)
					BranchDelivery bd = new BranchDelivery(0, // customerDeliveryId will be set after CustomerDelivery
																// is inserted
							branch.getId(), "delivered" // Default status for new branches
					);

					// Create ProductDeliveries
					List<ProductDelivery> productDeliveries = new ArrayList<>();
					for (ProductWithQuantity pwq : products) {
						ProductDelivery pd = new ProductDelivery(0, // branchDeliveryId will be set after BranchDelivery
																	// is inserted
								pwq.getProduct().getId(), pwq.getQuantity());
						productDeliveries.add(pd);
					}

					branchDeliveryMap.put(bd, productDeliveries);
				}

				CustomerBranchDelivery cbd = new CustomerBranchDelivery(cd, branchDeliveryMap);
				addedDeliveries.add(cbd);

			}
		}

		return new CustomerDeliveryChanges(addedDeliveries);
	}

	/**
	 * Build BranchDeliveryChanges
	 */
	private BranchDeliveryChanges buildBranchDeliveryChanges() {
		Map<BranchDelivery, List<ProductDelivery>> newBranches = new HashMap<>();

		// Process newly added branches to existing customers
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> addedBranches = state.getAddedBranches();

		if (addedBranches != null) {
			for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> entry : addedBranches.entrySet()) {
				Customer customer = entry.getKey();
				Map<Branch, List<ProductWithQuantity>> branches = entry.getValue();

				// Get customerDeliveryId for this customer
				CustomerDelivery cd = customerIdToCustomerDelivery.get(customer.getId());
				if (cd == null || cd.getId() == null) {
					continue; // Skip if customer delivery not found (shouldn't happen for existing customers)
				}

				for (Map.Entry<Branch, List<ProductWithQuantity>> branchEntry : branches.entrySet()) {
					Branch branch = branchEntry.getKey();
					List<ProductWithQuantity> products = branchEntry.getValue();

					// Create BranchDelivery
					BranchDelivery bd = new BranchDelivery(cd.getId(), branch.getId(), "delivered");

					// Create ProductDeliveries
					List<ProductDelivery> productDeliveries = new ArrayList<>();
					for (ProductWithQuantity pwq : products) {
						ProductDelivery pd = new ProductDelivery(0, // Will be set after BranchDelivery is inserted
								pwq.getProduct().getId(), pwq.getQuantity());
						productDeliveries.add(pd);
					}

					newBranches.put(bd, productDeliveries);
				}
			}
		}

		return new BranchDeliveryChanges(newBranches);
	}

	/**
	 * Build DeliveryStatusChanges
	 */
	private DeliveryStatusChanges buildDeliveryStatusChanges() {

		Map<Integer, String> customerStatuses = new HashMap<>();
		Map<Integer, String> branchStatuses = new HashMap<>();

		// Process customer delivery statuses
		Map<Customer, String> customerDeliveryStatuses = state.getCustomerDeliveryStatuses();

		for (Map.Entry<Customer, String> entry : customerDeliveryStatuses.entrySet()) {
			System.out.println("Customer: " + entry.getValue().toString() + " status" + entry.getValue());
		}

		if (customerDeliveryStatuses != null) {
			for (Map.Entry<Customer, String> entry : customerDeliveryStatuses.entrySet()) {
				Customer customer = entry.getKey();
				String status = state.getCustomerDeliveryStatus(customer);

				CustomerDelivery cd = customerIdToCustomerDelivery.get(customer.getId());
				if (cd != null && cd.getId() != null) {
					customerStatuses.put(cd.getId(), status.toLowerCase());
				}
			}
		}

		// Process branch delivery statuses
		Map<Branch, String> branchDeliveryStatuses = state.getBranchDeliveryStatuses();
		if (branchDeliveryStatuses != null) {
			for (Map.Entry<Branch, String> entry : branchDeliveryStatuses.entrySet()) {
				Branch branch = entry.getKey();
				String status = entry.getValue();

				// Find the BranchDelivery for this branch
				// Need to search through all customers' branches
				for (Map<Integer, BranchDelivery> customerBranches : customerIdToBranchDeliveries.values()) {
					BranchDelivery bd = customerBranches.get(branch.getId());
					if (bd != null && bd.getId() != null) {
						branchStatuses.put(bd.getId(), status.toLowerCase());
						break;
					}
				}
			}
		}

		return new DeliveryStatusChanges(customerStatuses, branchStatuses);
	}

	/**
	 * Build ProductDeliveryChanges
	 */
	private ProductDeliveryChanges buildProductDeliveryChanges() {
		List<ProductDelivery> addedProducts = new ArrayList<>();
		Map<Integer, Integer> updatedQuantities = new HashMap<>();
		List<Integer> removedProductIds = new ArrayList<>();

		// Process added products
		Map<Branch, List<ProductWithQuantity>> addedProductsMap = state.getAddedProducts();
		if (addedProductsMap != null) {
			for (Map.Entry<Branch, List<ProductWithQuantity>> entry : addedProductsMap.entrySet()) {
				Branch branch = entry.getKey();
				List<ProductWithQuantity> products = entry.getValue();

				// Find BranchDelivery ID for this branch
				BranchDelivery bd = findBranchDelivery(branch.getId());
				if (bd == null || bd.getId() == null) {
					continue;
				}

				for (ProductWithQuantity pwq : products) {
					ProductDelivery pd = new ProductDelivery(bd.getId(), pwq.getProduct().getId(), pwq.getQuantity());
					addedProducts.add(pd);
				}
			}
		}

		// Process edited product quantities
		Map<Branch, Map<Product, DeliveryDetailsState.QuantityChange>> editedQuantities = state
				.getEditedProductQuantities();

		if (editedQuantities != null) {
			for (Map.Entry<Branch, Map<Product, DeliveryDetailsState.QuantityChange>> entry : editedQuantities
					.entrySet()) {
				Branch branch = entry.getKey();
				Map<Product, DeliveryDetailsState.QuantityChange> productChanges = entry.getValue();

				BranchDelivery bd = findBranchDelivery(branch.getId());
				if (bd == null) {
					System.err.println("WARNING: Could not find BranchDelivery for branch ID: " + branch.getId());
					continue;
				}

				// Check if bd.getId() is null (happens for newly added branches) we skip
				// this since this doesn't have a productDelivery in DB yet
				if (bd.getId() == null) {
					System.err.println("WARNING: BranchDelivery has null ID for branch: " + branch.getId()
							+ ". This might be a newly added branch that hasn't been saved yet.");
					continue;
				}

				Map<Integer, ProductDelivery> branchProducts = branchDeliveryIdToProductDeliveries.get(bd.getId());
				if (branchProducts == null) {
					// This is fine - it means this is a newly added branch
					continue;
				}

				for (Map.Entry<Product, DeliveryDetailsState.QuantityChange> productEntry : productChanges.entrySet()) {
					Product product = productEntry.getKey();
					DeliveryDetailsState.QuantityChange quantityChange = productEntry.getValue();

					ProductDelivery pd = branchProducts.get(product.getId());
					if (pd == null || pd.getId() == null) {
						// This is fine - the product was recently added, so it's already tracked
						// in addedProducts or addedBranches with the correct quantity
						continue;
					}

					// Only existing products from DB reach here
					updatedQuantities.put(pd.getId(), quantityChange.getNewQuantity());
				}
			}
		}

		// Process removed products
		Map<Branch, List<ProductWithQuantity>> removedProductsMap = state.getRemovedProducts();
		if (removedProductsMap != null) {
			for (Map.Entry<Branch, List<ProductWithQuantity>> entry : removedProductsMap.entrySet()) {
				Branch branch = entry.getKey();
				List<ProductWithQuantity> products = entry.getValue();

				BranchDelivery bd = findBranchDelivery(branch.getId());
				if (bd == null || bd.getId() == null) {
					continue;
				}

				Map<Integer, ProductDelivery> branchProducts = branchDeliveryIdToProductDeliveries.get(bd.getId());
				if (branchProducts == null) {
					continue;
				}

				for (ProductWithQuantity pwq : products) {
					ProductDelivery pd = branchProducts.get(pwq.getProduct().getId());
					if (pd != null && pd.getId() != null) {
						removedProductIds.add(pd.getId());
					}
				}
			}
		}

		return new ProductDeliveryChanges(addedProducts, updatedQuantities, removedProductIds);
	}

	/**
	 * Build CustomerPayments list
	 */
	private List<CustomerPayments> buildCustomerPayments() {
		List<CustomerPayments> payments = new ArrayList<>();

		Map<Customer, String> paymentTypes = state.getTemporaryPaymentTypes();
		if (paymentTypes == null) {
			return payments;
		}

		for (Map.Entry<Customer, String> entry : paymentTypes.entrySet()) {
			Customer customer = entry.getKey();
			String paymentTypeDisplay = entry.getValue();

			// Skip the cancelled delivery status
			String customerDeliveryStatus = state.getCustomerDeliveryStatus(customer);
			if (customerDeliveryStatus.equals("Cancelled")) {
				continue;
			}

			// Skip "Not Set" payments
			if ("Not Set".equals(paymentTypeDisplay)) {
				throw new IllegalStateException("Payment type is not set for customer: " + customer.getDisplayName());
			}

			// Calculate customer's total sales (excluding cancelled branches)
			double totalSales = calculateCustomerTotalSales(customer);

			// Parse payment type and extract additional info
			String paymentType;
			String status;
			double totalPayment = 0.0;
			Date promiseToPay = null;

			if (paymentTypeDisplay.startsWith("Partial (₱")) {
				paymentType = "Partial";
				status = "pending"; // Partial payments start as unpaid

				// Extract amount from "Partial (₱1,234.56)"
				String amountStr = paymentTypeDisplay.substring(paymentTypeDisplay.indexOf("₱") + 1,
						paymentTypeDisplay.indexOf(")"));
				amountStr = amountStr.replace(",", "");
				totalPayment = Double.parseDouble(amountStr);

				// promiseToPay remains null for partial payments

			} else if (paymentTypeDisplay.startsWith("Loan (Due: ")) {
				paymentType = "Loan";
				status = "pending"; // Loans start as unpaid
				totalPayment = 0.0;

				// Extract due date from "Loan (Due: 01/15/2024)"
				String dueDateStr = paymentTypeDisplay.substring(paymentTypeDisplay.indexOf("Due: ") + 5,
						paymentTypeDisplay.indexOf(")"));

				// Parse the date string to Date object
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
					promiseToPay = sdf.parse(dueDateStr);
				} catch (Exception e) {
					// If parsing fails, log error and skip this payment
					System.err.println("Failed to parse loan date: " + dueDateStr);
					e.printStackTrace();
					continue;
				}

			} else {
				// "Paid Cash" or "Paid Cheque"
				paymentType = paymentTypeDisplay;
				status = "complete"; // Fully paid
				totalPayment = totalSales;
				// promiseToPay remains null for fully paid transactions
			}

			// Get CustomerDelivery ID
			CustomerDelivery cd = customerIdToCustomerDelivery.get(customer.getId());

			int customerDeliveryId = cd == null ? 0 : cd.getId(); // initialized '0' for the newly added customer this
																	// will be updated in the service layer

			CustomerPayments payment = new CustomerPayments(customer.getId(), customerDeliveryId, paymentType, status,
					totalSales, totalPayment, promiseToPay, LocalDateTime.now());

			payments.add(payment);
		}

		return payments;
	}

	/**
	 * Helper: Find BranchDelivery by branch ID
	 */
	private BranchDelivery findBranchDelivery(int branchId) {
		for (Map<Integer, BranchDelivery> customerBranches : customerIdToBranchDeliveries.values()) {
			BranchDelivery bd = customerBranches.get(branchId);
			if (bd != null) {
				return bd;
			}
		}
		return null;
	}

	/**
	 * Helper: Calculate total sales for a customer (excluding cancelled branches)
	 */
	private double calculateCustomerTotalSales(Customer customer) {
		Map<Branch, List<ProductWithQuantity>> branches = state.getMappedCustomerDeliveries().get(customer);

		if (branches == null) {
			return 0.0;
		}

		// Check if customer is cancelled
		String customerStatus = state.getCustomerDeliveryStatus(customer);
		if ("Cancelled".equalsIgnoreCase(customerStatus)) {
			return 0.0;
		}

		double totalSales = 0.0;

		for (Map.Entry<Branch, List<ProductWithQuantity>> entry : branches.entrySet()) {
			Branch branch = entry.getKey();
			List<ProductWithQuantity> products = entry.getValue();

			// Skip cancelled branches
			String branchStatus = state.getBranchDeliveryStatus(branch);
			if ("Cancelled".equalsIgnoreCase(branchStatus)) {
				continue;
			}

			for (ProductWithQuantity pwq : products) {
				totalSales += pwq.getTotalSellingPrice();
			}
		}

		return totalSales;
	}
}