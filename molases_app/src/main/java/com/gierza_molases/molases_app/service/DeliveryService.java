package com.gierza_molases.molases_app.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.dao.BranchDao;
import com.gierza_molases.molases_app.dao.BranchDeliveryDao;
import com.gierza_molases.molases_app.dao.CustomerDao;
import com.gierza_molases.molases_app.dao.CustomerDeliveryDao;
import com.gierza_molases.molases_app.dao.CustomerPaymentDao;
import com.gierza_molases.molases_app.dao.DeliveryDao;
import com.gierza_molases.molases_app.dao.PaymentHistoryDao;
import com.gierza_molases.molases_app.dao.ProductDao;
import com.gierza_molases.molases_app.dao.ProductDeliveryDao;
import com.gierza_molases.molases_app.dto.delivery.BranchDeliveryChanges;
import com.gierza_molases.molases_app.dto.delivery.CustomerBranchDelivery;
import com.gierza_molases.molases_app.dto.delivery.CustomerDeliveryChanges;
import com.gierza_molases.molases_app.dto.delivery.DeliveryChanges;
import com.gierza_molases.molases_app.dto.delivery.DeliveryStatusChanges;
import com.gierza_molases.molases_app.dto.delivery.NewDelivery;
import com.gierza_molases.molases_app.dto.delivery.ProductDeliveryChanges;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.BranchDelivery;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.CustomerDelivery;
import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.PaymentHistory;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.model.ProductDelivery;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.model.response.DeliveryViewResponse;
import com.gierza_molases.molases_app.util.TransactionHelper;

public class DeliveryService {

	private final DeliveryDao deliveryDao;
	private final CustomerDeliveryDao customerDeliveryDao;
	private final BranchDeliveryDao branchDeliveryDao;
	private final ProductDeliveryDao productDeliveryDao;
	private final CustomerPaymentDao customerPaymentdao;
	private final PaymentHistoryDao paymentHistoryDao;

	// transaction fetcher (Mapper) Dao
	private final CustomerDao customerDao;
	private final BranchDao branchDao;
	private final ProductDao productDao;

	public DeliveryService(DeliveryDao deliveryDao, CustomerDeliveryDao customerDeliverDao,
			BranchDeliveryDao branchDeliveryDao, ProductDeliveryDao productDeliveryDao, CustomerDao customerDao,
			BranchDao branchDao, ProductDao productDao, CustomerPaymentDao customerPaymentdao,
			PaymentHistoryDao paymentHistoryDao) {
		this.deliveryDao = deliveryDao;
		this.customerDeliveryDao = customerDeliverDao;
		this.branchDeliveryDao = branchDeliveryDao;
		this.productDeliveryDao = productDeliveryDao;
		this.customerPaymentdao = customerPaymentdao;
		this.paymentHistoryDao = paymentHistoryDao;

		this.customerDao = customerDao;
		this.branchDao = branchDao;
		this.productDao = productDao;
	}

	public void addNewDelivery(NewDelivery newDelivery) {
		TransactionHelper.executeInTransaction(conn -> {
			Delivery delivery = newDelivery.getDelivery();

			int deliveryId = deliveryDao.insert(conn, delivery);

			LocalDateTime deliverySchdule = delivery.getScheduleDate();

			insertCustomerDeliveries(deliveryId, newDelivery.getCustomerBranchDeliveries(), conn, true,
					deliverySchdule);

		});
	}

	// return a map of customerId -> customerDeliveryId
	private Map<Integer, Integer> insertCustomerDeliveries(int deliveryId,
			List<CustomerBranchDelivery> customerBranchDeliveries, Connection conn, boolean isNewDelivery,
			LocalDateTime deliveryDate) throws Exception {

		Map<Integer, Integer> customerIdToCustomerDeliveryId = new HashMap<>();

		for (CustomerBranchDelivery cbd : customerBranchDeliveries) {

			CustomerDelivery cd = cbd.getCustomerDelivery();
			cd.setDeliveryId(deliveryId);

			int customerDeliveryId = customerDeliveryDao.insert(conn, cd, isNewDelivery);

			customerIdToCustomerDeliveryId.put(cd.getCustomerId(), customerDeliveryId);
			insertAllBranchAndProducts(customerDeliveryId, cbd.getBranches(), deliveryDate, conn);
		}

		return customerIdToCustomerDeliveryId;
	}

	private void insertAllBranchAndProducts(int customerDeliveryId, Map<BranchDelivery, List<ProductDelivery>> branches,
			LocalDateTime deliveryDate, Connection conn) throws Exception {
		for (Map.Entry<BranchDelivery, List<ProductDelivery>> entry : branches.entrySet()) {

			BranchDelivery bd = entry.getKey();

			List<ProductDelivery> pd = entry.getValue();

			int branchDeliveryId = branchDeliveryDao.insert(conn, customerDeliveryId, bd);

			productDeliveryDao.insertAll(branchDeliveryId, pd, conn);

		}
	}

	public List<Delivery> fetchNextPage(Long lastSeenId, String search, Date startAt, Date endAt, int pageSize) {
		return deliveryDao.fetchNextPageNewest(lastSeenId, search, startAt, endAt, pageSize + 1); // page size fixed to
																									// 15
	}

	public DeliveryViewResponse getDeliveryDetials(int deliveryId) {
		DeliveryViewResponse response = new DeliveryViewResponse();

		TransactionHelper.executeInTransaction(conn -> {
			assembleDeliveryViewResponse(response, deliveryId, conn);
		});

		return response;
	}

	private void assembleDeliveryViewResponse(DeliveryViewResponse response, int deliveryId, Connection conn)
			throws Exception {

		Delivery delivery = deliveryDao.findById(conn, deliveryId);

		List<CustomerBranchDelivery> customerBranchDeliveries = fetchCustomerDeliveries(deliveryId, conn);

		// fetch customer payment if delivery is 'delivered'
		if (delivery.getStatus().equals("delivered")) {
			List<CustomerPayments> customerPayments = fetchCustomerDeliveryPayments(customerBranchDeliveries, conn);
			response.setCustomerPaymentType(customerPayments);
		}

		Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDeliveries = fetchMappedCustomerDeliveries(
				customerBranchDeliveries, conn);

		response.setCustomerDeliveries(customerBranchDeliveries);
		response.setMappedCustomerDelivery(mappedCustomerDeliveries);
		response.setDelivery(delivery);

	}

	/**
	 * fetches the raw data of the customer deliveries.
	 */
	private List<CustomerBranchDelivery> fetchCustomerDeliveries(int deliveryId, Connection conn) throws Exception {
		List<CustomerBranchDelivery> customerBranchDeliveries = new ArrayList<>();

		List<CustomerDelivery> customerDeliveryList = customerDeliveryDao.findAllByDeliveryId(conn, deliveryId);

		for (CustomerDelivery cd : customerDeliveryList) {
			CustomerBranchDelivery cbd = new CustomerBranchDelivery();
			cbd.setCustomerDelivery(cd);

			Map<BranchDelivery, List<ProductDelivery>> branchesAndProductsOrders = fetchBranchesAndProductsOrders(
					cd.getId(), conn);

			cbd.setBranches(branchesAndProductsOrders);

			customerBranchDeliveries.add(cbd);

		}
		return customerBranchDeliveries;
	}

	private List<CustomerPayments> fetchCustomerDeliveryPayments(List<CustomerBranchDelivery> customerBranchDeliveries,
			Connection conn) throws SQLException {
		List<CustomerPayments> customerPayments = new ArrayList<>();

		for (CustomerBranchDelivery cbd : customerBranchDeliveries) {
			String customerBranchDeliveryStatus = cbd.getCustomerDelivery().getStatus();

			if ("cancelled".equals(customerBranchDeliveryStatus)) {
				continue;
			}

			if ("delivered".equals(customerBranchDeliveryStatus)) {
				CustomerPayments cp = customerPaymentdao.findByCustomerDeliveryId(cbd.getCustomerDelivery().getId(),
						conn);

				customerPayments.add(cp);
			}
		}

		return customerPayments;
	}

	private Map<BranchDelivery, List<ProductDelivery>> fetchBranchesAndProductsOrders(int customerDeliveryId,
			Connection conn) throws Exception {
		Map<BranchDelivery, List<ProductDelivery>> customerBranchesProductsDelivery = new HashMap<>();

		List<BranchDelivery> customerBranchDelivery = fetchCustomerBranches(customerDeliveryId, conn);

		for (BranchDelivery branchDelivery : customerBranchDelivery) {
			List<ProductDelivery> customerBranchProductsOrders = fetchCustomerProductsOrders(branchDelivery.getId(),
					conn);
			customerBranchesProductsDelivery.put(branchDelivery, customerBranchProductsOrders);

		}

		return customerBranchesProductsDelivery;
	}

	private List<BranchDelivery> fetchCustomerBranches(int customerDeliveryId, Connection conn) {
		return branchDeliveryDao.findAllByCustomerDelivery(conn, customerDeliveryId);
	}

	private List<ProductDelivery> fetchCustomerProductsOrders(int branchDeliveryId, Connection conn) throws Exception {
		return productDeliveryDao.fetchProductDeliverisByBranchDeliveryId(branchDeliveryId, conn);
	}

	/**
	 * fetches the mapped data used for ui.
	 */
	private Map<Customer, Map<Branch, List<ProductWithQuantity>>> fetchMappedCustomerDeliveries(
			List<CustomerBranchDelivery> customerBranchDeliveries, Connection conn) {
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDelivery = new HashMap<>();

		for (CustomerBranchDelivery cbd : customerBranchDeliveries) {
			Customer customer = customerDao.findById(cbd.getCustomerDelivery().getCustomerId());

			Map<Branch, List<ProductWithQuantity>> customerBranchOrders = fetchMappedBranchProductsDeliveries(
					cbd.getBranches(), conn);

			mappedCustomerDelivery.put(customer, customerBranchOrders);
		}

		return mappedCustomerDelivery;

	}

	private Map<Branch, List<ProductWithQuantity>> fetchMappedBranchProductsDeliveries(
			Map<BranchDelivery, List<ProductDelivery>> branchesOrders, Connection conn) {
		Map<Branch, List<ProductWithQuantity>> customerOrders = new HashMap<>();

		for (Map.Entry<BranchDelivery, List<ProductDelivery>> entry : branchesOrders.entrySet()) {
			BranchDelivery bd = entry.getKey();
			List<ProductDelivery> productDeliveryList = entry.getValue();

			Branch branch = branchDao.findById(bd.getBranchId(), conn);

			// fetch products
			List<ProductWithQuantity> productWithQuantityList = fetchBranchProductsOrders(productDeliveryList, conn);

			customerOrders.put(branch, productWithQuantityList);

		}

		return customerOrders;

	}

	private List<ProductWithQuantity> fetchBranchProductsOrders(List<ProductDelivery> productDeliveryList,
			Connection conn) {
		List<ProductWithQuantity> productWithQuantityList = new ArrayList<>();
		for (ProductDelivery pd : productDeliveryList) {
			Product product = productDao.findById(pd.getProductId(), conn);

			ProductWithQuantity pwq = new ProductWithQuantity(product, pd.getQuantity());

			productWithQuantityList.add(pwq);

		}

		return productWithQuantityList;
	}

	public void markAsDelivered(Delivery delivery, DeliveryChanges deliveryChanges) {
		TransactionHelper.executeInTransaction(conn -> {

			// Mark delivered and apply changes
			proccessMarkAsDeliveredAndApplyChanges(delivery, conn);
			// Delivery changes and payment types
			processCustomerDeliveryChanges(delivery, deliveryChanges, conn);
		});
	}

	private void processCustomerDeliveryChanges(Delivery delivery, DeliveryChanges deliveryChanges, Connection conn)
			throws Exception {
		int deliveryId = delivery.getId();
		LocalDateTime deliverySchedule = delivery.getScheduleDate();

		Map<Integer, Integer> customerIdToCustomerDeliveryId = new HashMap<>(); // customerId -> customerDelivery (added
																				// customerDelivery)

		// Save customer delivery changes, and status
		CustomerDeliveryChanges customerDeliveryChanges = deliveryChanges.getCustomerDeliveryChanges();
		if (customerDeliveryChanges != null) {
			customerIdToCustomerDeliveryId = proccessCustomerDeliveriesChanges(deliveryId, deliverySchedule,
					customerDeliveryChanges, conn);
		}

		// Save branches updates
		BranchDeliveryChanges branchDeliveryChanges = deliveryChanges.getBranchDeliveryChanges();
		if (branchDeliveryChanges != null) {
			proccessBranchDeliveryChanges(branchDeliveryChanges, conn);
		}

		// Delivery statuses changes
		DeliveryStatusChanges deliveryStatusChanges = deliveryChanges.getDeliveryStatusChanges();
		proccessDeliveryStatusesChanges(deliveryStatusChanges, conn);

		// Product Delivery changes
		ProductDeliveryChanges productDeliveryChanges = deliveryChanges.getProductDeliveryChanges();
		if (productDeliveryChanges != null) {
			proccessProductDeliveryChanges(deliveryId, productDeliveryChanges, conn);
		}

		// proccess payments
		List<CustomerPayments> customerPayments = deliveryChanges.getCustomerPaymentTypes();
		processCustomerDeliveryPayments(customerIdToCustomerDeliveryId, customerPayments, conn);

		// update the last deliver of all the branches that is in this delibery by the
		// schduled date.
		branchDao.updateBranchesLastDeliveryDate(deliveryId, deliverySchedule, conn);
	}

	private void proccessMarkAsDeliveredAndApplyChanges(Delivery delivery, Connection conn) throws SQLException {
		deliveryDao.markDeliveryAsDelivered(delivery, conn);
		// apply changes (expenses, profit, etc)
		deliveryDao.update(delivery, conn);
	}

	// return a map of customerId -> customerDeliveryId
	private Map<Integer, Integer> proccessCustomerDeliveriesChanges(int deliveryId, LocalDateTime deliverySchdule,
			CustomerDeliveryChanges customerDeliveryChanges, Connection conn) throws Exception {
		List<CustomerBranchDelivery> addedDeliveries = customerDeliveryChanges.getAddedDelivery();
		Map<Integer, Integer> customerIdToCustomerDeliveryId = new HashMap<>();

		LocalDateTime DeliveryDate = LocalDate.now().atStartOfDay();

		// check for new deliveries added
		if (addedDeliveries.size() > 0) {
			customerIdToCustomerDeliveryId = insertCustomerDeliveries(deliveryId, addedDeliveries, conn, false,
					DeliveryDate);

		}
		return customerIdToCustomerDeliveryId;
	}

	private void proccessBranchDeliveryChanges(BranchDeliveryChanges branchDeliveryChanges, Connection conn)
			throws Exception {

		Map<BranchDelivery, List<ProductDelivery>> newBranchesDelivery = branchDeliveryChanges.getNewBranchesDelivery();

		// check for new branch delivery
		if (newBranchesDelivery.size() > 0) {
			for (Map.Entry<BranchDelivery, List<ProductDelivery>> entry : newBranchesDelivery.entrySet()) {
				BranchDelivery bd = entry.getKey();
				List<ProductDelivery> productDeliveryList = entry.getValue();

				int branchDeliveryId = branchDeliveryDao.insert(conn, bd.getCustomerDeliveryId(), bd);

				productDeliveryDao.insertAll(branchDeliveryId, productDeliveryList, conn);
			}
		}

	}

	private void proccessDeliveryStatusesChanges(DeliveryStatusChanges deliveryStatusChanges, Connection conn)
			throws SQLException {
		Map<Integer, String> customerDeliveryStatuses = deliveryStatusChanges.getCustomerDeliveryStatuses();
		customerDeliveryDao.setStatusesBatch(customerDeliveryStatuses, conn);

		Map<Integer, String> branchDeliveryStatuses = deliveryStatusChanges.getBranchDeliveryStatuses();
		branchDeliveryDao.setBranchDeliverStatusBatch(branchDeliveryStatuses, conn);

	}

	private void proccessProductDeliveryChanges(int deliveryId, ProductDeliveryChanges productDeliveryChanges,
			Connection conn) throws Exception {
		// new products deliveries
		List<ProductDelivery> addedProductDeliveries = productDeliveryChanges.getAddedProductDelivery();
		if (addedProductDeliveries != null && addedProductDeliveries.size() > 0) {
			productDeliveryDao.insertAll(addedProductDeliveries, conn);
		}

		// updated quantities
		Map<Integer, Integer> updatedProductDeliveryQuantity = productDeliveryChanges
				.getUpdatedProductDeliveryQuantity();
		if (updatedProductDeliveryQuantity != null && updatedProductDeliveryQuantity.size() > 0) {
			productDeliveryDao.updateQuantitiesBatch(updatedProductDeliveryQuantity, conn);
		}

		List<Integer> removedProductsIds = productDeliveryChanges.getRemovedProductDeliveryIds();
		if (removedProductsIds != null && removedProductsIds.size() > 0) {
			productDeliveryDao.dropBatch(removedProductsIds, conn);
		}
	}

	private void processCustomerDeliveryPayments(Map<Integer, Integer> customerIdToCustomerDeliveryId,
			List<CustomerPayments> customerPayments, Connection conn) throws SQLException {

		for (CustomerPayments cp : customerPayments) {
			boolean isCustomerNewlyAdded = customerIdToCustomerDeliveryId.containsKey(cp.getCustomerId());

			// if this is the new customer delivery, we initialize the customerDeliveryId
			if (isCustomerNewlyAdded) {
				cp.setCustomerDeliveryId(customerIdToCustomerDeliveryId.get(cp.getCustomerId()));
			}

			int insertedCustomerPaymentId = customerPaymentdao.insert(cp, conn);

			// check if it is: paid cheque, paid cash, partial, we create a payment history
			// for this type of payment.
			boolean isNeedToCreatePayment = List.of("Partial", "Paid Cheque", "Paid Cash")
					.contains(cp.getPaymentType());

			if (isNeedToCreatePayment) {
				// paid cheque/cash fill create a payment that is equal to the total
				// payment(fully paid)
				// partial will get the set the partials payment as the first payment history

				boolean isFullyPaid = List.of("Paid Cheque", "Paid Cash").contains(cp.getPaymentType());

				double amount = isFullyPaid ? cp.getTotal() : cp.getTotalPayment();

				PaymentHistory ph = new PaymentHistory(insertedCustomerPaymentId, amount);

				paymentHistoryDao.insert(ph, conn);
			}

		}

	}

	public void deleteDelivery(int deliveryId) {
		deliveryDao.deleteDelivery(deliveryId);
	}

}
