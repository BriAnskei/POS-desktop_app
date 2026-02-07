package molases_appcom.gierza_molases.molases_app.ui.pages.Delivery_detials;

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
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.ui.components.delivery.UIComponentFactory;
import com.gierza_molases.molases_app.ui.dialogs.Delivery.AddCustomerBranchDialog;
import com.gierza_molases.molases_app.ui.dialogs.Delivery.CustomerBranchDetailsDialog;
import com.gierza_molases.molases_app.ui.dialogs.Delivery.SetPaymentTypeDialog;

public class CustomerDeliveriesTab {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color STEP_INACTIVE = new Color(200, 190, 180);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color SECTION_BG = new Color(245, 242, 237);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);
	private static final Color TABLE_HOVER = new Color(245, 239, 231);
	private static final Color STATUS_DELIVERED = new Color(34, 139, 34);
	private static final Color STATUS_CANCELLED = new Color(180, 50, 50);

	private static Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliveries;
	private static String deliveryStatus;

	private static JTable customerTable;
	private static DefaultTableModel customerTableModel;

	public static void initialize() {
		Delivery delivery = AppContext.deliveryDetialsController.getState().getDelivery();

		if (delivery == null) {
			return;
		}

		deliveryStatus = capitalizeStatus(delivery.getStatus());
		customerDeliveries = AppContext.deliveryDetialsController.getState().getMappedCustomerDeliveries();
	}

	public static JPanel createPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(CONTENT_BG);

		JPanel customerPanel = createCustomerDeliveriesTable();

		JScrollPane scrollPane = new JScrollPane(customerPanel);
		scrollPane.setBorder(null);
		scrollPane.getViewport().setBackground(CONTENT_BG);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

		mainPanel.add(scrollPane, BorderLayout.CENTER);

		return mainPanel;
	}

	private static String capitalizeStatus(String status) {
		if (status == null || status.isEmpty()) {
			return "Scheduled";
		}
		return status.substring(0, 1).toUpperCase() + status.substring(1);
	}

	private static JPanel createCustomerDeliveriesTable() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(SECTION_BG);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1),
				new EmptyBorder(12, 12, 12, 12)));

		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(SECTION_BG);
		headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		JLabel titleLabel = new JLabel("Customer Deliveries");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(SIDEBAR_ACTIVE);
		headerPanel.add(titleLabel, BorderLayout.WEST);

		if (!deliveryStatus.equalsIgnoreCase("Delivered")) {
			JButton addCustomerBtn = UIComponentFactory.createStyledButton("+ Add Customer", ACCENT_GOLD);
			addCustomerBtn.setPreferredSize(new Dimension(150, 35));
			addCustomerBtn.addActionListener(e -> {
				AddCustomerBranchDialog.show(SwingUtilities.getWindowAncestor(addCustomerBtn), () -> {
					updateCustomerTable();
					DeliveryOverviewTab.updateFinancialSummary();
				}, "additionalDelivery");
			});
			headerPanel.add(addCustomerBtn, BorderLayout.EAST);
		}

		panel.add(headerPanel, BorderLayout.NORTH);

		// Updated column names - added "Status" column after "Payment Type"
		String[] columnNames = { "Customer Name", "Payment Type", "Status", "Branches", "Total Sales (₱)",
				"Total Profit (₱)", "Actions", "Customer" };
		customerTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 7) { // Customer column moved to index 7
					return Customer.class;
				}
				return Object.class;
			}
		};

		updateCustomerTable();

		customerTable = new JTable(customerTableModel);
		customerTable.setFont(new Font("Arial", Font.PLAIN, 13));
		customerTable.setRowHeight(45);
		customerTable.setShowGrid(true);
		customerTable.setGridColor(new Color(220, 210, 200));
		customerTable.setBackground(TABLE_ROW_EVEN);
		customerTable.setSelectionBackground(TABLE_HOVER);
		customerTable.setSelectionForeground(TEXT_DARK);
		customerTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader header = customerTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 13));
		header.setBackground(SIDEBAR_ACTIVE);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));

		// Adjust column widths
		customerTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Customer Name
		customerTable.getColumnModel().getColumn(1).setPreferredWidth(140); // Payment Type
		customerTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Status
		customerTable.getColumnModel().getColumn(3).setPreferredWidth(90); // Branches
		customerTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Total Sales
		customerTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Total Profit
		customerTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Actions
		customerTable.getColumnModel().getColumn(7).setPreferredWidth(0); // Customer (hidden)

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		customerTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Status
		customerTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Branches
		customerTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Total Sales
		customerTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Total Profit
		customerTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // Actions

		// Hide Customer column
		customerTable.getColumnModel().getColumn(7).setMinWidth(0);
		customerTable.getColumnModel().getColumn(7).setMaxWidth(0);
		customerTable.getColumnModel().getColumn(7).setWidth(0);

		// Custom renderer for Status column with color coding
		customerTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				setHorizontalAlignment(SwingConstants.CENTER);

				// Set color based on status
				String status = value != null ? value.toString() : "";
				if ("Delivered".equalsIgnoreCase(status)) {
					setForeground(STATUS_DELIVERED);
				} else if ("Cancelled".equalsIgnoreCase(status)) {
					setForeground(STATUS_CANCELLED);
				} else {
					setForeground(TEXT_DARK);
				}

				setFont(new Font("Arial", Font.BOLD, 13));
				((JLabel) c).setBorder(new EmptyBorder(6, 12, 6, 12));
				return c;
			}
		});

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
				} else if (column >= 2 && column <= 5) {
					setHorizontalAlignment(SwingConstants.CENTER);
				} else {
					setHorizontalAlignment(SwingConstants.LEFT);
				}

				// Don't override status column color
				if (column != 2) {
					setForeground(TEXT_DARK);
					setFont(new Font("Arial", Font.PLAIN, 13));
				}

				((JLabel) c).setBorder(new EmptyBorder(6, 12, 6, 12));
				return c;
			}
		});

		customerTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = customerTable.rowAtPoint(e.getPoint());
				int col = customerTable.columnAtPoint(e.getPoint());

				if (col == 6 && row >= 0) { // Actions column
					Customer customer = (Customer) customerTableModel.getValueAt(row, 7);
					showActionMenu(e.getComponent(), customer);
				}
			}
		});

		customerTable.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int col = customerTable.columnAtPoint(e.getPoint());
				customerTable.setCursor(new Cursor(col == 6 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
			}
		});

		JScrollPane scrollPane = new JScrollPane(customerTable);
		scrollPane.setBorder(null);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private static void updateCustomerTable() {
		customerTableModel.setRowCount(0);

		for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> entry : customerDeliveries.entrySet()) {
			Customer customer = entry.getKey();
			Map<Branch, List<ProductWithQuantity>> branches = entry.getValue();

			double totalSales = 0.0;
			double totalCapital = 0.0;

			// Get customer status
			String customerStatus = AppContext.deliveryDetialsController.getCustomerDeliveryStatus(customer);

			// Calculate totals only if customer is not cancelled
			if (!"Cancelled".equalsIgnoreCase(customerStatus)) {
				// Calculate totals, excluding cancelled branches
				for (Map.Entry<Branch, List<ProductWithQuantity>> branchEntry : branches.entrySet()) {
					Branch branch = branchEntry.getKey();

					// Skip cancelled branches
					String branchStatus = AppContext.deliveryDetialsController.getBranchDeliveryStatus(branch);
					if ("Cancelled".equalsIgnoreCase(branchStatus)) {
						continue;
					}

					List<ProductWithQuantity> products = branchEntry.getValue();

					for (ProductWithQuantity product : products) {
						totalSales += product.getTotalSellingPrice();
						totalCapital += product.getTotalCapital();
					}
				}
			}

			double totalProfit = totalSales - totalCapital;

			String paymentType = AppContext.deliveryDetialsController.getState().getPaymentType(customer);

			customerTableModel.addRow(new Object[] { customer.getDisplayName(), paymentType, customerStatus,
					branches.size(), String.format("₱%,.2f", totalSales), String.format("₱%,.2f", totalProfit),
					"⚙ Actions", customer });
		}
	}

	/**
	 * Calculate total sales for a specific customer (excluding cancelled branches)
	 */
	private static double calculateCustomerTotalSales(Customer customer) {
		Map<Branch, List<ProductWithQuantity>> branches = AppContext.deliveryDetialsController.getState()
				.getMappedCustomerDeliveries().get(customer);

		if (branches == null) {
			return 0.0;
		}

		// Check if customer is cancelled
		String customerStatus = AppContext.deliveryDetialsController.getCustomerDeliveryStatus(customer);
		if ("Cancelled".equalsIgnoreCase(customerStatus)) {
			return 0.0;
		}

		double totalSales = 0.0;

		for (Map.Entry<Branch, List<ProductWithQuantity>> branchEntry : branches.entrySet()) {
			Branch branch = branchEntry.getKey();

			// Skip cancelled branches
			String branchStatus = AppContext.deliveryDetialsController.getBranchDeliveryStatus(branch);
			if ("Cancelled".equalsIgnoreCase(branchStatus)) {
				continue;
			}

			List<ProductWithQuantity> products = branchEntry.getValue();

			for (ProductWithQuantity product : products) {
				totalSales += product.getTotalSellingPrice();
			}
		}

		return totalSales;
	}

	/**
	 * Show dialog for setting customer delivery status
	 */
	/**
	 * Show dialog for setting customer delivery status
	 */
	private static void showSetStatusDialog(Component parent, Customer customer) {
		JDialog statusDialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Set Customer Delivery Status");
		statusDialog.setLayout(new GridBagLayout());
		statusDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);

		JLabel titleLabel = new JLabel("Set Delivery Status for: " + customer.getDisplayName());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusDialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);
		JLabel instructionLabel = new JLabel("Choose the delivery status:");
		instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		instructionLabel.setForeground(TEXT_DARK);
		statusDialog.add(instructionLabel, gbc);

		// Get current status
		String currentStatus = AppContext.deliveryDetialsController.getCustomerDeliveryStatus(customer);

		// Radio buttons for status
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(15, 50, 5, 30);
		gbc.anchor = GridBagConstraints.WEST;

		JRadioButton deliveredRadio = new JRadioButton("Delivered");
		deliveredRadio.setFont(new Font("Arial", Font.PLAIN, 14));
		deliveredRadio.setBackground(Color.WHITE);
		deliveredRadio.setForeground(STATUS_DELIVERED);
		deliveredRadio.setSelected("Delivered".equalsIgnoreCase(currentStatus));
		statusDialog.add(deliveredRadio, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(15, 30, 5, 50);

		JRadioButton cancelledRadio = new JRadioButton("Cancelled");
		cancelledRadio.setFont(new Font("Arial", Font.PLAIN, 14));
		cancelledRadio.setBackground(Color.WHITE);
		cancelledRadio.setForeground(STATUS_CANCELLED);
		cancelledRadio.setSelected("Cancelled".equalsIgnoreCase(currentStatus));
		statusDialog.add(cancelledRadio, gbc);

		// Button group
		ButtonGroup statusGroup = new ButtonGroup();
		statusGroup.add(deliveredRadio);
		statusGroup.add(cancelledRadio);

		// Action buttons
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 30, 20, 10);
		gbc.anchor = GridBagConstraints.CENTER;

		JButton cancelBtn = createActionButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> statusDialog.dispose());
		statusDialog.add(cancelBtn, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(20, 10, 20, 30);
		JButton saveBtn = createActionButton("Save", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(120, 40));
		saveBtn.addActionListener(e -> {
			String selectedStatus = deliveredRadio.isSelected() ? "Delivered" : "Cancelled";

			// NEW: Check if this is a newly added customer being cancelled
			boolean isNewlyAdded = AppContext.deliveryDetialsController.getState()
					.wasCancelledCustomerNewlyAdded(customer);
			boolean willBeRemoved = "Cancelled".equals(selectedStatus) && isNewlyAdded;

			// Call controller to set customer status
			AppContext.deliveryDetialsController.setCustomerDeliveryStatus(customer, selectedStatus, () -> {
				SwingUtilities.invokeLater(() -> {
					statusDialog.dispose();

					// NEW: Refresh the customerDeliveries map from state
					customerDeliveries = AppContext.deliveryDetialsController.getState().getMappedCustomerDeliveries();

					// Update customer table
					updateCustomerTable();

					// Update financial summary in overview tab
					DeliveryOverviewTab.updateFinancialSummary();

					// Show success notification with appropriate message
					String message;
					if (willBeRemoved) {
						message = "Customer removed from delivery";
					} else {
						message = "Customer status updated to: " + selectedStatus;
					}

					ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(parent), message);
				});
			}, (error) -> {
				SwingUtilities.invokeLater(() -> {
					ToastNotification.showError(SwingUtilities.getWindowAncestor(parent),
							"Failed to update status: " + error);
				});
			});
		});
		statusDialog.add(saveBtn, gbc);

		statusDialog.pack();
		statusDialog.setMinimumSize(new Dimension(450, 220));
		statusDialog.setLocationRelativeTo(null);
		statusDialog.setVisible(true);
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

		if (!deliveryStatus.equalsIgnoreCase("Delivered")) {
			gbc.gridy++;
			gbc.insets = new Insets(15, 20, 5, 20);

			JButton setPaymentBtn = createActionButton("💳 Set Payment", ACCENT_GOLD);
			setPaymentBtn.addActionListener(e -> {
				// WRAP IN invokeLater
				SwingUtilities.invokeLater(() -> {
					actionDialog.dispose();

					// Calculate customer's total sales
					double totalSales = calculateCustomerTotalSales(customer);

					// Check if total sales is 0
					if (totalSales <= 0) {
						ToastNotification.showError(SwingUtilities.getWindowAncestor(parent),
								String.format("Cannot set payment type. Customer has no sales (₱0.00)"));
						return;
					}

					String currentPaymentType = AppContext.deliveryDetialsController.getState()
							.getPaymentType(customer);

					SetPaymentTypeDialog.show(SwingUtilities.getWindowAncestor(parent), customer, currentPaymentType,
							totalSales, (paymentType, partialAmount, loanDate) -> {

								System.out.println("Payment: " + paymentType + partialAmount + loanDate);
								String displayText = paymentType;
								if (paymentType.equals("Partial") && partialAmount != null) {
									displayText = String.format("Partial (₱%.2f)", partialAmount);
								} else if (paymentType.equals("Loan") && loanDate != null) {
									SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
									displayText = "Loan (Due: " + sdf.format(loanDate) + ")";
								}

								AppContext.deliveryDetialsController.setTemporaryPaymentType(customer, displayText);

								updateCustomerTable();
							});
				});
			});
			actionDialog.add(setPaymentBtn, gbc);
		}

		if (!deliveryStatus.equalsIgnoreCase("Delivered")) {

			gbc.gridy++;
			gbc.insets = new Insets(5, 20, 5, 20);
			JButton setStatusBtn = createActionButton("📋 Set Status", SIDEBAR_ACTIVE);
			setStatusBtn.addActionListener(e -> {
				// WRAP IN invokeLater
				SwingUtilities.invokeLater(() -> {
					actionDialog.dispose();
					showSetStatusDialog(parent, customer);
				});
			});
			actionDialog.add(setStatusBtn, gbc);
		}

		gbc.gridy++;
		gbc.insets = new Insets(5, 20, 5, 20);
		JButton viewDetailsBtn = createActionButton("👁️ View Details", SIDEBAR_ACTIVE);
		viewDetailsBtn.addActionListener(e -> {
			// WRAP IN invokeLater
			SwingUtilities.invokeLater(() -> {
				actionDialog.dispose();

				Map<Branch, List<ProductWithQuantity>> branchDeliveries = AppContext.deliveryDetialsController
						.getState().getMappedCustomerDeliveries().get(customer);

				if (branchDeliveries == null) {
					ToastNotification.showError(SwingUtilities.getWindowAncestor(parent),
							"No branch deliveries found for this customer");
					return;
				}

				int nullBranchCount = 0;
				int validBranchCount = 0;

				for (Map.Entry<Branch, List<ProductWithQuantity>> entry : branchDeliveries.entrySet()) {
					Branch branch = entry.getKey();
					List<ProductWithQuantity> products = entry.getValue();

					if (branch == null) {
						nullBranchCount++;
					} else {
						validBranchCount++;
					}
				}

				if (branchDeliveries.isEmpty()) {
					ToastNotification.showError(SwingUtilities.getWindowAncestor(parent),
							"No branch deliveries found for this customer");
					return;
				}

				if (nullBranchCount > 0) {
					ToastNotification.showWarning(SwingUtilities.getWindowAncestor(parent),
							"Warning: Found " + nullBranchCount + " null branches. These will be skipped.");
				}

				CustomerBranchDetailsDialog.show(SwingUtilities.getWindowAncestor(parent), customer, branchDeliveries,
						() -> {
							updateCustomerTable();
							DeliveryOverviewTab.updateFinancialSummary();
						});
			});
		});
		actionDialog.add(viewDetailsBtn, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 20, 15, 20);
		JButton cancelBtn = createActionButton("Cancel", new Color(120, 120, 120));
		cancelBtn.addActionListener(e -> actionDialog.dispose());
		actionDialog.add(cancelBtn, gbc);

		actionDialog.pack();
		actionDialog.setMinimumSize(new Dimension(380, deliveryStatus.equalsIgnoreCase("Delivered") ? 240 : 300));
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

	public static boolean validateAllPaymentsSet() {
		for (Customer customer : customerDeliveries.keySet()) {
			// Skip cancelled customers
			String customerStatus = AppContext.deliveryDetialsController.getCustomerDeliveryStatus(customer);
			if ("Cancelled".equalsIgnoreCase(customerStatus)) {
				continue;
			}

			String paymentType = AppContext.deliveryDetialsController.getState().getPaymentType(customer);
			if (paymentType.equals("Not Set")) {
				return false;
			}
		}
		return true;
	}

	public static boolean isThereCustomerDeliveries() {
		// Check if there's at least one customer with "Delivered" status
		for (Customer customer : customerDeliveries.keySet()) {
			String customerStatus = AppContext.deliveryDetialsController.getCustomerDeliveryStatus(customer);
			if ("Delivered".equalsIgnoreCase(customerStatus)) {
				return true;
			}
		}
		return false;
	}

	public static void refreshFinancials() {
		// This method is called when expenses are updated from the Overview tab
		// or when branch statuses change to ensure customer deliveries data stays in
		// sync
		customerDeliveries = AppContext.deliveryDetialsController.getState().getMappedCustomerDeliveries();
		if (customerTable != null && customerTableModel != null) {
			updateCustomerTable();
		}
	}
}