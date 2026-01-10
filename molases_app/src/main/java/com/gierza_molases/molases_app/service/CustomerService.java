package com.gierza_molases.molases_app.service;

import java.util.List;

import com.gierza_molases.molases_app.dao.BranchDao;
import com.gierza_molases.molases_app.dao.CustomerDao;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.util.TransactionHelper;

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

		TransactionHelper.executeInTransaction(conn -> {
			int customerId = customerDao.insertAsIndividual(customer, conn);
			branchDao.insertBranches(customerId, branches, conn);
		});
	}

	public void addCustomerAsCompanyType(String companyName, String contactNumber, String address,
			List<Branch> branches) {

		Customer customer = Customer.newCompany(companyName, contactNumber, address);
		customer.validate();

		TransactionHelper.executeInTransaction(conn -> {
			int customerId = customerDao.insertAsCompany(customer, conn);
			branchDao.insertBranches(customerId, branches, conn);
		});
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
