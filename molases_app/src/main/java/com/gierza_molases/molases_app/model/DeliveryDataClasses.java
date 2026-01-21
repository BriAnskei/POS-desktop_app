package com.gierza_molases.molases_app.model;

import java.util.List;
import java.util.Map;

/**
 * Data classes for delivery operations
 */
public class DeliveryDataClasses {
    
    public static class CustomerDeliveryData {
        public String paymentType;
        public Map<Branch, BranchDeliveryData> branches;
    }

    public static class BranchDeliveryData {
        public String status;
        public List<ProductWithQuantity> products;
    }

    public static class CustomerTotals {
        public double sales = 0.0;
        public double capital = 0.0;
        public double profit = 0.0;
    }

    public static class FinancialTotals {
        public double grossSales = 0.0;
        public double totalCapital = 0.0;
        public double grossProfit = 0.0;
        public double totalExpenses = 0.0;
        public double netProfit = 0.0;
    }
}