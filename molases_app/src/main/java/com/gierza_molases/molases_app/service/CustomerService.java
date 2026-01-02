package com.gierza_molases.molases_app.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.gierza_molases.molases_app.dao.BranchDao;
import com.gierza_molases.molases_app.dao.CustomerDao;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.util.DatabaseUtil;

public class CustomerService {

	private final CustomerDao customerDao = new CustomerDao();
	private final BranchDao branchDao = new BranchDao();

	public void addCustomerAsIndividualType(String firstName, String midName, String lastName, String contactNumber,
			String address, List<Branch> branches) {

		Customer customer = Customer.newIndividual(firstName, midName, lastName, contactNumber, address);

		// validate input
		customer.validate();

		try (Connection conn = DatabaseUtil.getConnection()) {

			conn.setAutoCommit(false); // BEGIN TRANSACTION

			try {
				int insertedCustomerId = customerDao.insertAsIndividual(customer, conn);
				branchDao.insertBranches(insertedCustomerId, branches, conn);

				conn.commit(); // COMMIT
			} catch (Exception e) {
				conn.rollback(); // ROLLBACK
				throw new RuntimeException("Failed to create customer", e);
			}

		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}

	}

	public void addCustomerAsCompanyType(String companyName, String contactNumber, String address) {
		Customer customer = Customer.newCompany(companyName, contactNumber, address);

		customer.validate();

		customerDao.insertAsCompany(customer);
	}

	public List<Customer> fetchAll(int page, int pageSize, String search, String sortOrder) {
		return customerDao.selectAll(page, pageSize, search, sortOrder);
	}

	public int getTotalCustomerCount(String search) {
		return customerDao.getTotalCount(search);
	}

	public void updateIndividual(int id, String firstName, String midName, String lastName, String contactNumber,
			String address) {
		Customer customer = new Customer(id, "individual", firstName, midName, lastName, null, contactNumber, address,
				null);

		customer.validate();

		System.out.println("Updating customer(individual: )" + customer.getId());
		customerDao.updateIndividual(customer);

	}

	public void updateCompany(int id, String companyName, String contactNumber, String address) {
		Customer customer = new Customer(id, "company", null, null, null, companyName, contactNumber, address, null);

		customer.validate();

		customerDao.updateCompany(customer);

	}

	public void delete(int id) {
		customerDao.deleteById(id);
	}

}
