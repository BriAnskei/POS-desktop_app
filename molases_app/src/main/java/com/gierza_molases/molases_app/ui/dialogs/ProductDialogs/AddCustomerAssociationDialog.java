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

import com.gierza_molases.molases_app.Context.AppContext;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.service.CustomerService;
import com.gierza_molases.molases_app.service.ProductAssociationService;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.util.UiSwingWorker;

public class AddCustomerAssociationDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);

	// Dialog data
	private final int productId;
	private final String productName;
	private final Runnable onChangeCallback;

	// UI Components
	private JTable customerTable;
	private DefaultTableModel customerModel;
	private JTextField searchField;
	private JButton searchButton;
	private JButton addButton;
	private JButton cancelButton;
	private JLabel countLabel;
	private JPanel tableContainer;
	private LoadingSpinner loadingSpinner;
	private JScrollPane tableScrollPane;

	// Data
	private List<Customer> displayedCustomers = new ArrayList<>();
	private List<Integer> selectedCustomerIndices = new ArrayList<>();

	// Services
	private final ProductAssociationService productAssociationService = AppContext.productAssociationService;
	private final CustomerService customerService = AppContext.customerService;

	/**
	 * Show dialog
	 */
	public static void show(Window parent, int productId, String productName, Runnable onChangeCallback) {
		AddCustomerAssociationDialog dialog = new AddCustomerAssociationDialog(parent, productId, productName,
				onChangeCallback);
		dialog.setVisible(true);
	}

	/**
	 * Constructor
	 */
	private AddCustomerAssociationDialog(Window parent, int productId, String productName, Runnable onChangeCallback) {
		super(parent, "Add Customer Association", ModalityType.APPLICATION_MODAL);

		this.productId = productId;
		this.productName = productName;
		this.onChangeCallback = onChangeCallback;

		initializeUI();
		loadCustomers(); // Load initial customer data
	}

	/**
	 * Initialize the UI
	 */
	private void initializeUI() {
		setLayout(new BorderLayout(0, 15));
		getContentPane().setBackground(Color.WHITE);

		// Main content with padding
		JPanel mainContent = new JPanel(new BorderLayout(0, 15));
		mainContent.setBackground(Color.WHITE);
		mainContent.setBorder(new EmptyBorder(20, 25, 20, 25));

		mainContent.add(createHeaderPanel(), BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
		centerPanel.setBackground(Color.WHITE);
		centerPanel.add(createSearchPanel(), BorderLayout.NORTH);
		centerPanel.add(createTablePanel(), BorderLayout.CENTER);

		mainContent.add(centerPanel, BorderLayout.CENTER);
		mainContent.add(createButtonPanel(), BorderLayout.SOUTH);

		add(mainContent);

		// Dialog settings
		setSize(700, 550);
		setMinimumSize(new Dimension(700, 550));
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Create header panel
	 */
	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.WHITE);

		// Title
		JLabel titleLabel = new JLabel("Add Customer Association");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(titleLabel);

		panel.add(Box.createVerticalStrut(8));

		// Product info
		JLabel productLabel = new JLabel("Product: " + productName + " (ID: " + productId + ")");
		productLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		productLabel.setForeground(new Color(100, 100, 100));
		productLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(productLabel);

		panel.add(Box.createVerticalStrut(5));

		// Count label (will be updated dynamically)
		countLabel = new JLabel("0 customer(s) selected");
		countLabel.setFont(new Font("Arial", Font.BOLD, 13));
		countLabel.setForeground(new Color(70, 130, 180));
		countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(countLabel);

		return panel;
	}

	/**
	 * Create search panel
	 */
	private JPanel createSearchPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);

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
		panel.add(searchField, gbc);

		gbc.gridx = 1;
		gbc.weightx = 0;
		gbc.insets = new Insets(0, 0, 0, 0);
		searchButton = createStyledButton("Search", SIDEBAR_ACTIVE);
		searchButton.setPreferredSize(new Dimension(100, 38));
		searchButton.addActionListener(e -> performSearch());
		panel.add(searchButton, gbc);

		return panel;
	}

	/**
	 * Create table panel with loading state
	 */
	private JPanel createTablePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);

		// Table container with CardLayout for switching between table and loading
		tableContainer = new JPanel(new java.awt.CardLayout());
		tableContainer.setBackground(Color.WHITE);

		// Create table view
		createCustomerTable();
		tableScrollPane = new JScrollPane(customerTable);
		tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
		tableScrollPane.getViewport().setBackground(TABLE_ROW_EVEN);

		// Create loading view
		loadingSpinner = new LoadingSpinner(40, SIDEBAR_ACTIVE);
		JPanel loadingPanel = new JPanel(new GridBagLayout());
		loadingPanel.setBackground(Color.WHITE);
		loadingPanel.add(loadingSpinner);

		// Add both views to container
		tableContainer.add(tableScrollPane, "TABLE");
		tableContainer.add(loadingPanel, "LOADING");

		panel.add(tableContainer, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Create and configure customer table
	 */
	private void createCustomerTable() {
		String[] columns = { "Select", "Customer Name" };

		customerModel = new DefaultTableModel(columns, 0) {
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
		customerTable.setRowHeight(50);
		customerTable.setShowGrid(true);
		customerTable.setGridColor(new Color(220, 210, 200));
		customerTable.setBackground(TABLE_ROW_EVEN);
		customerTable.setSelectionBackground(new Color(255, 235, 205));
		customerTable.setSelectionForeground(TEXT_DARK);
		customerTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// Set column widths
		customerTable.getColumnModel().getColumn(0).setPreferredWidth(60);
		customerTable.getColumnModel().getColumn(0).setMaxWidth(60);
		customerTable.getColumnModel().getColumn(1).setPreferredWidth(550);

		// Style table header
		JTableHeader header = customerTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(TABLE_HEADER);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setBackground(TABLE_HEADER);
		headerRenderer.setForeground(TEXT_LIGHT);
		headerRenderer.setFont(new Font("Arial", Font.BOLD, 14));
		headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		for (int i = 0; i < customerTable.getColumnCount(); i++) {
			customerTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}

		// Alternating row colors
		customerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		});

		// Handle checkbox selections
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

				updateCountLabel();
			}
		});
	}

	/**
	 * Create button panel
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(15, 0, 0, 0));

		cancelButton = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelButton.setPreferredSize(new Dimension(120, 42));
		cancelButton.addActionListener(e -> dispose());

		addButton = createStyledButton("Add Selected", ACCENT_GOLD);
		addButton.setPreferredSize(new Dimension(140, 42));
		addButton.addActionListener(e -> handleAddCustomers());

		panel.add(cancelButton);
		panel.add(addButton);

		return panel;
	}

	/**
	 * Show/hide loading state
	 */
	private void showLoading(boolean loading) {
		java.awt.CardLayout cardLayout = (java.awt.CardLayout) tableContainer.getLayout();

		if (loading) {
			loadingSpinner.start();
			cardLayout.show(tableContainer, "LOADING");
			searchField.setEnabled(false);
			searchButton.setEnabled(false);
			addButton.setEnabled(false);
		} else {
			loadingSpinner.stop();
			cardLayout.show(tableContainer, "TABLE");
			searchField.setEnabled(true);
			searchButton.setEnabled(true);
			addButton.setEnabled(true);
		}
	}

	/**
	 * Load customers from service (fetch 20 customer
	 */
	private void loadCustomers() {
		showLoading(true);

		new UiSwingWorker<List<Customer>, Void>() {
			@Override
			protected List<Customer> doInBackground() throws Exception {
				// Fetch all available customers
				return customerService.fetch20Customer("");

			}

			@Override
			protected void onSuccess(List<Customer> result) {
				displayedCustomers = result;
				updateTable();
				showLoading(false);
			}

			@Override
			protected void onError(Exception e) {
				ToastNotification.showError(AddCustomerAssociationDialog.this,
						"Error loading customers: " + e.getMessage());
				e.printStackTrace();
				displayedCustomers.clear();
				updateTable();
				showLoading(false);
			}
		}.execute();
	}

	/**
	 * Perform search using the service (fetch 20 customers with search term
	 */
	private void performSearch() {
		String searchTerm = searchField.getText().trim();
		showLoading(true);

		new UiSwingWorker<List<Customer>, Void>() {
			@Override
			protected List<Customer> doInBackground() throws Exception {

				return customerService.fetch20Customer(searchTerm);

			}

			@Override
			protected void onSuccess(List<Customer> result) {
				displayedCustomers = result;
				updateTable();

				if (displayedCustomers.isEmpty()) {
					ToastNotification.showInfo(AddCustomerAssociationDialog.this,
							searchTerm.isEmpty() ? "All customers are already associated with this product."
									: "No available customers found matching your search.");
				}

				showLoading(false);
			}

			@Override
			protected void onError(Exception e) {
				ToastNotification.showError(AddCustomerAssociationDialog.this,
						"Error searching customers: " + e.getMessage());
				e.printStackTrace();
				showLoading(false);
			}
		}.execute();
	}

	/**
	 * Update table with customer data
	 */
	private void updateTable() {
		selectedCustomerIndices.clear();
		customerModel.setRowCount(0);

		for (Customer customer : displayedCustomers) {
			customerModel.addRow(new Object[] { Boolean.FALSE, customer.getDisplayName() });
		}

		updateCountLabel();
	}

	/**
	 * Update count label
	 */
	private void updateCountLabel() {
		int count = selectedCustomerIndices.size();
		countLabel.setText(count + " customer(s) selected");
	}

	/**
	 * Handle add customers button click
	 */
	private void handleAddCustomers() {
		// Validate selection
		if (selectedCustomerIndices.isEmpty()) {
			ToastNotification.showError(this, "Please select at least one customer to associate.");
			return;
		}

		// Get selected customer IDs
		List<Integer> selectedCustomerIds = new ArrayList<>();
		for (Integer index : selectedCustomerIndices) {
			if (index < displayedCustomers.size()) {
				selectedCustomerIds.add(displayedCustomers.get(index).getId());
			}
		}

		// Disable form during save
		setFormEnabled(false);
		addButton.setText("Adding...");

		// Use the controller instead of service directly
		AppContext.productsController.addCustomerAssociations(productId, selectedCustomerIds,
				// onSuccess
				() -> {
					int count = selectedCustomerIds.size();
					ToastNotification.showSuccess(AddCustomerAssociationDialog.this,
							count + " customer(s) associated successfully!");

					// Trigger callback to refresh parent dialog
					if (onChangeCallback != null) {
						onChangeCallback.run();
					}

					// Close dialog
					dispose();
				},
				// onError
				(errorMsg) -> {
					ToastNotification.showError(AddCustomerAssociationDialog.this, "Failed: " + errorMsg);
					setFormEnabled(true);
					addButton.setText("Add Selected");
				});
	}

	/**
	 * Enable/disable form elements
	 */
	private void setFormEnabled(boolean enabled) {
		searchField.setEnabled(enabled);
		searchButton.setEnabled(enabled);
		customerTable.setEnabled(enabled);
		addButton.setEnabled(enabled);
		cancelButton.setEnabled(enabled);

		// Update button colors
		if (!enabled) {
			addButton.setBackground(ACCENT_GOLD.darker());
			cancelButton.setBackground(new Color(150, 150, 150));
		} else {
			addButton.setBackground(ACCENT_GOLD);
			cancelButton.setBackground(new Color(120, 120, 120));
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
}