package com.gierza_molases.molases_app.ui.dialogs.CustomerDialogs;

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

import com.gierza_molases.molases_app.Context.AppContext;
import com.gierza_molases.molases_app.UiController.CustomersController;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.util.Listener;

public class UpdateCustomerDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	// Use controller instead of direct service
	private final CustomersController controller = AppContext.customersController;

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);

	// Form fields
	private JTextField firstNameField;
	private JTextField middleNameField;
	private JTextField lastNameField;
	private JTextField companyNameField;
	private JTextField addressField;
	private JTextField contactNumberField;

	// Data
	private Customer customer;
	private Listener listener;

	// UI Components
	private JButton updateBtn;
	private JButton cancelBtn;

	public UpdateCustomerDialog(Window parent, Customer customer, Listener listener) {
		super(parent, "Update Customer", ModalityType.APPLICATION_MODAL);

		this.customer = customer;
		this.listener = listener;

		setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);

		JPanel mainContent = new JPanel(new BorderLayout(0, 20));
		mainContent.setBackground(Color.WHITE);
		mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

		JPanel customerInfoSection = createCustomerInfoSection();
		JPanel buttonPanel = createButtonPanel();

		mainContent.add(customerInfoSection, BorderLayout.CENTER);
		mainContent.add(buttonPanel, BorderLayout.SOUTH);

		add(mainContent);

		loadCustomerData();

		setSize(650, 500);
		setMinimumSize(new Dimension(650, 500));
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

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

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		section.add(createCustomerTypeLabel(), gbc);

		gbc.gridy = 1;
		gbc.gridwidth = 3;
		if (customer.isIndividual()) {
			section.add(createIndividualFieldsPanel(), gbc);
		} else {
			section.add(createCompanyFieldsPanel(), gbc);
		}

		gbc.gridy = 2;
		gbc.gridwidth = 3;
		addressField = new JTextField();
		JPanel addressPanel = createLabeledField("Address *", addressField);
		section.add(addressPanel, gbc);

		gbc.gridy = 3;
		contactNumberField = new JTextField();
		section.add(createLabeledField("Contact Number *", contactNumberField), gbc);

		return section;
	}

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

	private JPanel createIndividualFieldsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 5, 8, 5);
		gbc.gridy = 0;

		gbc.gridx = 0;
		gbc.weightx = 0.33;
		firstNameField = new JTextField();
		panel.add(createLabeledField("First Name *", firstNameField), gbc);

		gbc.gridx = 1;
		middleNameField = new JTextField();
		panel.add(createLabeledField("Middle Name", middleNameField), gbc);

		gbc.gridx = 2;
		lastNameField = new JTextField();
		panel.add(createLabeledField("Last Name *", lastNameField), gbc);

		return panel;
	}

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

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(20, 0, 0, 0));

		cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 42));
		cancelBtn.addActionListener(e -> dispose());

		updateBtn = createStyledButton("Update Customer", ACCENT_GOLD);
		updateBtn.setPreferredSize(new Dimension(160, 42));
		updateBtn.addActionListener(e -> onUpdateButtonClicked());

		panel.add(cancelBtn);
		panel.add(updateBtn);

		return panel;
	}

	private void onUpdateButtonClicked() {
		// Validate
		ValidationResult validation = validateInput();
		if (!validation.isValid()) {
			ToastNotification.showError(this, validation.getErrorMessage());
			validation.getFocusField().requestFocus();
			return;
		}

		// Disable UI
		setUIEnabled(false);
		updateBtn.setText("Updating...");

		// Update via controller
		if (customer.getType().equals("individual")) {
			controller.updateCustomerIndividual(customer.getId(), validation.getFirstName(), validation.getMiddleName(),
					validation.getLastName(), validation.getContactNumber(), validation.getAddress(),

					// Success
					() -> {
						if (listener != null)
							listener.onAction();
						dispose();
					},

					// Error
					(Exception e) -> {
						setUIEnabled(true);
						updateBtn.setText("Update Customer");

						ToastNotification.showError(UpdateCustomerDialog.this,
								e.getMessage() != null ? e.getMessage() : "Failed to update customer");
					});
		} else {
			controller.updateCustomerCompany(customer.getId(), validation.getCompanyName(),
					validation.getContactNumber(), validation.getAddress(), () -> {
						// Success
						if (listener != null)
							listener.onAction();
						dispose();
					}, // Error
					(Exception e) -> {
						setUIEnabled(true);
						updateBtn.setText("Update Customer");

						ToastNotification.showError(UpdateCustomerDialog.this,
								e.getMessage() != null ? e.getMessage() : "Failed to update customer");
					});
		}
	}

	private ValidationResult validateInput() {
		String firstName = "";
		String middleName = "";
		String lastName = "";
		String companyName = "";

		String address = addressField.getText().trim();
		String contactNumber = contactNumberField.getText().trim();

		if (customer.isIndividual()) {
			firstName = firstNameField.getText().trim();
			middleName = middleNameField.getText().trim();
			lastName = lastNameField.getText().trim();

			if (firstName.isEmpty()) {
				return ValidationResult.error("First Name is required", firstNameField);
			}
			if (lastName.isEmpty()) {
				return ValidationResult.error("Last Name is required", lastNameField);
			}
		} else {
			companyName = companyNameField.getText().trim();
			if (companyName.isEmpty()) {
				return ValidationResult.error("Company Name is required", companyNameField);
			}
		}

		if (address.isEmpty()) {
			return ValidationResult.error("Address is required", addressField);
		}

		if (contactNumber.isEmpty()) {
			return ValidationResult.error("Contact Number is required", contactNumberField);
		}

		return ValidationResult.success(firstName, middleName, lastName, companyName, address, contactNumber);
	}

	private void setUIEnabled(boolean enabled) {
		updateBtn.setEnabled(enabled);
		cancelBtn.setEnabled(enabled);

		if (customer.isIndividual()) {
			firstNameField.setEnabled(enabled);
			middleNameField.setEnabled(enabled);
			lastNameField.setEnabled(enabled);
		} else {
			companyNameField.setEnabled(enabled);
		}

		addressField.setEnabled(enabled);
		contactNumberField.setEnabled(enabled);
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

	// Inner class for validation
	private static class ValidationResult {
		private final boolean valid;
		private final String errorMessage;
		private final JTextField focusField;
		private final String firstName;
		private final String middleName;
		private final String lastName;
		private final String companyName;
		private final String address;
		private final String contactNumber;

		private ValidationResult(boolean valid, String errorMessage, JTextField focusField, String firstName,
				String middleName, String lastName, String companyName, String address, String contactNumber) {
			this.valid = valid;
			this.errorMessage = errorMessage;
			this.focusField = focusField;
			this.firstName = firstName;
			this.middleName = middleName;
			this.lastName = lastName;
			this.companyName = companyName;
			this.address = address;
			this.contactNumber = contactNumber;
		}

		static ValidationResult error(String message, JTextField field) {
			return new ValidationResult(false, message, field, null, null, null, null, null, null);
		}

		static ValidationResult success(String firstName, String middleName, String lastName, String companyName,
				String address, String contactNumber) {
			return new ValidationResult(true, null, null, firstName, middleName, lastName, companyName, address,
					contactNumber);
		}

		boolean isValid() {
			return valid;
		}

		String getErrorMessage() {
			return errorMessage;
		}

		JTextField getFocusField() {
			return focusField;
		}

		String getFirstName() {
			return firstName;
		}

		String getMiddleName() {
			return middleName;
		}

		String getLastName() {
			return lastName;
		}

		String getCompanyName() {
			return companyName;
		}

		String getAddress() {
			return address;
		}

		String getContactNumber() {
			return contactNumber;
		}
	}
}