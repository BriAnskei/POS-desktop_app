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
import java.util.Date;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.ui.components.ToastNotification;

@SuppressWarnings("serial")
public class AddPaymentDialog extends JDialog {

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);

	// Currency formatter
	private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

	// Date formatter
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy");

	static {
		currencyFormatter.setMaximumFractionDigits(2);
		currencyFormatter.setMinimumFractionDigits(2);
	}

	// UI Components
	private JTextField paymentAmountField;
	private JButton saveBtn;
	private JButton cancelBtn;

	// Data
	private CustomerPayments currentPayment;
	private double remainingBalance;

	// Callback
	private Runnable onSuccessCallback;

	/**
	 * Constructor
	 */
	public AddPaymentDialog(Window parent, Runnable onSuccess) {
		super(parent, "Add Payment", ModalityType.APPLICATION_MODAL);
		this.onSuccessCallback = onSuccess;
		this.currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();

		if (currentPayment != null) {
			this.remainingBalance = currentPayment.getTotal() - currentPayment.getTotalPayment();
		}

		initializeUI();
	}

	/**
	 * Initialize the UI
	 */
	private void initializeUI() {
		setLayout(new GridBagLayout());
		getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Title
		JLabel titleLabel = new JLabel("Add New Payment");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(titleLabel, gbc);

		// Current Date (Read-only)
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(15, 30, 5, 10);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;

		JLabel dateLabel = new JLabel("Payment Date:");
		dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
		dateLabel.setForeground(TEXT_DARK);
		add(dateLabel, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(15, 10, 5, 30);
		gbc.anchor = GridBagConstraints.EAST;

		String todayDate = dateFormatter.format(new Date());
		JLabel dateValueLabel = new JLabel(todayDate);
		dateValueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		dateValueLabel.setForeground(TEXT_DARK);
		add(dateValueLabel, gbc);

		// Total Amount (Read-only)
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 10);
		gbc.anchor = GridBagConstraints.WEST;

		JLabel totalLabel = new JLabel("Total Amount:");
		totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
		totalLabel.setForeground(TEXT_DARK);
		add(totalLabel, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(10, 10, 5, 30);
		gbc.anchor = GridBagConstraints.EAST;

		String totalAmount = currencyFormatter.format(currentPayment.getTotal()).replace("PHP", "₱");
		JLabel totalValueLabel = new JLabel(totalAmount);
		totalValueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		totalValueLabel.setForeground(TEXT_DARK);
		add(totalValueLabel, gbc);

		// Total Paid (Read-only)
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 10);
		gbc.anchor = GridBagConstraints.WEST;

		JLabel paidLabel = new JLabel("Total Paid:");
		paidLabel.setFont(new Font("Arial", Font.BOLD, 14));
		paidLabel.setForeground(TEXT_DARK);
		add(paidLabel, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(10, 10, 5, 30);
		gbc.anchor = GridBagConstraints.EAST;

		String totalPaid = currencyFormatter.format(currentPayment.getTotalPayment()).replace("PHP", "₱");
		JLabel paidValueLabel = new JLabel(totalPaid);
		paidValueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		paidValueLabel.setForeground(TEXT_DARK);
		add(paidValueLabel, gbc);

		// Remaining Balance (Read-only, highlighted)
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 10);
		gbc.anchor = GridBagConstraints.WEST;

		JLabel balanceLabel = new JLabel("Remaining Balance:");
		balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
		balanceLabel.setForeground(TEXT_DARK);
		add(balanceLabel, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(10, 10, 5, 30);
		gbc.anchor = GridBagConstraints.EAST;

		String remainingBalanceStr = currencyFormatter.format(remainingBalance).replace("PHP", "₱");
		JLabel balanceValueLabel = new JLabel(remainingBalanceStr);
		balanceValueLabel.setFont(new Font("Arial", Font.BOLD, 16));
		balanceValueLabel.setForeground(new Color(245, 124, 0)); // Orange color for emphasis
		add(balanceValueLabel, gbc);

		// Separator line
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(15, 30, 15, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		javax.swing.JSeparator separator = new javax.swing.JSeparator();
		separator.setForeground(new Color(220, 210, 200));
		add(separator, gbc);

		// Payment Amount Input
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 30, 5, 10);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;

		JLabel amountLabel = new JLabel("Payment Amount:");
		amountLabel.setFont(new Font("Arial", Font.BOLD, 14));
		amountLabel.setForeground(TEXT_DARK);
		add(amountLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(5, 30, 20, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		paymentAmountField = new JTextField();
		paymentAmountField.setFont(new Font("Arial", Font.PLAIN, 14));
		paymentAmountField.setPreferredSize(new Dimension(300, 35));
		add(paymentAmountField, gbc);

		// Buttons
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 30, 20, 10);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;

		cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> dispose());
		add(cancelBtn, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(20, 10, 20, 30);
		gbc.anchor = GridBagConstraints.EAST;

		saveBtn = createStyledButton("Save", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(120, 40));
		saveBtn.addActionListener(e -> handleSave());
		add(saveBtn, gbc);

		// Dialog settings
		pack();
		setMinimumSize(new Dimension(450, getHeight()));
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Handle save button click
	 */
	private void handleSave() {
		String amountText = paymentAmountField.getText().trim();

		// Validation: Check if amount is entered
		if (amountText.isEmpty()) {
			ToastNotification.showError(this, "Please enter a payment amount.");
			return;
		}

		// Validation: Parse amount
		double paymentAmount;
		try {
			paymentAmount = Double.parseDouble(amountText);
		} catch (NumberFormatException ex) {
			ToastNotification.showError(this, "Please enter a valid number.");
			return;
		}

		// Validation: Amount must be > 0
		if (paymentAmount <= 0) {
			ToastNotification.showError(this, "Payment amount must be greater than 0.");
			return;
		}

		// Validation: Amount must be <= remaining balance
		if (paymentAmount > remainingBalance) {
			ToastNotification.showError(this, "Payment amount cannot exceed the remaining balance ("
					+ currencyFormatter.format(remainingBalance).replace("PHP", "₱") + ").");
			return;
		}

		// Disable buttons during processing
		saveBtn.setEnabled(false);
		cancelBtn.setEnabled(false);
		saveBtn.setText("Saving...");

		// Call controller to add payment
		AppContext.customerPaymentViewController.addPayment(paymentAmount, () -> {
			SwingUtilities.invokeLater(() -> {
				dispose();
				ToastNotification.showSuccess(getOwner(), "Payment added successfully!");

				// Call success callback to refresh the page
				if (onSuccessCallback != null) {
					onSuccessCallback.run();
				}
			});
		}, (errorMsg) -> {
			SwingUtilities.invokeLater(() -> {
				saveBtn.setEnabled(true);
				cancelBtn.setEnabled(true);
				saveBtn.setText("Save");
				ToastNotification.showError(this, "Failed to add payment: " + errorMsg);
			});
		});
	}

	/**
	 * Create styled button
	 */
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
	 * Show the dialog
	 */
	public static void show(Window parent, Runnable onSuccess) {
		AddPaymentDialog dialog = new AddPaymentDialog(parent, onSuccess);
		dialog.setVisible(true);
	}
}