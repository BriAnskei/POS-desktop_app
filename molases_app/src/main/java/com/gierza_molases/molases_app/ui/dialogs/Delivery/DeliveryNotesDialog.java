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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.ui.components.ToastNotification;

public class DeliveryNotesDialog extends JDialog {

	// Color Palette (matching PaymentViewPage)
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);

	private JTextArea notesTextArea;
	private JButton saveBtn;
	private JButton clearBtn;
	private JButton cancelBtn;

	private static final int MAX_CHARACTERS = 1000;

	public DeliveryNotesDialog(Window parent) {
		super(parent, "Payment Notes", ModalityType.APPLICATION_MODAL);
		initComponents();
		loadExistingNotes();
	}

	private void initComponents() {
		setLayout(new GridBagLayout());
		getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();

		// ========== TITLE ==========
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.insets = new Insets(20, 30, 15, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Payment Notes");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(titleLabel, gbc);

		// ========== INSTRUCTION LABEL ==========
		gbc.gridy++;
		gbc.insets = new Insets(0, 30, 10, 30);

		JLabel instructionLabel = new JLabel("Add notes or reminders about this payment:");
		instructionLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		instructionLabel.setForeground(TEXT_DARK);
		add(instructionLabel, gbc);

		// ========== TEXTAREA (with scroll) ==========
		gbc.gridy++;
		gbc.insets = new Insets(0, 30, 5, 30);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;

		notesTextArea = new JTextArea(10, 40);
		notesTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
		notesTextArea.setLineWrap(true);
		notesTextArea.setWrapStyleWord(true);
		notesTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));

		JScrollPane scrollPane = new JScrollPane(notesTextArea);
		scrollPane.setPreferredSize(new Dimension(450, 200));
		scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

		add(scrollPane, gbc);

		// ========== CHARACTER COUNTER ==========
		gbc.gridy++;
		gbc.insets = new Insets(0, 30, 15, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0;

		JLabel charCountLabel = new JLabel("0 / " + MAX_CHARACTERS + " characters");
		charCountLabel.setFont(new Font("Arial", Font.ITALIC, 11));
		charCountLabel.setForeground(new Color(120, 120, 120));
		charCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(charCountLabel, gbc);

		// Update character counter on text change
		notesTextArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void update() {
				int length = notesTextArea.getText().length();
				charCountLabel.setText(length + " / " + MAX_CHARACTERS + " characters");

				// Enforce character limit
				if (length > MAX_CHARACTERS) {
					SwingUtilities.invokeLater(() -> {
						String text = notesTextArea.getText();
						notesTextArea.setText(text.substring(0, MAX_CHARACTERS));
					});
				}
			}

			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				update();
			}

			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				update();
			}

			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				update();
			}
		});

		// ========== BUTTONS ==========
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.insets = new Insets(5, 30, 20, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.setBackground(Color.WHITE);

		GridBagConstraints btnGbc = new GridBagConstraints();

		// Clear button (left)
		btnGbc.gridx = 0;
		btnGbc.gridy = 0;
		btnGbc.insets = new Insets(0, 0, 0, 10);
		btnGbc.anchor = GridBagConstraints.WEST;

		clearBtn = createStyledButton("Clear", new Color(220, 100, 100));
		clearBtn.setPreferredSize(new Dimension(110, 38));
		clearBtn.addActionListener(e -> clearNotes());
		buttonPanel.add(clearBtn, btnGbc);

		// Cancel button (left, next to Clear)
		btnGbc.gridx = 1;
		btnGbc.insets = new Insets(0, 0, 0, 0);

		cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(110, 38));
		cancelBtn.addActionListener(e -> dispose());
		buttonPanel.add(cancelBtn, btnGbc);

		// Spacer (pushes Save to the right)
		btnGbc.gridx = 2;
		btnGbc.weightx = 1.0;
		btnGbc.fill = GridBagConstraints.HORIZONTAL;
		buttonPanel.add(new JPanel() {
			{
				setBackground(Color.WHITE);
			}
		}, btnGbc);

		// Save button (right)
		btnGbc.gridx = 3;
		btnGbc.weightx = 0;
		btnGbc.fill = GridBagConstraints.NONE;
		btnGbc.anchor = GridBagConstraints.EAST;

		saveBtn = createStyledButton("Save", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(110, 38));
		saveBtn.addActionListener(e -> saveNotes());
		buttonPanel.add(saveBtn, btnGbc);

		add(buttonPanel, gbc);

		// ========== DIALOG PROPERTIES ==========
		pack();
		setMinimumSize(new Dimension(550, 400));
		setLocationRelativeTo(getParent());
	}

	/**
	 * Load existing notes from CustomerPayments object
	 */
	private void loadExistingNotes() {
		CustomerPayments currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();
		if (currentPayment != null && currentPayment.getNotes() != null) {
			notesTextArea.setText(currentPayment.getNotes());
		}
	}

	/**
	 * Clear the textarea
	 */
	private void clearNotes() {
		notesTextArea.setText("");
		notesTextArea.requestFocus();
	}

	/**
	 * Save notes to CustomerPayments object and close dialog
	 */
	private void saveNotes() {
		String notes = notesTextArea.getText().trim();

		// Disable buttons during save
		saveBtn.setEnabled(false);
		clearBtn.setEnabled(false);
		cancelBtn.setEnabled(false);
		saveBtn.setText("Saving...");

		// Update notes via controller
		AppContext.customerPaymentViewController.updateNotes(notes, () -> {
			SwingUtilities.invokeLater(() -> {
				dispose();
				ToastNotification.showSuccess(getOwner(), "Notes saved successfully!");
			});
		}, (errorMsg) -> {
			SwingUtilities.invokeLater(() -> {
				// Re-enable buttons on error
				saveBtn.setEnabled(true);
				clearBtn.setEnabled(true);
				cancelBtn.setEnabled(true);
				saveBtn.setText("Save");
				ToastNotification.showError(this, "Failed to save notes: " + errorMsg);
			});
		});
	}

	/**
	 * Create styled button matching PaymentViewPage style
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
	 * Static method to show the dialog
	 */
	public static void show(Window parent) {
		DeliveryNotesDialog dialog = new DeliveryNotesDialog(parent);
		dialog.setVisible(true);
	}
}