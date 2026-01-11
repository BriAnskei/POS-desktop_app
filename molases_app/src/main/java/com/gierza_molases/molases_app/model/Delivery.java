package com.gierza_molases.molases_app.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Delivery {

	private Integer id;
	private LocalDateTime scheduleDate;
	private String name;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	// Stored as JSON in DB, exposed as Map in model
	private Map<String, Double> expenses;

	private String status; // scheduled, delivered, cancelled
	private Double overAllCapital;
	private Double overAllProfit;

	private LocalDateTime createdAt;

	// Derived field
	private int totalCustomers;
	private int totalBranches;

	/*
	 * ========================= Constructors =========================
	 */

	// create
	public Delivery(Integer id, LocalDateTime scheduleDate, String name, Map<String, Double> expenses, String status,
			Double overAllProfit, Double overAllCapital, LocalDateTime createdAt) {
		this(id, scheduleDate, name, expenses, status, overAllProfit, overAllCapital, createdAt, 0, 0);
	}

	// fetch
	public Delivery(Integer id, LocalDateTime scheduleDate, String name, Map<String, Double> expenses, String status,
			double overAllProfit, Double overAllCapital, LocalDateTime createdAt, int totalCustomers,
			int totalBranches) {
		this.id = id;
		this.scheduleDate = scheduleDate;
		this.name = name;
		this.expenses = expenses != null ? new HashMap<>(expenses) : new HashMap<>();
		this.status = status;
		this.overAllProfit = overAllProfit;
		this.overAllCapital = overAllCapital;
		this.createdAt = createdAt;
		this.totalCustomers = totalCustomers;
		this.totalBranches = totalBranches;

		validate();
	}

	/*
	 * ========================= JSON helpers (DAO-facing) =========================
	 */

	public String getExpensesAsJson() {
		try {
			return expenses == null || expenses.isEmpty() ? null : MAPPER.writeValueAsString(expenses);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to serialize expenses", e);
		}
	}

	/** Convert JSON -> Map (read from DB) */
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

	private void validate() {

		if (createdAt == null) {
			throw new IllegalArgumentException("Created date must not be null");
		}

		if (status == null || status.isBlank()) {
			throw new IllegalArgumentException("Status must not be blank");
		}
//
//		if (!status.equals("scheduled") && !status.equals("delivered") && !status.equals("cancelled")) {
//			throw new IllegalArgumentException("Invalid status: " + status);
//		}

		if (overAllProfit != null && overAllProfit < 0) {
			throw new IllegalArgumentException("Overall profit cannot be negative");
		}

		if (overAllCapital != null && overAllCapital < 0) {
			throw new IllegalArgumentException("Overall capital cannot be negative");
		}

		if (totalCustomers < 0) {
			throw new IllegalArgumentException("Total customers cannot be negative");
		}

		if (expenses != null) {
			for (Map.Entry<String, Double> entry : expenses.entrySet()) {

				if (entry.getKey() == null || entry.getKey().isBlank()) {
					throw new IllegalArgumentException("Expense key must not be blank");
				}

				if (entry.getValue() == null || entry.getValue() < 0) {
					throw new IllegalArgumentException("Expense value must be non-negative: " + entry.getKey());
				}
			}
		}
	}

	public double getTotalExpenses() {
		if (expenses == null)
			return 0;
		return expenses.values().stream().mapToDouble(Double::doubleValue).sum();
	}

	/*
	 * ========================= Getters / setters =========================
	 */

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
		this.expenses = new HashMap<>(expenses);
		validate();
	}

	public String getStatus() {
		return status;
	}

	public Double getOverAllProfit() {
		return overAllProfit;
	}

	public Double getOverAllCapital() {
		return overAllCapital;
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
		return "Delivery{" + "id=" + id + ", name='" + name + '\'' + ", expenses=" + expenses + ", totalCustomers="
				+ totalCustomers + '}';
	}
}
