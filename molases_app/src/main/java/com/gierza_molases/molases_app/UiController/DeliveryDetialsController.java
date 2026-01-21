package com.gierza_molases.molases_app.UiController;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.context.DeliveryDetialsState;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.service.DeliveryService;

public class DeliveryDetialsController {

	private final DeliveryDetialsState state;

	private final DeliveryService deliveryService;

	public DeliveryDetialsController(DeliveryDetialsState state, DeliveryService deliveryService) {
		this.state = state;
		this.deliveryService = deliveryService;
	}

	public DeliveryDetialsState getState() {
		return state;
	}

	public void loadDeliveryData(int deliveryId, Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {

				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();

					if (onError != null)
						onError.accept(error.getMessage());
				}
			}
		}.execute();
	}

	// customer deliveries function
	public void addAdditionalCustomer(Customer customer, Map<Branch, List<ProductWithQuantity>> branchProducts,
			Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {

				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();

					if (onError != null)
						onError.accept(error.getMessage());
				}
			}
		}.execute();
	}

	// expenses
	public void addExpenses() {

	}

	public void removeExpenses() {

	}

}
