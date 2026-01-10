package com.gierza_molases.molases_app.ui.dialogs.BranchDialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
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
import com.gierza_molases.molases_app.UiController.BranchesController;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.service.CustomerService;
import com.gierza_molases.molases_app.ui.components.ToastNotification;

public class AddBranchDialog extends JDialog {

	// Color Palette
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);

	// Service
	private CustomerService customerService = AppContext.customerService;
	private final BranchesController controller; // Add controller
	private final Runnable onSaveCallback; // Add callback

	// Data
	private List<Customer> displayedCustomers = new ArrayList<>();

	// UI Components
	private JTable customerTable;
	private DefaultTableModel customerModel;
	private JTextField customerSearchField;
	private JTextField addressField;
	private JTextField noteField;
	private int selectedCustomerIndex = -1;

	/**
	 * Constructor
	 */
	public AddBranchDialog(Window parent, BranchesController controller, Runnable onSaveCallback) {
		super(parent, "Add New Branch", ModalityType.APPLICATION_MODAL);
		this.controller = controller;
		this.onSaveCallback = onSaveCallback;
		this.customerService = AppContext.customerService; // Keep for customer search

		initializeUI();
		loadCustomers();
	}

	/**
	 * Load customers from service (fetch 20 customers without search)
	 */
	private void loadCustomers() {
		try {
			displayedCustomers = customerService.fetch20Customer("");
			updateCustomerTable();
		} catch (Exception e) {
			ToastNotification.showError(this, "Error loading customers: " + e.getMessage());
			displayedCustomers.clear();
			updateCustomerTable();
		}
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
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Title
		JLabel titleLabel = new JLabel("Add New Branch");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		titleLabel.setForeground(TEXT_DARK);
		add(titleLabel, gbc);

		// Customer Selection Section
		gbc.gridy++;
		gbc.insets = new Insets(20, 30, 5, 30);
		JLabel customerLabel = new JLabel("Select Customer *");
		customerLabel.setFont(new Font("Arial", Font.BOLD, 14));
		customerLabel.setForeground(TEXT_DARK);
		add(customerLabel, gbc);

		// Customer search field with button
		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 5, 30);
		gbc.gridwidth = 1;
		customerSearchField = new JTextField();
		customerSearchField.setFont(new Font("Arial", Font.PLAIN, 14));
		customerSearchField.setPreferredSize(new Dimension(340, 35));
		customerSearchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		add(customerSearchField, gbc);

		// Search button
		gbc.gridx = 1;
		gbc.insets = new Insets(5, 10, 5, 30);
		gbc.fill = GridBagConstraints.NONE;
		JButton searchButton = createStyledButton("Search", SIDEBAR_ACTIVE);
		searchButton.setPreferredSize(new Dimension(100, 35));
		searchButton.addActionListener(e -> performSearch());
		add(searchButton, gbc);

		// Reset gridx for next components
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Customer table
		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 10, 30);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.4;

		createCustomerTable();
		JScrollPane customerScrollPane = new JScrollPane(customerTable);
		customerScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
		customerScrollPane.setPreferredSize(new Dimension(450, 200));
		add(customerScrollPane, gbc);

		// Add search functionality (optional: real-time search on Enter key)
		setupSearchListener();

		// Address field
		gbc.gridy++;
		gbc.insets = new Insets(15, 30, 5, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0;
		JLabel addressLabel = new JLabel("Address *");
		addressLabel.setFont(new Font("Arial", Font.BOLD, 14));
		addressLabel.setForeground(TEXT_DARK);
		add(addressLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 10, 30);
		addressField = new JTextField();
		addressField.setFont(new Font("Arial", Font.PLAIN, 14));
		addressField.setPreferredSize(new Dimension(450, 35));
		addressField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		add(addressField, gbc);

		// Note field
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);
		JLabel noteLabel = new JLabel("Note (Optional)");
		noteLabel.setFont(new Font("Arial", Font.BOLD, 14));
		noteLabel.setForeground(TEXT_DARK);
		add(noteLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 20, 30);
		noteField = new JTextField();
		noteField.setFont(new Font("Arial", Font.PLAIN, 14));
		noteField.setPreferredSize(new Dimension(450, 35));
		noteField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		add(noteField, gbc);

		// Buttons
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 30, 20, 10);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;

		JButton cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> dispose());
		add(cancelBtn, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(20, 10, 20, 30);
		gbc.anchor = GridBagConstraints.WEST;
		JButton saveBtn = createStyledButton("Save", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(120, 40));
		saveBtn.addActionListener(e -> handleSave());
		add(saveBtn, gbc);

		pack();
		setMinimumSize(new Dimension(550, 650));
		setLocationRelativeTo(getParent());
	}

	/**
	 * Create and configure customer table
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
		customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Set column widths
		customerTable.getColumnModel().getColumn(0).setPreferredWidth(60);
		customerTable.getColumnModel().getColumn(0).setMaxWidth(60);
		customerTable.getColumnModel().getColumn(1).setPreferredWidth(390);

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

		// Handle single checkbox selection
		customerModel.addTableModelListener(e -> {
			if (e.getColumn() == 0) {
				int row = e.getFirstRow();
				Boolean isChecked = (Boolean) customerModel.getValueAt(row, 0);

				if (isChecked) {
					for (int i = 0; i < customerModel.getRowCount(); i++) {
						if (i != row) {
							customerModel.setValueAt(Boolean.FALSE, i, 0);
						}
					}
					selectedCustomerIndex = row;
				} else {
					if (selectedCustomerIndex == row) {
						selectedCustomerIndex = -1;
					}
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
	 * Perform search using the service (fetch 20 customers with search term)
	 */
	private void performSearch() {
		String searchTerm = customerSearchField.getText().trim();
		try {
			displayedCustomers = customerService.fetch20Customer(searchTerm);
			updateCustomerTable();

			if (displayedCustomers.isEmpty()) {
				ToastNotification.showInfo(this, "No customers found matching your search.");
			}
		} catch (Exception e) {
			ToastNotification.showError(this, "Error searching customers: " + e.getMessage());
		}
	}

	/**
	 * Update the customer table with current data
	 */
	private void updateCustomerTable() {
		selectedCustomerIndex = -1;
		customerModel.setRowCount(0);

		for (Customer customer : displayedCustomers) {
			customerModel.addRow(new Object[] { Boolean.FALSE, customer.getDisplayName() });
		}
	}

	/**
	 * Handle save button click
	 */
	private void handleSave() {
		String address = addressField.getText().trim();

		if (selectedCustomerIndex == -1) {
			ToastNotification.showError(this, "Please select a customer.");
			return;
		}

		if (address.isEmpty()) {
			ToastNotification.showError(this, "Please enter an address.");
			return;
		}

		setFormEnabled(false);

		// Get selected customer
		Customer selectedCustomer = displayedCustomers.get(selectedCustomerIndex);
		String note = noteField.getText().trim();

		Branch newBranch = new Branch(selectedCustomer.getId(), address, note);

		controller.addBranch(selectedCustomer.getId(), newBranch, () -> {
			// Success callback
			if (onSaveCallback != null) {
				onSaveCallback.run();
			}
			dispose();
		}, () -> {
			// Error callback
			ToastNotification.showError(AddBranchDialog.this, "Failed to add branch");
			setFormEnabled(true);
		});

	}

	private void setFormEnabled(boolean enabled) {
		customerSearchField.setEnabled(enabled);
		customerTable.setEnabled(enabled);
		addressField.setEnabled(enabled);
		noteField.setEnabled(enabled);

		// Disable all buttons in the dialog
		for (Component comp : getContentPane().getComponents()) {
			if (comp instanceof JButton) {
				comp.setEnabled(enabled);
			}
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
				button.setBackground(bgColor.brighter());
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
	public static void show(Window parent, BranchesController controller, Runnable onSaveCallback) {
		AddBranchDialog dialog = new AddBranchDialog(parent, controller, onSaveCallback);
		dialog.setVisible(true);
	}
}