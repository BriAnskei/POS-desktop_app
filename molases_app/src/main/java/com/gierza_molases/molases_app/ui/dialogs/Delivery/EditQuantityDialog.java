package com.gierza_molases.molases_app.ui.dialogs.Delivery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.ui.components.delivery.UIComponentFactory;

public class EditQuantityDialog extends JDialog {

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color CONTENT_BG = new Color(250, 247, 242);

	private JTextField quantityField;
	private ProductWithQuantity product;
	private Consumer<Integer> onQuantityChanged;

	/**
	 * Constructor
	 */
	public EditQuantityDialog(Window parent, ProductWithQuantity product, Consumer<Integer> onQuantityChanged) {
		super(parent, "Edit Quantity", ModalityType.APPLICATION_MODAL);
		this.product = product;
		this.onQuantityChanged = onQuantityChanged;

		initializeUI();
	}

	/**
	 * Initialize the UI
	 */
	private void initializeUI() {
		setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);

		// Main content panel
		JPanel mainContent = new JPanel(new BorderLayout(0, 0));
		mainContent.setBackground(Color.WHITE);

		// Header
		mainContent.add(createHeaderSection(), BorderLayout.NORTH);

		// Center - Quantity Input
		mainContent.add(createQuantityInputSection(), BorderLayout.CENTER);

		// Bottom - Buttons
		mainContent.add(createButtonSection(), BorderLayout.SOUTH);

		add(mainContent);

		// Dialog settings
		setSize(500, 370);
		setMinimumSize(new Dimension(500, 320));
		setResizable(false);
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Create header section
	 */
	private JPanel createHeaderSection() {
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setBackground(SIDEBAR_ACTIVE);
		headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

		// Title
		JLabel titleLabel = new JLabel("Edit Product Quantity");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		titleLabel.setForeground(TEXT_LIGHT);
		titleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		headerPanel.add(titleLabel);

		headerPanel.add(Box.createVerticalStrut(8));

		// Product name subtitle
		JLabel productLabel = new JLabel("Product: " + product.getProduct().getName());
		productLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		productLabel.setForeground(new Color(230, 220, 210));
		productLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		headerPanel.add(productLabel);

		return headerPanel;
	}

	/**
	 * Create quantity input section
	 */
	private JPanel createQuantityInputSection() {
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
		inputPanel.setBackground(CONTENT_BG);
		inputPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

		// Current quantity label
		JLabel currentLabel = new JLabel("Current Quantity: " + product.getQuantity());
		currentLabel.setFont(new Font("Arial", Font.BOLD, 14));
		currentLabel.setForeground(TEXT_DARK);
		currentLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		inputPanel.add(currentLabel);

		inputPanel.add(Box.createVerticalStrut(20));

		// New quantity label
		JLabel newLabel = new JLabel("New Quantity: *");
		newLabel.setFont(new Font("Arial", Font.BOLD, 14));
		newLabel.setForeground(TEXT_DARK);
		newLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		inputPanel.add(newLabel);

		inputPanel.add(Box.createVerticalStrut(8));

		// Quantity input field
		quantityField = new JTextField(String.valueOf(product.getQuantity()));
		quantityField.setFont(new Font("Arial", Font.PLAIN, 16));
		quantityField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
		quantityField.setPreferredSize(new Dimension(0, 45));
		quantityField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(10, 15, 10, 15)));

		// Only allow positive integers
		quantityField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
					e.consume();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Submit on Enter key
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleSave();
				}
			}
		});

		// Select all text when focused
		quantityField.selectAll();
		quantityField.requestFocus();
		quantityField.setAlignmentX(JTextField.LEFT_ALIGNMENT);

		inputPanel.add(quantityField);

		inputPanel.add(Box.createVerticalStrut(8));

		// Helper text
		JLabel helperLabel = new JLabel("Enter a positive number");
		helperLabel.setFont(new Font("Arial", Font.ITALIC, 12));
		helperLabel.setForeground(new Color(120, 120, 120));
		helperLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		inputPanel.add(helperLabel);

		return inputPanel;
	}

	/**
	 * Create button section
	 */
	private JPanel createButtonSection() {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(Color.WHITE);
		buttonPanel.setBorder(new EmptyBorder(15, 25, 20, 25));

		// Cancel button
		JButton cancelButton = UIComponentFactory.createStyledButton("Cancel", new Color(120, 120, 120));
		cancelButton.setPreferredSize(new Dimension(120, 40));
		cancelButton.addActionListener(e -> dispose());
		buttonPanel.add(cancelButton);

		// Save button
		JButton saveButton = UIComponentFactory.createStyledButton("Save", ACCENT_GOLD);
		saveButton.setPreferredSize(new Dimension(120, 40));
		saveButton.addActionListener(e -> handleSave());
		buttonPanel.add(saveButton);

		return buttonPanel;
	}

	/**
	 * Handle save action
	 */
	private void handleSave() {
		String qtyText = quantityField.getText().trim();

		// Validation
		if (qtyText.isEmpty()) {
			showError("Please enter a quantity");
			quantityField.requestFocus();
			return;
		}

		try {
			int newQuantity = Integer.parseInt(qtyText);

			if (newQuantity <= 0) {
				showError("Quantity must be greater than 0");
				quantityField.requestFocus();
				quantityField.selectAll();
				return;
			}

			if (newQuantity == product.getQuantity()) {
				showError("New quantity is the same as current quantity");
				quantityField.requestFocus();
				quantityField.selectAll();
				return;
			}

			// Call callback with new quantity
			if (onQuantityChanged != null) {
				onQuantityChanged.accept(newQuantity);
			}

			dispose();

		} catch (NumberFormatException e) {
			showError("Please enter a valid number");
			quantityField.requestFocus();
			quantityField.selectAll();
		}
	}

	/**
	 * Show error message
	 */
	private void showError(String message) {
		// Create error label
		JLabel errorLabel = new JLabel(message);
		errorLabel.setFont(new Font("Arial", Font.BOLD, 12));
		errorLabel.setForeground(new Color(180, 50, 50));
		errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Flash the error (simple visual feedback)
		quantityField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(180, 50, 50), 2), new EmptyBorder(8, 12, 8, 12)));

		// Reset border after 2 seconds
		javax.swing.Timer timer = new javax.swing.Timer(2000, e -> {
			quantityField.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(8, 12, 8, 12)));
		});
		timer.setRepeats(false);
		timer.start();
	}

	/**
	 * Static method to show the dialog
	 */
	public static void show(Window parent, ProductWithQuantity product, Consumer<Integer> onQuantityChanged) {
		EditQuantityDialog dialog = new EditQuantityDialog(parent, product, onQuantityChanged);
		dialog.setVisible(true);
	}
}