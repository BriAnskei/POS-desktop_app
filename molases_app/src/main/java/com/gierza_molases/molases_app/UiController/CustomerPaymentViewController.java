package com.gierza_molases.molases_app.UiController;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.context.CustomerPaymentViewState;
import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.model.PaymentHistory;
import com.gierza_molases.molases_app.service.CustomerPaymentsService;

public class CustomerPaymentViewController {
	private final CustomerPaymentsService customerPaymentService;
	private final CustomerPaymentViewState state;

	public CustomerPaymentViewController(CustomerPaymentViewState state,
			CustomerPaymentsService customerPaymentService) {
		this.state = state;
		this.customerPaymentService = customerPaymentService;
	}

	public CustomerPaymentViewState getState() {
		return state;
	}

	public void setCustomerPayment(CustomerPayments customerPayment) {
		state.setCustomerPayment(customerPayment);
		// Clear payment history when setting a new customer payment
		state.clearPaymentHistory();
	}

	/**
	 * Add a new payment to the payment history
	 *
	 * @param amount      The payment amount
	 * @param paymentDate The payment date
	 * @param onSuccess   Callback on successful addition
	 * @param onError     Callback on error with error message
	 */
	public void addPayment(double amount, Runnable onSuccess, Consumer<String> onError) {
		CustomerPayments payment = state.getCustomerPayment();
		if (payment == null) {
			if (onError != null) {
				onError.accept("No payment data available");
			}
			return;
		}

		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					// Add to payment history
					PaymentHistory newPaymentHistory = new PaymentHistory(payment.getId(), amount);
					customerPaymentService.addPaymentAmount(newPaymentHistory);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					if (onError != null) {
						onError.accept("Failed to add payment: " + error.getMessage());
					}
				} else {
					// Reload latest payment + history to keep state in sync
					refreshPaymentData(payment.getId(), onSuccess, onError);
				}
			}
		}.execute();
	}

	/**
	 * Load payment history from the database for the current customer payment
	 *
	 * @param onSuccess Callback on successful load
	 * @param onError   Callback on error with error message
	 */
	public void loadPaymentHistory(Runnable onSuccess, Consumer<String> onError) {
		CustomerPayments payment = state.getCustomerPayment();
		if (payment == null) {
			if (onError != null) {
				onError.accept("No payment data available");
			}
			return;
		}

		new SwingWorker<List<PaymentHistory>, Void>() {

			private Exception error; // store exception if occurs

			@Override
			protected List<PaymentHistory> doInBackground() {
				try {
					return customerPaymentService.getPaymentHistory(payment.getId());
				} catch (Exception e) {
					error = e;
					return null; // return null if error occurs
				}
			}

			@Override
			protected void done() {
				if (error != null) {
					if (onError != null) {
						onError.accept("Failed to load payment history: " + error.getMessage());
					}
					return;
				}

				try {
					List<PaymentHistory> history = get(); // will be null if exception occurred
					state.setPaymentHistory(history);
					if (onSuccess != null) {
						onSuccess.run();
					}
				} catch (Exception e) {
					if (onError != null) {
						onError.accept("Failed to load payment history: " + e.getMessage());
					}
				}
			}
		}.execute();
	}

	/**
	 * Update payment history amount
	 *
	 * @param paymentHistoryId The payment history ID to update
	 * @param newAmount        The new payment amount
	 * @param onSuccess        Callback on successful update
	 * @param onError          Callback on error with error message
	 */
	public void updatePaymentHistoryAmount(int paymentHistoryId, double newAmount, Runnable onSuccess,
			Consumer<String> onError) {
		CustomerPayments payment = state.getCustomerPayment();
		if (payment == null) {
			if (onError != null) {
				onError.accept("No payment data available");
			}
			return;
		}

		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					// TODO: Backend implementation - update payment history amount in database
					// customerPaymentService.updatePaymentHistoryAmount(paymentHistoryId,
					// newAmount);

					// Placeholder for now
					System.out.println("Updating payment history ID " + paymentHistoryId + " to amount: " + newAmount);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					if (onError != null) {
						onError.accept("Failed to update payment amount: " + error.getMessage());
					}
				} else {
					// Reload latest payment + history to keep state in sync
					refreshPaymentData(payment.getId(), onSuccess, onError);
				}
			}
		}.execute();
	}

	/**
	 * Reload latest customer payment and payment history from database and update
	 * the state.
	 */
	private void refreshPaymentData(int customerPaymentId, Runnable onSuccess, Consumer<String> onError) {

		new SwingWorker<Void, Void>() {
			private Exception error;
			private CustomerPayments updatedPayment;
			private List<PaymentHistory> updatedHistory;

			@Override
			protected Void doInBackground() {
				try {
					updatedPayment = customerPaymentService.findById(customerPaymentId);
					updatedHistory = customerPaymentService.getPaymentHistory(customerPaymentId);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					if (onError != null) {
						onError.accept("Failed to refresh payment data: " + error.getMessage());
					}
					return;
				}

				state.setCustomerPayment(updatedPayment);
				state.setPaymentHistory(updatedHistory);

				if (onSuccess != null) {
					onSuccess.run();
				}
			}
		}.execute();
	}

	/**
	 * Update payment status (Pending → Complete) Only allowed for Loan payment type
	 *
	 * @param newStatus The new status (should be "Complete")7
	 * @param onSuccess Callback on successful update
	 * @param onError   Callback on error with error message
	 */
	public void updatePaymentStatus(String newStatus, Runnable onSuccess, Consumer<String> onError) {
		CustomerPayments payment = state.getCustomerPayment();
		if (payment == null) {
			onError.accept("No payment data available");
			return;
		}

		// Validate: Only allow status update for Loan payment type
		if (!"Loan".equals(payment.getPaymentType())) {
			onError.accept("Status can only be updated for Loan payment type");
			return;
		}

		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {

				int customerPaymentId = payment.getId();

				if ("pending".equals(newStatus.toLowerCase())) {
					customerPaymentService.updatePaymentStatusToPending(customerPaymentId);
				} else if ("complete".equals(newStatus.toLowerCase())) {
					customerPaymentService.updatePaymentStatusToComplete(customerPaymentId);
				}

				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					if (onError != null)
						onError.accept("Failed to update payment status: " + error.getMessage());
				} else {
					refreshPaymentData(payment.getId(), onSuccess, onError);
					if (onSuccess != null)
						onSuccess.run();
				}
			}
		}.execute();
	}

	/**
	 * Update payment type with partial payment amount (Loan → Partial)
	 *
	 * @param newType       The new payment type (should be "Partial")
	 * @param partialAmount The partial payment amount to add
	 * @param onSuccess     Callback on successful update
	 * @param onError       Callback on error with error message
	 */
	public void updatePaymentTypeWithPartialPayment(String newType, double partialAmount, Runnable onSuccess,
			Consumer<String> onError) {

		CustomerPayments payment = state.getCustomerPayment();
		if (payment == null) {
			onError.accept("No payment data available");
			return;
		}

		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					int customerPaymentId = payment.getId();

					customerPaymentService.updateFromLoanToPartial(customerPaymentId, partialAmount);

				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					System.err.print("Failed " + error.getMessage());
					if (onError != null)
						onError.accept("Failed to update payment type: " + error.getMessage());
				} else {
					refreshPaymentData(payment.getId(), onSuccess, onError);
					if (onSuccess != null)
						onSuccess.run();
				}
			}
		}.execute();
	}

	/**
	 * Update payment type with promise to pay date (Partial → Loan)
	 *
	 * @param newType     The new payment type (should be "Loan")
	 * @param promiseDate The promise to pay date
	 * @param onSuccess   Callback on successful update
	 * @param onError     Callback on error with error message
	 */
	public void updatePaymentTypeWithPromiseToPay(String newType, Date promiseDate, Runnable onSuccess,
			Consumer<String> onError) {

		CustomerPayments payment = state.getCustomerPayment();
		if (payment == null) {
			onError.accept("No payment data available");
			return;
		}

		new SwingWorker<Void, Void>() {
			private Exception error;
			private CustomerPayments updatedPayment;

			@Override
			protected Void doInBackground() {
				try {
					int customerPaymentId = payment.getId();
					customerPaymentService.updateFromPartialToLoan(customerPaymentId, promiseDate);
					updatedPayment = customerPaymentService.findById(customerPaymentId);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					if (onError != null)
						onError.accept("Failed to update payment type: " + error.getMessage());
				} else {
					refreshPaymentData(payment.getId(), onSuccess, onError);
					if (onSuccess != null)
						onSuccess.run();
				}
			}
		}.execute();
	}

	/**
	 * Update promise to pay date for the current payment
	 *
	 * @param newDate   The new promise to pay date
	 * @param onSuccess Callback on successful update
	 * @param onError   Callback on error with error message
	 */
	public void updatePromiseToPay(Date newDate, Runnable onSuccess, Consumer<String> onError) {
		CustomerPayments payment = state.getCustomerPayment();
		if (payment == null) {
			if (onError != null) {
				onError.accept("No payment data available");
			}
			return;
		}

		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					customerPaymentService.updatePromiseToPay(payment.getId(), newDate);
					payment.setPromiseToPay(newDate);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				// Ensures any hidden exception from doInBackground is surfaced
				try {
					get();
				} catch (Exception e) {
					if (error == null) {
						error = e;
					}
				}

				if (error != null) {
					if (onError != null) {
						onError.accept("Failed to update promise to pay date: " + error.getMessage());
					}
				} else {
					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}

	/**
	 * Reset state when navigating away from payment view
	 */
	public void resetState() {
		state.setCustomerPayment(null);
		state.clearPaymentHistory();
	}
}