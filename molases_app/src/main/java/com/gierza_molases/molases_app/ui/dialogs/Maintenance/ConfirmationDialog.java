package com.gierza_molases.molases_app.ui.dialogs.Maintenance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.UiController.MaintenanceController;
import com.gierza_molases.molases_app.ui.components.ToastNotification;

public class ConfirmationDialog extends JDialog {

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_MEDIUM = new Color(120, 90, 70);
	private static final Color DANGER_RED = new Color(211, 47, 47);

	private final MaintenanceController controller;
	private final int selectedYears;
	private final Runnable onSuccess;

	private JLabel titleLabel;
	private JLabel warningLabel;
	private JProgressBar progressBar;
	private JLabel statusLabel;
	private JButton cancelButton;
	private JButton confirmButton;

	public ConfirmationDialog(Window parent, MaintenanceController controller, int selectedYears, Runnable onSuccess) {
		super(parent, "Confirm Deletion", ModalityType.APPLICATION_MODAL);
		this.controller = controller;
		this.selectedYears = selectedYears;
		this.onSuccess = onSuccess;

		initializeUI();
		setLocationRelativeTo(parent);
	}

	private void initializeUI() {
		setLayout(new BorderLayout());
		setSize(500, 320);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 15, 0);
		gbc.anchor = GridBagConstraints.CENTER;

		// Warning icon and title
		titleLabel = new JLabel("⚠️  Confirm Deletion");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		titleLabel.setForeground(DANGER_RED);
		contentPanel.add(titleLabel, gbc);

		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 20, 0);
		warningLabel = new JLabel(
				"<html><div style='text-align: center;'>You are about to delete delivery records<br>older than <b>"
						+ selectedYears + " year" + (selectedYears > 1 ? "s" : "")
						+ "</b>.<br><br>This action cannot be undone.</div></html>");
		warningLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		warningLabel.setForeground(TEXT_DARK);
		contentPanel.add(warningLabel, gbc);

		// Progress bar (initially hidden)
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 15, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(350, 30));
		progressBar.setVisible(false);
		contentPanel.add(progressBar, gbc);

		// Status label (initially hidden)
		gbc.gridy = 3;
		gbc.insets = new Insets(0, 0, 20, 0);
		gbc.fill = GridBagConstraints.NONE;
		statusLabel = new JLabel("Processing deletion...");
		statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		statusLabel.setForeground(TEXT_MEDIUM);
		statusLabel.setVisible(false);
		contentPanel.add(statusLabel, gbc);

		// Button panel
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 0, 0, 10);
		gbc.anchor = GridBagConstraints.EAST;

		cancelButton = createStyledButton("Cancel", new Color(158, 158, 158), Color.WHITE);
		cancelButton.setPreferredSize(new Dimension(120, 40));
		cancelButton.addActionListener(e -> handleCancel());
		contentPanel.add(cancelButton, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.WEST;
		confirmButton = createStyledButton("Confirm Delete", DANGER_RED, Color.WHITE);
		confirmButton.setPreferredSize(new Dimension(150, 40));
		confirmButton.addActionListener(e -> startDeletion());
		contentPanel.add(confirmButton, gbc);

		add(contentPanel, BorderLayout.CENTER);
	}

	private void handleCancel() {
		// Just close the dialog
		dispose();
	}

	private void startDeletion() {
		// Transform dialog to show progress
		confirmButton.setVisible(false);
		cancelButton.setVisible(false); // Hide cancel since we can't safely cancel mid-transaction
		warningLabel.setVisible(false);
		progressBar.setVisible(true);
		statusLabel.setVisible(true);
		titleLabel.setText("Deleting Records...");
		titleLabel.setForeground(TEXT_DARK);

		// Call controller with progress callback
		controller.deleteOldCompletedDeliveries(selectedYears,
				// onSuccess callback
				deletedCount -> {
					dispose();
					if (onSuccess != null) {
						onSuccess.run();
					}
					new SuccessDialog(getOwner(), deletedCount).setVisible(true);
				},
				// onError callback
				errorMsg -> {
					dispose();
					ToastNotification.showError(getOwner(), "Deletion failed: " + errorMsg);
				},
				// onProgress callback
				progress -> {
					progressBar.setValue(progress);
					progressBar.setString(progress + "%");

					// Update status label with more detail
					if (progress < 100) {
						statusLabel.setText("Deleting records... " + progress + "% complete");
					} else {
						statusLabel.setText("Finalizing deletion...");
					}
				});
	}

	private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 14));
		button.setForeground(fgColor);
		button.setBackground(bgColor);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

		// Hover effect
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				button.setBackground(bgColor.darker());
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				button.setBackground(bgColor);
			}
		});

		return button;
	}
}