package com.gierza_molases.molases_app.service;

import com.gierza_molases.molases_app.dao.CustomerPaymentDao;

public class CustomerPaymentsService {
	private final CustomerPaymentDao customerPaymentDao;

	public CustomerPaymentsService(CustomerPaymentDao customerPaymentDao) {
		this.customerPaymentDao = customerPaymentDao;
	}

}
