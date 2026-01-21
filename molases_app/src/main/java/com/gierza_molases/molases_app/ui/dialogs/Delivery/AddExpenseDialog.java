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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.ui.components.delivery.UIComponentFactory;

public class AddExpenseDialog {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);

	@FunctionalInterface
	public interface ExpenseCallback {
		void onAdd(String name, Double amount);
	}

	public static void show(Component parent, ExpenseCallback callback) {
		JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Add Expense");
		dialog.setLayout(new BorderLayout());
		dialog.getContentPane().setBackground(Color.WHITE);

		// Header
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(SIDEBAR_ACTIVE);
		headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

		JLabel titleLabel = new JLabel("Add Additional Expense");
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
		gbc.insets = new Insets(0, 0, 10, 0);

		// Expense Name
		JLabel nameLabel = new JLabel("Expense Name: *");
		nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
		nameLabel.setForeground(TEXT_DARK);
		contentPanel.add(nameLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 20, 0);
		JTextField nameField = new JTextField(20);
		nameField.setFont(new Font("Arial", Font.PLAIN, 14));
		nameField.setPreferredSize(new Dimension(300, 35));
		nameField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		contentPanel.add(nameField, gbc);

		// Expense Amount
		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 10, 0);
		JLabel amountLabel = new JLabel("Amount: *");
		amountLabel.setFont(new Font("Arial", Font.BOLD, 14));
		amountLabel.setForeground(TEXT_DARK);
		contentPanel.add(amountLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 0, 0);
		JTextField amountField = new JTextField(20);
		amountField.setFont(new Font("Arial", Font.PLAIN, 14));
		amountField.setPreferredSize(new Dimension(300, 35));
		amountField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		contentPanel.add(amountField, gbc);

		dialog.add(contentPanel, BorderLayout.CENTER);

		// Footer with buttons
		JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		footerPanel.setBackground(CONTENT_BG);
		footerPanel.setBorder(new EmptyBorder(0, 25, 25, 25));

		JButton cancelBtn = UIComponentFactory.createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> dialog.dispose());
		footerPanel.add(cancelBtn);

		JButton addBtn = UIComponentFactory.createStyledButton("Add", ACCENT_GOLD);
		addBtn.setPreferredSize(new Dimension(120, 40));
		addBtn.addActionListener(e -> {
			String name = nameField.getText().trim();
			String amountStr = amountField.getText().trim();

			// Validation
			if (name.isEmpty()) {
				ToastNotification.showError(dialog, "Please enter expense name");
				nameField.requestFocus();
				return;
			}

			if (amountStr.isEmpty()) {
				ToastNotification.showError(dialog, "Please enter amount");
				amountField.requestFocus();
				return;
			}

			try {
				double amount = Double.parseDouble(amountStr);
				if (amount <= 0) {
					ToastNotification.showError(dialog, "Amount must be greater than 0");
					amountField.requestFocus();
					return;
				}

				callback.onAdd(name, amount);
				dialog.dispose();

			} catch (NumberFormatException ex) {
				ToastNotification.showError(dialog, "Please enter a valid number for amount");
				amountField.requestFocus();
			}
		});
		footerPanel.add(addBtn);

		dialog.add(footerPanel, BorderLayout.SOUTH);

		dialog.pack();
		dialog.setMinimumSize(new Dimension(450, 320));
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}
}