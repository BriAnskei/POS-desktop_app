package com.gierza_molases.molases_app.ui.dialogs.Delivery;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.model.PaymentHistory;
import com.gierza_molases.molases_app.ui.components.ToastNotification;

public class EditPaymentAmountDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	// Colors
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);

	// Formatters
	private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy");

	static {
		currencyFormatter.setMaximumFractionDigits(2);
		currencyFormatter.setMinimumFractionDigits(2);
	}

	private final PaymentHistory paymentHistory;
	private final Runnable onSuccess;

	public EditPaymentAmountDialog(Window parent, PaymentHistory paymentHistory, Runnable onSuccess) {
		super(parent, "Edit Payment Amount", ModalityType.APPLICATION_MODAL);
		this.paymentHistory = paymentHistory;
		this.onSuccess = onSuccess;

		initComponents();
	}

	private void initComponents() {
		CustomerPayments currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();
		if (currentPayment == null) {
			dispose();
			return;
		}

		setLayout(new GridBagLayout());
		getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Title
		JLabel titleLabel = new JLabel("Edit Payment Amount");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(titleLabel, gbc);

		// Current amount
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);

		String currentAmountStr = currencyFormatter.format(paymentHistory.getAmount()).replace("PHP", "₱");
		JLabel currentAmountLabel = new JLabel("Current Amount: " + currentAmountStr);
		currentAmountLabel.setFont(new Font("Arial", Font.BOLD, 13));
		currentAmountLabel.setForeground(TEXT_DARK);
		add(currentAmountLabel, gbc);

		// Date paid
		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 5, 30);

		String datePaidStr = dateFormatter.format(paymentHistory.getCreatedAt());
		JLabel datePaidLabel = new JLabel("Date Paid: " + datePaidStr);
		datePaidLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		datePaidLabel.setForeground(new Color(100, 100, 100));
		add(datePaidLabel, gbc);

		// Remaining balance context
		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 15, 30);

		double remainingBalance = currentPayment.getTotal() - currentPayment.getTotalPayment();
		String remainingBalanceStr = currencyFormatter.format(remainingBalance).replace("PHP", "₱");
		JLabel remainingBalanceLabel = new JLabel("Current Remaining Balance: " + remainingBalanceStr);
		remainingBalanceLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		remainingBalanceLabel.setForeground(new Color(100, 100, 100));
		add(remainingBalanceLabel, gbc);

		// New amount input
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);

		JLabel newAmountLabel = new JLabel("Enter new payment amount:");
		newAmountLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		newAmountLabel.setForeground(TEXT_DARK);
		add(newAmountLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 20, 30);

		JTextField newAmountField = new JTextField();
		newAmountField.setFont(new Font("Arial", Font.PLAIN, 14));
		newAmountField.setPreferredSize(new Dimension(300, 35));
		newAmountField.setText(String.valueOf(paymentHistory.getAmount()));
		add(newAmountField, gbc);

		// Buttons
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 30, 20, 10);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;

		JButton cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> dispose());
		add(cancelBtn, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(20, 10, 20, 30);
		gbc.anchor = GridBagConstraints.EAST;

		JButton saveBtn = createStyledButton("Save", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(120, 40));
		saveBtn.addActionListener(e -> handleSave(newAmountField, saveBtn, cancelBtn, currentPayment));
		add(saveBtn, gbc);

		pack();
		setMinimumSize(new Dimension(400, getHeight()));
		setLocationRelativeTo(getParent());
	}

	private void handleSave(JTextField newAmountField, JButton saveBtn, JButton cancelBtn,
			CustomerPayments currentPayment) {
		String newAmountText = newAmountField.getText().trim();

		// Validation: Empty check
		if (newAmountText.isEmpty()) {
			ToastNotification.showError(this, "Please enter a payment amount.");
			return;
		}

		// Validation: Parse number
		double newAmount;
		try {
			newAmount = Double.parseDouble(newAmountText);
		} catch (NumberFormatException ex) {
			ToastNotification.showError(this, "Please enter a valid number.");
			return;
		}

		// Validation: Must be greater than 0
		if (newAmount <= 0) {
			ToastNotification.showError(this, "Payment amount must be greater than 0.");
			return;
		}

		// Validation: Total paid cannot exceed total amount
		// Calculate what the new total would be after this edit
		double currentTotalPaid = currentPayment.getTotalPayment();
		double oldAmount = paymentHistory.getAmount();
		double newTotalPaid = currentTotalPaid - oldAmount + newAmount;

		if (newTotalPaid > currentPayment.getTotal()) {
			double maxAllowed = currentPayment.getTotal() - currentTotalPaid + oldAmount;
			ToastNotification.showError(this, "Payment amount would exceed total amount due. Maximum allowed: "
					+ currencyFormatter.format(maxAllowed).replace("PHP", "₱"));
			return;
		}

		// Disable buttons during save
		saveBtn.setEnabled(false);
		cancelBtn.setEnabled(false);
		saveBtn.setText("Saving...");

		// Call controller to update
		AppContext.customerPaymentViewController.updatePaymentHistoryAmount(paymentHistory.getId(), newAmount, () -> {
			SwingUtilities.invokeLater(() -> {
				dispose();
				ToastNotification.showSuccess(getOwner(), newAmountText);
				if (onSuccess != null) {
					onSuccess.run();
				}
			});
		}, (errorMsg) -> {
			SwingUtilities.invokeLater(() -> {
				saveBtn.setEnabled(true);
				cancelBtn.setEnabled(true);
				saveBtn.setText("Save");
				ToastNotification.showError(this, "Failed to update payment amount: " + errorMsg);
			});
		});
	}

	private JButton createStyledButton(String text, Color bgColor) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 14));
		button.setBackground(bgColor);
		button.setForeground(TEXT_LIGHT);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (button.isEnabled()) {
					button.setBackground(bgColor.brighter());
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setBackground(bgColor);
			}
		});

		return button;
	}

	/**
	 * Static method to show the dialog
	 */
	public static void show(Window parent, PaymentHistory paymentHistory, Runnable onSuccess) {
		EditPaymentAmountDialog dialog = new EditPaymentAmountDialog(parent, paymentHistory, onSuccess);
		dialog.setVisible(true);
	}
}