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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.table.TableCellRenderer;

import com.gierza_molases.molases_app.ui.components.ToastNotification;

public class SelectCustomerDialog extends JDialog {

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);

	// Mock Customer Data Class
	public static class Customer {
		int id;
		String name;
		String address;
		String contact;

		public Customer(int id, String name, String address, String contact) {
			this.id = id;
			this.name = name;
			this.address = address;
			this.contact = contact;
		}
	}

	// UI Components
	private JTable customerTable;
	private DefaultTableModel customerTableModel;
	private List<Customer> availableCustomers = new ArrayList<>();
	private JTextField searchField;
	private JButton searchButton;
	private JButton clearFiltersButton;
	private JButton selectButton;
	private JButton cancelButton;
	private Customer selectedCustomer = null;
	private int selectedRowIndex = -1;

	// Callback
	private Consumer<Customer> onCustomerSelectedCallback;

	/**
	 * Constructor
	 */
	public SelectCustomerDialog(Window parent, Consumer<Customer> onCustomerSelected) {
		super(parent, "Select Customer", ModalityType.APPLICATION_MODAL);
		this.onCustomerSelectedCallback = onCustomerSelected;
		initializeUI();
		loadMockCustomers();
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

		// Center - Customer Selection
		mainContent.add(createCustomerSelectionSection(), BorderLayout.CENTER);

		// Bottom - Buttons
		mainContent.add(createBottomSection(), BorderLayout.SOUTH);

		add(mainContent);

		// Dialog settings
		setSize(800, 600);
		setMinimumSize(new Dimension(800, 600));
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
		JLabel titleLabel = new JLabel("Select a Customer");
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
		gbc.insets = new Insets(0, 0, 0, 10);
		searchButton = createStyledButton("Search", SIDEBAR_ACTIVE);
		searchButton.setPreferredSize(new Dimension(100, 38));
		searchButton.addActionListener(e -> performSearch());
		searchPanel.add(searchButton, gbc);

		gbc.gridx = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		clearFiltersButton = createStyledButton("Clear Filters", new Color(120, 120, 120));
		clearFiltersButton.setPreferredSize(new Dimension(120, 38));
		clearFiltersButton.addActionListener(e -> clearFilters());
		searchPanel.add(clearFiltersButton, gbc);

		headerPanel.add(searchPanel, BorderLayout.CENTER);

		return headerPanel;
	}

	/**
	 * Clear all filters and reset the customer list
	 */
	private void clearFilters() {
		searchField.setText("");
		loadMockCustomers();
	}

	/**
	 * Create customer selection section
	 */
	private JPanel createCustomerSelectionSection() {
		JPanel selectionPanel = new JPanel(new BorderLayout(0, 10));
		selectionPanel.setBackground(Color.WHITE);

		// Instructions
		JLabel instructionLabel = new JLabel("Select a customer from the list:");
		instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		instructionLabel.setForeground(TEXT_DARK);
		selectionPanel.add(instructionLabel, BorderLayout.NORTH);

		// Create customer table
		createCustomerTable();
		JScrollPane scrollPane = new JScrollPane(customerTable);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
		scrollPane.setPreferredSize(new Dimension(0, 400));

		selectionPanel.add(scrollPane, BorderLayout.CENTER);

		return selectionPanel;
	}

	/**
	 * Create customer table
	 */
	private void createCustomerTable() {
		String[] columns = { "", "Customer Name", "Address" };
		customerTableModel = new DefaultTableModel(columns, 0) {
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

		customerTable = new JTable(customerTableModel);
		customerTable.setFont(new Font("Arial", Font.PLAIN, 14));
		customerTable.setRowHeight(45);
		customerTable.setShowGrid(true);
		customerTable.setGridColor(new Color(220, 210, 200));
		customerTable.setBackground(TABLE_ROW_EVEN);
		customerTable.setSelectionBackground(new Color(255, 235, 205));
		customerTable.setSelectionForeground(TEXT_DARK);
		customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Set column widths
		customerTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		customerTable.getColumnModel().getColumn(0).setMaxWidth(50);
		customerTable.getColumnModel().getColumn(1).setPreferredWidth(250);
		customerTable.getColumnModel().getColumn(2).setPreferredWidth(450);

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

		// Custom renderer for checkbox column
		customerTable.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {
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

				setHorizontalAlignment(SwingConstants.LEFT);
				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		};

		customerTable.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
		customerTable.getColumnModel().getColumn(2).setCellRenderer(cellRenderer);

		// Selection listener
		customerTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				int selectedRow = customerTable.getSelectedRow();
				handleRowSelection(selectedRow);
			}
		});

		// Mouse listener for clicking anywhere on the row
		customerTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = customerTable.rowAtPoint(e.getPoint());
				if (row >= 0) {
					// Toggle selection
					if (selectedRowIndex == row) {
						// Deselect if clicking the same row
						customerTable.clearSelection();
						handleRowSelection(-1);
					} else {
						// Select the clicked row
						customerTable.setRowSelectionInterval(row, row);
						handleRowSelection(row);
					}

					// Double-click behavior - select and close
					if (e.getClickCount() == 2 && row < availableCustomers.size()) {
						handleSelectCustomer();
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
		for (int i = 0; i < customerTableModel.getRowCount(); i++) {
			customerTableModel.setValueAt(false, i, 0);
		}

		// Set selected row
		if (selectedRow >= 0 && selectedRow < availableCustomers.size()) {
			selectedCustomer = availableCustomers.get(selectedRow);
			selectedRowIndex = selectedRow;
			customerTableModel.setValueAt(true, selectedRow, 0);
		} else {
			selectedCustomer = null;
			selectedRowIndex = -1;
		}

		updateSelectButtonState();
	}

	/**
	 * Load mock customers
	 */
	private void loadMockCustomers() {
		availableCustomers.clear();

		// Mock customers
		availableCustomers.add(new Customer(1, "ABC Corporation", "123 Rizal Avenue, Makati City", "09171234567"));
		availableCustomers
				.add(new Customer(2, "XYZ Trading Company", "456 Bonifacio Drive, Taguig City", "09187654321"));
		availableCustomers
				.add(new Customer(3, "Golden Sun Industries", "789 Commonwealth Ave, Quezon City", "09191112233"));
		availableCustomers
				.add(new Customer(4, "Metro Manila Distributors", "321 Ortigas Avenue, Pasig City", "09201234567"));
		availableCustomers
				.add(new Customer(5, "Pacific Foods Inc.", "654 Shaw Boulevard, Mandaluyong City", "09217654321"));
		availableCustomers.add(new Customer(6, "Premier Trading Co.", "987 Quezon Avenue, Quezon City", "09221112233"));
		availableCustomers.add(new Customer(7, "Southeast Merchants", "147 EDSA, Makati City", "09231234567"));
		availableCustomers.add(new Customer(8, "Global Supplies Ltd.", "258 Ayala Avenue, Makati City", "09247654321"));

		updateCustomerTable();
	}

	/**
	 * Perform search
	 */
	private void performSearch() {
		String searchText = searchField.getText().trim().toLowerCase();

		if (searchText.isEmpty()) {
			loadMockCustomers();
			return;
		}

		availableCustomers.clear();

		// Mock search - filter by name only
		List<Customer> allCustomers = new ArrayList<>();
		allCustomers.add(new Customer(1, "ABC Corporation", "123 Rizal Avenue, Makati City", "09171234567"));
		allCustomers.add(new Customer(2, "XYZ Trading Company", "456 Bonifacio Drive, Taguig City", "09187654321"));
		allCustomers.add(new Customer(3, "Golden Sun Industries", "789 Commonwealth Ave, Quezon City", "09191112233"));
		allCustomers.add(new Customer(4, "Metro Manila Distributors", "321 Ortigas Avenue, Pasig City", "09201234567"));
		allCustomers.add(new Customer(5, "Pacific Foods Inc.", "654 Shaw Boulevard, Mandaluyong City", "09217654321"));
		allCustomers.add(new Customer(6, "Premier Trading Co.", "987 Quezon Avenue, Quezon City", "09221112233"));
		allCustomers.add(new Customer(7, "Southeast Merchants", "147 EDSA, Makati City", "09231234567"));
		allCustomers.add(new Customer(8, "Global Supplies Ltd.", "258 Ayala Avenue, Makati City", "09247654321"));

		for (Customer customer : allCustomers) {
			if (customer.name.toLowerCase().contains(searchText)) {
				availableCustomers.add(customer);
			}
		}

		updateCustomerTable();
	}

	/**
	 * Update customer table
	 */
	private void updateCustomerTable() {
		customerTableModel.setRowCount(0);

		for (Customer customer : availableCustomers) {
			customerTableModel.addRow(new Object[] { false, // Checkbox unchecked by default
					customer.name, customer.address });
		}

		selectedCustomer = null;
		selectedRowIndex = -1;
		updateSelectButtonState();
	}

	/**
	 * Create bottom section with buttons
	 */
	private JPanel createBottomSection() {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(Color.WHITE);

		cancelButton = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelButton.setPreferredSize(new Dimension(120, 42));
		cancelButton.addActionListener(e -> dispose());
		buttonPanel.add(cancelButton);

		selectButton = createStyledButton("Select", ACCENT_GOLD);
		selectButton.setPreferredSize(new Dimension(120, 42));
		selectButton.setEnabled(false);
		selectButton.addActionListener(e -> handleSelectCustomer());
		buttonPanel.add(selectButton);

		return buttonPanel;
	}

	/**
	 * Update select button state
	 */
	private void updateSelectButtonState() {
		selectButton.setEnabled(selectedCustomer != null);
	}

	/**
	 * Handle select customer
	 */
	private void handleSelectCustomer() {
		if (selectedCustomer == null) {
			ToastNotification.showError(this, "Please select a customer.");
			return;
		}

		// Call callback with selected customer
		if (onCustomerSelectedCallback != null) {
			onCustomerSelectedCallback.accept(selectedCustomer);
		}

		dispose();
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
	public static void show(Window parent, Consumer<Customer> onCustomerSelected) {
		SelectCustomerDialog dialog = new SelectCustomerDialog(parent, onCustomerSelected);
		dialog.setVisible(true);
	}
}