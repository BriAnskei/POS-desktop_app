package com.gierza_molases.molases_app.ui.dialogs.ProductDialogs;

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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
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

import com.gierza_molases.molases_app.UiController.ProductsController;
import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.service.CustomerService;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.util.UiSwingWorker;

public class AddProductDialog extends JDialog {

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color PROFIT_GREEN = new Color(34, 139, 34);
	private static final Color ERROR_RED = new Color(180, 50, 50);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);

	// UI Components - Product Fields
	private JTextField nameField;
	private JTextField sellingPriceField;
	private JTextField capitalField;
	private JLabel profitLabel;
	private JButton saveBtn;
	private JButton cancelBtn;

	// UI Components - Customer Selection
	private JTable customerTable;
	private DefaultTableModel customerModel;
	private JTextField customerSearchField;
	private JButton searchButton;
	private List<Customer> displayedCustomers = new ArrayList<>();
	private List<Integer> selectedCustomerIndices = new ArrayList<>();
	private JPanel customerTableContainer;
	private LoadingSpinner customerLoadingSpinner;
	private JScrollPane customerScrollPane;

	// service to fetch 20 customer
	private CustomerService customerService = AppContext.customerService;

	// controller
	private final ProductsController productController;

	// Callback
	private Runnable onSuccessCallback;

	// Validation constants
	private static final int MIN_NAME_LENGTH = 3;
	private static final int MAX_NAME_LENGTH = 100;

	/**
	 * Constructor
	 */
	public AddProductDialog(Window parent, Runnable onSuccess, ProductsController productController) {
		super(parent, "Add New Product", ModalityType.APPLICATION_MODAL);
		this.onSuccessCallback = onSuccess;
		this.productController = productController;
		initializeUI();
		loadCustomers(); // Load initial customer data
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

		// Create left and right panels
		JPanel leftPanel = createProductInfoSection();
		JPanel rightPanel = createCustomerSelectionSection();

		// Combine panels horizontally in a scrollable container
		JPanel formPanel = new JPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.X_AXIS));
		formPanel.setBackground(Color.WHITE);
		formPanel.add(leftPanel);
		formPanel.add(Box.createHorizontalStrut(20));
		formPanel.add(rightPanel);

		JScrollPane scrollPane = new JScrollPane(formPanel);
		scrollPane.setBorder(null);
		scrollPane.getViewport().setBackground(Color.WHITE);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		mainContent.add(scrollPane, BorderLayout.CENTER);
		mainContent.add(createButtonPanel(), BorderLayout.SOUTH);

		add(mainContent);

		// Dialog settings - wider and shorter for horizontal layout
		setSize(1100, 550);
		setMinimumSize(new Dimension(1100, 550));
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Create product information section (LEFT SIDE)
	 */
	private JPanel createProductInfoSection() {
		JPanel section = new JPanel(new GridBagLayout());
		section.setBackground(Color.WHITE);
		section.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
						"Product Information", javax.swing.border.TitledBorder.LEFT,
						javax.swing.border.TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), TEXT_DARK),
				new EmptyBorder(15, 15, 15, 15)));
		section.setPreferredSize(new Dimension(480, 0));
		section.setMinimumSize(new Dimension(480, 0));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(8, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		// Product Name field
		nameField = new JTextField();
		section.add(createLabeledField("Product Name *", nameField,
				"Min " + MIN_NAME_LENGTH + " characters, Max " + MAX_NAME_LENGTH + " characters"), gbc);

		// Selling Price field
		gbc.gridy++;
		gbc.insets = new Insets(10, 5, 5, 5);
		sellingPriceField = new JTextField();
		section.add(createLabeledField("Selling Price (₱) *", sellingPriceField, "Enter amount (e.g., 450.00)"), gbc);

		// Capital field
		gbc.gridy++;
		capitalField = new JTextField();
		section.add(createLabeledField("Capital (₱) *", capitalField, "Enter amount (e.g., 320.00)"), gbc);

		// Profit calculation display
		gbc.gridy++;
		gbc.insets = new Insets(10, 5, 5, 5);
		JPanel profitPanel = new JPanel(new GridBagLayout());
		profitPanel.setBackground(new Color(240, 255, 240));
		profitPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(PROFIT_GREEN, 1),
				new EmptyBorder(10, 15, 10, 15)));

		profitLabel = new JLabel("Estimated Profit: ₱0.00 (0.0%)");
		profitLabel.setFont(new Font("Arial", Font.BOLD, 14));
		profitLabel.setForeground(PROFIT_GREEN);
		profitPanel.add(profitLabel);

		section.add(profitPanel, gbc);

		// Add real-time profit calculation listeners
		KeyAdapter profitCalculator = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				updateProfitCalculation();
			}
		};
		sellingPriceField.addKeyListener(profitCalculator);
		capitalField.addKeyListener(profitCalculator);

		return section;
	}

	/**
	 * Create customer selection section (RIGHT SIDE)
	 */
	private JPanel createCustomerSelectionSection() {
		JPanel section = new JPanel(new BorderLayout(0, 15));
		section.setBackground(Color.WHITE);
		section.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
						"Associate with Customers (Optional)", javax.swing.border.TitledBorder.LEFT,
						javax.swing.border.TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), TEXT_DARK),
				new EmptyBorder(15, 15, 15, 15)));
		section.setPreferredSize(new Dimension(480, 0));
		section.setMinimumSize(new Dimension(480, 0));

		// Search panel
		JPanel searchPanel = new JPanel(new GridBagLayout());
		searchPanel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 10, 10);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;

		customerSearchField = new JTextField();
		customerSearchField.setFont(new Font("Arial", Font.PLAIN, 14));
		customerSearchField.setPreferredSize(new Dimension(0, 35));
		customerSearchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		searchPanel.add(customerSearchField, gbc);

		gbc.gridx = 1;
		gbc.weightx = 0;
		gbc.insets = new Insets(0, 0, 10, 0);
		searchButton = createStyledButton("Search", SIDEBAR_ACTIVE);
		searchButton.setPreferredSize(new Dimension(100, 35));
		searchButton.addActionListener(e -> performSearch());
		searchPanel.add(searchButton, gbc);

		section.add(searchPanel, BorderLayout.NORTH);

		// Customer table container with CardLayout for switching between table and
		// loading
		customerTableContainer = new JPanel(new java.awt.CardLayout());
		customerTableContainer.setBackground(Color.WHITE);
		customerTableContainer.setPreferredSize(new Dimension(0, 250));

		// Create table view
		createCustomerTable();
		customerScrollPane = new JScrollPane(customerTable);
		customerScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));

		// Create loading view
		customerLoadingSpinner = new LoadingSpinner(40, SIDEBAR_ACTIVE);
		JPanel loadingPanel = new JPanel(new GridBagLayout());
		loadingPanel.setBackground(Color.WHITE);
		loadingPanel.add(customerLoadingSpinner);

		// Add both views to container with card names
		customerTableContainer.add(customerScrollPane, "TABLE");
		customerTableContainer.add(loadingPanel, "LOADING");

		section.add(customerTableContainer, BorderLayout.CENTER);

		// Helper text
		JLabel customerHelper = new JLabel("Select one or more customers to associate with this product");
		customerHelper.setFont(new Font("Arial", Font.ITALIC, 11));
		customerHelper.setForeground(new Color(120, 120, 120));
		customerHelper.setBorder(new EmptyBorder(10, 0, 0, 0));
		section.add(customerHelper, BorderLayout.SOUTH);

		// Setup search listener
		setupSearchListener();

		return section;
	}

	/**
	 * Create labeled field with helper text
	 */
	private JPanel createLabeledField(String labelText, JTextField field, String helperText) {
		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setBackground(Color.WHITE);

		JLabel label = new JLabel(labelText);
		label.setFont(new Font("Arial", Font.BOLD, 13));
		label.setForeground(TEXT_DARK);

		field.setFont(new Font("Arial", Font.PLAIN, 14));
		field.setPreferredSize(new Dimension(0, 38));
		field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 190, 180), 1),
				new EmptyBorder(5, 10, 5, 10)));

		JLabel helper = new JLabel(helperText);
		helper.setFont(new Font("Arial", Font.ITALIC, 11));
		helper.setForeground(new Color(120, 120, 120));

		panel.add(label, BorderLayout.NORTH);
		panel.add(field, BorderLayout.CENTER);
		panel.add(helper, BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * Create button panel
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(20, 0, 0, 0));

		cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 42));
		cancelBtn.addActionListener(e -> dispose());

		saveBtn = createStyledButton("Save", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(120, 42));
		saveBtn.addActionListener(e -> handleSave());

		panel.add(cancelBtn);
		panel.add(saveBtn);

		return panel;
	}

	/**
	 * Create and configure customer table (supports multiple selection)
	 */
	private void createCustomerTable() {
		String[] customerColumns = { "Select", "Customer Name" };
		customerModel = new DefaultTableModel(customerColumns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 0;
			}

			@Override
			public Class<?> getColumnClass(int column) {
				return column == 0 ? Boolean.class : String.class;
			}
		};

		customerTable = new JTable(customerModel);
		customerTable.setFont(new Font("Arial", Font.PLAIN, 14));
		customerTable.setRowHeight(35);
		customerTable.setShowGrid(true);
		customerTable.setGridColor(new Color(220, 210, 200));
		customerTable.setBackground(TABLE_ROW_EVEN);
		customerTable.setSelectionBackground(new Color(255, 235, 205));
		customerTable.setSelectionForeground(TEXT_DARK);
		customerTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// Set column widths
		customerTable.getColumnModel().getColumn(0).setPreferredWidth(60);
		customerTable.getColumnModel().getColumn(0).setMaxWidth(60);
		customerTable.getColumnModel().getColumn(1).setPreferredWidth(350);

		// Style table header
		JTableHeader customerHeader = customerTable.getTableHeader();
		customerHeader.setFont(new Font("Arial", Font.BOLD, 14));
		customerHeader.setBackground(TABLE_HEADER);
		customerHeader.setForeground(TEXT_LIGHT);
		customerHeader.setPreferredSize(new Dimension(customerHeader.getPreferredSize().width, 40));

		DefaultTableCellRenderer customerHeaderRenderer = new DefaultTableCellRenderer();
		customerHeaderRenderer.setBackground(TABLE_HEADER);
		customerHeaderRenderer.setForeground(TEXT_LIGHT);
		customerHeaderRenderer.setFont(new Font("Arial", Font.BOLD, 14));
		customerHeaderRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		customerTable.getColumnModel().getColumn(0).setHeaderRenderer(customerHeaderRenderer);
		customerTable.getColumnModel().getColumn(1).setHeaderRenderer(customerHeaderRenderer);

		// Alternating row colors
		customerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		});

		// Handle multiple checkbox selections
		customerModel.addTableModelListener(e -> {
			if (e.getColumn() == 0) {
				int row = e.getFirstRow();
				Boolean isChecked = (Boolean) customerModel.getValueAt(row, 0);

				if (isChecked) {
					if (!selectedCustomerIndices.contains(row)) {
						selectedCustomerIndices.add(row);
					}
				} else {
					selectedCustomerIndices.remove(Integer.valueOf(row));
				}
			}
		});
	}

	/**
	 * Setup search listener (Enter key to search)
	 */
	private void setupSearchListener() {
		customerSearchField.addActionListener(e -> performSearch());
	}

	/**
	 * Show customer loading state
	 */
	private void showCustomerLoading(boolean loading) {
		java.awt.CardLayout cardLayout = (java.awt.CardLayout) customerTableContainer.getLayout();

		if (loading) {
			customerLoadingSpinner.start();
			cardLayout.show(customerTableContainer, "LOADING");
			customerSearchField.setEnabled(false);
			searchButton.setEnabled(false);
		} else {
			customerLoadingSpinner.stop();
			cardLayout.show(customerTableContainer, "TABLE");
			customerSearchField.setEnabled(true);
			searchButton.setEnabled(true);
		}
	}

	/**
	 * Load customers from service (fetch 20 customers without search)
	 */
	private void loadCustomers() {
		showCustomerLoading(true);
		String searchTerm = customerSearchField.getText().trim();

		new UiSwingWorker<List<Customer>, Void>() {
			@Override
			protected List<Customer> doInBackground() throws Exception {
				return customerService.fetch20Customer(searchTerm);
			}

			@Override
			protected void onSuccess(List<Customer> result) {
				displayedCustomers = result;
				updateCustomerTable();
				showCustomerLoading(false);
			}

			@Override
			protected void onError(Exception e) {
				ToastNotification.showError(AddProductDialog.this, "Error loading customers: " + e.getMessage());
				e.printStackTrace();
				displayedCustomers.clear();
				updateCustomerTable();
				showCustomerLoading(false);
			}
		}.execute();
	}

	/**
	 * Perform search using the service (fetch 20 customers with search term)
	 */
	private void performSearch() {
		String searchTerm = customerSearchField.getText().trim();
		showCustomerLoading(true);

		new UiSwingWorker<List<Customer>, Void>() {
			@Override
			protected List<Customer> doInBackground() throws Exception {
				return customerService.fetch20Customer(searchTerm);
			}

			@Override
			protected void onSuccess(List<Customer> result) {
				displayedCustomers = result;
				updateCustomerTable();

				if (displayedCustomers.isEmpty()) {
					ToastNotification.showInfo(AddProductDialog.this, "No customers found matching your search.");
				}

				showCustomerLoading(false);
			}

			@Override
			protected void onError(Exception e) {
				ToastNotification.showError(AddProductDialog.this, "Error searching customers: " + e.getMessage());
				showCustomerLoading(false);
			}
		}.execute();
	}

	/**
	 * Update the customer table with current data
	 */
	private void updateCustomerTable() {
		selectedCustomerIndices.clear();
		customerModel.setRowCount(0);

		for (Customer customer : displayedCustomers) {
			customerModel.addRow(new Object[] { Boolean.FALSE, customer.getDisplayName() });
		}
	}

	/**
	 * Update profit calculation in real-time
	 */
	private void updateProfitCalculation() {
		try {
			String sellingPriceText = sellingPriceField.getText().trim();
			String capitalText = capitalField.getText().trim();

			if (sellingPriceText.isEmpty() || capitalText.isEmpty()) {
				profitLabel.setText("Estimated Profit: ₱0.00 (0.0%)");
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

			profitLabel.setText(String.format("Estimated Profit: ₱%.2f (%.1f%%)", profit, profitMargin));

		} catch (NumberFormatException e) {
			profitLabel.setText("Estimated Profit: Invalid input");
			profitLabel.setForeground(new Color(180, 100, 50));
		}
	}

	/**
	 * Handle save button click
	 */
	private void handleSave() {
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

		// Get selected customer IDs
		List<Integer> selectedCustomerIds = new ArrayList<>();
		for (Integer index : selectedCustomerIndices) {
			if (index < displayedCustomers.size()) {
				selectedCustomerIds.add(displayedCustomers.get(index).getId());
			}
		}

		// Create product (validation will happen in service via product.validate())
		Product newProduct = new Product(name, sellingPrice, capital);

		// Disable form during save
		setFormEnabled(false);
		saveBtn.setText("Saving...");

		// Call controller
		productController.addProduct(newProduct, selectedCustomerIds,
				// onSuccess
				() -> {
					ToastNotification.showSuccess(AddProductDialog.this, "Product added successfully!");
					if (onSuccessCallback != null) {
						onSuccessCallback.run();
					}
					dispose();
				},
				// onError
				errMessage -> {
					ToastNotification.showError(AddProductDialog.this, "Failed to add product: " + errMessage);
					setFormEnabled(true);
					saveBtn.setText("Save");
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

		// Customer search fields
		customerSearchField.setEnabled(enabled);
		searchButton.setEnabled(enabled);
		customerTable.setEnabled(enabled);

		// Action buttons
		saveBtn.setEnabled(enabled);
		cancelBtn.setEnabled(enabled);

		// Update button colors
		if (!enabled) {
			saveBtn.setBackground(ACCENT_GOLD.darker());
			cancelBtn.setBackground(new Color(150, 150, 150));
		} else {
			saveBtn.setBackground(ACCENT_GOLD);
			cancelBtn.setBackground(new Color(120, 120, 120));
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
	public static void show(Window parent, Runnable onSuccess, ProductsController productController) {
		AddProductDialog dialog = new AddProductDialog(parent, onSuccess, productController);
		dialog.setVisible(true);
	}
}