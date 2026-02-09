package com.gierza_molases.molases_app.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.gierza_molases.molases_app.dao.CustomerPaymentDao;
import com.gierza_molases.molases_app.model.CustomerPayments;

public class CustomerPaymentsService {
	private final CustomerPaymentDao customerPaymentDao;

	public CustomerPaymentsService(CustomerPaymentDao customerPaymentDao) {
		this.customerPaymentDao = customerPaymentDao;
	}

	public List<CustomerPayments> fetchPaymentsCursor(Long lastSeenPaymentId, String search, String paymentType,
			String status, LocalDateTime fromDate, LocalDateTime toDate, int pageSize) {
		return customerPaymentDao.fetchNextPage(lastSeenPaymentId, search, paymentType, status, fromDate, toDate,
				pageSize + 1);
	}

	public CustomerPayments findById(int id) throws SQLException {
		return customerPaymentDao.findById(id);
	}

}
