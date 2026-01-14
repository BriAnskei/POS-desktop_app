package com.gierza_molases.molases_app.ui.components.delivery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.ui.components.delivery.DeliveryFormStep1.Step1Data;
import com.gierza_molases.molases_app.ui.components.delivery.DeliveryFormStep2.BranchData;
import com.gierza_molases.molases_app.ui.components.delivery.DeliveryFormStep2.CustomerDeliveryData;
import com.gierza_molases.molases_app.ui.components.delivery.DeliveryFormStep2.Step2Data;
import com.gierza_molases.molases_app.ui.dialogs.Delivery.CustomerDeliveryDetailsDialog;

public class DeliveryFormStep3 {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color STEP_INACTIVE = new Color(200, 190, 180);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);
	private static final Color SECTION_BG = new Color(245, 242, 237);
	private static final Color PROFIT_GREEN = new Color(34, 139, 34);

	private Step1Data step1Data;
	private Step2Data step2Data;
	private JPanel mainPanel;

	// Financial totals
	private double grossSales = 0.0;
	private double totalCapital = 0.0;
	private double grossProfit = 0.0;
	private double totalExpenses = 0.0;
	private double netProfit = 0.0;

	// Customer summary data
	private Map<String, CustomerSummary> customerSummaries;

	public JPanel createPanel(Step1Data step1Data, Step2Data step2Data) {
		this.step1Data = step1Data;
		this.step2Data = step2Data;

		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBackground(CONTENT_BG);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 10, 15, 10);

		// Title
		JLabel summaryTitle = new JLabel("Delivery Summary");
		summaryTitle.setFont(new Font("Arial", Font.BOLD, 24));
		summaryTitle.setForeground(TEXT_DARK);
		summaryTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
		mainPanel.add(summaryTitle, gbc);

		// Delivery Details Section (unchanged)
		gbc.gridy++;
		mainPanel.add(createDeliveryDetailsSection(), gbc);

		// Customer Deliveries and Financial Summary Section (side by side)
		gbc.gridy++;
		mainPanel.add(createCustomerAndFinancialSection(), gbc);

		// Add vertical glue to push everything to the top
		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		mainPanel.add(Box.createVerticalGlue(), gbc);

		return mainPanel;
	}

	private JPanel createDeliveryDetailsSection() {
		JPanel section = new JPanel(new BorderLayout());
		section.setBackground(SECTION_BG);
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1),
				new EmptyBorder(15, 15, 15, 15)));

		// Header spanning both columns
		JLabel titleLabel = new JLabel("Delivery Details");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(SIDEBAR_ACTIVE);
		titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
		section.add(titleLabel, BorderLayout.NORTH);

		// Two-column content panel
		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBackground(SECTION_BG);

		GridBagConstraints gbc = new GridBagConstraints();

		// LEFT COLUMN - Delivery Info
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.4;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 0, 0, 20);

		JPanel leftPanel = new JPanel(new GridBagLayout());
		leftPanel.setBackground(SECTION_BG);

		GridBagConstraints leftGbc = new GridBagConstraints();
		leftGbc.gridx = 0;
		leftGbc.gridy = 0;
		leftGbc.anchor = GridBagConstraints.WEST;
		leftGbc.insets = new Insets(5, 0, 5, 10);

		// Delivery Name
		JLabel nameLabel = new JLabel("Delivery Name:");
		nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
		nameLabel.setForeground(TEXT_DARK);
		leftPanel.add(nameLabel, leftGbc);

		leftGbc.gridx = 1;
		leftGbc.weightx = 1.0;
		JLabel nameValue = new JLabel(step1Data.deliveryName);
		nameValue.setFont(new Font("Arial", Font.PLAIN, 14));
		nameValue.setForeground(TEXT_DARK);
		leftPanel.add(nameValue, leftGbc);

		// Delivery Date
		leftGbc.gridx = 0;
		leftGbc.gridy++;
		leftGbc.weightx = 0.0;
		JLabel dateLabel = new JLabel("Date:");
		dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
		dateLabel.setForeground(TEXT_DARK);
		leftPanel.add(dateLabel, leftGbc);

		leftGbc.gridx = 1;
		leftGbc.weightx = 1.0;
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
		JLabel dateValue = new JLabel(sdf.format(step1Data.deliveryDate));
		dateValue.setFont(new Font("Arial", Font.PLAIN, 14));
		dateValue.setForeground(TEXT_DARK);
		leftPanel.add(dateValue, leftGbc);

		contentPanel.add(leftPanel, gbc);

		// RIGHT COLUMN - Expenses
		gbc.gridx = 1;
		gbc.weightx = 0.6;
		gbc.insets = new Insets(0, 0, 0, 0);

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBackground(SECTION_BG);

		// Expenses list
		JPanel expensesListPanel = new JPanel();
		expensesListPanel.setLayout(new GridBagLayout());
		expensesListPanel.setBackground(SECTION_BG);

		GridBagConstraints expGbc = new GridBagConstraints();
		expGbc.gridx = 0;
		expGbc.gridy = 0;
		expGbc.anchor = GridBagConstraints.WEST;
		expGbc.fill = GridBagConstraints.HORIZONTAL;
		expGbc.weightx = 1.0;
		expGbc.insets = new Insets(3, 0, 3, 0);

		totalExpenses = 0.0;

		for (Map.Entry<String, Double> expense : step1Data.expenses.entrySet()) {
			JPanel expenseRow = new JPanel(new BorderLayout());
			expenseRow.setBackground(SECTION_BG);

			JLabel expenseNameLabel = new JLabel("• " + expense.getKey());
			expenseNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
			expenseNameLabel.setForeground(TEXT_DARK);
			expenseRow.add(expenseNameLabel, BorderLayout.WEST);

			JLabel expenseAmountLabel = new JLabel(String.format("₱%.2f", expense.getValue()));
			expenseAmountLabel.setFont(new Font("Arial", Font.BOLD, 14));
			expenseAmountLabel.setForeground(SIDEBAR_ACTIVE);
			expenseRow.add(expenseAmountLabel, BorderLayout.EAST);

			expensesListPanel.add(expenseRow, expGbc);
			expGbc.gridy++;

			totalExpenses += expense.getValue();
		}

		rightPanel.add(expensesListPanel, BorderLayout.CENTER);

		// Total Expenses at bottom of right column
		JPanel totalPanel = new JPanel(new BorderLayout());
		totalPanel.setBackground(new Color(240, 235, 225));
		totalPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(2, 0, 0, 0, STEP_INACTIVE), new EmptyBorder(10, 0, 5, 0)));

		JLabel totalLabel = new JLabel("Total Expenses:");
		totalLabel.setFont(new Font("Arial", Font.BOLD, 15));
		totalLabel.setForeground(TEXT_DARK);
		totalPanel.add(totalLabel, BorderLayout.WEST);

		JLabel totalValue = new JLabel(String.format("₱%.2f", totalExpenses));
		totalValue.setFont(new Font("Arial", Font.BOLD, 16));
		totalValue.setForeground(ACCENT_GOLD);
		totalPanel.add(totalValue, BorderLayout.EAST);

		rightPanel.add(totalPanel, BorderLayout.SOUTH);

		contentPanel.add(rightPanel, gbc);

		section.add(contentPanel, BorderLayout.CENTER);

		return section;
	}

	private JPanel createCustomerAndFinancialSection() {
		JPanel section = new JPanel(new GridBagLayout());
		section.setBackground(CONTENT_BG);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.6;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 0, 0, 15);

		// LEFT SIDE - Customer Deliveries (60%)
		section.add(createCustomerDeliveriesPanel(), gbc);

		// RIGHT SIDE - Financial Summary (40%)
		gbc.gridx = 1;
		gbc.weightx = 0.4;
		gbc.insets = new Insets(0, 0, 0, 0);
		section.add(createFinancialSummarySection(), gbc);

		return section;
	}

	private JPanel createCustomerDeliveriesPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(SECTION_BG);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1),
				new EmptyBorder(15, 15, 15, 15)));

		// Header with title and buttons
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(SECTION_BG);
		headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

		JLabel titleLabel = new JLabel("Customer Deliveries");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(SIDEBAR_ACTIVE);
		headerPanel.add(titleLabel, BorderLayout.WEST);

		// Action buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		buttonPanel.setBackground(SECTION_BG);

		JButton viewDetailsBtn = UIComponentFactory.createStyledButton("View Details", SIDEBAR_ACTIVE);
		viewDetailsBtn.setPreferredSize(new Dimension(120, 35));
		viewDetailsBtn.addActionListener(e -> {
			CustomerDeliveryDetailsDialog dialog = new CustomerDeliveryDetailsDialog(
					(JFrame) javax.swing.SwingUtilities.getWindowAncestor(mainPanel), step2Data);
			dialog.setVisible(true);
		});
		buttonPanel.add(viewDetailsBtn);

		headerPanel.add(buttonPanel, BorderLayout.EAST);
		panel.add(headerPanel, BorderLayout.NORTH);

		// Calculate customer summaries
		calculateCustomerSummaries();

		// Customer summary table
		JPanel tablePanel = createCustomerSummaryTable();
		panel.add(tablePanel, BorderLayout.CENTER);

		return panel;
	}

	private void calculateCustomerSummaries() {
		customerSummaries = new java.util.LinkedHashMap<>();
		grossSales = 0.0;
		totalCapital = 0.0;

		// Loop through customers (already grouped in new structure)
		for (CustomerDeliveryData customer : step2Data.customers) {
			double customerSales = 0.0;
			double customerCapital = 0.0;

			// Loop through branches for this customer
			for (BranchData branch : customer.branches) {
				// Get mock products for this branch
				List<ProductOrderData> products = getMockProductsForBranch(customer.customerName, branch);

				for (ProductOrderData product : products) {
					double totalPrice = product.quantity * product.sellingPrice;
					double totalCapitalCost = product.quantity * product.capital;

					customerSales += totalPrice;
					customerCapital += totalCapitalCost;
				}
			}

			// Create customer summary
			CustomerSummary summary = new CustomerSummary();
			summary.customerName = customer.customerName;
			summary.totalBranches = customer.getBranchCount();
			summary.totalSales = customerSales;

			customerSummaries.put(customer.customerName, summary);

			grossSales += customerSales;
			totalCapital += customerCapital;
		}
	}

	private JPanel createCustomerSummaryTable() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(Color.WHITE);

		String[] columnNames = { "Customer Name", "Total Branches", "Total Sales (₱)" };
		DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		// Populate table with customer summaries
		for (CustomerSummary summary : customerSummaries.values()) {
			tableModel.addRow(new Object[] { summary.customerName, summary.totalBranches,
					String.format("₱%,.2f", summary.totalSales) });
		}

		JTable table = new JTable(tableModel);
		table.setFont(new Font("Arial", Font.PLAIN, 14));
		table.setRowHeight(40);
		table.setShowGrid(true);
		table.setGridColor(new Color(220, 210, 200));
		table.setBackground(TABLE_ROW_EVEN);

		// Style table header
		JTableHeader header = table.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 13));
		header.setBackground(SIDEBAR_ACTIVE);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));

		// Column widths
		table.getColumnModel().getColumn(0).setPreferredWidth(250);
		table.getColumnModel().getColumn(1).setPreferredWidth(120);
		table.getColumnModel().getColumn(2).setPreferredWidth(150);

		// Center align numeric columns
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

		// Alternating row colors
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				if (column == 0) {
					setHorizontalAlignment(SwingConstants.LEFT);
				} else {
					setHorizontalAlignment(SwingConstants.CENTER);
				}

				((JLabel) c).setBorder(new EmptyBorder(8, 15, 8, 15));
				return c;
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(null);
		scrollPane.setPreferredSize(new Dimension(520, Math.min(400, (customerSummaries.size() + 1) * 40 + 45)));
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		return tablePanel;
	}

	private JPanel createFinancialSummarySection() {
		grossProfit = grossSales - totalCapital;
		netProfit = grossProfit - totalExpenses;

		JPanel section = new JPanel();
		section.setLayout(new GridBagLayout());
		section.setBackground(SECTION_BG);
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ACCENT_GOLD, 2),
				new EmptyBorder(20, 20, 20, 20)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 15, 0);

		// Title
		JLabel titleLabel = new JLabel("Financial Summary");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(SIDEBAR_ACTIVE);
		titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
		section.add(titleLabel, gbc);

		// Financial rows container
		JPanel rowsPanel = new JPanel(new GridBagLayout());
		rowsPanel.setBackground(SECTION_BG);

		GridBagConstraints rowGbc = new GridBagConstraints();
		rowGbc.gridx = 0;
		rowGbc.gridy = 0;
		rowGbc.anchor = GridBagConstraints.WEST;
		rowGbc.fill = GridBagConstraints.HORIZONTAL;
		rowGbc.weightx = 1.0;
		rowGbc.insets = new Insets(8, 0, 8, 0);

		// Financial rows
		rowsPanel.add(createFinancialRow("Gross Sales:", grossSales, TEXT_DARK), rowGbc);

		rowGbc.gridy++;
		rowsPanel.add(createFinancialRow("Total Capital:", totalCapital, SIDEBAR_ACTIVE), rowGbc);

		rowGbc.gridy++;
		rowsPanel.add(createFinancialRow("Gross Profit:", grossProfit, PROFIT_GREEN), rowGbc);

		rowGbc.gridy++;
		rowGbc.insets = new Insets(8, 0, 15, 0);
		rowsPanel.add(createFinancialRow("Total Expenses:", totalExpenses, new Color(180, 100, 50)), rowGbc);

		// Net Profit (highlighted)
		rowGbc.gridy++;
		rowGbc.insets = new Insets(15, 0, 0, 0);
		JPanel netProfitPanel = new JPanel(new BorderLayout());
		netProfitPanel.setBackground(SECTION_BG);
		netProfitPanel.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, ACCENT_GOLD));
		netProfitPanel.add(Box.createVerticalStrut(15), BorderLayout.NORTH);

		JPanel netProfitRow = createFinancialRow("Net Profit:", netProfit,
				netProfit >= 0 ? PROFIT_GREEN : new Color(180, 50, 50));
		netProfitPanel.add(netProfitRow, BorderLayout.CENTER);

		rowsPanel.add(netProfitPanel, rowGbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 0, 0);
		section.add(rowsPanel, gbc);

		// Add vertical glue to push content to top
		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		section.add(Box.createVerticalGlue(), gbc);

		return section;
	}

	private JPanel createFinancialRow(String label, double value, Color valueColor) {
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

	// MOCK DATA - Generate products based on customer name and branch
	private List<ProductOrderData> getMockProductsForBranch(String customerName, BranchData branch) {
		List<ProductOrderData> products = new ArrayList<>();

		// Generate mock products based on customer name
		if (customerName.contains("ABC")) {
			products.add(new ProductOrderData(1, "Premium Molasses A", 10, 100.0, 70.0));
			products.add(new ProductOrderData(2, "Standard Molasses B", 5, 200.0, 150.0));
		} else if (customerName.contains("XYZ")) {
			products.add(new ProductOrderData(3, "Organic Molasses C", 8, 150.0, 100.0));
			products.add(new ProductOrderData(4, "Dark Molasses D", 12, 120.0, 85.0));
			products.add(new ProductOrderData(5, "Light Molasses E", 6, 180.0, 130.0));
		} else if (customerName.contains("Golden")) {
			products.add(new ProductOrderData(6, "Golden Molasses F", 15, 90.0, 60.0));
		} else {
			// Default products for any other customer
			products.add(new ProductOrderData(7, "Standard Molasses", 10, 110.0, 75.0));
		}

		return products;
	}

	public void printSummary() {
		// Create a print job
		java.awt.print.PrinterJob printerJob = java.awt.print.PrinterJob.getPrinterJob();

		// Set up the page format
		java.awt.print.PageFormat pageFormat = printerJob.defaultPage();
		pageFormat.setOrientation(java.awt.print.PageFormat.PORTRAIT);

		// Create a printable component
		printerJob.setPrintable(new java.awt.print.Printable() {
			@Override
			public int print(Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) {
				if (pageIndex > 0) {
					return java.awt.print.Printable.NO_SUCH_PAGE;
				}

				Graphics2D g2d = (Graphics2D) graphics;
				g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

				// Scale to fit the page width
				double scaleX = pageFormat.getImageableWidth() / mainPanel.getWidth();
				double scaleY = pageFormat.getImageableHeight() / mainPanel.getHeight();
				double scale = Math.min(scaleX, scaleY);

				g2d.scale(scale, scale);

				// Print the panel
				mainPanel.printAll(graphics);

				return java.awt.print.Printable.PAGE_EXISTS;
			}
		});

		// Show print dialog
		if (printerJob.printDialog()) {
			try {
				printerJob.print();
				JOptionPane.showMessageDialog(null, "Printing completed successfully!", "Print",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (PrinterException e) {
				JOptionPane.showMessageDialog(null, "Failed to print: " + e.getMessage(), "Print Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null, "Printing was cancelled.", "Print Cancelled",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	// Helper class for customer summary data
	private static class CustomerSummary {
		public String customerName;
		public int totalBranches;
		public double totalSales;
	}

	// TEMPORARY Mock Product Order Data Class
	// TODO: Replace with actual implementation in Step 2
	public static class ProductOrderData {
		public int productId;
		public String productName;
		public int quantity;
		public double sellingPrice;
		public double capital;
		public double profitPerUnit;

		public ProductOrderData(int productId, String productName, int quantity, double sellingPrice, double capital) {
			this.productId = productId;
			this.productName = productName;
			this.quantity = quantity;
			this.sellingPrice = sellingPrice;
			this.capital = capital;
			this.profitPerUnit = sellingPrice - capital;
		}
	}
}