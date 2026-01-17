package com.gierza_molases.molases_app.ui.dialogs.Delivery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.ProductWithQuantity;

public class ViewCustomerDeliveryDetails extends JDialog {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color STEP_INACTIVE = new Color(200, 190, 180);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);
	private static final Color CARD_BG = new Color(255, 255, 255);

	private Customer customer;
	private Map<Branch, List<ProductWithQuantity>> branches;

	private ViewCustomerDeliveryDetails(Window parent, Customer customer,
			Map<Branch, List<ProductWithQuantity>> branches) {
		super(parent, "Customer Delivery Details", ModalityType.APPLICATION_MODAL);
		this.customer = customer;
		this.branches = branches;

		initializeDialog();
	}

	/**
	 * Show the customer details dialog
	 */
	public static void show(Window parent, Customer customer, Map<Branch, List<ProductWithQuantity>> branches) {
		ViewCustomerDeliveryDetails dialog = new ViewCustomerDeliveryDetails(parent, customer, branches);
		dialog.setVisible(true);
	}

	private void initializeDialog() {
		setLayout(new BorderLayout());
		getContentPane().setBackground(CONTENT_BG);

		// Header
		add(createHeader(), BorderLayout.NORTH);

		// Content (scrollable)
		add(createContent(), BorderLayout.CENTER);

		// Footer with totals and close button
		add(createFooter(), BorderLayout.SOUTH);

		// Dialog settings
		setPreferredSize(new Dimension(900, 700));
		setMinimumSize(new Dimension(800, 600));
		pack();
		setLocationRelativeTo(getParent());
	}

	private JPanel createHeader() {
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(SIDEBAR_ACTIVE);
		headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

		JLabel titleLabel = new JLabel("Customer Delivery Details");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		titleLabel.setForeground(TEXT_LIGHT);
		headerPanel.add(titleLabel, BorderLayout.NORTH);

		JLabel customerLabel = new JLabel(customer.getDisplayName());
		customerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		customerLabel.setForeground(TEXT_LIGHT);
		customerLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
		headerPanel.add(customerLabel, BorderLayout.CENTER);

		JLabel summaryLabel = new JLabel(branches.size() + (branches.size() == 1 ? " Branch" : " Branches") + " • "
				+ getTotalProductCount() + " Total Products");
		summaryLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		summaryLabel.setForeground(new Color(220, 210, 200));
		summaryLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
		headerPanel.add(summaryLabel, BorderLayout.SOUTH);

		return headerPanel;
	}

	private JScrollPane createContent() {
		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBackground(CONTENT_BG);
		contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 15, 0);

		int branchNumber = 1;
		for (Map.Entry<Branch, List<ProductWithQuantity>> entry : branches.entrySet()) {
			Branch branch = entry.getKey();
			List<ProductWithQuantity> products = entry.getValue();

			JPanel branchCard = createBranchCard(branch, products, branchNumber);
			contentPanel.add(branchCard, gbc);
			gbc.gridy++;
			branchNumber++;
		}

		// Add vertical glue to push content to top
		gbc.weighty = 1.0;
		contentPanel.add(Box.createVerticalGlue(), gbc);

		JScrollPane scrollPane = new JScrollPane(contentPanel);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setBackground(CONTENT_BG);

		return scrollPane;
	}

	private JPanel createBranchCard(Branch branch, List<ProductWithQuantity> products, int branchNumber) {
		JPanel card = new JPanel(new BorderLayout(0, 10));
		card.setBackground(CARD_BG);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1),
				new EmptyBorder(15, 15, 15, 15)));

		// Branch header
		JPanel branchHeader = new JPanel(new BorderLayout());
		branchHeader.setBackground(CARD_BG);

		JLabel branchLabel = new JLabel("Branch " + branchNumber);
		branchLabel.setFont(new Font("Arial", Font.BOLD, 15));
		branchLabel.setForeground(ACCENT_GOLD);
		branchHeader.add(branchLabel, BorderLayout.NORTH);

		JLabel addressLabel = new JLabel("📍 " + branch.getAddress());
		addressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		addressLabel.setForeground(TEXT_DARK);
		addressLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
		branchHeader.add(addressLabel, BorderLayout.CENTER);

		card.add(branchHeader, BorderLayout.NORTH);

		// Products table
		JPanel tablePanel = createProductsTable(products);
		card.add(tablePanel, BorderLayout.CENTER);

		// Branch subtotal
		double branchTotal = products.stream().mapToDouble(ProductWithQuantity::getTotalSellingPrice).sum();

		JPanel subtotalPanel = new JPanel(new BorderLayout());
		subtotalPanel.setBackground(CARD_BG);
		subtotalPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

		JLabel subtotalLabel = new JLabel("Branch Subtotal: ₱" + String.format("%,.2f", branchTotal));
		subtotalLabel.setFont(new Font("Arial", Font.BOLD, 14));
		subtotalLabel.setForeground(TEXT_DARK);
		subtotalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		subtotalPanel.add(subtotalLabel, BorderLayout.EAST);

		card.add(subtotalPanel, BorderLayout.SOUTH);

		return card;
	}

	private JPanel createProductsTable(List<ProductWithQuantity> products) {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(CARD_BG);

		String[] columnNames = { "Product Name", "Quantity", "Unit Price", "Total" };
		DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		// Add products to table
		for (ProductWithQuantity pwq : products) {
			model.addRow(new Object[] { pwq.getProduct().getName(), pwq.getQuantity(),
					"₱" + String.format("%,.2f", pwq.getProduct().getSellingPrice()),
					"₱" + String.format("%,.2f", pwq.getTotalSellingPrice()) });
		}

		JTable table = new JTable(model);
		table.setFont(new Font("Arial", Font.PLAIN, 13));
		table.setRowHeight(35);
		table.setShowGrid(true);
		table.setGridColor(new Color(230, 220, 210));
		table.setBackground(TABLE_ROW_EVEN);
		table.setSelectionBackground(new Color(245, 239, 231));
		table.setSelectionForeground(TEXT_DARK);

		// Set column widths
		table.getColumnModel().getColumn(0).setPreferredWidth(350);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(120);
		table.getColumnModel().getColumn(3).setPreferredWidth(120);

		// Style header
		JTableHeader header = table.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 13));
		header.setBackground(new Color(245, 239, 231));
		header.setForeground(TEXT_DARK);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 35));

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setBackground(new Color(245, 239, 231));
		headerRenderer.setForeground(TEXT_DARK);
		headerRenderer.setFont(new Font("Arial", Font.BOLD, 13));
		headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}

		// Cell renderer for alternating rows and alignment
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				// Right-align quantity and prices
				if (column == 1 || column == 2 || column == 3) {
					setHorizontalAlignment(SwingConstants.RIGHT);
				} else {
					setHorizontalAlignment(SwingConstants.LEFT);
				}

				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1));
		scrollPane.setPreferredSize(new Dimension(0, Math.min(200, (products.size() + 1) * 35 + 5)));

		tablePanel.add(scrollPane, BorderLayout.CENTER);

		return tablePanel;
	}

	private JPanel createFooter() {
		JPanel footerPanel = new JPanel(new BorderLayout());
		footerPanel.setBackground(CONTENT_BG);
		footerPanel.setBorder(new EmptyBorder(15, 25, 20, 25));

		// Grand total
		double grandTotal = branches.values().stream().flatMap(List::stream)
				.mapToDouble(ProductWithQuantity::getTotalSellingPrice).sum();

		JPanel totalPanel = new JPanel(new GridBagLayout());
		totalPanel.setBackground(CONTENT_BG);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 10, 0);

		JLabel grandTotalLabel = new JLabel("Grand Total: ₱" + String.format("%,.2f", grandTotal));
		grandTotalLabel.setFont(new Font("Arial", Font.BOLD, 18));
		grandTotalLabel.setForeground(ACCENT_GOLD);
		totalPanel.add(grandTotalLabel, gbc);

		footerPanel.add(totalPanel, BorderLayout.NORTH);

		// Close button
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(CONTENT_BG);

		JButton closeButton = new JButton("Close");
		closeButton.setFont(new Font("Arial", Font.BOLD, 14));
		closeButton.setPreferredSize(new Dimension(120, 40));
		closeButton.setBackground(SIDEBAR_ACTIVE);
		closeButton.setForeground(TEXT_LIGHT);
		closeButton.setFocusPainted(false);
		closeButton.setBorderPainted(false);
		closeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		closeButton.addActionListener(e -> dispose());

		buttonPanel.add(closeButton);
		footerPanel.add(buttonPanel, BorderLayout.CENTER);

		return footerPanel;
	}

	private int getTotalProductCount() {
		return branches.values().stream().mapToInt(products -> products != null ? products.size() : 0).sum();
	}
}