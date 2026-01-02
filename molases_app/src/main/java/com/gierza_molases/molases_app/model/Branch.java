package com.gierza_molases.molases_app.model;

public class Branch {

	private int id;
	private int customerId;
	private String address;
	private String note;
	private String createdAt;

	// No-args constructor
	public Branch() {
	}

	public Branch(String address, String note) {
		this.address = address;
		this.note = note;
	}

	// Constructor without id (for INSERT)
	public Branch(int customerId, String address, String note) {
		this.customerId = customerId;
		this.address = address;
		this.note = note;
	}

	// Full constructor (for SELECT)
	public Branch(int id, int customerId, String address, String note, String createdAt) {
		this.id = id;
		this.customerId = customerId;
		this.address = address;
		this.note = note;
		this.createdAt = createdAt;
	}

	// Getters and setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "Branch{" + "id=" + id + ", customerId=" + customerId + ", address='" + address + '\'' + ", note='"
				+ note + '\'' + ", createdAt='" + createdAt + '\'' + '}';
	}
}
