package com.gierza_molases.molases_app.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Delivery {

	private Integer id;
	private LocalDateTime scheduleDate;
	private String name;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	// Stored as JSON in DB
	private Map<String, Double> expenses;

	private String status; // scheduled, delivered, cancelled

	private Double totalGross;
	private Double totalCapital;
	private Double grossProfit;
	private Double netProfit;

	private LocalDateTime createdAt;

	// Derived / aggregated
	private int totalCustomers;
	private int totalBranches;

	private static final Set<String> VALID_STATUSES = Set.of("scheduled", "delivered", "cancelled");

	/* ========================= Constructors ========================= */

	// create
	public Delivery(LocalDateTime scheduleDate, String name, Map<String, Double> expenses, Double totalGross,
			Double totalCapital, Double grossProfit, Double netProfit) {
		this(null, scheduleDate, name, expenses, "scheduled", totalGross, totalCapital, grossProfit, netProfit, null, 0,
				0);
	}

	// fetch
	public Delivery(Integer id, LocalDateTime scheduleDate, String name, Map<String, Double> expenses, String status,
			Double totalGross, Double totalCapital, Double grossProfit, Double netProfit, LocalDateTime createdAt,
			int totalCustomers, int totalBranches) {
		this.id = id;
		this.scheduleDate = scheduleDate;
		this.name = name;
		this.expenses = expenses != null ? new HashMap<>(expenses) : new HashMap<>();
		this.status = status != null ? status.toLowerCase() : null;

		this.totalGross = totalGross;
		this.totalCapital = totalCapital;
		this.grossProfit = grossProfit;
		this.netProfit = netProfit;

		this.createdAt = createdAt;
		this.totalCustomers = totalCustomers;
		this.totalBranches = totalBranches;

		validate();
	}

	/* ========================= JSON helpers ========================= */

	public String getExpensesAsJson() {
		try {
			return expenses == null || expenses.isEmpty() ? null : MAPPER.writeValueAsString(expenses);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to serialize expenses", e);
		}
	}

	public static Map<String, Double> parseExpensesJson(String json) {
		try {
			if (json == null || json.isBlank()) {
				return new HashMap<>();
			}
			return MAPPER.readValue(json, new TypeReference<Map<String, Double>>() {
			});
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid expenses JSON", e);
		}
	}

	/* ========================= Validation ========================= */

	private void validate() {

		if (scheduleDate == null) {
			throw new IllegalArgumentException("Schedule date must not be null");
		}

		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Delivery name must not be blank");
		}

		if (status == null || !VALID_STATUSES.contains(status)) {
			throw new IllegalArgumentException("Invalid status: " + status);
		}

		if (totalGross != null && totalGross < 0) {
			throw new IllegalArgumentException("Total gross cannot be negative");
		}

		if (totalCapital != null && totalCapital < 0) {
			throw new IllegalArgumentException("Total capital cannot be negative");
		}

		if (grossProfit != null && grossProfit < 0) {
			throw new IllegalArgumentException("Gross profit cannot be negative");
		}

		if (netProfit != null && netProfit < 0) {
			throw new IllegalArgumentException("Net profit cannot be negative");
		}

		if (totalCustomers < 0) {
			throw new IllegalArgumentException("Total customers cannot be negative");
		}

		if (totalBranches < 0) {
			throw new IllegalArgumentException("Total branches cannot be negative");
		}

		if (expenses != null) {
			for (var entry : expenses.entrySet()) {
				if (entry.getKey() == null || entry.getKey().isBlank()) {
					throw new IllegalArgumentException("Expense key must not be blank");
				}
				if (entry.getValue() == null || entry.getValue() < 0) {
					throw new IllegalArgumentException("Expense value must be non-negative: " + entry.getKey());
				}
			}
		}

		// Only validate createdAt for fetched entities
		if (id != null && id > 0 && createdAt == null) {
			throw new IllegalArgumentException("Created date must not be null for persisted delivery");
		}
	}

	/* ========================= Derived getters ========================= */

	public double getTotalExpenses() {
		return expenses == null ? 0 : expenses.values().stream().mapToDouble(Double::doubleValue).sum();
	}

	/* ========================= Getters / setters ========================= */

	public Integer getId() {
		return id;
	}

	public LocalDateTime getScheduleDate() {
		return scheduleDate;
	}

	public String getName() {
		return name;
	}

	public Map<String, Double> getExpenses() {
		return expenses == null ? Collections.emptyMap() : Collections.unmodifiableMap(expenses);
	}

	public void setExpenses(Map<String, Double> expenses) {
		this.expenses = expenses != null ? new HashMap<>(expenses) : new HashMap<>();
		validate();
	}

	public String getStatus() {
		return status;
	}

	public Double getTotalGross() {
		return totalGross;
	}

	public Double getTotalCapital() {
		return totalCapital;
	}

	public Double getGrossProfit() {
		return grossProfit;
	}

	public Double getNetProfit() {
		return netProfit;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public int getTotalCustomers() {
		return totalCustomers;
	}

	public int getTotalBranches() {
		return totalBranches;
	}

	public void setTotalCustomers(int totalCustomers) {
		this.totalCustomers = totalCustomers;
		validate();
	}

	@Override
	public String toString() {
		return "Delivery{" + "id=" + id + ", name='" + name + '\'' + ", status='" + status + '\'' + ", totalCustomers="
				+ totalCustomers + ", totalExpenses=" + getTotalExpenses() + '}';
	}
}
