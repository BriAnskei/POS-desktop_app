package com.gierza_molases.molases_app.service;

import java.util.Date;
import java.util.List;

import com.gierza_molases.molases_app.dao.CustomerPaymentDao;
import com.gierza_molases.molases_app.model.CustomerPayments;

public class CustomerPaymentsService {
	private final CustomerPaymentDao customerPaymentDao;

	public CustomerPaymentsService(CustomerPaymentDao customerPaymentDao) {
		this.customerPaymentDao = customerPaymentDao;
	}

	public List<CustomerPayments> fetchPayamentsCursor(Long lastSeenPaymentId, String search, String paymentType,
			String status, Date fromDate, Date toDate, int pageSize) {
		return customerPaymentDao.fetchNextPage(lastSeenPaymentId, search, paymentType, status, fromDate, toDate,
				pageSize + 1);
	}

}
