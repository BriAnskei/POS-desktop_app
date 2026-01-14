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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.ui.dialogs.Delivery.AddCustomerBranchDialog;

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
	private List<CustomerDeliveryData> customers;

	public DeliveryFormStep2() {
		customers = new ArrayList<>();
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
			// Open your custom dialog

			AddCustomerBranchDialog.show(SwingUtilities.getWindowAncestor(formPanel), () -> {
				// This callback runs when the dialog is saved successfully
//						AddCustomerBranchDialog.CustomerBranchResult result = AddCustomerBranchDialog.getResult();
//
//						if (result != null) {
//							// Process the result and add to your table
//							handleCustomerBranchResult(result);
//						}
			});
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

		return formPanel;
	}

	private JPanel createCustomerBranchTable() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(CONTENT_BG);
		tablePanel.setBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1));
		tablePanel.setPreferredSize(new Dimension(900, 400));

		// Create table model with new columns
		String[] columnNames = { "Customer Name", "Branch Count", "Actions" };
		customerTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		// Add mock data
		addMockCustomerData();

		customerTable = new JTable(customerTableModel);
		customerTable.setFont(new Font("Arial", Font.PLAIN, 14));
		customerTable.setRowHeight(50);
		customerTable.setShowGrid(true);
		customerTable.setGridColor(new Color(220, 210, 200));
		customerTable.setBackground(TABLE_ROW_EVEN);
		customerTable.setSelectionBackground(new Color(245, 239, 231));
		customerTable.setSelectionForeground(TEXT_DARK);

		// Style table header
		JTableHeader header = customerTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(SIDEBAR_ACTIVE);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));

		// Set column widths
		customerTable.getColumnModel().getColumn(0).setPreferredWidth(400);
		customerTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		customerTable.getColumnModel().getColumn(2).setPreferredWidth(300);

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

				setHorizontalAlignment(column == 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
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

				if (col == 2 && row >= 0 && row < customers.size()) {
					CustomerDeliveryData data = customers.get(row);
					showCustomerActionMenu(e.getComponent(), data, row);
				}
			}
		});

		customerTable.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int col = customerTable.columnAtPoint(e.getPoint());
				customerTable.setCursor(new Cursor(col == 2 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
			}
		});

		JScrollPane scrollPane = new JScrollPane(customerTable);
		scrollPane.setBorder(null);
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		return tablePanel;
	}

	private void addMockCustomerData() {
		customers.clear();

		// ABC Corporation with 2 branches
		CustomerDeliveryData abc = new CustomerDeliveryData("ABC Corporation");
		abc.addBranch(new BranchData("123 Rizal Avenue, Makati City, Metro Manila",
				List.of("Molasses Type A", "Molasses Type B")));
		abc.addBranch(new BranchData("456 Ayala Avenue, Makati City, Metro Manila", List.of("Molasses Type C")));
		customers.add(abc);

		// XYZ Trading Company with 1 branch
		CustomerDeliveryData xyz = new CustomerDeliveryData("XYZ Trading Company");
		xyz.addBranch(new BranchData("456 Bonifacio Drive, Taguig City, Metro Manila",
				List.of("Molasses Type A", "Molasses Type B", "Molasses Type D")));
		customers.add(xyz);

		// Golden Sun Industries with 3 branches
		CustomerDeliveryData golden = new CustomerDeliveryData("Golden Sun Industries");
		golden.addBranch(
				new BranchData("789 Commonwealth Avenue, Quezon City, Metro Manila", List.of("Molasses Type A")));
		golden.addBranch(new BranchData("321 Ortigas Avenue, Pasig City, Metro Manila",
				List.of("Molasses Type B", "Molasses Type C")));
		golden.addBranch(new BranchData("555 España Boulevard, Manila City, Metro Manila", List.of("Molasses Type D")));
		customers.add(golden);

		// Populate table
		customerTableModel.setRowCount(0);
		for (CustomerDeliveryData customer : customers) {
			customerTableModel.addRow(new Object[] { customer.customerName,
					customer.getBranchCount() + " " + (customer.getBranchCount() == 1 ? "Branch" : "Branches"),
					"⚙ Actions" });
		}
	}

	private void showCustomerActionMenu(Component parent, CustomerDeliveryData data, int row) {
		JDialog actionDialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Actions");
		actionDialog.setLayout(new GridBagLayout());
		actionDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 20, 5, 20);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Actions for: " + data.customerName);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		actionDialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(15, 20, 5, 20);

		JButton viewProductsBtn = UIComponentFactory.createActionButton("👁️ View Selected Products",
				new Color(70, 130, 180));
		viewProductsBtn.addActionListener(e -> {
			actionDialog.dispose();
			JOptionPane.showMessageDialog(parent,
					"View Selected Products functionality will be implemented in the next iteration.", "Coming Soon",
					JOptionPane.INFORMATION_MESSAGE);
		});
		actionDialog.add(viewProductsBtn, gbc);

		gbc.gridy++;
		JButton removeBtn = UIComponentFactory.createActionButton("🗑️ Remove from Delivery", new Color(180, 50, 50));
		removeBtn.addActionListener(e -> {
			actionDialog.dispose();
			removeCustomer(row);
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

	private void removeCustomer(int row) {
		if (row >= 0 && row < customers.size()) {
			customers.remove(row);
			customerTableModel.removeRow(row);
		}
	}

	public boolean validate() {
		if (customers.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Please add at least one customer/branch to the delivery",
					"Validation Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	public Step2Data getData() {
		Step2Data data = new Step2Data();
		data.customers = new ArrayList<>(customers);
		return data;
	}

	public void loadData(Step2Data data) {
		if (data == null || data.customers == null)
			return;

		customers.clear();
		customers.addAll(data.customers);

		customerTableModel.setRowCount(0);
		for (CustomerDeliveryData customer : customers) {
			customerTableModel.addRow(new Object[] { customer.customerName,
					customer.getBranchCount() + " " + (customer.getBranchCount() == 1 ? "Branch" : "Branches"),
					"⚙ Actions" });
		}
	}

	// New data structure for customer with multiple branches
	public static class CustomerDeliveryData {
		public String customerName;
		public List<BranchData> branches;

		public CustomerDeliveryData(String customerName) {
			this.customerName = customerName;
			this.branches = new ArrayList<>();
		}

		public void addBranch(BranchData branch) {
			this.branches.add(branch);
		}

		public int getBranchCount() {
			return branches.size();
		}
	}

	// Branch data with address and products
	public static class BranchData {
		public String branchAddress;
		public List<String> products;

		public BranchData(String branchAddress, List<String> products) {
			this.branchAddress = branchAddress;
			this.products = new ArrayList<>(products);
		}
	}

	public static class Step2Data {
		public List<CustomerDeliveryData> customers;
	}
}