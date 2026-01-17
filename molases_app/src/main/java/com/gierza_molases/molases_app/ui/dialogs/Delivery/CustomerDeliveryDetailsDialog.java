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

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.ui.components.delivery.DeliveryFormStep2.Step2Data;
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
	private Map<Integer, Boolean> customerExpandedState = new HashMap<>(); // customerId -> expanded
	private Map<String, Boolean> branchExpandedState = new HashMap<>(); // customerId_branchId -> expanded
	private JPanel contentPanel;

	public CustomerDeliveryDetailsDialog(JFrame parent, Step2Data step2Data) {
		super(parent, "Customer Delivery Details", true);
		this.step2Data = step2Data;

		// Initialize all customers and branches as collapsed
		for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> customerEntry : step2Data.customerDeliveries
				.entrySet()) {
			Customer customer = customerEntry.getKey();
			customerExpandedState.put(customer.getId(), false);

			Map<Branch, List<ProductWithQuantity>> branches = customerEntry.getValue();
			for (Branch branch : branches.keySet()) {
				String branchKey = customer.getId() + "_" + branch.getId();
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

		for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> entry : step2Data.customerDeliveries
				.entrySet()) {
			Customer customer = entry.getKey();
			contentPanel.add(createCustomerPanel(customer, entry.getValue()));
			contentPanel.add(Box.createVerticalStrut(4));
		}

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	private JPanel createCustomerPanel(Customer customer, Map<Branch, List<ProductWithQuantity>> branches) {
		JPanel customerPanel = new JPanel();
		customerPanel.setLayout(new BoxLayout(customerPanel, BoxLayout.Y_AXIS));
		customerPanel.setBackground(CUSTOMER_BG);
		customerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SIDEBAR_ACTIVE, 2),
				new EmptyBorder(0, 0, 0, 0)));

		// Customer Header (clickable)
		JPanel customerHeader = createCustomerHeader(customer, branches);
		customerPanel.add(customerHeader);

		// Customer Content (branches) - only shown if expanded
		if (customerExpandedState.get(customer.getId())) {
			JPanel branchesContainer = new JPanel();
			branchesContainer.setLayout(new BoxLayout(branchesContainer, BoxLayout.Y_AXIS));
			branchesContainer.setBackground(CUSTOMER_BG);
			branchesContainer.setBorder(new EmptyBorder(5, 15, 10, 10));

			for (Map.Entry<Branch, List<ProductWithQuantity>> branchEntry : branches.entrySet()) {
				Branch branch = branchEntry.getKey();
				List<ProductWithQuantity> products = branchEntry.getValue();
				branchesContainer.add(createBranchPanel(customer, branch, products));
				branchesContainer.add(Box.createVerticalStrut(5));
			}

			customerPanel.add(branchesContainer);
		}

		return customerPanel;
	}

	private JPanel createCustomerHeader(Customer customer, Map<Branch, List<ProductWithQuantity>> branches) {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(CUSTOMER_BG);
		header.setBorder(new EmptyBorder(10, 10, 10, 10));
		header.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

		// Calculate customer totals
		double customerSales = calculateCustomerSales(branches);

		// Left side - Icon and name
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setBackground(CUSTOMER_BG);

		boolean isExpanded = customerExpandedState.get(customer.getId());
		JLabel expandIcon = new JLabel(isExpanded ? "▼" : "▶");
		expandIcon.setFont(new Font("Arial", Font.BOLD, 14));
		expandIcon.setForeground(SIDEBAR_ACTIVE);
		expandIcon.setBorder(new EmptyBorder(0, 0, 0, 10));

		JLabel nameLabel = new JLabel(customer.getDisplayName());
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
		JLabel branchesLabel = new JLabel("Branches: " + branches.size());
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
				customerExpandedState.put(customer.getId(), !customerExpandedState.get(customer.getId()));
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

	private JPanel createBranchPanel(Customer customer, Branch branch, List<ProductWithQuantity> products) {
		JPanel branchPanel = new JPanel();
		branchPanel.setLayout(new BoxLayout(branchPanel, BoxLayout.Y_AXIS));
		branchPanel.setBackground(BRANCH_BG);
		branchPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(0, 0, 0, 0)));

		// Branch Header (clickable)
		JPanel branchHeader = createBranchHeader(customer, branch, products);
		branchPanel.add(branchHeader);

		// Branch Content (products) - only shown if expanded
		String branchKey = customer.getId() + "_" + branch.getId();
		if (branchExpandedState.get(branchKey)) {
			JPanel productsContainer = new JPanel();
			productsContainer.setLayout(new BoxLayout(productsContainer, BoxLayout.Y_AXIS));
			productsContainer.setBackground(BRANCH_BG);
			productsContainer.setBorder(new EmptyBorder(5, 20, 10, 10));

			for (ProductWithQuantity product : products) {
				productsContainer.add(createProductPanel(product));
				productsContainer.add(Box.createVerticalStrut(3));
			}

			branchPanel.add(productsContainer);
		}

		return branchPanel;
	}

	private JPanel createBranchHeader(Customer customer, Branch branch, List<ProductWithQuantity> products) {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(BRANCH_BG);
		header.setBorder(new EmptyBorder(12, 15, 12, 15));
		header.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

		// Calculate branch totals
		BranchTotals totals = calculateBranchTotals(products);

		// Left side - Icon and address
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setBackground(BRANCH_BG);

		String branchKey = customer.getId() + "_" + branch.getId();
		boolean isExpanded = branchExpandedState.get(branchKey);
		JLabel expandIcon = new JLabel(isExpanded ? "▼" : "▶");
		expandIcon.setFont(new Font("Arial", Font.BOLD, 12));
		expandIcon.setForeground(SIDEBAR_ACTIVE);
		expandIcon.setBorder(new EmptyBorder(0, 0, 0, 10));

		JLabel addressLabel = new JLabel("📍 " + branch.getAddress());
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

	private JPanel createProductPanel(ProductWithQuantity productWithQty) {
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
		JLabel nameLabel = new JLabel("📦 " + productWithQty.getProduct().getName());
		nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
		nameLabel.setForeground(TEXT_DARK);
		productPanel.add(nameLabel, gbc);

		// Second row - all details
		gbc.gridy = 1;
		gbc.gridwidth = 1;

		// Quantity
		gbc.gridx = 0;
		productPanel.add(createDetailLabel("Qty:", String.valueOf(productWithQty.getQuantity())), gbc);

		// Selling Price
		gbc.gridx = 1;
		productPanel.add(
				createDetailLabel("Price:", String.format("₱%.2f", productWithQty.getProduct().getSellingPrice())),
				gbc);

		// Capital
		gbc.gridx = 2;
		productPanel.add(
				createDetailLabel("Capital:", String.format("₱%.2f", productWithQty.getProduct().getCapital())), gbc);

		// Total Price
		gbc.gridx = 3;
		productPanel.add(
				createDetailLabel("Total Price:", String.format("₱%.2f", productWithQty.getTotalSellingPrice())), gbc);

		// Profit per Unit
		gbc.gridx = 4;
		productPanel.add(
				createDetailLabel("Profit/Unit:", String.format("₱%.2f", productWithQty.getProduct().getProfit())),
				gbc);

		// Total Profit
		gbc.gridx = 5;
		JPanel profitPanel = createDetailLabel("Total Profit:",
				String.format("₱%.2f", productWithQty.getTotalProfit()));
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
	private double calculateCustomerSales(Map<Branch, List<ProductWithQuantity>> branches) {
		double total = 0.0;
		for (List<ProductWithQuantity> products : branches.values()) {
			for (ProductWithQuantity product : products) {
				total += product.getTotalSellingPrice();
			}
		}
		return total;
	}

	private BranchTotals calculateBranchTotals(List<ProductWithQuantity> products) {
		BranchTotals totals = new BranchTotals();

		for (ProductWithQuantity product : products) {
			totals.sales += product.getTotalSellingPrice();
			totals.capital += product.getTotalCapital();
		}
		totals.profit = totals.sales - totals.capital;

		return totals;
	}

	// Helper class for branch totals
	private static class BranchTotals {
		double sales = 0.0;
		double capital = 0.0;
		double profit = 0.0;
	}
}