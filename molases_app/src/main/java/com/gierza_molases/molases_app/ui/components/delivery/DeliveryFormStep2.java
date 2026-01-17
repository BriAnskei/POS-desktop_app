package com.gierza_molases.molases_app.ui.components.delivery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.UiController.NewDeliveryController;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.ui.dialogs.Delivery.AddCustomerBranchDialog;
import com.gierza_molases.molases_app.ui.dialogs.Delivery.ViewCustomerDeliveryDetails;

public class DeliveryFormStep2 {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color STEP_INACTIVE = new Color(200, 190, 180);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);

	private DefaultTableModel customerTableModel;
	private JTable customerTable;

	private NewDeliveryController newDeliveryController;

	public DeliveryFormStep2(NewDeliveryController newDeliveryController) {
		this.newDeliveryController = newDeliveryController;
	}

	public JPanel createPanel() {
		JPanel formPanel = new JPanel(new GridBagLayout());
		formPanel.setBackground(CONTENT_BG);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Section Title and Add Button
		JPanel titleSection = new JPanel(new BorderLayout());
		titleSection.setBackground(CONTENT_BG);

		JLabel customersTitle = new JLabel("Customers & Branches *");
		customersTitle.setFont(new Font("Arial", Font.BOLD, 16));
		customersTitle.setForeground(TEXT_DARK);
		titleSection.add(customersTitle, BorderLayout.WEST);

		JButton addCustomerBtn = UIComponentFactory.createStyledButton("+ Add Customer", ACCENT_GOLD);
		addCustomerBtn.setPreferredSize(new Dimension(150, 35));
		addCustomerBtn.addActionListener(e -> {
			AddCustomerBranchDialog.show(SwingUtilities.getWindowAncestor(formPanel), () -> {
				// Reload table after customer is added
				loadCustomersFromState();
			}, newDeliveryController);
		});
		titleSection.add(addCustomerBtn, BorderLayout.EAST);

		gbc.gridwidth = 2;
		formPanel.add(titleSection, gbc);

		// Customer & Branch Table
		gbc.gridy++;
		gbc.insets = new Insets(10, 10, 10, 10);
		JPanel tablePanel = createCustomerBranchTable();
		formPanel.add(tablePanel, gbc);

		// Add vertical glue to push everything to the top
		gbc.gridy++;
		gbc.weighty = 1.0;
		formPanel.add(Box.createVerticalGlue(), gbc);

		// Load data from state
		loadCustomersFromState();

		return formPanel;
	}

	private JPanel createCustomerBranchTable() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(CONTENT_BG);
		tablePanel.setBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1));
		tablePanel.setPreferredSize(new Dimension(900, 400));

		// Create table model with new columns
		String[] columnNames = { "Customer Name", "Branch Count", "Product Count", "Actions" };
		customerTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		customerTable = new JTable(customerTableModel);
		customerTable.setFont(new Font("Arial", Font.PLAIN, 14));
		customerTable.setRowHeight(50);
		customerTable.setShowGrid(true);
		customerTable.setGridColor(new Color(220, 210, 200));
		customerTable.setBackground(TABLE_ROW_EVEN);
		customerTable.setSelectionBackground(new Color(245, 239, 231));
		customerTable.setSelectionForeground(TEXT_DARK);

		// Set column widths
		customerTable.getColumnModel().getColumn(0).setPreferredWidth(350);
		customerTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		customerTable.getColumnModel().getColumn(2).setPreferredWidth(150);
		customerTable.getColumnModel().getColumn(3).setPreferredWidth(150);

		// Style table header
		JTableHeader header = customerTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(SIDEBAR_ACTIVE);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));

		// Custom header renderer
		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setBackground(SIDEBAR_ACTIVE);
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

				if (column == 3) {
					setHorizontalAlignment(SwingConstants.CENTER);
				} else if (column == 1 || column == 2) {
					setHorizontalAlignment(SwingConstants.CENTER);
				} else {
					setHorizontalAlignment(SwingConstants.LEFT);
				}

				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		});

		// Mouse listener for actions column
		customerTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = customerTable.rowAtPoint(e.getPoint());
				int col = customerTable.columnAtPoint(e.getPoint());

				if (col == 3 && row >= 0) {
					Customer customer = getCustomerAtRow(row);
					if (customer != null) {
						showCustomerActionMenu(e.getComponent(), customer);
					}
				}
			}
		});

		customerTable.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int col = customerTable.columnAtPoint(e.getPoint());
				customerTable.setCursor(new Cursor(col == 3 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
			}
		});

		JScrollPane scrollPane = new JScrollPane(customerTable);
		scrollPane.setBorder(null);
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		return tablePanel;
	}

	/**
	 * Load customers from state and populate table
	 */
	private void loadCustomersFromState() {
		customerTableModel.setRowCount(0);

		Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliveries = newDeliveryController
				.getCustomerDeliveries();

		for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> entry : customerDeliveries.entrySet()) {
			Customer customer = entry.getKey();
			Map<Branch, List<ProductWithQuantity>> branches = entry.getValue();

			int branchCount = branches.size();

			// Calculate total product count across all branches
			int productCount = branches.values().stream().mapToInt(products -> products != null ? products.size() : 0)
					.sum();

			customerTableModel.addRow(new Object[] { customer.getDisplayName(),
					branchCount + (branchCount == 1 ? " Branch" : " Branches"),
					productCount + (productCount == 1 ? " Product" : " Products"), "⚙ Actions" });
		}
	}

	/**
	 * Get customer at specific row index
	 */
	private Customer getCustomerAtRow(int row) {
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliveries = newDeliveryController
				.getCustomerDeliveries();

		int currentRow = 0;
		for (Customer customer : customerDeliveries.keySet()) {
			if (currentRow == row) {
				return customer;
			}
			currentRow++;
		}
		return null;
	}

	/**
	 * Show action menu for customer
	 */
	private void showCustomerActionMenu(Component parent, Customer customer) {
		JDialog actionDialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Actions");
		actionDialog.setLayout(new GridBagLayout());
		actionDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 20, 5, 20);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Actions for: " + customer.getDisplayName());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		actionDialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(15, 20, 5, 20);

		JButton viewDetailsBtn = UIComponentFactory.createActionButton("👁️ View Details", new Color(70, 130, 180));
		viewDetailsBtn.addActionListener(e -> {
			actionDialog.dispose();
			showCustomerDetails(customer);
		});
		actionDialog.add(viewDetailsBtn, gbc);

		gbc.gridy++;
		JButton removeBtn = UIComponentFactory.createActionButton("🗑️ Remove from Delivery", new Color(180, 50, 50));
		removeBtn.addActionListener(e -> {
			actionDialog.dispose();
			removeCustomer(customer);
		});
		actionDialog.add(removeBtn, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 20, 15, 20);
		JButton cancelBtn = UIComponentFactory.createActionButton("Cancel", new Color(120, 120, 120));
		cancelBtn.addActionListener(e -> actionDialog.dispose());
		actionDialog.add(cancelBtn, gbc);

		actionDialog.pack();
		actionDialog.setMinimumSize(new Dimension(380, 230));
		actionDialog.setLocationRelativeTo(null);
		actionDialog.setVisible(true);
	}

	/**
	 * Show customer details (branches and products)
	 */
	private void showCustomerDetails(Customer customer) {
		Map<Branch, List<ProductWithQuantity>> branches = newDeliveryController.getCustomerBranches(customer);
		ViewCustomerDeliveryDetails.show(SwingUtilities.getWindowAncestor(customerTable), customer, branches);
	}

	/**
	 * Remove customer from delivery
	 */
	private void removeCustomer(Customer customer) {
		newDeliveryController.removeCustomerDelivery(customer);
		loadCustomersFromState();

		ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(customerTable),
				customer.getDisplayName() + " removed from delivery");
	}

	/**
	 * Validate that at least one customer is added
	 */
	public boolean validate() {
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliveries = newDeliveryController
				.getCustomerDeliveries();

		if (customerDeliveries.isEmpty()) {
			ToastNotification.showError(SwingUtilities.getWindowAncestor(customerTable),
					"Please add at least one customer/branch to the delivery");
			return false;
		}

		return true;
	}

	/**
	 * Data transfer object for Step 2. Holds a REFERENCE to the customer deliveries
	 * map stored in the controller.
	 * 
	 * Note: This doesn't copy the data - it references the same map object stored
	 * in NewDeliveryController state.
	 */
	public Step2Data getData() {
		Step2Data data = new Step2Data();
		data.customerDeliveries = newDeliveryController.getCustomerDeliveries();
		return data;
	}

	/**
	 * Load data - loads from state (for back navigation)
	 */
	public void loadData(Step2Data data) {
		// Data is already in state, just reload the table
		loadCustomersFromState();
	}

	/**
	 * Step 2 data structure
	 */
	public static class Step2Data {
		public Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliveries;
	}
}