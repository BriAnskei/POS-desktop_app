package com.gierza_molases.molases_app.ui.dialogs.Delivery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.ui.components.delivery.DeliveryFormStep2.BranchData;
import com.gierza_molases.molases_app.ui.components.delivery.DeliveryFormStep2.CustomerDeliveryData;
import com.gierza_molases.molases_app.ui.components.delivery.DeliveryFormStep2.Step2Data;
import com.gierza_molases.molases_app.ui.components.delivery.DeliveryFormStep3.ProductOrderData;
import com.gierza_molases.molases_app.ui.components.delivery.UIComponentFactory;

public class CustomerDeliveryDetailsDialog extends JDialog {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color CUSTOMER_BG = new Color(245, 242, 237);
	private static final Color BRANCH_BG = new Color(255, 252, 247);
	private static final Color PRODUCT_BG = new Color(255, 255, 255);
	private static final Color HOVER_COLOR = new Color(240, 235, 225);
	private static final Color PROFIT_GREEN = new Color(34, 139, 34);

	private Step2Data step2Data;
	private Map<String, Boolean> customerExpandedState = new HashMap<>();
	private Map<String, Boolean> branchExpandedState = new HashMap<>();
	private Map<String, String> branchAddressMockData = new HashMap<>();
	private JPanel contentPanel;

	public CustomerDeliveryDetailsDialog(JFrame parent, Step2Data step2Data) {
		super(parent, "Customer Delivery Details", true);
		this.step2Data = step2Data;

		// Initialize mock addresses for branches
		initializeMockAddresses();

		// Initialize all customers and branches as collapsed
		for (CustomerDeliveryData customer : step2Data.customers) {
			customerExpandedState.put(customer.customerName, false);
			for (BranchData branch : customer.branches) {
				String branchKey = customer.customerName + "_" + getBranchAddress(customer.customerName, branch);
				branchExpandedState.put(branchKey, false);
			}
		}

		setLayout(new BorderLayout());
		setBackground(CONTENT_BG);

		// Header
		add(createHeader(), BorderLayout.NORTH);

		// Content
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(CONTENT_BG);
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		buildContent();

		JScrollPane scrollPane = new JScrollPane(contentPanel);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, BorderLayout.CENTER);

		// Footer with close button
		add(createFooter(), BorderLayout.SOUTH);

		// Dialog settings
		setSize(1000, 700);
		setLocationRelativeTo(parent);
	}

	private void initializeMockAddresses() {
		// Mock addresses for different customers and branches
		// Format: "customerName_branchIndex" -> "address"
		branchAddressMockData.put("ABC_0", "123 Main St, Butuan City");
		branchAddressMockData.put("ABC_1", "456 Commerce Ave, Butuan City");

		branchAddressMockData.put("XYZ_0", "789 Industrial Rd, Butuan City");
		branchAddressMockData.put("XYZ_1", "321 Business Park, Butuan City");
		branchAddressMockData.put("XYZ_2", "555 Trade Center, Butuan City");

		branchAddressMockData.put("Golden_0", "888 Golden Plaza, Butuan City");
		branchAddressMockData.put("Golden_1", "999 Sunshine Blvd, Butuan City");

		// Default addresses for other customers
		branchAddressMockData.put("default_0", "100 Central St, Butuan City");
		branchAddressMockData.put("default_1", "200 Downtown Ave, Butuan City");
	}

	private String getBranchAddress(String customerName, BranchData branch) {
		// Get branch index from customer's branch list
		CustomerDeliveryData customer = step2Data.customers.stream().filter(c -> c.customerName.equals(customerName))
				.findFirst().orElse(null);

		if (customer != null) {
			int branchIndex = customer.branches.indexOf(branch);

			// Try to find mock address based on customer name pattern
			String addressKey = null;
			if (customerName.contains("ABC")) {
				addressKey = "ABC_" + branchIndex;
			} else if (customerName.contains("XYZ")) {
				addressKey = "XYZ_" + branchIndex;
			} else if (customerName.contains("Golden")) {
				addressKey = "Golden_" + branchIndex;
			} else {
				addressKey = "default_" + branchIndex;
			}

			return branchAddressMockData.getOrDefault(addressKey, "Unknown Address, Butuan City");
		}

		return "Unknown Address, Butuan City";
	}

	private JPanel createHeader() {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(SIDEBAR_ACTIVE);
		header.setBorder(new EmptyBorder(20, 25, 20, 25));

		JLabel title = new JLabel("Customer Delivery Details");
		title.setFont(new Font("Arial", Font.BOLD, 22));
		title.setForeground(Color.WHITE);

		JLabel subtitle = new JLabel("Expand customers and branches to view detailed information");
		subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
		subtitle.setForeground(new Color(230, 220, 210));

		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setBackground(SIDEBAR_ACTIVE);
		textPanel.add(title);
		textPanel.add(Box.createVerticalStrut(5));
		textPanel.add(subtitle);

		header.add(textPanel, BorderLayout.WEST);

		return header;
	}

	private JPanel createFooter() {
		JPanel footer = new JPanel(new BorderLayout());
		footer.setBackground(CONTENT_BG);
		footer.setBorder(new EmptyBorder(15, 25, 15, 25));

		JButton closeButton = UIComponentFactory.createStyledButton("Close", SIDEBAR_ACTIVE);
		closeButton.setPreferredSize(new Dimension(120, 40));
		closeButton.addActionListener(e -> dispose());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(CONTENT_BG);
		buttonPanel.add(closeButton);

		footer.add(buttonPanel, BorderLayout.EAST);

		return footer;
	}

	private void buildContent() {
		contentPanel.removeAll();

		for (CustomerDeliveryData customer : step2Data.customers) {
			contentPanel.add(createCustomerPanel(customer));
			contentPanel.add(Box.createVerticalStrut(4));
		}

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	private JPanel createCustomerPanel(CustomerDeliveryData customer) {
		JPanel customerPanel = new JPanel();
		customerPanel.setLayout(new BoxLayout(customerPanel, BoxLayout.Y_AXIS));
		customerPanel.setBackground(CUSTOMER_BG);
		customerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SIDEBAR_ACTIVE, 2),
				new EmptyBorder(0, 0, 0, 0)));

		// Customer Header (clickable)
		JPanel customerHeader = createCustomerHeader(customer);
		customerPanel.add(customerHeader);

		// Customer Content (branches) - only shown if expanded
		if (customerExpandedState.get(customer.customerName)) {
			JPanel branchesContainer = new JPanel();
			branchesContainer.setLayout(new BoxLayout(branchesContainer, BoxLayout.Y_AXIS));
			branchesContainer.setBackground(CUSTOMER_BG);
			branchesContainer.setBorder(new EmptyBorder(5, 15, 10, 10));

			for (BranchData branch : customer.branches) {
				branchesContainer.add(createBranchPanel(customer.customerName, branch));
				branchesContainer.add(Box.createVerticalStrut(5));
			}

			customerPanel.add(branchesContainer);
		}

		return customerPanel;
	}

	private JPanel createCustomerHeader(CustomerDeliveryData customer) {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(CUSTOMER_BG);
		header.setBorder(new EmptyBorder(10, 10, 10, 10));
		header.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

		// Calculate customer totals
		double customerSales = calculateCustomerSales(customer);

		// Left side - Icon and name
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setBackground(CUSTOMER_BG);

		boolean isExpanded = customerExpandedState.get(customer.customerName);
		JLabel expandIcon = new JLabel(isExpanded ? "▼" : "▶");
		expandIcon.setFont(new Font("Arial", Font.BOLD, 14));
		expandIcon.setForeground(SIDEBAR_ACTIVE);
		expandIcon.setBorder(new EmptyBorder(0, 0, 0, 10));

		JLabel nameLabel = new JLabel(customer.customerName);
		nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
		nameLabel.setForeground(TEXT_DARK);

		leftPanel.add(expandIcon, BorderLayout.WEST);
		leftPanel.add(nameLabel, BorderLayout.CENTER);

		// Right side - Stats
		JPanel statsPanel = new JPanel(new GridBagLayout());
		statsPanel.setBackground(CUSTOMER_BG);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 20, 0, 0);

		// Total Branches
		JLabel branchesLabel = new JLabel("Branches: " + customer.getBranchCount());
		branchesLabel.setFont(new Font("Arial", Font.BOLD, 14));
		branchesLabel.setForeground(SIDEBAR_ACTIVE);
		statsPanel.add(branchesLabel, gbc);

		// Total Sales
		gbc.gridx = 1;
		JLabel salesLabel = new JLabel(String.format("Total Sales: ₱%,.2f", customerSales));
		salesLabel.setFont(new Font("Arial", Font.BOLD, 14));
		salesLabel.setForeground(ACCENT_GOLD);
		statsPanel.add(salesLabel, gbc);

		header.add(leftPanel, BorderLayout.WEST);
		header.add(statsPanel, BorderLayout.EAST);

		// Click listener to toggle expansion
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				customerExpandedState.put(customer.customerName, !customerExpandedState.get(customer.customerName));
				buildContent();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				header.setBackground(HOVER_COLOR);
				leftPanel.setBackground(HOVER_COLOR);
				statsPanel.setBackground(HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				header.setBackground(CUSTOMER_BG);
				leftPanel.setBackground(CUSTOMER_BG);
				statsPanel.setBackground(CUSTOMER_BG);
			}
		});

		return header;
	}

	private JPanel createBranchPanel(String customerName, BranchData branch) {
		JPanel branchPanel = new JPanel();
		branchPanel.setLayout(new BoxLayout(branchPanel, BoxLayout.Y_AXIS));
		branchPanel.setBackground(BRANCH_BG);
		branchPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(0, 0, 0, 0)));

		// Branch Header (clickable)
		JPanel branchHeader = createBranchHeader(customerName, branch);
		branchPanel.add(branchHeader);

		// Branch Content (products) - only shown if expanded
		String branchAddress = getBranchAddress(customerName, branch);
		String branchKey = customerName + "_" + branchAddress;
		if (branchExpandedState.get(branchKey)) {
			JPanel productsContainer = new JPanel();
			productsContainer.setLayout(new BoxLayout(productsContainer, BoxLayout.Y_AXIS));
			productsContainer.setBackground(BRANCH_BG);
			productsContainer.setBorder(new EmptyBorder(5, 20, 10, 10));

			// Get mock products for this branch
			List<ProductOrderData> products = getMockProductsForBranch(customerName, branch);

			for (ProductOrderData product : products) {
				productsContainer.add(createProductPanel(product));
				productsContainer.add(Box.createVerticalStrut(3));
			}

			branchPanel.add(productsContainer);
		}

		return branchPanel;
	}

	private JPanel createBranchHeader(String customerName, BranchData branch) {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(BRANCH_BG);
		header.setBorder(new EmptyBorder(12, 15, 12, 15));
		header.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

		// Calculate branch totals
		BranchTotals totals = calculateBranchTotals(customerName, branch);

		// Left side - Icon and address
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setBackground(BRANCH_BG);

		String branchAddress = getBranchAddress(customerName, branch);
		String branchKey = customerName + "_" + branchAddress;
		boolean isExpanded = branchExpandedState.get(branchKey);
		JLabel expandIcon = new JLabel(isExpanded ? "▼" : "▶");
		expandIcon.setFont(new Font("Arial", Font.BOLD, 12));
		expandIcon.setForeground(SIDEBAR_ACTIVE);
		expandIcon.setBorder(new EmptyBorder(0, 0, 0, 10));

		JLabel addressLabel = new JLabel("📍 " + branchAddress);
		addressLabel.setFont(new Font("Arial", Font.BOLD, 14));
		addressLabel.setForeground(TEXT_DARK);

		leftPanel.add(expandIcon, BorderLayout.WEST);
		leftPanel.add(addressLabel, BorderLayout.CENTER);

		// Right side - Stats
		JPanel statsPanel = new JPanel(new GridBagLayout());
		statsPanel.setBackground(BRANCH_BG);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 15, 0, 0);

		JLabel salesLabel = new JLabel(String.format("Sales: ₱%,.2f", totals.sales));
		salesLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		salesLabel.setForeground(TEXT_DARK);
		statsPanel.add(salesLabel, gbc);

		gbc.gridx = 1;
		JLabel capitalLabel = new JLabel(String.format("Capital: ₱%,.2f", totals.capital));
		capitalLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		capitalLabel.setForeground(SIDEBAR_ACTIVE);
		statsPanel.add(capitalLabel, gbc);

		gbc.gridx = 2;
		JLabel profitLabel = new JLabel(String.format("Profit: ₱%,.2f", totals.profit));
		profitLabel.setFont(new Font("Arial", Font.BOLD, 13));
		profitLabel.setForeground(PROFIT_GREEN);
		statsPanel.add(profitLabel, gbc);

		header.add(leftPanel, BorderLayout.WEST);
		header.add(statsPanel, BorderLayout.EAST);

		// Click listener to toggle expansion
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				branchExpandedState.put(branchKey, !branchExpandedState.get(branchKey));
				buildContent();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				header.setBackground(HOVER_COLOR);
				leftPanel.setBackground(HOVER_COLOR);
				statsPanel.setBackground(HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				header.setBackground(BRANCH_BG);
				leftPanel.setBackground(BRANCH_BG);
				statsPanel.setBackground(BRANCH_BG);
			}
		});

		return header;
	}

	private JPanel createProductPanel(ProductOrderData product) {
		JPanel productPanel = new JPanel(new GridBagLayout());
		productPanel.setBackground(PRODUCT_BG);
		productPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(230, 225, 220), 1), new EmptyBorder(10, 15, 10, 15)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(2, 0, 2, 15);

		// Product Name (larger, first row)
		gbc.gridx = 0;
		gbc.gridwidth = 7;
		JLabel nameLabel = new JLabel("📦 " + product.productName);
		nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
		nameLabel.setForeground(TEXT_DARK);
		productPanel.add(nameLabel, gbc);

		// Second row - all details
		gbc.gridy = 1;
		gbc.gridwidth = 1;

		// Quantity
		gbc.gridx = 0;
		productPanel.add(createDetailLabel("Qty:", String.valueOf(product.quantity)), gbc);

		// Selling Price
		gbc.gridx = 1;
		productPanel.add(createDetailLabel("Price:", String.format("₱%.2f", product.sellingPrice)), gbc);

		// Capital
		gbc.gridx = 2;
		productPanel.add(createDetailLabel("Capital:", String.format("₱%.2f", product.capital)), gbc);

		// Total Price
		gbc.gridx = 3;
		double totalPrice = product.quantity * product.sellingPrice;
		productPanel.add(createDetailLabel("Total Price:", String.format("₱%.2f", totalPrice)), gbc);

		// Profit per Unit
		gbc.gridx = 4;
		productPanel.add(createDetailLabel("Profit/Unit:", String.format("₱%.2f", product.profitPerUnit)), gbc);

		// Total Profit
		gbc.gridx = 5;
		double totalProfit = product.quantity * product.profitPerUnit;
		JPanel profitPanel = createDetailLabel("Total Profit:", String.format("₱%.2f", totalProfit));
		// Make total profit stand out
		((JLabel) profitPanel.getComponent(1)).setForeground(PROFIT_GREEN);
		((JLabel) profitPanel.getComponent(1)).setFont(new Font("Arial", Font.BOLD, 13));
		productPanel.add(profitPanel, gbc);

		return productPanel;
	}

	private JPanel createDetailLabel(String label, String value) {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		panel.setBackground(PRODUCT_BG);

		JLabel labelComponent = new JLabel(label);
		labelComponent.setFont(new Font("Arial", Font.PLAIN, 12));
		labelComponent.setForeground(new Color(100, 90, 80));

		JLabel valueComponent = new JLabel(value);
		valueComponent.setFont(new Font("Arial", Font.BOLD, 12));
		valueComponent.setForeground(TEXT_DARK);

		panel.add(labelComponent, BorderLayout.WEST);
		panel.add(valueComponent, BorderLayout.CENTER);

		return panel;
	}

	// Calculation methods
	private double calculateCustomerSales(CustomerDeliveryData customer) {
		double total = 0.0;
		for (BranchData branch : customer.branches) {
			List<ProductOrderData> products = getMockProductsForBranch(customer.customerName, branch);
			for (ProductOrderData product : products) {
				total += product.quantity * product.sellingPrice;
			}
		}
		return total;
	}

	private BranchTotals calculateBranchTotals(String customerName, BranchData branch) {
		BranchTotals totals = new BranchTotals();
		List<ProductOrderData> products = getMockProductsForBranch(customerName, branch);

		for (ProductOrderData product : products) {
			totals.sales += product.quantity * product.sellingPrice;
			totals.capital += product.quantity * product.capital;
		}
		totals.profit = totals.sales - totals.capital;

		return totals;
	}

	// MOCK DATA - Same as DeliveryFormStep3
	private List<ProductOrderData> getMockProductsForBranch(String customerName, BranchData branch) {
		java.util.List<ProductOrderData> products = new java.util.ArrayList<>();

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
			products.add(new ProductOrderData(7, "Standard Molasses", 10, 110.0, 75.0));
		}

		return products;
	}

	// Helper class for branch totals
	private static class BranchTotals {
		double sales = 0.0;
		double capital = 0.0;
		double profit = 0.0;

	}
}