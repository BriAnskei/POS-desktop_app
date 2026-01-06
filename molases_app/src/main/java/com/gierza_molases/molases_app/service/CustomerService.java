package com.gierza_molases.molases_app.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.gierza_molases.molases_app.dao.BranchDao;
import com.gierza_molases.molases_app.dao.CustomerDao;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.util.Database;

public class CustomerService {

	private final CustomerDao customerDao;
	private final BranchDao branchDao;

	public CustomerService(CustomerDao customerDao, BranchDao branchDao) {
		this.customerDao = customerDao;
		this.branchDao = branchDao;
	}

	public void addCustomerAsIndividualType(String firstName, String midName, String lastName, String contactNumber,
			String address, List<Branch> branches) {

		Customer customer = Customer.newIndividual(firstName, midName, lastName, contactNumber, address);

		customer.validate();

		Connection conn = Database.init();
		boolean originalAutoCommit = false;

		try {
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			int customerId = customerDao.insertAsIndividual(customer, conn);

			branchDao.insertBranches(customerId, branches, conn);

			conn.commit();

		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				throw new RuntimeException("Rollback failed", ex);
			}
			throw new RuntimeException("Failed to create customer", e);

		} finally {
			try {
				conn.setAutoCommit(originalAutoCommit);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void addCustomerAsCompanyType(String companyName, String contactNumber, String address,
			List<Branch> branches) {
		Customer customer = Customer.newCompany(companyName, contactNumber, address);

		customer.validate();

		Connection conn = Database.init();
		boolean originalAutoCommit = false;

		try {
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false); // BEGIN TRANSACTION

			int insertedCustomerId = customerDao.insertAsCompany(customer, conn);

			branchDao.insertBranches(insertedCustomerId, branches, conn);

			conn.commit(); // COMMIT
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				throw new RuntimeException("Rollback failed", ex);
			}
			throw new RuntimeException("Failed to create customer", e);
		} finally {
			try {
				conn.setAutoCommit(originalAutoCommit);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

	}

	public List<Customer> fetchAll(int page, int pageSize, String search, String sortOrder) {
		return customerDao.selectAll(page, pageSize, search, sortOrder);
	}

	public List<Customer> fetch20Customer(String search) {
		return customerDao.fetch20Customer(search);
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
