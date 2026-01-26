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

		customerTable.getColumnModel().getColumn(0).setPreferredWidth(220);
		customerTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		customerTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		customerTable.getColumnModel().getColumn(3).setPreferredWidth(130);
		customerTable.getColumnModel().getColumn(4).setPreferredWidth(130);
		customerTable.getColumnModel().getColumn(5).setPreferredWidth(100);
		customerTable.getColumnModel().getColumn(6).setPreferredWidth(0);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		customerTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		customerTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		customerTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
		customerTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

		customerTable.getColumnModel().getColumn(6).setMinWidth(0);
		customerTable.getColumnModel().getColumn(6).setMaxWidth(0);
		customerTable.getColumnModel().getColumn(6).setWidth(0);

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

			double totalProfit = totalSales - totalCapital;

			String paymentType = AppContext.deliveryDetialsController.getState().getPaymentType(customer);

			customerTableModel.addRow(new Object[] { customer.getDisplayName(), paymentType, branches.size(),
					String.format("₱%,.2f", totalSales), String.format("₱%,.2f", totalProfit), "⚙ Actions", customer });
		}
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
				actionDialog.dispose();

				String currentPaymentType = AppContext.deliveryDetialsController.getState().getPaymentType(customer);

				SetPaymentTypeDialog.show(SwingUtilities.getWindowAncestor(parent), customer, currentPaymentType,
						(paymentType, partialAmount, loadDate) -> {
							String displayText = paymentType;
							if (paymentType.equals("Partial") && partialAmount != null) {
								displayText = String.format("Partial (₱%.2f)", partialAmount);
							} else if (paymentType.equals("Load") && loadDate != null) {
								SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
								displayText = "Load (Due: " + sdf.format(loadDate) + ")";
							}

							AppContext.deliveryDetialsController.setTemporaryPaymentType(customer, displayText);

							updateCustomerTable();
						});
			});
			actionDialog.add(setPaymentBtn, gbc);
		}

		gbc.gridy++;
		gbc.insets = new Insets(deliveryStatus.equalsIgnoreCase("Delivered") ? 15 : 5, 20, 5, 20);
		JButton viewDetailsBtn = createActionButton("👁️ View Details", SIDEBAR_ACTIVE);
		viewDetailsBtn.addActionListener(e -> {
			actionDialog.dispose();

			Map<Branch, List<ProductWithQuantity>> branchDeliveries = AppContext.deliveryDetialsController.getState()
					.getMappedCustomerDeliveries().get(customer);

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
		actionDialog.add(viewDetailsBtn, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 20, 15, 20);
		JButton cancelBtn = createActionButton("Cancel", new Color(120, 120, 120));
		cancelBtn.addActionListener(e -> actionDialog.dispose());
		actionDialog.add(cancelBtn, gbc);

		actionDialog.pack();
		actionDialog.setMinimumSize(new Dimension(380, deliveryStatus.equalsIgnoreCase("Delivered") ? 180 : 220));
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
			String paymentType = AppContext.deliveryDetialsController.getState().getPaymentType(customer);
			if (paymentType.equals("Not Set")) {
				return false;
			}
		}
		return true;
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