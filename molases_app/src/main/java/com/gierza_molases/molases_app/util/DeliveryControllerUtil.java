package com.gierza_molases.molases_app.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

import com.gierza_molases.molases_app.model.Delivery;

public class DeliveryControllerUtil {
	public static Delivery buildDelivery(Date deliveryDate, String deliveryName, Map<String, Double> expenses,
			double totalGross, double totalCapital, double grossProfit, double netProfit, int totalCustomers,
			int totalBranches) {

		LocalDateTime scheduleDate = deliveryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		return new Delivery(scheduleDate, deliveryName, expenses, totalCustomers, totalBranches, totalGross,
				totalCapital, grossProfit, netProfit);
	}

//	public static Map<CustomerDelivery, List<BranchDelivery>> buildCustomerDeliveryMap(
//			Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliveries) {
//
//		Map<CustomerDelivery, List<BranchDelivery>> result = new HashMap<>();
//
//		for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> customerEntry : customerDeliveries
//				.entrySet()) {
//
//			Customer customer = customerEntry.getKey();
//			Map<Branch, List<ProductWithQuantity>> branches = customerEntry.getValue();
//
//			CustomerDelivery customerDelivery = new CustomerDelivery(customer.getId(), 0 // deliveryId to be set later
//			);
//
//			List<BranchDelivery> branchDeliveries = new ArrayList<>();
//
//			for (Map.Entry<Branch, List<ProductWithQuantity>> branchEntry : branches.entrySet()) {
//				Branch branch = branchEntry.getKey();
//
//				for (ProductWithQuantity productWithQty : branchEntry.getValue()) {
//					branchDeliveries.add(new BranchDelivery(0, // customerDeliveryId to be set later
//							branch.getId(), productWithQty.getProduct().getId(), productWithQty.getQuantity(),
//							"scheduled"));
//				}
//			}
//
//			result.put(customerDelivery, branchDeliveries);
//		}
//
//		return result;
//	}

}
