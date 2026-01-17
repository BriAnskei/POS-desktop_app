package com.gierza_molases.molases_app.ui.dialogs.Delivery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.gierza_molases.molases_app.UiController.NewDeliveryController;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;

public class SelectProductDialog extends JDialog {

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);
	private static final Color CONTENT_BG = new Color(250, 247, 242);

	private JPanel loadingOverlay;
	private LoadingSpinner loadingSpinner;

	// Result class
	public static class ProductSelectionResult {
		public final Product product;
		public final int quantity;

		public ProductSelectionResult(Product product, int quantity) {
			this.product = product;
			this.quantity = quantity;
		}
	}

	// UI Components
	private JTable productTable;
	private DefaultTableModel productTableModel;
	private List<Product> availableProducts = new ArrayList<>();
	private JTextField searchField;
	private JTextField quantityField;
	private JButton searchButton;
	private JButton clearFilterButton;
	private JButton addButton;
	private JButton cancelButton;
	private Product selectedProduct = null;
	private int selectedRowIndex = -1;

	// Callback
	private Consumer<ProductSelectionResult> onProductSelectedCallback;

	// controller
	private final NewDeliveryController newDeliveryController;

	// customer id
	private final int customerId;

	/**
	 * Constructor
	 */
	public SelectProductDialog(Window parent, Consumer<ProductSelectionResult> onProductSelected,
			NewDeliveryController newDeliveryController, int customerId) {
		super(parent, "Select Product", ModalityType.APPLICATION_MODAL);
		this.onProductSelectedCallback = onProductSelected;
		this.newDeliveryController = newDeliveryController;
		this.customerId = customerId;

		initializeUI();
		loadProductsAsync();
	}

	/**
	 * Initialize the UI
	 */
	private void initializeUI() {
		setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);

		// Main content panel with padding
		JPanel mainContent = new JPanel(new BorderLayout(0, 20));
		mainContent.setBackground(Color.WHITE);
		mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

		// Header
		mainContent.add(createHeaderSection(), BorderLayout.NORTH);

		// Center - Product Selection
		mainContent.add(createProductSelectionSection(), BorderLayout.CENTER);

		// Bottom - Quantity Input and Buttons
		mainContent.add(createBottomSection(), BorderLayout.SOUTH);

		add(mainContent);

		// Dialog settings
		setSize(800, 650);
		setMinimumSize(new Dimension(800, 650));
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Create header section
	 */
	private JPanel createHeaderSection() {
		JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
		headerPanel.setBackground(Color.WHITE);

		// Title
		JLabel titleLabel = new JLabel("Select a Product");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(TEXT_DARK);
		headerPanel.add(titleLabel, BorderLayout.NORTH);

		// Search Panel
		JPanel searchPanel = new JPanel(new GridBagLayout());
		searchPanel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 10);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;

		searchField = new JTextField();
		searchField.setFont(new Font("Arial", Font.PLAIN, 14));
		searchField.setPreferredSize(new Dimension(0, 38));
		searchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		searchField.addActionListener(e -> performSearch());
		searchPanel.add(searchField, gbc);

		gbc.gridx = 1;
		gbc.weightx = 0;
		searchButton = createStyledButton("Search", SIDEBAR_ACTIVE);
		searchButton.setPreferredSize(new Dimension(100, 38));
		searchButton.addActionListener(e -> performSearch());
		searchPanel.add(searchButton, gbc);

		gbc.gridx = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		clearFilterButton = createStyledButton("Clear Filter", new Color(100, 100, 100));
		clearFilterButton.setPreferredSize(new Dimension(120, 38));
		clearFilterButton.addActionListener(e -> clearFilter());
		searchPanel.add(clearFilterButton, gbc);

		headerPanel.add(searchPanel, BorderLayout.CENTER);

		return headerPanel;
	}

	/**
	 * Create product selection section
	 */
	private JPanel createProductSelectionSection() {
		JPanel selectionPanel = new JPanel(new BorderLayout(0, 10));
		selectionPanel.setBackground(Color.WHITE);

		// Instructions
		JLabel instructionLabel = new JLabel("Select a product from the list:");
		instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		instructionLabel.setForeground(TEXT_DARK);
		selectionPanel.add(instructionLabel, BorderLayout.NORTH);

		// Create product table
		createProductTable();
		JScrollPane scrollPane = new JScrollPane(productTable);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
		scrollPane.setPreferredSize(new Dimension(0, 350));

		selectionPanel.add(scrollPane, BorderLayout.CENTER);

		return selectionPanel;
	}

	/**
	 * Create product table
	 */
	private void createProductTable() {
		String[] columns = { "", "Product Name", "Selling Price" };
		productTableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int column) {
				if (column == 0)
					return Boolean.class;
				return Object.class;
			}
		};

		productTable = new JTable(productTableModel);
		productTable.setFont(new Font("Arial", Font.PLAIN, 14));
		productTable.setRowHeight(45);
		productTable.setShowGrid(true);
		productTable.setGridColor(new Color(220, 210, 200));
		productTable.setBackground(TABLE_ROW_EVEN);
		productTable.setSelectionBackground(new Color(255, 235, 205));
		productTable.setSelectionForeground(TEXT_DARK);
		productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Set column widths
		productTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		productTable.getColumnModel().getColumn(0).setMaxWidth(50);
		productTable.getColumnModel().getColumn(1).setPreferredWidth(500);
		productTable.getColumnModel().getColumn(2).setPreferredWidth(200);

		// Style table header
		JTableHeader header = productTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(TABLE_HEADER);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setBackground(TABLE_HEADER);
		headerRenderer.setForeground(TEXT_LIGHT);
		headerRenderer.setFont(new Font("Arial", Font.BOLD, 14));
		headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		for (int i = 0; i < productTable.getColumnCount(); i++) {
			productTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}

		// Custom renderer for checkbox column
		productTable.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {
			private final JCheckBox checkBox = new JCheckBox();

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				checkBox.setSelected(value != null && (Boolean) value);
				checkBox.setHorizontalAlignment(SwingConstants.CENTER);
				checkBox.setBackground(
						isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD));
				checkBox.setEnabled(false); // Visual only, clicks handled by mouse listener
				return checkBox;
			}
		});

		// Alternating row colors for other columns
		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				if (column == 2) {
					setHorizontalAlignment(SwingConstants.RIGHT);
				} else {
					setHorizontalAlignment(SwingConstants.LEFT);
				}

				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		};

		productTable.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
		productTable.getColumnModel().getColumn(2).setCellRenderer(cellRenderer);

		// Selection listener
		productTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				int selectedRow = productTable.getSelectedRow();
				handleRowSelection(selectedRow);
			}
		});

		// Mouse listener for clicking anywhere on the row
		productTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = productTable.rowAtPoint(e.getPoint());
				if (row >= 0) {
					// Toggle selection
					if (selectedRowIndex == row) {
						// Deselect if clicking the same row
						productTable.clearSelection();
						handleRowSelection(-1);
					} else {
						// Select the clicked row
						productTable.setRowSelectionInterval(row, row);
						handleRowSelection(row);
					}

					// Double-click behavior - focus on quantity
					if (e.getClickCount() == 2 && row < availableProducts.size()) {
						quantityField.requestFocus();
						quantityField.selectAll();
					}
				}
			}
		});
	}

	/**
	 * Handle row selection and update checkboxes
	 */
	private void handleRowSelection(int selectedRow) {
		// Clear all checkboxes
		for (int i = 0; i < productTableModel.getRowCount(); i++) {
			productTableModel.setValueAt(false, i, 0);
		}

		// Set selected row
		if (selectedRow >= 0 && selectedRow < availableProducts.size()) {
			selectedProduct = availableProducts.get(selectedRow);
			selectedRowIndex = selectedRow;
			productTableModel.setValueAt(true, selectedRow, 0);
		} else {
			selectedProduct = null;
			selectedRowIndex = -1;
		}

		updateAddButtonState();
	}

	private void showLoading() {
		if (loadingOverlay == null) {
			// Create semi-transparent overlay
			loadingOverlay = new JPanel(new GridBagLayout());
			loadingOverlay.setBackground(new Color(255, 255, 255, 180));
			loadingOverlay.setOpaque(false);

			// Create spinner
			loadingSpinner = new LoadingSpinner(50, SIDEBAR_ACTIVE);

			// Add spinner to overlay
			GridBagConstraints gbc = new GridBagConstraints();
			loadingOverlay.add(loadingSpinner, gbc);
		}

		// Add overlay to glass pane
		setGlassPane(loadingOverlay);
		getGlassPane().setVisible(true);

		// Start animation
		loadingSpinner.start();
	}

	private void hideLoading() {
		if (loadingSpinner != null) {
			loadingSpinner.stop();
		}

		getGlassPane().setVisible(false);
	}

	/**
	 * Load mock products
	 */
	private void loadProductsAsync() {
		availableProducts.clear();

		this.showLoading();

		this.newDeliveryController.loadProductSelection(() -> {
			this.availableProducts = this.newDeliveryController.getProductState().getProducts();

			hideLoading();
			updateProductTable();
		}, err -> {
			System.out.println("Failed to load" + err);
			hideLoading();
		}, customerId);

	}

	/**
	 * Perform search
	 */
	private void performSearch() {
		String searchText = searchField.getText().trim().toLowerCase();

		if (searchText.isEmpty()) {
			return;
		}

		this.showLoading();

		this.newDeliveryController.searchProducts(searchText, customerId, () -> {
			this.availableProducts = this.newDeliveryController.getProductState().getProducts();

			this.hideLoading();
			updateProductTable();

		}, err -> System.out.println("Failed to search product " + err));

	}

	/**
	 * Clear filter and reload all products
	 */
	private void clearFilter() {
		// Clear search field
		searchField.setText("");

		// Reload all products
		loadProductsAsync();
	}

	/**
	 * Update product table
	 */
	private void updateProductTable() {
		productTableModel.setRowCount(0);

		for (Product product : availableProducts) {
			productTableModel.addRow(new Object[] { false, // Checkbox unchecked by default
					product.getName(), "₱" + String.format("%.2f", product.getSellingPrice()) });
		}

		selectedProduct = null;
		selectedRowIndex = -1;
		updateAddButtonState();
	}

	/**
	 * Create bottom section with quantity input and buttons
	 */
	private JPanel createBottomSection() {
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setBackground(Color.WHITE);

		// Quantity Input Panel
		JPanel quantityPanel = new JPanel(new GridBagLayout());
		quantityPanel.setBackground(CONTENT_BG);
		quantityPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(220, 210, 200), 1), new EmptyBorder(15, 20, 15, 20)));
		quantityPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 15);

		JLabel quantityLabel = new JLabel("Quantity: *");
		quantityLabel.setFont(new Font("Arial", Font.BOLD, 14));
		quantityLabel.setForeground(TEXT_DARK);
		quantityPanel.add(quantityLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);

		quantityField = new JTextField("1");
		quantityField.setFont(new Font("Arial", Font.PLAIN, 14));
		quantityField.setPreferredSize(new Dimension(200, 38));
		quantityField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));

		// Only allow numbers
		quantityField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
					e.consume();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				updateAddButtonState();
			}
		});

		quantityPanel.add(quantityField, gbc);

		gbc.gridx = 2;
		gbc.weightx = 0;
		gbc.insets = new Insets(0, 10, 0, 0);
		JLabel helperLabel = new JLabel("Enter the quantity to order");
		helperLabel.setFont(new Font("Arial", Font.ITALIC, 12));
		helperLabel.setForeground(new Color(120, 120, 120));
		quantityPanel.add(helperLabel, gbc);

		bottomPanel.add(quantityPanel);
		bottomPanel.add(Box.createVerticalStrut(20));

		// Button Panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(Color.WHITE);
		buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

		cancelButton = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelButton.setPreferredSize(new Dimension(120, 42));
		cancelButton.addActionListener(e -> dispose());
		buttonPanel.add(cancelButton);

		addButton = createStyledButton("Add Product", ACCENT_GOLD);
		addButton.setPreferredSize(new Dimension(150, 42));
		addButton.setEnabled(false);
		addButton.addActionListener(e -> handleAddProduct());
		buttonPanel.add(addButton);

		bottomPanel.add(buttonPanel);

		return bottomPanel;
	}

	/**
	 * Update add button state
	 */
	private void updateAddButtonState() {
		boolean hasProduct = selectedProduct != null;
		boolean hasValidQuantity = false;

		try {
			String qtyText = quantityField.getText().trim();
			if (!qtyText.isEmpty()) {
				int qty = Integer.parseInt(qtyText);
				hasValidQuantity = qty > 0;
			}
		} catch (NumberFormatException e) {
			hasValidQuantity = false;
		}

		addButton.setEnabled(hasProduct && hasValidQuantity);
	}

	/**
	 * Handle add product
	 */
	private void handleAddProduct() {
		if (selectedProduct == null) {
			JOptionPane.showMessageDialog(this, "Please select a product", "No Product Selected",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		String qtyText = quantityField.getText().trim();
		if (qtyText.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter a quantity", "Quantity Required",
					JOptionPane.WARNING_MESSAGE);
			quantityField.requestFocus();
			return;
		}

		try {
			int quantity = Integer.parseInt(qtyText);
			if (quantity <= 0) {
				JOptionPane.showMessageDialog(this, "Quantity must be greater than 0", "Invalid Quantity",
						JOptionPane.ERROR_MESSAGE);
				quantityField.requestFocus();
				quantityField.selectAll();
				return;
			}

			// Create result and call callback
			ProductSelectionResult result = new ProductSelectionResult(selectedProduct, quantity);

			if (onProductSelectedCallback != null) {
				onProductSelectedCallback.accept(result);
			}

			dispose();

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Please enter a valid number", "Invalid Quantity",
					JOptionPane.ERROR_MESSAGE);
			quantityField.requestFocus();
			quantityField.selectAll();
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
	public static void show(Window parent, Consumer<ProductSelectionResult> onProductSelected,
			NewDeliveryController newDeliveryController, int customerId) {
		SelectProductDialog dialog = new SelectProductDialog(parent, onProductSelected, newDeliveryController,
				customerId);
		dialog.setVisible(true);
	}
}