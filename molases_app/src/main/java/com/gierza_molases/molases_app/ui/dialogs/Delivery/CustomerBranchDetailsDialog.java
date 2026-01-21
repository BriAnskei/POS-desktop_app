package com.gierza_molases.molases_app.ui.dialogs.Delivery;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.DeliveryDataClasses.BranchDeliveryData;
import com.gierza_molases.molases_app.model.DeliveryDataClasses.CustomerDeliveryData;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.ui.components.delivery.UIComponentFactory;

public class CustomerBranchDetailsDialog extends JDialog {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color BRANCH_BG = new Color(255, 252, 247);
	private static final Color PROFIT_GREEN = new Color(34, 139, 34);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);

	private Customer customer;
	private CustomerDeliveryData customerData;
	private Runnable onUpdate;
	private JPanel branchesContainer;

	public CustomerBranchDetailsDialog(Window parent, Customer customer, CustomerDeliveryData customerData,
			Runnable onUpdate) {
		super(parent, "Branch Details - " + customer.getDisplayName(), ModalityType.APPLICATION_MODAL);
		this.customer = customer;
		this.customerData = customerData;
		this.onUpdate = onUpdate;

		setLayout(new BorderLayout());
		setBackground(CONTENT_BG);

		// Header
		add(createHeader(), BorderLayout.NORTH);

		// Content - Branches
		branchesContainer = new JPanel();
		branchesContainer.setLayout(new BoxLayout(branchesContainer, BoxLayout.Y_AXIS));
		branchesContainer.setBackground(CONTENT_BG);
		branchesContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

		buildBranchesContent();

		JScrollPane scrollPane = new JScrollPane(branchesContainer);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, BorderLayout.CENTER);

		// Footer
		add(createFooter(), BorderLayout.SOUTH);

		// Dialog settings
		setSize(900, 650);
		setLocationRelativeTo(parent);
	}

	private JPanel createHeader() {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(SIDEBAR_ACTIVE);
		header.setBorder(new EmptyBorder(20, 25, 20, 25));

		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setBackground(SIDEBAR_ACTIVE);

		JLabel title = new JLabel("Branch Deliveries - " + customer.getDisplayName());
		title.setFont(new Font("Arial", Font.BOLD, 22));
		title.setForeground(Color.WHITE);

		JLabel subtitle = new JLabel("View and manage delivery status for each branch");
		subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
		subtitle.setForeground(new Color(230, 220, 210));

		textPanel.add(title);
		textPanel.add(Box.createVerticalStrut(5));
		textPanel.add(subtitle);

		header.add(textPanel, BorderLayout.WEST);

		// Add Branch Button
		JButton addBranchBtn = UIComponentFactory.createStyledButton("+ Add Branch", ACCENT_GOLD);
		addBranchBtn.setPreferredSize(new Dimension(150, 40));
		addBranchBtn.addActionListener(e -> {
			// Placeholder - will be implemented later
			JOptionPane.showMessageDialog(this, "Add Branch dialog will be implemented later", "Coming Soon",
					JOptionPane.INFORMATION_MESSAGE);
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.setBackground(SIDEBAR_ACTIVE);
		buttonPanel.add(addBranchBtn);

		header.add(buttonPanel, BorderLayout.EAST);

		return header;
	}

	private void buildBranchesContent() {
		branchesContainer.removeAll();

		for (Map.Entry<Branch, BranchDeliveryData> entry : customerData.branches.entrySet()) {
			branchesContainer.add(createBranchPanel(entry.getKey(), entry.getValue()));
			branchesContainer.add(Box.createVerticalStrut(12));
		}

		branchesContainer.revalidate();
		branchesContainer.repaint();
	}

	private JPanel createBranchPanel(Branch branch, BranchDeliveryData branchData) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BRANCH_BG);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 190, 180), 2),
				new EmptyBorder(15, 15, 15, 15)));

		// Header - Address, Add Product Button, and Status
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(BRANCH_BG);
		headerPanel.setBorder(new EmptyBorder(0, 0, 12, 0));

		JLabel addressLabel = new JLabel("📍 " + branch.getAddress());
		addressLabel.setFont(new Font("Arial", Font.BOLD, 16));
		addressLabel.setForeground(TEXT_DARK);
		headerPanel.add(addressLabel, BorderLayout.WEST);

		// Right side: Add Product Button and Status
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightPanel.setBackground(BRANCH_BG);

		// Add Product Button
		JButton addProductBtn = UIComponentFactory.createStyledButton("+ Add Product", ACCENT_GOLD);
		addProductBtn.setPreferredSize(new Dimension(140, 35));
		addProductBtn.addActionListener(e -> {
			// Placeholder - will be implemented later
			JOptionPane.showMessageDialog(this, "Add Product dialog will be implemented later", "Coming Soon",
					JOptionPane.INFORMATION_MESSAGE);
		});
		rightPanel.add(addProductBtn);

		// Status selector
		JLabel statusLabel = new JLabel("Status:");
		statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
		statusLabel.setForeground(TEXT_DARK);
		rightPanel.add(statusLabel);

		String[] statuses = { "Delivered", "Cancelled" };
		JComboBox<String> statusCombo = new JComboBox<>(statuses);
		statusCombo.setSelectedItem(branchData.status);
		statusCombo.setPreferredSize(new Dimension(140, 35));
		statusCombo.setFont(new Font("Arial", Font.PLAIN, 14));
		statusCombo.addActionListener(e -> {
			branchData.status = (String) statusCombo.getSelectedItem();
			if (onUpdate != null) {
				onUpdate.run();
			}
		});
		rightPanel.add(statusCombo);

		headerPanel.add(rightPanel, BorderLayout.EAST);
		panel.add(headerPanel, BorderLayout.NORTH);

		// Products
		JPanel productsPanel = new JPanel();
		productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
		productsPanel.setBackground(BRANCH_BG);
		productsPanel.setBorder(new EmptyBorder(8, 0, 8, 0));

		for (ProductWithQuantity product : branchData.products) {
			productsPanel.add(createProductRow(product, branch, branchData));
			productsPanel.add(Box.createVerticalStrut(6));
		}

		panel.add(productsPanel, BorderLayout.CENTER);

		// Totals
		BranchTotals totals = calculateBranchTotals(branchData.products);
		JPanel totalsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 8));
		totalsPanel.setBackground(new Color(240, 235, 225));
		totalsPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(200, 190, 180)));

		JLabel salesLabel = new JLabel(String.format("Total Sales: ₱%,.2f", totals.sales));
		salesLabel.setFont(new Font("Arial", Font.BOLD, 14));
		salesLabel.setForeground(TEXT_DARK);
		totalsPanel.add(salesLabel);

		JLabel capitalLabel = new JLabel(String.format("Capital: ₱%,.2f", totals.capital));
		capitalLabel.setFont(new Font("Arial", Font.BOLD, 14));
		capitalLabel.setForeground(SIDEBAR_ACTIVE);
		totalsPanel.add(capitalLabel);

		JLabel profitLabel = new JLabel(String.format("Profit: ₱%,.2f", totals.profit));
		profitLabel.setFont(new Font("Arial", Font.BOLD, 14));
		profitLabel.setForeground(PROFIT_GREEN);
		totalsPanel.add(profitLabel);

		panel.add(totalsPanel, BorderLayout.SOUTH);

		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		return panel;
	}

	private JPanel createProductRow(ProductWithQuantity productWithQty, Branch branch, BranchDeliveryData branchData) {
		JPanel row = new JPanel(new GridBagLayout());
		row.setBackground(BRANCH_BG);
		row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(230, 225, 220), 1),
				new EmptyBorder(10, 15, 10, 15)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(2, 0, 2, 15);

		// Product Name (first row)
		gbc.gridx = 0;
		gbc.gridwidth = 6;
		JLabel nameLabel = new JLabel("📦 " + productWithQty.getProduct().getName());
		nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
		nameLabel.setForeground(TEXT_DARK);
		row.add(nameLabel, gbc);

		// Actions button (first row, right side)
		gbc.gridx = 6;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		JLabel actionsLabel = new JLabel("⚙ Actions");
		actionsLabel.setFont(new Font("Arial", Font.BOLD, 12));
		actionsLabel.setForeground(SIDEBAR_ACTIVE);
		actionsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		actionsLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				showProductActionMenu(productWithQty, branch, branchData);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				actionsLabel.setForeground(ACCENT_GOLD);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				actionsLabel.setForeground(SIDEBAR_ACTIVE);
			}
		});
		row.add(actionsLabel, gbc);

		// Second row - details
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;

		// Quantity
		gbc.gridx = 0;
		row.add(createDetailLabel("Qty:", String.valueOf(productWithQty.getQuantity())), gbc);

		// Selling Price
		gbc.gridx = 1;
		row.add(createDetailLabel("Price:", String.format("₱%.2f", productWithQty.getProduct().getSellingPrice())),
				gbc);

		// Capital
		gbc.gridx = 2;
		row.add(createDetailLabel("Capital:", String.format("₱%.2f", productWithQty.getProduct().getCapital())), gbc);

		// Total Price
		gbc.gridx = 3;
		row.add(createDetailLabel("Total Price:", String.format("₱%.2f", productWithQty.getTotalSellingPrice())), gbc);

		// Profit per Unit
		gbc.gridx = 4;
		row.add(createDetailLabel("Profit/Unit:", String.format("₱%.2f", productWithQty.getProduct().getProfit())),
				gbc);

		// Total Profit (highlighted)
		gbc.gridx = 5;
		JPanel profitPanel = createDetailLabel("Total Profit:",
				String.format("₱%.2f", productWithQty.getTotalProfit()));
		((JLabel) profitPanel.getComponent(1)).setForeground(PROFIT_GREEN);
		((JLabel) profitPanel.getComponent(1)).setFont(new Font("Arial", Font.BOLD, 13));
		row.add(profitPanel, gbc);

		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		return row;
	}

	private void showProductActionMenu(ProductWithQuantity product, Branch branch, BranchDeliveryData branchData) {
		JDialog actionDialog = new JDialog(this, "Product Actions");
		actionDialog.setLayout(new GridBagLayout());
		actionDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 20, 5, 20);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Choose Action for: " + product.getProduct().getName());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		actionDialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(15, 20, 5, 20);

		JButton editBtn = createActionButton("✏️ Edit Quantity", ACCENT_GOLD);
		editBtn.addActionListener(e -> {
			actionDialog.dispose();
			// Placeholder - will be implemented later
			JOptionPane.showMessageDialog(this, "Edit Quantity dialog will be implemented later", "Coming Soon",
					JOptionPane.INFORMATION_MESSAGE);
		});
		actionDialog.add(editBtn, gbc);

		gbc.gridy++;
		JButton removeBtn = createActionButton("🗑️ Remove Product", new Color(180, 50, 50));
		removeBtn.addActionListener(e -> {
			actionDialog.dispose();
			showRemoveProductConfirmation(product, branch, branchData);
		});
		actionDialog.add(removeBtn, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 20, 15, 20);
		JButton cancelBtn = createActionButton("Cancel", new Color(120, 120, 120));
		cancelBtn.addActionListener(e -> actionDialog.dispose());
		actionDialog.add(cancelBtn, gbc);

		actionDialog.pack();
		actionDialog.setMinimumSize(new Dimension(380, 220));
		actionDialog.setLocationRelativeTo(null);
		actionDialog.setVisible(true);
	}

	private void showRemoveProductConfirmation(ProductWithQuantity product, Branch branch,
			BranchDeliveryData branchData) {
		// Check if this is the last product
		if (branchData.products.size() <= 1) {
			JOptionPane.showMessageDialog(this,
					"Cannot remove the last product from a branch.\nAt least one product must remain.", "Cannot Remove",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		JDialog confirmDialog = new JDialog(this, "Confirm Remove");
		confirmDialog.setLayout(new GridBagLayout());
		confirmDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);

		JLabel messageLabel = new JLabel("<html><center>Are you sure you want to remove this product?<br><b>"
				+ product.getProduct().getName() + "</b><br>(Quantity: " + product.getQuantity() + ")</center></html>");
		messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		messageLabel.setForeground(TEXT_DARK);
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		confirmDialog.add(messageLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 10, 30);
		JLabel warningLabel = new JLabel("This action cannot be undone.");
		warningLabel.setFont(new Font("Arial", Font.ITALIC, 13));
		warningLabel.setForeground(new Color(180, 50, 50));
		warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
		confirmDialog.add(warningLabel, gbc);

		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 30, 20, 10);

		JButton cancelBtn = UIComponentFactory.createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> confirmDialog.dispose());
		confirmDialog.add(cancelBtn, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(20, 10, 20, 30);
		JButton removeBtn = UIComponentFactory.createStyledButton("Remove", new Color(180, 50, 50));
		removeBtn.setPreferredSize(new Dimension(120, 40));
		removeBtn.addActionListener(e -> {
			confirmDialog.dispose();

			// Remove product from branch data
			branchData.products.remove(product);

			// Rebuild UI
			buildBranchesContent();

			// Update parent page financial summary
			if (onUpdate != null) {
				onUpdate.run();
			}
		});
		confirmDialog.add(removeBtn, gbc);

		confirmDialog.pack();
		confirmDialog.setMinimumSize(new Dimension(420, 220));
		confirmDialog.setLocationRelativeTo(null);
		confirmDialog.setVisible(true);
	}

	private JButton createActionButton(String text, Color bgColor) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 14));
		button.setBackground(bgColor);
		button.setForeground(Color.WHITE);
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

	private JPanel createDetailLabel(String label, String value) {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		panel.setBackground(BRANCH_BG);

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

	private BranchTotals calculateBranchTotals(List<ProductWithQuantity> products) {
		BranchTotals totals = new BranchTotals();
		for (ProductWithQuantity product : products) {
			totals.sales += product.getTotalSellingPrice();
			totals.capital += product.getTotalCapital();
		}
		totals.profit = totals.sales - totals.capital;
		return totals;
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

	private static class BranchTotals {
		double sales = 0.0;
		double capital = 0.0;
		double profit = 0.0;
	}
}