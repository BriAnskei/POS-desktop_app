package com.gierza_molases.molases_app.ui.dialogs.Delivery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.ui.components.delivery.UIComponentFactory;
import com.toedter.calendar.JDateChooser;

public class SetPaymentTypeDialog {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color INFO_TEXT = new Color(100, 100, 100);

	@FunctionalInterface
	public interface PaymentCallback {
		void onSave(String paymentType, Double partialAmount, Date loadDate);
	}

	public static void show(Component parent, Customer customer, String currentPaymentType, double totalSales,
			PaymentCallback callback) {
		JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Set Payment Type");
		dialog.setLayout(new BorderLayout());
		dialog.getContentPane().setBackground(Color.WHITE);

		// Header
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(SIDEBAR_ACTIVE);
		headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

		JLabel titleLabel = new JLabel("Set Payment Type for " + customer.getDisplayName());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(Color.WHITE);
		headerPanel.add(titleLabel, BorderLayout.WEST);

		dialog.add(headerPanel, BorderLayout.NORTH);

		// Content
		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBackground(CONTENT_BG);
		contentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 15, 0);

		// Total Sales Info Label
		JLabel totalSalesInfoLabel = new JLabel(String.format("Total Sales: ₱%,.2f", totalSales));
		totalSalesInfoLabel.setFont(new Font("Arial", Font.BOLD, 15));
		totalSalesInfoLabel.setForeground(SIDEBAR_ACTIVE);
		contentPanel.add(totalSalesInfoLabel, gbc);

		// Payment Type Label
		gbc.gridy++;
		gbc.insets = new Insets(10, 0, 15, 0);
		JLabel paymentLabel = new JLabel("Payment Type: *");
		paymentLabel.setFont(new Font("Arial", Font.BOLD, 14));
		paymentLabel.setForeground(TEXT_DARK);
		contentPanel.add(paymentLabel, gbc);

		// Payment Type Dropdown
		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 20, 0);
		String[] paymentTypes = { "Paid Cheque", "Paid Cash", "Partial", "Loan" };
		JComboBox<String> paymentCombo = new JComboBox<>(paymentTypes);
		paymentCombo.setFont(new Font("Arial", Font.PLAIN, 14));
		paymentCombo.setPreferredSize(new Dimension(300, 35));
		contentPanel.add(paymentCombo, gbc);

		// Conditional Panel for Partial/Load
		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 0, 0);
		JPanel conditionalPanel = new JPanel(new GridBagLayout());
		conditionalPanel.setBackground(CONTENT_BG);
		contentPanel.add(conditionalPanel, gbc);

		// Partial Amount Field
		JPanel partialPanel = new JPanel(new GridBagLayout());
		partialPanel.setBackground(CONTENT_BG);

		GridBagConstraints partialGbc = new GridBagConstraints();
		partialGbc.gridx = 0;
		partialGbc.gridy = 0;
		partialGbc.anchor = GridBagConstraints.WEST;
		partialGbc.fill = GridBagConstraints.HORIZONTAL;
		partialGbc.insets = new Insets(0, 0, 10, 0);

		JLabel partialLabel = new JLabel("Initial Payment Amount: *");
		partialLabel.setFont(new Font("Arial", Font.BOLD, 14));
		partialLabel.setForeground(TEXT_DARK);
		partialPanel.add(partialLabel, partialGbc);

		partialGbc.gridy++;
		JTextField partialField = new JTextField(15);
		partialField.setFont(new Font("Arial", Font.PLAIN, 14));
		partialField.setPreferredSize(new Dimension(300, 35));
		partialField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		partialPanel.add(partialField, partialGbc);

		// Add hint text for partial payment
		partialGbc.gridy++;
		partialGbc.insets = new Insets(5, 0, 0, 0);
		JLabel partialHintLabel = new JLabel(String.format("Must be less than ₱%,.2f", totalSales));
		partialHintLabel.setFont(new Font("Arial", Font.ITALIC, 12));
		partialHintLabel.setForeground(INFO_TEXT);
		partialPanel.add(partialHintLabel, partialGbc);

		// Loan Date Chooser
		JPanel loanPanel = new JPanel(new GridBagLayout());
		loanPanel.setBackground(CONTENT_BG);

		GridBagConstraints loanGbc = new GridBagConstraints();
		loanGbc.gridx = 0;
		loanGbc.gridy = 0;
		loanGbc.anchor = GridBagConstraints.WEST;
		loanGbc.fill = GridBagConstraints.HORIZONTAL;
		loanGbc.insets = new Insets(0, 0, 10, 0);

		JLabel loanLabel = new JLabel("Promise to Pay Date: *");
		loanLabel.setFont(new Font("Arial", Font.BOLD, 14));
		loanLabel.setForeground(TEXT_DARK);
		loanPanel.add(loanLabel, loanGbc);

		loanGbc.gridy++;
		JDateChooser loanDateChooser = new JDateChooser();
		loanDateChooser.setDateFormatString("MM/dd/yyyy");
		loanDateChooser.setPreferredSize(new Dimension(300, 35));
		loanDateChooser.setFont(new Font("Arial", Font.PLAIN, 14));
		loanPanel.add(loanDateChooser, loanGbc);

		// Add hint text for loan date
		loanGbc.gridy++;
		loanGbc.insets = new Insets(5, 0, 0, 0);
		JLabel loanHintLabel = new JLabel("Date must be in the future");
		loanHintLabel.setFont(new Font("Arial", Font.ITALIC, 12));
		loanHintLabel.setForeground(INFO_TEXT);
		loanPanel.add(loanHintLabel, loanGbc);

		// Update conditional panel based on selection
		updateConditionalPanel(conditionalPanel, paymentCombo.getSelectedItem().toString(), partialPanel, loanPanel);

		paymentCombo.addActionListener(e -> {
			String selected = (String) paymentCombo.getSelectedItem();
			updateConditionalPanel(conditionalPanel, selected, partialPanel, loanPanel);
		});

		dialog.add(contentPanel, BorderLayout.CENTER);

		// Footer with buttons
		JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		footerPanel.setBackground(CONTENT_BG);
		footerPanel.setBorder(new EmptyBorder(0, 25, 25, 25));

		JButton cancelBtn = UIComponentFactory.createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> dialog.dispose());
		footerPanel.add(cancelBtn);

		JButton saveBtn = UIComponentFactory.createStyledButton("Save", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(120, 40));
		saveBtn.addActionListener(e -> {
			String paymentType = (String) paymentCombo.getSelectedItem();
			Double partialAmount = null;
			Date loanDate = null;

			// Validate based on payment type
			if ("Partial".equals(paymentType)) {
				String amountStr = partialField.getText().trim();
				if (amountStr.isEmpty()) {
					ToastNotification.showError(dialog, "Please enter initial payment amount");
					return;
				}

				try {
					partialAmount = Double.parseDouble(amountStr);

					if (partialAmount <= 0) {
						ToastNotification.showError(dialog, "Amount must be greater than 0");
						return;
					}

					// NEW VALIDATION: Check if partial amount is >= total sales
					if (partialAmount >= totalSales) {
						ToastNotification.showError(dialog, String.format(
								"Partial payment cannot be equal to or greater than total sales (₱%,.2f)", totalSales));
						return;
					}
				} catch (NumberFormatException ex) {
					ToastNotification.showError(dialog, "Please enter a valid amount");
					return;
				}
			} else if ("Loan".equals(paymentType)) {
				loanDate = loanDateChooser.getDate();
				if (loanDate == null) {
					ToastNotification.showError(dialog, "Please select promise to pay date");
					return;
				}

				LocalDate today = LocalDate.now();

				LocalDate selectedDate = loanDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

				if (selectedDate.isBefore(today)) {
					ToastNotification.showError(dialog, "Promise to pay date cannot be in the past");
					return;
				}
			}

			callback.onSave(paymentType, partialAmount, loanDate);
			dialog.dispose();
		});
		footerPanel.add(saveBtn);

		dialog.add(footerPanel, BorderLayout.SOUTH);

		dialog.pack();
		dialog.setMinimumSize(new Dimension(450, 380));
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

	}

	private static void updateConditionalPanel(JPanel conditionalPanel, String paymentType, JPanel partialPanel,
			JPanel loanPanel) {
		conditionalPanel.removeAll();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		if ("Partial".equals(paymentType)) {
			conditionalPanel.add(partialPanel, gbc);
		} else if ("Loan".equals(paymentType)) {
			conditionalPanel.add(loanPanel, gbc);
		}

		conditionalPanel.revalidate();
		conditionalPanel.repaint();
	}
}