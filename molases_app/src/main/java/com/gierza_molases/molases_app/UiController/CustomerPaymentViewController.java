package com.gierza_molases.molases_app.UiController;

import com.gierza_molases.molases_app.context.CustomerPaymentViewState;
import com.gierza_molases.molases_app.service.CustomerPaymentsService;

public class CustomerPaymentViewController {
	private final CustomerPaymentsService customerPaymentService;
	private final CustomerPaymentViewState customerPaymentViewState;

	public CustomerPaymentViewController(CustomerPaymentViewState customerPaymentViewState,
			CustomerPaymentsService customerPaymentService) {
		this.customerPaymentViewState = customerPaymentViewState;
		this.customerPaymentService = customerPaymentService;

	}

}
