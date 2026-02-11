package com.gierza_molases.molases_app.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.gierza_molases.molases_app.dao.CustomerPaymentDao;
import com.gierza_molases.molases_app.dao.PaymentHistoryDao;
import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.model.PaymentHistory;
import com.gierza_molases.molases_app.util.TransactionHelper;

public class CustomerPaymentsService {
	private final CustomerPaymentDao customerPaymentDao;
	private final PaymentHistoryDao paymentHistoryDao;

	public CustomerPaymentsService(CustomerPaymentDao customerPaymentDao, PaymentHistoryDao paymentHistoryDao) {
		this.customerPaymentDao = customerPaymentDao;
		this.paymentHistoryDao = paymentHistoryDao;
	}

	public void addPaymentAmount(PaymentHistory ph) {
		TransactionHelper.executeInTransaction(conn -> {
			int customerPaymentId = ph.getCustomerPaymentId();
			double amount = ph.getAmount();

			CustomerPayments cp = customerPaymentDao.findById(customerPaymentId);

			if (cp == null) {
				throw new SQLException("Cannot find customer payment with this ID: " + customerPaymentId);
			}

			double overallPayment = cp.getTotalPayment() + ph.getAmount();
			boolean isFullyPaid = cp.getTotal() == overallPayment;

			customerPaymentDao.updateForNewPayment(customerPaymentId, amount, conn);

			if (isFullyPaid) {
				customerPaymentDao.updateStatus(customerPaymentId, "complete", conn);
			}

			paymentHistoryDao.insert(ph, conn);

		});
	}

	public List<CustomerPayments> fetchPaymentsCursor(Long lastSeenPaymentId, String search, String paymentType,
			String status, LocalDateTime fromDate, LocalDateTime toDate, int pageSize) {
		return customerPaymentDao.fetchNextPage(lastSeenPaymentId, search, paymentType, status, fromDate, toDate,
				pageSize + 1);
	}

	public CustomerPayments findById(int id) throws SQLException {
		return customerPaymentDao.findById(id);
	}

	public List<PaymentHistory> getPaymentHistory(int customerDeliveryId) throws SQLException {
		return paymentHistoryDao.fetchByCustomerPaymentId(customerDeliveryId);
	}

	public void updateFromLoanToPartial(int id, double amount) {
		TransactionHelper.executeInTransaction(conn -> {

			// update type
			customerPaymentDao.updateType(id, "Partial", conn);

			// add add the new amount of the partials in the tota_payment
			customerPaymentDao.updateForNewPayment(id, amount, conn);

			// set null the promise to pay
			customerPaymentDao.updatePromiseToPay(id, null, conn);

			// record the partial payment
			PaymentHistory ph = new PaymentHistory(id, amount);
			paymentHistoryDao.insert(ph, conn);
		});

	}

	public void updateFromPartialToLoan(int id, Date promiseToPay) {

		TransactionHelper.executeInTransaction(conn -> {
			// set the payment tyoe to partial
			customerPaymentDao.updateType(id, "Loan", conn);

			// set the promise to pay to the current input
			customerPaymentDao.updatePromiseToPay(id, promiseToPay, conn);
		});

	}

	public void updatePromiseToPay(int id, Date newPromiseToPay) throws SQLException {
		customerPaymentDao.updatePromiseToPay(id, newPromiseToPay);
	}

	// update payment status, this is used by 'loan' types payments
	public void updatePaymentStatusToComplete(int id) {
		TransactionHelper.executeInTransaction(conn -> {

			// fetch first to get the remaining balance
			CustomerPayments cp = customerPaymentDao.findById(id, conn);

			if (cp == null) {
				throw new SQLException("Cannot find customer payment with this ID: " + id);
			}
			double currentBalance = cp.getTotal() - cp.getTotalPayment();

			customerPaymentDao.updateStatus(id, "complete", conn);

			// update total payment
			customerPaymentDao.updateForNewPayment(id, currentBalance, conn);

			// save payment history as of todays full payment of completion
			PaymentHistory ph = new PaymentHistory(cp.getId(), currentBalance);
			paymentHistoryDao.insert(ph, conn);

		});
	}

	public void updatePaymentStatusToPending(int id) {
		TransactionHelper.executeInTransaction(conn -> {

			// latest payment is the payment that is created when status is setted to
			// complete
			// payment status can be edited when the type is 'LOAN'
			PaymentHistory latestPayment = paymentHistoryDao.findLatestByCustomerPaymentId(id, conn);

			// update record based on the latest payment
			CustomerPayments cp = customerPaymentDao.findById(id, conn);

			// there should be a payment history record for this payment because
			// customer payment cannot be set to complete if the 'status' is still "PENDING"
			if (latestPayment == null || cp == null) {
				throw new SQLException("Cannot find a latest payment or customer payment of this record " + id);
			}

			double amountReturn = cp.getTotalPayment() - latestPayment.getAmount();

			customerPaymentDao.updateStatus(id, "pending", conn);
			customerPaymentDao.updateTotalPayment(id, amountReturn, conn);

			int latestPaymentId = latestPayment.getId();
			paymentHistoryDao.delete(latestPaymentId, conn);
		});
	}

	public void updatePaymentAmount(int customerPaymentId, int paymentHistoryId, double updatedAmount) {
		TransactionHelper.executeInTransaction(conn -> {
			CustomerPayments cp = customerPaymentDao.findById(customerPaymentId, conn);
			PaymentHistory ph = paymentHistoryDao.findById(paymentHistoryId, conn);

			if (cp == null || ph == null) {
				throw new SQLException("Action failed, cannot find customerPayments or paymentHistry for this action");
			}

			// we get the diff value of the over all payment without the sum that add up
			// this customerPayment
			double overAllPaymentDiff = cp.getTotalPayment() - ph.getAmount();
			double newPaymentAmount = overAllPaymentDiff + updatedAmount;

			// now we update the payment
			customerPaymentDao.updateTotalPayment(customerPaymentId, newPaymentAmount, conn);
			paymentHistoryDao.updateAmount(paymentHistoryId, updatedAmount);

			customerPaymentDao.updateStatusBasedOnTotalPayment(customerPaymentId, conn);

		});
	}

	public void setNotes(int id, String notes) throws SQLException {
		customerPaymentDao.setNotes(id, notes);
	}

}
