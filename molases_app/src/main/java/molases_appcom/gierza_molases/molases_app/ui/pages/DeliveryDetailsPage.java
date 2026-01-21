package molases_appcom.gierza_molases.molases_app.ui.pages;

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.DeliveryDataClasses.BranchDeliveryData;
import com.gierza_molases.molases_app.model.DeliveryDataClasses.CustomerDeliveryData;
import com.gierza_molases.molases_app.model.DeliveryDataClasses.CustomerTotals;
import com.gierza_molases.molases_app.model.DeliveryDataClasses.FinancialTotals;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.ui.components.delivery.UIComponentFactory;
import com.gierza_molases.molases_app.ui.dialogs.Delivery.AddCustomerBranchDialog;
import com.gierza_molases.molases_app.ui.dialogs.Delivery.CustomerBranchDetailsDialog;
import com.gierza_molases.molases_app.ui.dialogs.Delivery.SetPaymentTypeDialog;

public class DeliveryDetailsPage {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color STEP_INACTIVE = new Color(200, 190, 180);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color SECTION_BG = new Color(245, 242, 237);
	private static final Color PROFIT_GREEN = new Color(34, 139, 34);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);
	private static final Color TABLE_HOVER = new Color(245, 239, 231);
	private static final Color PLACEHOLDER_COLOR = new Color(150, 150, 150);
	private static final Color STATUS_SCHEDULED = new Color(255, 140, 0);
	private static final Color STATUS_DELIVERED = new Color(34, 139, 34);

	// Delivery data
	private static int deliveryId;
	private static String deliveryName;
	private static Date deliveryDate;
	private static String deliveryStatus;
	private static Map<String, Double> expenses;
	private static Map<Customer, CustomerDeliveryData> customerDeliveries;

	// UI References
	private static JPanel mainContentPanel;
	private static JScrollPane mainScrollPane;
	private static JTable expenseTable;
	private static DefaultTableModel expenseTableModel;
	private static JLabel totalExpensesLabel;
	private static JPanel financialSummaryPanel;
	private static JTable customerTable;
	private static DefaultTableModel customerTableModel;
	private static JTextField expenseNameInput;
	private static JTextField expenseAmountInput;
	private static JPanel expenseInputPanel;

	public static JPanel createPanel(int deliveryId, Runnable onBack) {
		DeliveryDetailsPage.deliveryId = deliveryId;

		// TODO: Replace mock data with actual fetching from controller/database
		initializeMockData();

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

		// Header
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(CONTENT_BG);
		headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

		JLabel titleLabel = new JLabel("Delivery Details");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
		titleLabel.setForeground(TEXT_DARK);
		headerPanel.add(titleLabel, BorderLayout.WEST);

		JButton backButton = UIComponentFactory.createStyledButton("← Back to List", new Color(120, 120, 120));
		backButton.addActionListener(e -> onBack.run());
		headerPanel.add(backButton, BorderLayout.EAST);

		mainPanel.add(headerPanel, BorderLayout.NORTH);

		// Content panel with new structure
		mainContentPanel = new JPanel(new GridBagLayout());
		mainContentPanel.setBackground(CONTENT_BG);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.6;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(0, 5, 10, 5);

		// TOP ROW - LEFT: Delivery Details
		mainContentPanel.add(createDeliveryDetailsSection(), gbc);

		// TOP ROW - RIGHT: Financial Summary
		gbc.gridx = 1;
		gbc.weightx = 0.4;
		financialSummaryPanel = createFinancialSummaryPanel();
		mainContentPanel.add(financialSummaryPanel, gbc);

		// MIDDLE ROW - Expenses (FULL WIDTH)
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 5, 10, 5);
		mainContentPanel.add(createExpensesSection(), gbc);

		// BOTTOM ROW - Customer Deliveries (FULL WIDTH)
		gbc.gridy = 2;
		mainContentPanel.add(createCustomerDeliveriesTable(), gbc);

		// Add vertical glue
		gbc.gridy = 3;
		gbc.weighty = 1.0;
		mainContentPanel.add(Box.createVerticalGlue(), gbc);

		// Wrap in scroll pane
		mainScrollPane = new JScrollPane(mainContentPanel);
		mainScrollPane.setBorder(null);
		mainScrollPane.getViewport().setBackground(CONTENT_BG);
		mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		mainScrollPane.setMinimumSize(new Dimension(1024, 600));

		mainPanel.add(mainScrollPane, BorderLayout.CENTER);

		// Action Buttons
		mainPanel.add(createActionButtonsPanel(onBack), BorderLayout.SOUTH);

		return mainPanel;
	}

	private static void initializeMockData() {
		deliveryName = "Manila Delivery #001";
		deliveryDate = new Date();
		deliveryStatus = "Scheduled"; // Set initial status

		// Mock expenses
		expenses = new LinkedHashMap<>();
		expenses.put("Parking Fee", 120.0);
		expenses.put("Office Supplies", 450.0);
		expenses.put("Internet Load", 200.0);
		expenses.put("Mobile Load", 150.0);
		expenses.put("Electricity Bill", 1800.0);
		expenses.put("Water Bill", 650.0);
		expenses.put("Snacks", 180.0);
		expenses.put("Coffee", 140.0);

		// Mock customers and deliveries
		customerDeliveries = new LinkedHashMap<>();

		// Customer 1 - Individual
		Customer customer1 = new Customer(1, "individual", "Juan", "D.", "Dela Cruz", null, "09171234567",
				"123 Main St, Manila", null);

		CustomerDeliveryData data1 = new CustomerDeliveryData();
		data1.paymentType = "Not Set";
		data1.branches = new LinkedHashMap<>();

		// Branch 1 for Customer 1
		Branch branch1 = new Branch(1, 1, "Juan D. Dela Cruz", "123 Rizal St, Manila", "Main warehouse", null);
		BranchDeliveryData branchData1 = new BranchDeliveryData();
		branchData1.status = "Delivered";
		branchData1.products = new ArrayList<>();
		branchData1.products.add(new ProductWithQuantity(createProduct(1, "Molasses Type A", 150.0, 100.0), 10));
		branchData1.products.add(new ProductWithQuantity(createProduct(2, "Molasses Type B", 200.0, 150.0), 5));
		branchData1.products.add(new ProductWithQuantity(createProduct(3, "Molasses Type C", 250.0, 180.0), 8));
		data1.branches.put(branch1, branchData1);

		// Branch 2 for Customer 1
		Branch branch2 = new Branch(2, 1, "Juan D. Dela Cruz", "456 Bonifacio Ave, Manila", "Secondary location", null);
		BranchDeliveryData branchData2 = new BranchDeliveryData();
		branchData2.status = "Delivered";
		branchData2.products = new ArrayList<>();
		branchData2.products.add(new ProductWithQuantity(createProduct(1, "Molasses Type A", 150.0, 100.0), 15));
		branchData2.products.add(new ProductWithQuantity(createProduct(4, "Molasses Premium", 300.0, 220.0), 6));
		data1.branches.put(branch2, branchData2);

		customerDeliveries.put(customer1, data1);

		// Customer 2 - Company
		Customer customer2 = new Customer(2, "company", null, null, null, "Santos Trading Co.", "09181234567",
				"789 Business Ave, QC", null);

		CustomerDeliveryData data2 = new CustomerDeliveryData();
		data2.paymentType = "Paid Cash";
		data2.branches = new LinkedHashMap<>();

		Branch branch3 = new Branch(3, 2, "Santos Trading Co.", "789 Quezon Blvd, Quezon City", "Main office", null);
		BranchDeliveryData branchData3 = new BranchDeliveryData();
		branchData3.status = "Delivered";
		branchData3.products = new ArrayList<>();
		branchData3.products.add(new ProductWithQuantity(createProduct(2, "Molasses Type B", 200.0, 150.0), 12));
		branchData3.products.add(new ProductWithQuantity(createProduct(3, "Molasses Type C", 250.0, 180.0), 10));
		data2.branches.put(branch3, branchData3);

		customerDeliveries.put(customer2, data2);

		// Customer 3 - Individual
		Customer customer3 = new Customer(3, "individual", "Pedro", "M.", "Reyes", null, "09191234567",
				"321 Street, Cavite", null);

		CustomerDeliveryData data3 = new CustomerDeliveryData();
		data3.paymentType = "Not Set";
		data3.branches = new LinkedHashMap<>();

		Branch branch4 = new Branch(4, 3, "Pedro M. Reyes", "321 Aguinaldo Highway, Cavite", "Warehouse", null);
		BranchDeliveryData branchData4 = new BranchDeliveryData();
		branchData4.status = "Delivered";
		branchData4.products = new ArrayList<>();
		branchData4.products.add(new ProductWithQuantity(createProduct(1, "Molasses Type A", 150.0, 100.0), 20));
		branchData4.products.add(new ProductWithQuantity(createProduct(4, "Molasses Premium", 300.0, 220.0), 8));
		branchData4.products.add(new ProductWithQuantity(createProduct(5, "Molasses Special", 180.0, 130.0), 15));
		data3.branches.put(branch4, branchData4);

		customerDeliveries.put(customer3, data3);
	}

	private static Product createProduct(int id, String name, double sellingPrice, double capital) {
		return new Product(id, name, sellingPrice, capital, null);
	}

	private static JPanel createDeliveryDetailsSection() {
		JPanel section = new JPanel(new BorderLayout());
		section.setBackground(SECTION_BG);
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1),
				new EmptyBorder(12, 12, 12, 12)));

		JLabel titleLabel = new JLabel("Delivery Details");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(SIDEBAR_ACTIVE);
		titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
		section.add(titleLabel, BorderLayout.NORTH);

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBackground(SECTION_BG);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(3, 0, 3, 0);

		JPanel firstRow = new JPanel(new BorderLayout());
		firstRow.setBackground(SECTION_BG);

		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		namePanel.setBackground(SECTION_BG);

		JLabel nameLabel = new JLabel("Delivery Name: ");
		nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
		nameLabel.setForeground(TEXT_DARK);
		namePanel.add(nameLabel);

		JLabel nameValue = new JLabel(deliveryName);
		nameValue.setFont(new Font("Arial", Font.PLAIN, 14));
		nameValue.setForeground(TEXT_DARK);
		namePanel.add(nameValue);

		firstRow.add(namePanel, BorderLayout.WEST);

		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		statusPanel.setBackground(SECTION_BG);

		JLabel statusLabel = new JLabel("Status: ");
		statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
		statusLabel.setForeground(TEXT_DARK);
		statusPanel.add(statusLabel);

		JLabel statusValue = new JLabel(deliveryStatus);
		statusValue.setFont(new Font("Arial", Font.BOLD, 14));
		statusValue.setForeground(deliveryStatus.equals("Delivered") ? STATUS_DELIVERED : STATUS_SCHEDULED);
		statusPanel.add(statusValue);

		firstRow.add(statusPanel, BorderLayout.EAST);

		contentPanel.add(firstRow, gbc);

		gbc.gridy++;
		JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		datePanel.setBackground(SECTION_BG);

		JLabel dateLabel = new JLabel("Date: ");
		dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
		dateLabel.setForeground(TEXT_DARK);
		datePanel.add(dateLabel);

		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
		JLabel dateValue = new JLabel(sdf.format(deliveryDate));
		dateValue.setFont(new Font("Arial", Font.PLAIN, 14));
		dateValue.setForeground(TEXT_DARK);
		datePanel.add(dateValue);

		contentPanel.add(datePanel, gbc);

		section.add(contentPanel, BorderLayout.CENTER);

		return section;
	}

	private static JPanel createFinancialSummaryPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(SECTION_BG);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ACCENT_GOLD, 2),
				new EmptyBorder(12, 12, 12, 12)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 10, 0);

		JLabel titleLabel = new JLabel("Financial Summary");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(SIDEBAR_ACTIVE);
		titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
		panel.add(titleLabel, gbc);

		FinancialTotals totals = calculateFinancialTotals();

		JPanel rowsPanel = new JPanel(new GridBagLayout());
		rowsPanel.setBackground(SECTION_BG);

		GridBagConstraints rowGbc = new GridBagConstraints();
		rowGbc.gridx = 0;
		rowGbc.gridy = 0;
		rowGbc.anchor = GridBagConstraints.WEST;
		rowGbc.fill = GridBagConstraints.HORIZONTAL;
		rowGbc.weightx = 1.0;
		rowGbc.insets = new Insets(5, 0, 5, 0);

		rowsPanel.add(createFinancialRow("Gross Sales:", totals.grossSales, TEXT_DARK), rowGbc);

		rowGbc.gridy++;
		rowsPanel.add(createFinancialRow("Total Capital:", totals.totalCapital, SIDEBAR_ACTIVE), rowGbc);

		rowGbc.gridy++;
		rowsPanel.add(createFinancialRow("Gross Profit:", totals.grossProfit, PROFIT_GREEN), rowGbc);

		rowGbc.gridy++;
		rowGbc.insets = new Insets(5, 0, 10, 0);
		rowsPanel.add(createFinancialRow("Total Expenses:", totals.totalExpenses, new Color(180, 100, 50)), rowGbc);

		rowGbc.gridy++;
		rowGbc.insets = new Insets(10, 0, 0, 0);
		JPanel netProfitPanel = new JPanel(new BorderLayout());
		netProfitPanel.setBackground(SECTION_BG);
		netProfitPanel.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, ACCENT_GOLD));
		netProfitPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);

		JPanel netProfitRow = createFinancialRow("Net Profit:", totals.netProfit,
				totals.netProfit >= 0 ? PROFIT_GREEN : new Color(180, 50, 50));
		netProfitPanel.add(netProfitRow, BorderLayout.CENTER);

		rowsPanel.add(netProfitPanel, rowGbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 0, 0);
		panel.add(rowsPanel, gbc);

		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(Box.createVerticalGlue(), gbc);

		return panel;
	}

	private static void updateFinancialSummary() {
		if (financialSummaryPanel != null) {
			JPanel parent = (JPanel) financialSummaryPanel.getParent();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 0.4;
			gbc.weighty = 0.0;
			gbc.insets = new Insets(0, 5, 10, 5);

			parent.remove(financialSummaryPanel);
			financialSummaryPanel = createFinancialSummaryPanel();
			parent.add(financialSummaryPanel, gbc);
			parent.revalidate();
			parent.repaint();
		}
	}

	private static JPanel createFinancialRow(String label, double value, Color valueColor) {
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(SECTION_BG);

		JLabel labelComponent = new JLabel(label);
		labelComponent.setFont(new Font("Arial", Font.BOLD, 16));
		labelComponent.setForeground(TEXT_DARK);

		JLabel valueComponent = new JLabel(String.format("₱%,.2f", value));
		valueComponent.setFont(new Font("Arial", Font.BOLD, 18));
		valueComponent.setForeground(valueColor);

		row.add(labelComponent, BorderLayout.WEST);
		row.add(valueComponent, BorderLayout.EAST);

		return row;
	}

	private static FinancialTotals calculateFinancialTotals() {
		FinancialTotals totals = new FinancialTotals();

		for (CustomerDeliveryData customerData : customerDeliveries.values()) {
			for (Map.Entry<Branch, BranchDeliveryData> branchEntry : customerData.branches.entrySet()) {
				BranchDeliveryData branchData = branchEntry.getValue();

				if ("Delivered".equals(branchData.status)) {
					for (ProductWithQuantity product : branchData.products) {
						totals.grossSales += product.getTotalSellingPrice();
						totals.totalCapital += product.getTotalCapital();
					}
				}
			}
		}

		totals.grossProfit = totals.grossSales - totals.totalCapital;
		totals.totalExpenses = calculateTotalExpenses();
		totals.netProfit = totals.grossProfit - totals.totalExpenses;

		return totals;
	}

	private static JPanel createExpensesSection() {
		JPanel section = new JPanel(new BorderLayout());
		section.setBackground(SECTION_BG);
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1),
				new EmptyBorder(12, 12, 12, 12)));

		JLabel titleLabel = new JLabel("Expenses");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(SIDEBAR_ACTIVE);
		titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
		section.add(titleLabel, BorderLayout.NORTH);

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBackground(SECTION_BG);

		JPanel tablePanel = createExpensesTable();
		contentPanel.add(tablePanel, BorderLayout.CENTER);

		if (!deliveryStatus.equals("Delivered")) {
			expenseInputPanel = createExpenseInputPanel();
			contentPanel.add(expenseInputPanel, BorderLayout.SOUTH);
		}

		section.add(contentPanel, BorderLayout.CENTER);

		return section;
	}

	private static JPanel createExpensesTable() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(SECTION_BG);
		tablePanel.setBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1));
		tablePanel.setPreferredSize(new Dimension(900, 250));

		String[] columnNames = { "Expense Name", "Amount", "Action" };
		expenseTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 2;
			}
		};

		for (Map.Entry<String, Double> entry : expenses.entrySet()) {
			expenseTableModel
					.addRow(new Object[] { entry.getKey(), String.format("₱%.2f", entry.getValue()), "Remove" });
		}

		expenseTable = new JTable(expenseTableModel);
		expenseTable.setFont(new Font("Arial", Font.PLAIN, 14));
		expenseTable.setRowHeight(32);
		expenseTable.setBackground(Color.WHITE);
		expenseTable.setGridColor(STEP_INACTIVE);

		JTableHeader header = expenseTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(new Color(240, 235, 225));
		header.setForeground(TEXT_DARK);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));

		expenseTable.getColumnModel().getColumn(0).setPreferredWidth(450);
		expenseTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		expenseTable.getColumnModel().getColumn(2).setPreferredWidth(150);

		expenseTable.getColumnModel().getColumn(2).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
			JButton removeBtn = UIComponentFactory.createStyledButton("Remove", new Color(180, 50, 50));
			removeBtn.setPreferredSize(new Dimension(90, 26));
			return removeBtn;
		});

		expenseTable.getColumnModel().getColumn(2).setCellEditor(new javax.swing.DefaultCellEditor(new JTextField()) {
			private JButton button = UIComponentFactory.createStyledButton("Remove", new Color(180, 50, 50));
			private ActionListener currentListener = null;

			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
					int column) {
				if (currentListener != null) {
					button.removeActionListener(currentListener);
				}

				currentListener = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						fireEditingStopped();
						removeExpenseRow(row);
					}
				};

				button.addActionListener(currentListener);
				return button;
			}

			@Override
			public Object getCellEditorValue() {
				return "";
			}
		});

		JScrollPane scrollPane = new JScrollPane(expenseTable);
		scrollPane.setBorder(null);
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
		totalPanel.setBackground(new Color(240, 235, 225));
		totalPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, STEP_INACTIVE));

		JLabel totalTextLabel = new JLabel("Total:");
		totalTextLabel.setFont(new Font("Arial", Font.BOLD, 15));
		totalTextLabel.setForeground(TEXT_DARK);
		totalPanel.add(totalTextLabel);

		totalExpensesLabel = new JLabel(String.format("₱%.2f", calculateTotalExpenses()));
		totalExpensesLabel.setFont(new Font("Arial", Font.BOLD, 16));
		totalExpensesLabel.setForeground(ACCENT_GOLD);
		totalPanel.add(totalExpensesLabel);

		tablePanel.add(totalPanel, BorderLayout.SOUTH);

		return tablePanel;
	}

	private static JPanel createExpenseInputPanel() {
		JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
		inputPanel.setBackground(SECTION_BG);
		inputPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

		JLabel nameInputLabel = new JLabel("Expense Name:");
		nameInputLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		nameInputLabel.setForeground(TEXT_DARK);
		inputPanel.add(nameInputLabel);

		expenseNameInput = createPlaceholderTextField("e.g., Fuel, Toll Fee", 15);
		expenseNameInput.setPreferredSize(new Dimension(180, 32));
		inputPanel.add(expenseNameInput);

		JLabel amountInputLabel = new JLabel("Amount:");
		amountInputLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		amountInputLabel.setForeground(TEXT_DARK);
		inputPanel.add(amountInputLabel);

		expenseAmountInput = createPlaceholderTextField("0.00", 10);
		expenseAmountInput.setPreferredSize(new Dimension(100, 32));
		inputPanel.add(expenseAmountInput);

		JButton addBtn = UIComponentFactory.createStyledButton("+ Add", ACCENT_GOLD);
		addBtn.setPreferredSize(new Dimension(100, 32));
		addBtn.addActionListener(e -> addExpenseToTable());
		inputPanel.add(addBtn);

		return inputPanel;
	}

	private static JTextField createPlaceholderTextField(String placeholder, int columns) {
		JTextField textField = new JTextField(columns);
		textField.setFont(new Font("Arial", Font.PLAIN, 14));
		textField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));

		textField.setText(placeholder);
		textField.setForeground(PLACEHOLDER_COLOR);

		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (textField.getText().equals(placeholder)) {
					textField.setText("");
					textField.setForeground(TEXT_DARK);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (textField.getText().trim().isEmpty()) {
					textField.setText(placeholder);
					textField.setForeground(PLACEHOLDER_COLOR);
				}
			}
		});

		return textField;
	}

	private static String getTextFieldValue(JTextField textField, String placeholder) {
		String text = textField.getText();
		if (text.equals(placeholder)) {
			return "";
		}
		return text.trim();
	}

	private static void addExpenseToTable() {
		String name = getTextFieldValue(expenseNameInput, "e.g., Fuel, Toll Fee");
		String amountStr = getTextFieldValue(expenseAmountInput, "0.00");

		if (name.isEmpty() || amountStr.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Please fill in both expense name and amount", "Warning",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			double amount = Double.parseDouble(amountStr);
			if (amount <= 0) {
				JOptionPane.showMessageDialog(null, "Amount must be greater than 0", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			expenseTableModel.addRow(new Object[] { name, String.format("₱%.2f", amount), "Remove" });
			expenses.put(name, amount);

			expenseNameInput.setText("e.g., Fuel, Toll Fee");
			expenseNameInput.setForeground(PLACEHOLDER_COLOR);
			expenseAmountInput.setText("0.00");
			expenseAmountInput.setForeground(PLACEHOLDER_COLOR);
			expenseNameInput.requestFocus();

			updateTotal();
			updateFinancialSummary();

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Please enter a valid number for amount", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void removeExpenseRow(int row) {
		String expenseName = expenseTableModel.getValueAt(row, 0).toString();
		expenses.remove(expenseName);
		expenseTableModel.removeRow(row);
		updateTotal();
		updateFinancialSummary();
	}

	private static void updateTotal() {
		double total = 0.0;

		for (int i = 0; i < expenseTableModel.getRowCount(); i++) {
			String amountStr = expenseTableModel.getValueAt(i, 1).toString();
			amountStr = amountStr.replace("₱", "").replace(",", "").trim();
			total += Double.parseDouble(amountStr);
		}

		totalExpensesLabel.setText(String.format("₱%.2f", total));
	}

	private static double calculateTotalExpenses() {
		return expenses.values().stream().mapToDouble(Double::doubleValue).sum();
	}

	private static JPanel createCustomerDeliveriesTable() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(SECTION_BG);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1),
				new EmptyBorder(12, 12, 12, 12)));

		// Header panel with title and add button
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(SECTION_BG);
		headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		JLabel titleLabel = new JLabel("Customer Deliveries");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(SIDEBAR_ACTIVE);
		headerPanel.add(titleLabel, BorderLayout.WEST);

		// Only show Add Customer button if NOT delivered
		if (!deliveryStatus.equals("Delivered")) {
			JButton addCustomerBtn = UIComponentFactory.createStyledButton("+ Add Customer", ACCENT_GOLD);
			addCustomerBtn.setPreferredSize(new Dimension(150, 35));
			addCustomerBtn.addActionListener(e -> {
				AddCustomerBranchDialog.show(SwingUtilities.getWindowAncestor(mainContentPanel), // parent window
						() -> {

						}, "additionalDelivery" // additional type
				);
			});
			headerPanel.add(addCustomerBtn, BorderLayout.EAST);
		}

		panel.add(headerPanel, BorderLayout.NORTH);

		// Create table
		String[] columnNames = { "Customer Name", "Payment Type", "Branches", "Total Sales (₱)", "Total Profit (₱)",
				"Actions", "Customer" };
		customerTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 6) {
					return Customer.class;
				}
				return Object.class;
			}
		};

		// Populate table
		updateCustomerTable();

		customerTable = new JTable(customerTableModel);
		customerTable.setFont(new Font("Arial", Font.PLAIN, 13));
		customerTable.setRowHeight(45);
		customerTable.setShowGrid(true);
		customerTable.setGridColor(new Color(220, 210, 200));
		customerTable.setBackground(TABLE_ROW_EVEN);
		customerTable.setSelectionBackground(TABLE_HOVER);
		customerTable.setSelectionForeground(TEXT_DARK);

		// Style table header
		JTableHeader header = customerTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 13));
		header.setBackground(SIDEBAR_ACTIVE);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));

		// Column widths
		customerTable.getColumnModel().getColumn(0).setPreferredWidth(220);
		customerTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		customerTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		customerTable.getColumnModel().getColumn(3).setPreferredWidth(130);
		customerTable.getColumnModel().getColumn(4).setPreferredWidth(130);
		customerTable.getColumnModel().getColumn(5).setPreferredWidth(100);
		customerTable.getColumnModel().getColumn(6).setPreferredWidth(0);

		// Center align numeric columns and actions
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		customerTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		customerTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		customerTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
		customerTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

		// Hide the Customer column completely
		customerTable.getColumnModel().getColumn(6).setMinWidth(0);
		customerTable.getColumnModel().getColumn(6).setMaxWidth(0);
		customerTable.getColumnModel().getColumn(6).setWidth(0);

		// Alternating row colors
		customerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				if (column == 0) {
					setHorizontalAlignment(SwingConstants.LEFT);
				} else if (column >= 2 && column <= 4) {
					setHorizontalAlignment(SwingConstants.CENTER);
				} else {
					setHorizontalAlignment(SwingConstants.LEFT);
				}

				((JLabel) c).setBorder(new EmptyBorder(6, 12, 6, 12));
				return c;
			}
		});

		// Mouse listener for actions column
		customerTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = customerTable.rowAtPoint(e.getPoint());
				int col = customerTable.columnAtPoint(e.getPoint());

				if (col == 5 && row >= 0) {
					Customer customer = (Customer) customerTableModel.getValueAt(row, 6);
					showActionMenu(e.getComponent(), customer);
				}
			}
		});

		customerTable.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int col = customerTable.columnAtPoint(e.getPoint());
				customerTable.setCursor(new Cursor(col == 5 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
			}
		});

		JScrollPane scrollPane = new JScrollPane(customerTable);
		scrollPane.setBorder(null);
		scrollPane.setPreferredSize(new Dimension(900, 350));
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private static void updateCustomerTable() {
		customerTableModel.setRowCount(0);

		for (Map.Entry<Customer, CustomerDeliveryData> entry : customerDeliveries.entrySet()) {
			Customer customer = entry.getKey();
			CustomerDeliveryData data = entry.getValue();

			CustomerTotals totals = calculateCustomerTotals(data);

			customerTableModel.addRow(new Object[] { customer.getDisplayName(), data.paymentType, data.branches.size(),
					String.format("₱%,.2f", totals.sales), String.format("₱%,.2f", totals.profit), "⚙ Actions",
					customer });
		}
	}

	private static CustomerTotals calculateCustomerTotals(CustomerDeliveryData data) {
		CustomerTotals totals = new CustomerTotals();

		for (Map.Entry<Branch, BranchDeliveryData> branchEntry : data.branches.entrySet()) {
			BranchDeliveryData branchData = branchEntry.getValue();

			if ("Delivered".equals(branchData.status)) {
				for (ProductWithQuantity product : branchData.products) {
					totals.sales += product.getTotalSellingPrice();
					totals.capital += product.getTotalCapital();
				}
			}
		}

		totals.profit = totals.sales - totals.capital;
		return totals;
	}

	private static void showActionMenu(Component parent, Customer customer) {
		JDialog actionDialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Actions");
		actionDialog.setLayout(new GridBagLayout());
		actionDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 20, 5, 20);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Choose Action for: " + customer.getDisplayName());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		actionDialog.add(titleLabel, gbc);

		// Only show "Set Payment" button if status is NOT "Delivered"
		if (!deliveryStatus.equals("Delivered")) {
			gbc.gridy++;
			gbc.insets = new Insets(15, 20, 5, 20);

			JButton setPaymentBtn = createActionButton("💳 Set Payment", ACCENT_GOLD);
			setPaymentBtn.addActionListener(e -> {
				actionDialog.dispose();
				CustomerDeliveryData data = customerDeliveries.get(customer);
				SetPaymentTypeDialog.show(SwingUtilities.getWindowAncestor(parent), customer, data.paymentType,
						(paymentType, partialAmount, loadDate) -> {
							String displayText = paymentType;
							if (paymentType.equals("Partial") && partialAmount != null) {
								displayText = String.format("Partial (₱%.2f)", partialAmount);
							} else if (paymentType.equals("Load") && loadDate != null) {
								SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
								displayText = "Load (Due: " + sdf.format(loadDate) + ")";
							}
							data.paymentType = displayText;
							updateCustomerTable();
						});
			});
			actionDialog.add(setPaymentBtn, gbc);
		}

		gbc.gridy++;
		gbc.insets = new Insets(deliveryStatus.equals("Delivered") ? 15 : 5, 20, 5, 20);
		JButton viewDetailsBtn = createActionButton("👁️ View Details", SIDEBAR_ACTIVE);
		viewDetailsBtn.addActionListener(e -> {
			actionDialog.dispose();
			CustomerDeliveryData data = customerDeliveries.get(customer);
			CustomerBranchDetailsDialog dialog = new CustomerBranchDetailsDialog(
					SwingUtilities.getWindowAncestor(parent), customer, data, () -> {
						updateCustomerTable();
						updateFinancialSummary();
					});
			dialog.setVisible(true);
		});
		actionDialog.add(viewDetailsBtn, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 20, 15, 20);
		JButton cancelBtn = createActionButton("Cancel", new Color(120, 120, 120));
		cancelBtn.addActionListener(e -> actionDialog.dispose());
		actionDialog.add(cancelBtn, gbc);

		actionDialog.pack();
		actionDialog.setMinimumSize(new Dimension(380, deliveryStatus.equals("Delivered") ? 180 : 220));
		actionDialog.setLocationRelativeTo(null);
		actionDialog.setVisible(true);
	}

	private static JButton createActionButton(String text, Color bgColor) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 14));
		button.setBackground(bgColor);
		button.setForeground(TEXT_LIGHT);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setPreferredSize(new Dimension(320, 45));
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setBorder(new EmptyBorder(10, 15, 10, 15));

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

	private static JPanel createActionButtonsPanel(Runnable onBack) {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(CONTENT_BG);
		buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

		JButton printBtn = UIComponentFactory.createStyledButton("🖨️ Print", SIDEBAR_ACTIVE);
		printBtn.setPreferredSize(new Dimension(120, 40));
		printBtn.addActionListener(e -> printDelivery());
		buttonPanel.add(printBtn);

		// Only show these buttons if status is NOT "Delivered"
		if (!deliveryStatus.equals("Delivered")) {
			JButton cancelBtn = UIComponentFactory.createStyledButton("❌ Mark as Cancelled", new Color(180, 50, 50));
			cancelBtn.setPreferredSize(new Dimension(180, 40));
			cancelBtn.addActionListener(e -> markAsCancelled());
			buttonPanel.add(cancelBtn);

			JButton deliveredBtn = UIComponentFactory.createStyledButton("✓ Mark as Delivered", ACCENT_GOLD);
			deliveredBtn.setPreferredSize(new Dimension(180, 40));
			deliveredBtn.addActionListener(e -> markAsDelivered(onBack));
			buttonPanel.add(deliveredBtn);
		}

		return buttonPanel;
	}

	private static void printDelivery() {
		java.awt.print.PrinterJob printerJob = java.awt.print.PrinterJob.getPrinterJob();
		java.awt.print.PageFormat pageFormat = printerJob.defaultPage();
		pageFormat.setOrientation(java.awt.print.PageFormat.PORTRAIT);

		printerJob.setPrintable(new java.awt.print.Printable() {
			@Override
			public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) {
				if (pageIndex > 0) {
					return java.awt.print.Printable.NO_SUCH_PAGE;
				}

				java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
				g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

				double scaleX = pageFormat.getImageableWidth() / mainContentPanel.getWidth();
				double scaleY = pageFormat.getImageableHeight() / mainContentPanel.getHeight();
				double scale = Math.min(scaleX, scaleY);

				g2d.scale(scale, scale);
				mainContentPanel.printAll(graphics);

				return java.awt.print.Printable.PAGE_EXISTS;
			}
		});

		if (printerJob.printDialog()) {
			try {
				printerJob.print();
				JOptionPane.showMessageDialog(null, "Printing completed successfully!", "Print",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (java.awt.print.PrinterException e) {
				JOptionPane.showMessageDialog(null, "Failed to print: " + e.getMessage(), "Print Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private static void markAsCancelled() {
		int result = JOptionPane.showConfirmDialog(null,
				"Are you sure you want to mark ALL branches as CANCELLED?\nThis action will update all delivery statuses.",
				"Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (result == JOptionPane.YES_OPTION) {
			for (CustomerDeliveryData customerData : customerDeliveries.values()) {
				for (BranchDeliveryData branchData : customerData.branches.values()) {
					branchData.status = "Cancelled";
				}
			}

			updateCustomerTable();
			updateFinancialSummary();

			JOptionPane.showMessageDialog(null, "All branches marked as Cancelled", "Success",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private static void markAsDelivered(Runnable onBack) {
		int result = JOptionPane.showConfirmDialog(null,
				"Are you sure you want to save this delivery?\nAll current statuses and payment information will be saved.",
				"Confirm Delivery", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (result == JOptionPane.YES_OPTION) {
			// TODO: Save to database/controller
			JOptionPane.showMessageDialog(null, "Delivery saved successfully!", "Success",
					JOptionPane.INFORMATION_MESSAGE);
			onBack.run();
		}
	}

}