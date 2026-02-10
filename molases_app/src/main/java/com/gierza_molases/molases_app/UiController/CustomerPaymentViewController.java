package com.gierza_molases.molases_app.UiController;

import java.util.Date;
import java.util.function.Consumer;

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
	public void addPayment(double amount, Date paymentDate, Runnable onSuccess, Consumer<String> onError) {
		CustomerPayments payment = state.getCustomerPayment();
		if (payment == null) {
			onError.accept("No payment data available");
			return;
		}

		// TODO: Implement backend service call to insert into payment_history table
		// Example:
		// customerPaymentService.addPaymentHistory(
		// payment.getId(), amount, paymentDate,
		// () -> {
		// // Reload payment data and history after successful insert
		// CustomerPayments updatedPayment =
		// customerPaymentService.getById(payment.getId());
		// List<PaymentHistory> history =
		// customerPaymentService.getPaymentHistory(payment.getId());
		// state.setCustomerPayment(updatedPayment);
		// state.setPaymentHistory(history);
		// onSuccess.run();
		// },
		// onError
		// );

		// TEMPORARY: Mock implementation - update state directly
		try {
			// Add to payment history
			PaymentHistory newPayment = new PaymentHistory(null, 0, amount, paymentDate);
			state.addPaymentHistory(newPayment);

			// Update total payment
			double newTotalPayment = payment.getTotalPayment() + amount;
			payment.setTotalPayment(newTotalPayment);

			// Check if fully paid - auto-update status to Complete
			double remainingBalance = payment.getTotal() - newTotalPayment;
			if (remainingBalance <= 0.01) { // Use small threshold for floating point comparison
				payment.setStatus("complete");
			}

			state.setCustomerPayment(payment);
			onSuccess.run();
		} catch (Exception e) {
			onError.accept("Failed to add payment: " + e.getMessage());
		}
	}

	/**
	 * Update payment status (Pending → Complete) Only allowed for Loan payment type
	 * 
	 * @param newStatus The new status (should be "Complete")
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

		// TODO: Implement backend service call
		// Example:
		// customerPaymentService.updatePaymentStatus(
		// payment.getId(), newStatus,
		// () -> {
		// // Reload payment data after successful update
		// CustomerPayments updatedPayment =
		// customerPaymentService.getById(payment.getId());
		// state.setCustomerPayment(updatedPayment);
		// onSuccess.run();
		// },
		// onError
		// );

		// TEMPORARY: Mock implementation - update state directly
		try {
			payment.setStatus(newStatus);
			state.setCustomerPayment(payment);
			onSuccess.run();
		} catch (Exception e) {
			onError.accept("Failed to update payment status: " + e.getMessage());
		}
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

		// TODO: Implement backend service call
		// Example:
		// customerPaymentService.updatePaymentTypeWithPartialPayment(
		// payment.getId(), newType, partialAmount,
		// () -> {
		// // Reload payment data after successful update
		// CustomerPayments updatedPayment =
		// customerPaymentService.getById(payment.getId());
		// state.setCustomerPayment(updatedPayment);
		// onSuccess.run();
		// },
		// onError
		// );

		// TEMPORARY: Mock implementation - update state directly
		try {
			// Add partial payment to total payment
			double newTotalPayment = payment.getTotalPayment() + partialAmount;
			payment.setTotalPayment(newTotalPayment);

			// Update payment type
			payment.setPaymentType(newType);

			// Clear Promise to Pay date
			payment.setPromiseToPay(null);

			// Add to payment history
			PaymentHistory newPayment = new PaymentHistory(null, 0, partialAmount, new Date());
			state.addPaymentHistory(newPayment);

			// Note: Status remains "Pending" - manual update will be implemented later

			state.setCustomerPayment(payment);
			onSuccess.run();
		} catch (Exception e) {
			onError.accept("Failed to update payment type: " + e.getMessage());
		}
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

		// TODO: Implement backend service call
		// Example:
		// customerPaymentService.updatePaymentTypeWithPromiseToPay(
		// payment.getId(), newType, promiseDate,
		// () -> {
		// // Reload payment data after successful update
		// CustomerPayments updatedPayment =
		// customerPaymentService.getById(payment.getId());
		// state.setCustomerPayment(updatedPayment);
		// onSuccess.run();
		// },
		// onError
		// );

		// TEMPORARY: Mock implementation - update state directly
		try {
			// Update payment type
			payment.setPaymentType(newType);

			// Set Promise to Pay date
			payment.setPromiseToPay(promiseDate);

			state.setCustomerPayment(payment);
			onSuccess.run();
		} catch (Exception e) {
			onError.accept("Failed to update payment type: " + e.getMessage());
		}
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
			onError.accept("No payment data available");
			return;
		}

		// TODO: Implement backend service call
		// Example:
		// customerPaymentService.updatePromiseToPay(payment.getId(), newDate,
		// () -> {
		// // Reload payment data after successful update
		// CustomerPayments updatedPayment =
		// customerPaymentService.getById(payment.getId());
		// state.setCustomerPayment(updatedPayment);
		// onSuccess.run();
		// },
		// onError
		// );

		// TEMPORARY: Mock implementation - update state directly
		try {
			payment.setPromiseToPay(newDate);
			state.setCustomerPayment(payment);
			onSuccess.run();
		} catch (Exception e) {
			onError.accept("Failed to update promise to pay date: " + e.getMessage());
		}
	}

	/**
	 * Reset state when navigating away from payment view
	 */
	public void resetState() {
		state.setCustomerPayment(null);
		state.clearPaymentHistory();
	}
}