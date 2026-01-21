package com.gierza_molases.molases_app.ui.dialogs.BranchDialogs;

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

import com.gierza_molases.molases_app.UiController.BranchesController;
import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.response.BranchCustomerResponse;
import com.gierza_molases.molases_app.service.BranchService;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.util.UiSwingWorker;

public class UpdateBranchDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final BranchService branchService = AppContext.branchService;
	private final BranchesController controller;
	private final Runnable onSaveCallback;

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);

	// Branch data
	private final int branchId;

	// UI Components
	private JLabel customerNameLabel;
	private JTextField addressField;
	private JTextField noteField;
	private JButton saveBtn;
	private JButton cancelBtn;

	/**
	 * Constructor
	 */
	public UpdateBranchDialog(Window parent, int branchId, BranchesController controller, Runnable onSaveCallback) {
		super(parent, "Update Branch", ModalityType.APPLICATION_MODAL);

		this.branchId = branchId;
		this.controller = controller;
		this.onSaveCallback = onSaveCallback;

		setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);

		// Main content panel with padding
		JPanel mainContent = new JPanel(new BorderLayout(0, 20));
		mainContent.setBackground(Color.WHITE);
		mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

		// Create sections
		JPanel infoSection = createInfoSection();
		JPanel formSection = createFormSection();
		JPanel buttonPanel = createButtonPanel();

		mainContent.add(infoSection, BorderLayout.NORTH);
		mainContent.add(formSection, BorderLayout.CENTER);
		mainContent.add(buttonPanel, BorderLayout.SOUTH);

		add(mainContent);

		pack();
		setMinimumSize(new Dimension(600, 0));

		setLocationRelativeTo(parent);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Load branch data
		loadBranchData();
	}

	/**
	 * Load branch data using UiSwingWorker
	 */
	private void loadBranchData() {
		// Disable form while loading
		setFormEnabled(false);

		UiSwingWorker<BranchCustomerResponse, Void> worker = new UiSwingWorker<BranchCustomerResponse, Void>() {
			@Override
			protected BranchCustomerResponse doInBackground() throws Exception {
				return branchService.getBanchDetails(branchId);
			}

			@Override
			protected void onSuccess(BranchCustomerResponse data) {
				// Populate form with loaded data
				customerNameLabel.setText(data.getCustomer().getDisplayName());
				addressField.setText(data.getBranch().getAddress());
				noteField.setText(data.getBranch().getNote());

				setFormEnabled(true);
			}

			@Override
			protected void onError(Exception e) {
				e.printStackTrace();
				ToastNotification.showError(UpdateBranchDialog.this, "Failed to load branch data: " + e.getMessage());
				setFormEnabled(true);
			}
		};

		worker.execute();
	}

	/**
	 * Create info section (read-only)
	 */
	private JPanel createInfoSection() {
		JPanel section = new JPanel(new GridBagLayout());
		section.setBackground(Color.WHITE);
		section.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
								"Branch Information", javax.swing.border.TitledBorder.LEFT,
								javax.swing.border.TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), TEXT_DARK),
						new EmptyBorder(15, 15, 15, 15)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 5, 8, 5);

		// Branch ID
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.3;
		section.add(createReadOnlyField("Branch ID:", String.valueOf(branchId)), gbc);

		// Customer Name
		gbc.gridx = 1;
		gbc.weightx = 0.7;
		customerNameLabel = new JLabel("Loading...");
		section.add(createReadOnlyField("Customer:", customerNameLabel), gbc);

		return section;
	}

	/**
	 * Create form section (editable fields)
	 */
	private JPanel createFormSection() {
		JPanel section = new JPanel(new GridBagLayout());
		section.setBackground(Color.WHITE);
		section.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
								"Update Details", javax.swing.border.TitledBorder.LEFT,
								javax.swing.border.TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), TEXT_DARK),
						new EmptyBorder(15, 15, 15, 15)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 5, 8, 5);

		// Address field
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		addressField = new JTextField();
		section.add(createLabeledField("Address *", addressField), gbc);

		// Note field
		gbc.gridy = 1;
		noteField = new JTextField();
		section.add(createLabeledField("Note", noteField), gbc);

		return section;
	}

	/**
	 * Create read-only field display
	 */
	private JPanel createReadOnlyField(String labelText, String value) {
		JLabel label = new JLabel(value);
		label.setFont(new Font("Arial", Font.PLAIN, 14));
		label.setForeground(TEXT_DARK);
		return createReadOnlyField(labelText, label);
	}

	/**
	 * Create read-only field display with JLabel
	 */
	private JPanel createReadOnlyField(String labelText, JLabel valueLabel) {
		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setBackground(Color.WHITE);

		JLabel label = new JLabel(labelText);
		label.setFont(new Font("Arial", Font.BOLD, 13));
		label.setForeground(TEXT_DARK);

		valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		valueLabel.setForeground(new Color(80, 80, 80));

		panel.add(label, BorderLayout.NORTH);
		panel.add(valueLabel, BorderLayout.CENTER);

		return panel;
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
	 * Create button panel
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(20, 0, 0, 0));

		// Cancel button
		cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 42));
		cancelBtn.addActionListener(e -> dispose());

		// Save button
		saveBtn = createStyledButton("Update Branch", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(150, 42));
		saveBtn.addActionListener(e -> onSaveButtonClicked());

		panel.add(cancelBtn);
		panel.add(saveBtn);

		return panel;
	}

	/**
	 * Handle save button click
	 */
	private void onSaveButtonClicked() {
		// Validate form
		String validationError = validateForm();
		if (validationError != null) {
			ToastNotification.showError(this, validationError);
			return;
		}

		// Collect data
		final String address = addressField.getText().trim();
		final String note = noteField.getText().trim();

		// Disable form during save
		setFormEnabled(false);
		saveBtn.setText("Updating...");

		// Create updated branch
		Branch updatedBranch = new Branch(address, note);

		// Use controller to update branch
		controller.updateBranch(branchId, updatedBranch, () -> {
			// Success callback
			if (onSaveCallback != null) {
				onSaveCallback.run();
			}
			dispose();
		}, () -> {
			// Error callback
			ToastNotification.showError(UpdateBranchDialog.this, "Failed to update branch");

			// Re-enable form
			setFormEnabled(true);
			saveBtn.setText("Update Branch");
		});
	}

	/**
	 * Validate form
	 */
	private String validateForm() {
		if (addressField.getText().trim().isEmpty()) {
			addressField.requestFocus();
			return "Address is required";
		}
		return null;
	}

	/**
	 * Enable/disable form controls
	 */
	private void setFormEnabled(boolean enabled) {
		addressField.setEnabled(enabled);
		noteField.setEnabled(enabled);
		saveBtn.setEnabled(enabled);
		cancelBtn.setEnabled(enabled);
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
}