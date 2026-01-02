package com.gierza_molases.molases_app.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.service.CustomerService;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.util.Listener;

public class UpdateCustomerDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final CustomerService customerService = new CustomerService();

	// Color Palette - matching AddCustomerDialog
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);

	// Form fields - Individual
	private JTextField firstNameField;
	private JTextField middleNameField;
	private JTextField lastNameField;

	// Form fields - Company
	private JTextField companyNameField;

	// Common fields
	private JTextField addressField;
	private JTextField contactNumberField;

	// Data
	private Customer customer;
	private Listener listener;

	/**
	 * Constructor
	 */
	public UpdateCustomerDialog(Window parent, Customer customer, Listener listener) {
		super(parent, "Update Customer", ModalityType.APPLICATION_MODAL);

		this.customer = customer;
		this.listener = listener;

		setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);

		// Main content panel with padding
		JPanel mainContent = new JPanel(new BorderLayout(0, 20));
		mainContent.setBackground(Color.WHITE);
		mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

		// Create sections
		JPanel customerInfoSection = createCustomerInfoSection();
		JPanel buttonPanel = createButtonPanel();

		mainContent.add(customerInfoSection, BorderLayout.CENTER);
		mainContent.add(buttonPanel, BorderLayout.SOUTH);

		add(mainContent);

		// Load existing customer data
		loadCustomerData();

		// Dialog settings
		setSize(650, 500);
		setMinimumSize(new Dimension(650, 500));
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Create customer information section
	 */
	private JPanel createCustomerInfoSection() {
		JPanel section = new JPanel(new GridBagLayout());
		section.setBackground(Color.WHITE);
		section.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
						"Customer Information", javax.swing.border.TitledBorder.LEFT,
						javax.swing.border.TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), TEXT_DARK),
				new EmptyBorder(15, 15, 15, 15)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 5, 8, 5);

		// Row 0: Customer Type Label (read-only display)
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		section.add(createCustomerTypeLabel(), gbc);

		// Row 1: Name fields based on customer type
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		if (customer.isIndividual()) {
			section.add(createIndividualFieldsPanel(), gbc);
		} else {
			section.add(createCompanyFieldsPanel(), gbc);
		}

		// Row 2: Address
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		addressField = new JTextField();
		JPanel addressPanel = createLabeledField("Address *", addressField);
		section.add(addressPanel, gbc);

		// Row 3: Contact Number
		gbc.gridy = 3;
		contactNumberField = new JTextField();
		section.add(createLabeledField("Contact Number *", contactNumberField), gbc);

		return section;
	}

	/**
	 * Create customer type label (read-only, not editable)
	 */
	private JPanel createCustomerTypeLabel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
		panel.setBackground(Color.WHITE);

		JLabel typeLabel = new JLabel("Customer Type:");
		typeLabel.setFont(new Font("Arial", Font.BOLD, 13));
		typeLabel.setForeground(TEXT_DARK);

		JLabel typeValue = new JLabel(customer.isIndividual() ? "Individual" : "Company");
		typeValue.setFont(new Font("Arial", Font.PLAIN, 13));
		typeValue.setForeground(new Color(100, 100, 100));

		panel.add(typeLabel);
		panel.add(typeValue);

		return panel;
	}

	/**
	 * Create individual fields panel
	 */
	private JPanel createIndividualFieldsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 5, 8, 5);
		gbc.gridy = 0;

		// First Name
		gbc.gridx = 0;
		gbc.weightx = 0.33;
		firstNameField = new JTextField();
		panel.add(createLabeledField("First Name *", firstNameField), gbc);

		// Middle Name
		gbc.gridx = 1;
		middleNameField = new JTextField();
		panel.add(createLabeledField("Middle Name", middleNameField), gbc);

		// Last Name
		gbc.gridx = 2;
		lastNameField = new JTextField();
		panel.add(createLabeledField("Last Name *", lastNameField), gbc);

		return panel;
	}

	/**
	 * Create company fields panel
	 */
	private JPanel createCompanyFieldsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 5, 8, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;

		companyNameField = new JTextField();
		panel.add(createLabeledField("Company Name *", companyNameField), gbc);

		return panel;
	}

	/**
	 * Load existing customer data into fields
	 */
	private void loadCustomerData() {
		if (customer.isIndividual()) {
			firstNameField.setText(customer.getFirstName() != null ? customer.getFirstName() : "");
			middleNameField.setText(customer.getMidName() != null ? customer.getMidName() : "");
			lastNameField.setText(customer.getLastName() != null ? customer.getLastName() : "");
		} else {
			companyNameField.setText(customer.getCompanyName() != null ? customer.getCompanyName() : "");
		}

		addressField.setText(customer.getAddress() != null ? customer.getAddress() : "");
		contactNumberField.setText(customer.getContactNumber() != null ? customer.getContactNumber() : "");
	}

	/**
	 * Create labeled field
	 */
	private JPanel createLabeledField(String labelText, JTextField field) {
		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setBackground(Color.WHITE);

		JLabel label = new JLabel(labelText);
		label.setFont(new Font("Arial", Font.BOLD, 13));
		label.setForeground(TEXT_DARK);

		field.setFont(new Font("Arial", Font.PLAIN, 14));
		field.setPreferredSize(new Dimension(0, 38));
		field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 190, 180), 1),
				new EmptyBorder(5, 10, 5, 10)));

		panel.add(label, BorderLayout.NORTH);
		panel.add(field, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Create button panel (Update and Cancel)
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(20, 0, 0, 0));

		// Cancel button
		JButton cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 42));
		cancelBtn.addActionListener(e -> dispose());

		// Update button
		JButton updateBtn = createStyledButton("Update Customer", ACCENT_GOLD);
		updateBtn.setPreferredSize(new Dimension(160, 42));
		updateBtn.addActionListener(e -> onUpdateButtonClicked());

		panel.add(cancelBtn);
		panel.add(updateBtn);

		return panel;
	}

	/**
	 * Handle update button click
	 */
	private void onUpdateButtonClicked() {
		// Validate and update
		if (!validateAndUpdate()) {
			return; // Validation failed
		}

		// Notify listener (which will refresh the table and show toast)
		if (listener != null) {
			listener.onAction();
		}

		dispose();
	}

	/**
	 * Validate and update customer
	 * 
	 * @return true if successful, false if validation failed
	 */
	private boolean validateAndUpdate() {
		String firstName = "";
		String middleName = "";
		String lastName = "";
		String companyName = "";

		String address = addressField.getText().trim();
		String contactNumber = contactNumberField.getText().trim();

		// Validate based on customer type
		if (customer.isIndividual()) {
			firstName = firstNameField.getText().trim();
			middleName = middleNameField.getText().trim();
			lastName = lastNameField.getText().trim();

			if (firstName.isEmpty()) {
				ToastNotification.showError(this, "First Name is required");
				firstNameField.requestFocus();
				return false;
			}
			if (lastName.isEmpty()) {
				ToastNotification.showError(this, "Last Name is required");
				lastNameField.requestFocus();
				return false;
			}

		} else {
			companyName = companyNameField.getText().trim();
			if (companyName.isEmpty()) {
				ToastNotification.showError(this, "Company Name is required");
				companyNameField.requestFocus();
				return false;
			}

			// Update customer object
			customer.setCompanyName(companyName);
		}

		// Validate address
		if (address.isEmpty()) {
			ToastNotification.showError(this, "Address is required");
			addressField.requestFocus();
			return false;
		}

		// Validate contact number
		if (contactNumber.isEmpty()) {
			ToastNotification.showError(this, "Contact Number is required");
			contactNumberField.requestFocus();
			return false;
		}

		try {

			if (this.customer.getType().equals("individual")) {
				customerService.updateIndividual(customer.getId(), firstName, middleName, lastName, contactNumber,
						address);
			} else {
				customerService.updateCompany(customer.getId(), companyName, contactNumber, address);
			}
			return true;
		} catch (Exception e) {
			ToastNotification.showError(this, "Failed to update customer: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
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
				button.setBackground(bgColor.brighter());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setBackground(bgColor);
			}
		});

		return button;
	}
}