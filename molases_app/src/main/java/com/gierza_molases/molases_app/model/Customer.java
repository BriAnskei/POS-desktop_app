package com.gierza_molases.molases_app.model;

import java.time.LocalDateTime;

import com.gierza_molases.molases_app.util.NumberValidation;

public class Customer {

	private int id;

	// Individual fields
	private String firstName;
	private String midName;
	private String lastName;

	// Company field
	private String companyName;

	// Common fields
	private String type; // "individual" or "company"
	private String contactNumber;
	private String address;
	private LocalDateTime createdAt;

	// validation object
	private NumberValidation numberValidation = new NumberValidation();

	/*
	 * ===================== Constructors =====================
	 */

	// Constructor for NEW Individual customer
	public static Customer newIndividual(String firstName, String midName, String lastName, String contactNumber,
			String address) {
		return new Customer(0, "individual", firstName, midName, lastName, null, contactNumber, address, null);
	}

	// Constructor for NEW Company customer
	public static Customer newCompany(String companyName, String contactNumber, String address) {
		return new Customer(0, "company", null, null, null, companyName, contactNumber, address, null);
	}

	// Full constructor (used when reading from DB)
	public Customer(int id, String type, String firstName, String midName, String lastName, String companyName,
			String contactNumber, String address, LocalDateTime createdAt) {
		this.id = id;
		this.type = type;
		this.firstName = firstName;
		this.midName = midName;
		this.lastName = lastName;
		this.companyName = companyName;
		this.contactNumber = contactNumber;
		this.address = address;
		this.createdAt = createdAt;
	}

	/*
	 * ===================== Helper Methods =====================
	 */
	public boolean isIndividual() {
		return "individual".equalsIgnoreCase(type);
	}

	public boolean isCompany() {
		return "company".equalsIgnoreCase(type);
	}

	/**
	 * Validate before saving to DB (matches your SQLite CHECK constraint)
	 */
	public void validate() {

		// inputed number
		if (this.getContactNumber().length() < 11 || this.getContactNumber().length() > 11
				|| !numberValidation.isNumeric(this.getContactNumber())) {
			throw new IllegalStateException("Invalid contact number input, please double check");
		}

		if (isIndividual()) {
			if (firstName == null || lastName == null) {
				throw new IllegalStateException("Individual customer requires first and last name");
			}
		} else if (isCompany()) {
			if (companyName == null) {
				throw new IllegalStateException("Company customer requires company name");
			}
		} else {
			throw new IllegalStateException("Invalid customer type");
		}

		if (address.length() > 255) {
			throw new IllegalStateException("Customer address is too long");
		}
	}

	/*
	 * ===================== Getters =====================
	 */

	public int getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getMidName() {
		return midName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getDisplayName() {
		if (this.isCompany()) {
			return this.companyName;
		}

		// for individual type
		return this.getFirstName() + " " + this.getMidName() + " " + this.getLastName();

	}

	public String getContactNumber() {
		return contactNumber;
	}

	public String getFormatttedNumber() {
		// remove non-digits just in case
		String number = this.getContactNumber().replaceAll("\\D", "");

		return number.substring(0, 4) + "-" + number.substring(4, 7) + "-" + number.substring(7);
	}

	public String getAddress() {
		return address;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/*
	 * ===================== Setters =====================
	 */

	public void setType(String type) {
		this.type = type;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setMidName(String midName) {
		this.midName = midName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Customer customer = (Customer) o;
		return id == customer.id;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(id);
	}
}
