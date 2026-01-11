package com.gierza_molases.molases_app.ui.dialogs.ProductDialogs;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.UiController.ProductsController;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.ui.components.ToastNotification;

public class UpdateProductDialog extends JDialog {

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color PROFIT_GREEN = new Color(34, 139, 34);
	private static final Color ERROR_RED = new Color(180, 50, 50);
	private static final Color ASSOCIATION_BLUE = new Color(70, 130, 180);

	// UI Components
	private JTextField nameField;
	private JTextField sellingPriceField;
	private JTextField capitalField;
	private JLabel profitLabel;
	private JLabel currentProfitLabel;
	private JButton saveBtn;
	private JButton cancelBtn;
	private JButton addCustomerBtn;

	// Data
	private Product product;
	private Runnable onSuccessCallback;
	private ProductsController productsController;

	// Validation constants
	private static final int MIN_NAME_LENGTH = 3;
	private static final int MAX_NAME_LENGTH = 100;

	/**
	 * Constructor
	 */
	public UpdateProductDialog(Window parent, Product product, ProductsController productsController,
			Runnable onSuccess) {
		super(parent, "Update Product", ModalityType.APPLICATION_MODAL);
		this.productsController = productsController;
		this.product = product;
		this.onSuccessCallback = onSuccess;
		initializeUI();
		populateFields();
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
		gbc.gridwidth = 3;
		gbc.insets = new Insets(20, 30, 10, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Title
		JLabel titleLabel = new JLabel("Update Product");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		titleLabel.setForeground(TEXT_DARK);
		add(titleLabel, gbc);

		// Current profit info panel (shows original values)
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 20, 30);
		JPanel currentInfoPanel = new JPanel(new GridBagLayout());
		currentInfoPanel.setBackground(new Color(245, 245, 250));
		currentInfoPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(180, 180, 200), 1), new EmptyBorder(10, 15, 10, 15)));

		GridBagConstraints infogbc = new GridBagConstraints();
		infogbc.gridx = 0;
		infogbc.gridy = 0;
		infogbc.anchor = GridBagConstraints.WEST;
		infogbc.insets = new Insets(2, 0, 2, 0);

		JLabel currentInfoTitle = new JLabel("Current Values:");
		currentInfoTitle.setFont(new Font("Arial", Font.BOLD, 12));
		currentInfoTitle.setForeground(new Color(80, 80, 80));
		currentInfoPanel.add(currentInfoTitle, infogbc);

		infogbc.gridy++;
		currentProfitLabel = new JLabel();
		currentProfitLabel.setFont(new Font("Arial", Font.PLAIN, 11));
		currentProfitLabel.setForeground(new Color(100, 100, 100));
		currentInfoPanel.add(currentProfitLabel, infogbc);

		add(currentInfoPanel, gbc);

		// Product Name field
		gbc.gridy++;
		gbc.insets = new Insets(20, 30, 5, 30);
		JLabel nameLabel = new JLabel("Product Name *");
		nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
		nameLabel.setForeground(TEXT_DARK);
		add(nameLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 5, 30);
		nameField = new JTextField();
		nameField.setFont(new Font("Arial", Font.PLAIN, 14));
		nameField.setPreferredSize(new Dimension(450, 40));
		nameField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		add(nameField, gbc);

		// Helper text for name
		gbc.gridy++;
		gbc.insets = new Insets(0, 30, 10, 30);
		JLabel nameHelper = new JLabel(
				"Min " + MIN_NAME_LENGTH + " characters, Max " + MAX_NAME_LENGTH + " characters");
		nameHelper.setFont(new Font("Arial", Font.ITALIC, 11));
		nameHelper.setForeground(new Color(120, 120, 120));
		add(nameHelper, gbc);

		// Selling Price field
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);
		JLabel sellingPriceLabel = new JLabel("Selling Price (₱) *");
		sellingPriceLabel.setFont(new Font("Arial", Font.BOLD, 14));
		sellingPriceLabel.setForeground(TEXT_DARK);
		add(sellingPriceLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 5, 30);
		sellingPriceField = new JTextField();
		sellingPriceField.setFont(new Font("Arial", Font.PLAIN, 14));
		sellingPriceField.setPreferredSize(new Dimension(450, 40));
		sellingPriceField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		add(sellingPriceField, gbc);

		// Helper text for selling price
		gbc.gridy++;
		gbc.insets = new Insets(0, 30, 10, 30);
		JLabel priceHelper = new JLabel("Enter amount (e.g., 450.00)");
		priceHelper.setFont(new Font("Arial", Font.ITALIC, 11));
		priceHelper.setForeground(new Color(120, 120, 120));
		add(priceHelper, gbc);

		// Capital field
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);
		JLabel capitalLabel = new JLabel("Capital (₱) *");
		capitalLabel.setFont(new Font("Arial", Font.BOLD, 14));
		capitalLabel.setForeground(TEXT_DARK);
		add(capitalLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 5, 30);
		capitalField = new JTextField();
		capitalField.setFont(new Font("Arial", Font.PLAIN, 14));
		capitalField.setPreferredSize(new Dimension(450, 40));
		capitalField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		add(capitalField, gbc);

		// Helper text for capital
		gbc.gridy++;
		gbc.insets = new Insets(0, 30, 10, 30);
		JLabel capitalHelper = new JLabel("Enter amount (e.g., 320.00)");
		capitalHelper.setFont(new Font("Arial", Font.ITALIC, 11));
		capitalHelper.setForeground(new Color(120, 120, 120));
		add(capitalHelper, gbc);

		// New profit calculation display (real-time)
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 20, 30);
		JPanel profitPanel = new JPanel(new GridBagLayout());
		profitPanel.setBackground(new Color(240, 255, 240));
		profitPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(PROFIT_GREEN, 1),
				new EmptyBorder(10, 15, 10, 15)));

		profitLabel = new JLabel("New Profit: ₱0.00 (0.0%)");
		profitLabel.setFont(new Font("Arial", Font.BOLD, 14));
		profitLabel.setForeground(PROFIT_GREEN);
		profitPanel.add(profitLabel);

		add(profitPanel, gbc);

		// Add real-time profit calculation listeners
		KeyAdapter profitCalculator = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				updateProfitCalculation();
			}
		};
		sellingPriceField.addKeyListener(profitCalculator);
		capitalField.addKeyListener(profitCalculator);

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
		gbc.insets = new Insets(20, 10, 20, 10);
		saveBtn = createStyledButton("Update", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(120, 40));
		saveBtn.addActionListener(e -> handleUpdate());
		add(saveBtn, gbc);

		gbc.gridx = 2;
		gbc.insets = new Insets(20, 30, 20, 30);
		gbc.anchor = GridBagConstraints.EAST;
		addCustomerBtn = createStyledButton("Add Association", ASSOCIATION_BLUE);
		addCustomerBtn.setPreferredSize(new Dimension(160, 40));
		addCustomerBtn.addActionListener(e -> handleAddCustomerAssociation());
		add(addCustomerBtn, gbc);

		pack();
		setMinimumSize(new Dimension(550, 700));
		setLocationRelativeTo(getParent());
	}

	/**
	 * Handle add customer association button click
	 */
	private void handleAddCustomerAssociation() {
		AddCustomerAssociationDialog.show(this, product.getId(), product.getName(), () -> {

			// Notify parent if callback exists
			if (onSuccessCallback != null) {
				onSuccessCallback.run();
			}
		});
		dispose();
	}

	/**
	 * Populate fields with existing product data
	 */
	private void populateFields() {
		nameField.setText(product.getName());
		sellingPriceField.setText(String.format("%.2f", product.getSellingPrice()));
		capitalField.setText(String.format("%.2f", product.getCapital()));

		// Update current profit label
		currentProfitLabel.setText(String.format("Selling: ₱%.2f | Capital: ₱%.2f | Profit: ₱%.2f (%.1f%%)",
				product.getSellingPrice(), product.getCapital(), product.getProfit(), product.getProfitMargin()));

		// Initialize new profit calculation
		updateProfitCalculation();
	}

	/**
	 * Update profit calculation in real-time
	 */
	private void updateProfitCalculation() {
		try {
			String sellingPriceText = sellingPriceField.getText().trim();
			String capitalText = capitalField.getText().trim();

			if (sellingPriceText.isEmpty() || capitalText.isEmpty()) {
				profitLabel.setText("New Profit: ₱0.00 (0.0%)");
				profitLabel.setForeground(PROFIT_GREEN);
				return;
			}

			double sellingPrice = Double.parseDouble(sellingPriceText);
			double capital = Double.parseDouble(capitalText);

			double profit = sellingPrice - capital;
			double profitMargin = capital > 0 ? (profit / capital) * 100 : 0;

			// Change color based on profit
			if (profit < 0) {
				profitLabel.setForeground(ERROR_RED);
			} else {
				profitLabel.setForeground(PROFIT_GREEN);
			}

			profitLabel.setText(String.format("New Profit: ₱%.2f (%.1f%%)", profit, profitMargin));

		} catch (NumberFormatException e) {
			profitLabel.setText("New Profit: Invalid input");
			profitLabel.setForeground(new Color(180, 100, 50));
		}
	}

	/**
	 * Handle update button click
	 */
	private void handleUpdate() {
		// Get input values
		String name = nameField.getText().trim();
		String sellingPriceText = sellingPriceField.getText().trim();
		String capitalText = capitalField.getText().trim();

		// Basic empty field validation
		if (name.isEmpty() || sellingPriceText.isEmpty() || capitalText.isEmpty()) {
			ToastNotification.showError(this, "Please fill in all required fields.");
			return;
		}

		// Parse price values
		double sellingPrice;
		double capital;

		try {
			sellingPrice = Double.parseDouble(sellingPriceText);
			capital = Double.parseDouble(capitalText);
		} catch (NumberFormatException e) {
			ToastNotification.showError(this, "Invalid price format. Please enter valid numbers.");
			return;
		}

		// Create updated product with same ID (validation will happen in service via
		// product.validate())
		Product updatedProduct = new Product(product.getId(), name, sellingPrice, capital, product.getCreatedAt(),
				product.isProductAssociateWithCustomer(), product.getAssociatedCount());

		// Disable form during update
		setFormEnabled(false);
		saveBtn.setText("Updating...");

		// Call controller
		productsController.updateProduct(updatedProduct,
				// onSuccess
				() -> {
					ToastNotification.showSuccess(UpdateProductDialog.this, "Product updated successfully!");
					if (onSuccessCallback != null) {
						onSuccessCallback.run();
					}
					dispose();
				},
				// onError
				errMessage -> {
					ToastNotification.showError(UpdateProductDialog.this, "Failed to update product: " + errMessage);
					setFormEnabled(true);
					saveBtn.setText("Update");
				});
	}

	/**
	 * Enable/disable form elements
	 */
	private void setFormEnabled(boolean enabled) {
		// Product fields
		nameField.setEnabled(enabled);
		sellingPriceField.setEnabled(enabled);
		capitalField.setEnabled(enabled);

		// Action buttons
		saveBtn.setEnabled(enabled);
		cancelBtn.setEnabled(enabled);
		addCustomerBtn.setEnabled(enabled);

		// Update button colors
		if (!enabled) {
			saveBtn.setBackground(ACCENT_GOLD.darker());
			cancelBtn.setBackground(new Color(150, 150, 150));
			addCustomerBtn.setBackground(ASSOCIATION_BLUE.darker());
		} else {
			saveBtn.setBackground(ACCENT_GOLD);
			cancelBtn.setBackground(new Color(120, 120, 120));
			addCustomerBtn.setBackground(ASSOCIATION_BLUE);
		}

		// Update cursor
		Cursor cursor = enabled ? new Cursor(Cursor.DEFAULT_CURSOR) : new Cursor(Cursor.WAIT_CURSOR);
		setCursor(cursor);
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
	public static void show(Window parent, Product product, ProductsController productsController, Runnable onSuccess) {
		UpdateProductDialog dialog = new UpdateProductDialog(parent, product, productsController, onSuccess);
		dialog.setVisible(true);
	}
}