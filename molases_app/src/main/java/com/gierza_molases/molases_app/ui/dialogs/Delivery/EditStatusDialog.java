package com.gierza_molases.molases_app.ui.dialogs.Delivery;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.context.AppContext;

/**
 * Dialog for editing payment status (Pending ↔ Complete) Only applicable for
 * Loan payment types
 */
public class EditStatusDialog extends JDialog {

	// Colors
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color COLOR_PENDING = new Color(255, 152, 0);
	private static final Color COLOR_COMPLETE = new Color(76, 175, 80);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);

	// Status constants
	private static final String STATUS_PENDING = "Pending";
	private static final String STATUS_COMPLETE = "Complete";

	// Components
	private JRadioButton pendingRadio;
	private JRadioButton completeRadio;
	private JLabel currentStatusLabel;

	private final String initialStatus;
	private JButton confirmBtn;
	private JButton cancelBtn;

	// State
	private final String currentStatus;
	private String selectedStatus;
	private final Runnable onSuccess;

	/**
	 * Constructor
	 * 
	 * @param parent        Parent window
	 * @param currentStatus Current payment status
	 * @param onSuccess     Callback to execute on successful update
	 */
	public EditStatusDialog(java.awt.Window parent, String currentStatus, Runnable onSuccess) {
		super(parent, "Edit Payment Status");
		this.initialStatus = currentStatus;
		this.currentStatus = currentStatus;
		this.selectedStatus = currentStatus;
		this.onSuccess = onSuccess;

		initializeUI();
	}

	/**
	 * Initialize the dialog UI
	 */
	private void initializeUI() {
		setLayout(new GridBagLayout());
		getContentPane().setBackground(Color.WHITE);
		setModal(true);

		GridBagConstraints gbc = new GridBagConstraints();

		// Title
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel(getTitleText());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(titleLabel, gbc);

		// Current Status Display
		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 5, 30);

		currentStatusLabel = new JLabel("Current Status: " + currentStatus);
		currentStatusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		currentStatusLabel.setForeground(getStatusColor(currentStatus));
		currentStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(currentStatusLabel, gbc);

		// Information Message
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 15, 30);

		JLabel messageLabel = new JLabel(
				"<html><center>Changing the status from <b>Pending</b> to <b>Complete</b> will create a full payment record for this customer using the current date.</center></html>");

		messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		messageLabel.setForeground(new Color(100, 100, 100));
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(messageLabel, gbc);

		// Radio Buttons Panel
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 10, 30);

		JPanel radioPanel = createRadioPanel();
		add(radioPanel, gbc);

		// Warning if same status
		gbc.gridy++;
		gbc.insets = new Insets(15, 30, 20, 30);

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

		confirmBtn = createStyledButton("Save", getConfirmButtonColor());
		confirmBtn.setPreferredSize(new Dimension(120, 40));
		confirmBtn.addActionListener(e -> handleSave());
		add(confirmBtn, gbc);

		// Add listeners to radio buttons to update UI
		pendingRadio.addActionListener(e -> {
			selectedStatus = STATUS_PENDING;
			updateDynamicUI();
			updateSaveButtonState();
		});

		completeRadio.addActionListener(e -> {
			selectedStatus = STATUS_COMPLETE;
			updateDynamicUI();

			updateSaveButtonState();
		});

		pack();
		setMinimumSize(new Dimension(400, 350));
		setLocationRelativeTo(getParent());
	}

	/**
	 * Update save button enabled state based on whether changes were made
	 */
	private void updateSaveButtonState() {
		boolean hasChanges = !selectedStatus.equals(initialStatus);
		confirmBtn.setEnabled(hasChanges);

		// When disabled, make it look disabled
		if (!hasChanges) {
			confirmBtn.setBackground(new Color(180, 180, 180)); // Gray background
			confirmBtn.setForeground(new Color(220, 220, 220)); // Light gray text
			confirmBtn.setCursor(Cursor.getDefaultCursor()); // Default cursor (not hand)
		} else {
			confirmBtn.setBackground(getConfirmButtonColor()); // Original color
			confirmBtn.setForeground(TEXT_LIGHT); // Original text color
			confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor
		}
	}

	/**
	 * Create radio button panel
	 */
	private JPanel createRadioPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
				new EmptyBorder(15, 20, 15, 20)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 0, 10, 0);

		JLabel selectLabel = new JLabel("Select Status:");
		selectLabel.setFont(new Font("Arial", Font.BOLD, 13));
		selectLabel.setForeground(TEXT_DARK);
		panel.add(selectLabel, gbc);

		// Pending radio button - horizontal layout
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 0, 5, 15);

		pendingRadio = new JRadioButton("PENDING");
		pendingRadio.setFont(new Font("Arial", Font.BOLD, 14));
		pendingRadio.setForeground(COLOR_PENDING);
		pendingRadio.setBackground(Color.WHITE);
		pendingRadio.setFocusPainted(false);
		pendingRadio.setCursor(new Cursor(Cursor.HAND_CURSOR));
		panel.add(pendingRadio, gbc);

		// Complete radio button - same row
		gbc.gridx = 1;
		gbc.insets = new Insets(5, 15, 5, 0);

		completeRadio = new JRadioButton("COMPLETE");
		completeRadio.setFont(new Font("Arial", Font.BOLD, 14));
		completeRadio.setForeground(COLOR_COMPLETE);
		completeRadio.setBackground(Color.WHITE);
		completeRadio.setFocusPainted(false);
		completeRadio.setCursor(new Cursor(Cursor.HAND_CURSOR));
		panel.add(completeRadio, gbc);

		// Button group
		ButtonGroup group = new ButtonGroup();
		group.add(pendingRadio);
		group.add(completeRadio);

		// Set initial selection
		if (STATUS_PENDING.toLowerCase().equals(currentStatus.toLowerCase())) {
			pendingRadio.setSelected(true);
		} else {
			completeRadio.setSelected(true);
		}

		return panel;
	}

	/**
	 * Get dynamic title based on current status
	 */
	private String getTitleText() {
		if (STATUS_PENDING.equals(currentStatus)) {
			return "Update Payment Status";
		} else {
			return "Change Payment Status";
		}
	}

	/**
	 * Get color for status
	 */
	private Color getStatusColor(String status) {
		return STATUS_PENDING.equals(status) ? COLOR_PENDING : COLOR_COMPLETE;
	}

	/**
	 * Get confirm button color based on selected status
	 */
	private Color getConfirmButtonColor() {
		return STATUS_COMPLETE.equals(selectedStatus) ? COLOR_COMPLETE : COLOR_PENDING;
	}

	/**
	 * Update dynamic UI elements when selection changes
	 */
	private void updateDynamicUI() {
		confirmBtn.setBackground(getConfirmButtonColor());

		revalidate();
		repaint();
	}

	/**
	 * Handle save button click
	 */
	private void handleSave() {
		// Validation: Check if status actually changed
		if (selectedStatus.toLowerCase().equals(currentStatus.toLowerCase())) {
			com.gierza_molases.molases_app.ui.components.ToastNotification.showError(this,
					"No changes made. Please select a different status.");
			return;
		}

		// Disable buttons during processing
		confirmBtn.setEnabled(false);
		cancelBtn.setEnabled(false);
		confirmBtn.setText("Updating...");

		// Call controller to update status
		AppContext.customerPaymentViewController.updatePaymentStatus(selectedStatus, () -> {
			SwingUtilities.invokeLater(() -> {
				dispose();

				// Different success messages based on direction
				String successMessage = getSuccessMessage();
				com.gierza_molases.molases_app.ui.components.ToastNotification.showSuccess(getOwner(), successMessage);

				// Execute callback to refresh page
				if (onSuccess != null) {
					onSuccess.run();
				}
			});
		}, (errorMsg) -> {
			SwingUtilities.invokeLater(() -> {
				// Re-enable buttons
				confirmBtn.setEnabled(true);
				cancelBtn.setEnabled(true);
				confirmBtn.setText("Save");

				com.gierza_molases.molases_app.ui.components.ToastNotification.showError(this,
						"Failed to update status: " + errorMsg);
			});
		});
	}

	/**
	 * Get success message based on status change direction
	 */
	private String getSuccessMessage() {
		if (STATUS_PENDING.equals(currentStatus) && STATUS_COMPLETE.equals(selectedStatus)) {
			return "Payment status updated to Complete successfully!";
		} else if (STATUS_COMPLETE.equals(currentStatus) && STATUS_PENDING.equals(selectedStatus)) {
			return "Payment status updated to Pending successfully!";
		}
		return "Payment status updated successfully!";
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