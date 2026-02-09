package molases_appcom.gierza_molases.molases_app.ui.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.model.CustomerPayments;

public class PaymentViewPage {

	// Color Palette (matching PaymentsPage)
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);

	// Payment Type Colors
	private static final Color COLOR_PAID_CASH = new Color(46, 125, 50);
	private static final Color COLOR_PAID_CHEQUE = new Color(25, 118, 210);
	private static final Color COLOR_PARTIAL = new Color(245, 124, 0);
	private static final Color COLOR_LOAN = new Color(123, 31, 162);

	// Status Colors
	private static final Color COLOR_PENDING = new Color(255, 152, 0);
	private static final Color COLOR_COMPLETE = new Color(76, 175, 80);

	// Payment Types
	private static final String TYPE_PAID_CASH = "Paid Cash";
	private static final String TYPE_PAID_CHEQUE = "Paid Cheque";
	private static final String TYPE_PARTIAL = "Partial";
	private static final String TYPE_LOAN = "Loan";

	// Payment Status
	private static final String STATUS_PENDING = "Pending";
	private static final String STATUS_COMPLETE = "Complete";

	// Section background colors
	private static final Color SECTION_BG = new Color(255, 255, 255);
	private static final Color SECTION_BORDER = new Color(220, 210, 200);

	// Currency formatter
	private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

	// Date formatter
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy");

	static {
		currencyFormatter.setMaximumFractionDigits(2);
		currencyFormatter.setMinimumFractionDigits(2);
	}

	// State
	private static CustomerPayments currentPayment;
	private static Runnable currentOnBack;
	private static JPanel mainPanel;

	/**
	 * Create the Payment View Page panel
	 */
	public static JPanel createPanel(CustomerPayments payment, Runnable onBack) {
		currentPayment = payment;
		currentOnBack = onBack;

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

		// Header
		mainPanel.add(createHeader(), BorderLayout.NORTH);

		// Content (sections)
		mainPanel.add(createContentPanel(), BorderLayout.CENTER);

		return mainPanel;
	}

	/**
	 * Create header with title and buttons
	 */
	private static JPanel createHeader() {
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(CONTENT_BG);
		headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

		// Left side: Title
		JLabel titleLabel = new JLabel("Payment Details");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
		titleLabel.setForeground(TEXT_DARK);
		headerPanel.add(titleLabel, BorderLayout.WEST);

		// Right side: Action buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBackground(CONTENT_BG);

		// View Payment History button (only for Loan payments)
		if (TYPE_PARTIAL.equals(currentPayment.getPaymentType())) {
			JButton historyBtn = createStyledButton("📋 View Payment History", ACCENT_GOLD);
			historyBtn.setPreferredSize(new Dimension(200, 40));
			historyBtn.addActionListener(e -> showPaymentHistoryDialog());
			buttonPanel.add(historyBtn);
			buttonPanel.add(Box.createHorizontalStrut(10));
		}

		// Back button
		JButton backButton = createStyledButton("← Back", new Color(120, 120, 120));
		backButton.setPreferredSize(new Dimension(100, 40));
		backButton.addActionListener(e -> {
			if (currentOnBack != null) {
				currentOnBack.run();
			}
		});
		buttonPanel.add(backButton);

		headerPanel.add(buttonPanel, BorderLayout.EAST);

		return headerPanel;
	}

	/**
	 * Create content panel with sections
	 */
	private static JPanel createContentPanel() {
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(CONTENT_BG);

		// Delivery Information Section
		contentPanel.add(createDeliveryInfoSection());
		contentPanel.add(Box.createVerticalStrut(20));

		// Payment Information Section
		contentPanel.add(createPaymentInfoSection());

		// Add vertical glue to push content to top
		contentPanel.add(Box.createVerticalGlue());

		return contentPanel;
	}

	/**
	 * Create Delivery Information Section
	 */
	private static JPanel createDeliveryInfoSection() {
		JPanel section = new JPanel();
		section.setBackground(SECTION_BG);
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SECTION_BORDER, 1),
				new EmptyBorder(20, 25, 20, 25)));
		section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
		section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

		// Title
		JLabel titleLabel = new JLabel("📦 DELIVERY INFORMATION");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		section.add(titleLabel);

		section.add(Box.createVerticalStrut(15));

		// Delivery Name
		String deliveryName = currentPayment.getDeliveryName() != null ? currentPayment.getDeliveryName() : "N/A";
		section.add(createInfoRow("Delivery Name:", deliveryName));
		section.add(Box.createVerticalStrut(10));

		// Delivery Date
		String deliveryDate = currentPayment.getDeliveryDate() != null
				? dateFormatter.format(currentPayment.getDeliveryDate())
				: "N/A";
		section.add(createInfoRow("Delivery Date:", deliveryDate));
		section.add(Box.createVerticalStrut(15));

		// View Delivery Details button
		JButton viewDeliveryBtn = createStyledButton("🔍 View Delivery Details", SIDEBAR_ACTIVE);
		viewDeliveryBtn.setPreferredSize(new Dimension(200, 38));
		viewDeliveryBtn.setMaximumSize(new Dimension(200, 38));
		viewDeliveryBtn.setAlignmentX(JButton.LEFT_ALIGNMENT);
		viewDeliveryBtn.addActionListener(e -> navigateToDeliveryDetails());
		section.add(viewDeliveryBtn);

		return section;
	}

	/**
	 * Create Payment Information Section
	 */
	private static JPanel createPaymentInfoSection() {
		JPanel section = new JPanel();
		section.setBackground(SECTION_BG);
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SECTION_BORDER, 1),
				new EmptyBorder(20, 25, 20, 25)));
		section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

		// Title
		JLabel titleLabel = new JLabel("💰 PAYMENT INFORMATION");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		section.add(titleLabel);

		section.add(Box.createVerticalStrut(15));

		// Customer Name
		String customerName = currentPayment.getCustomerName() != null ? currentPayment.getCustomerName() : "N/A";
		section.add(createInfoRow("Customer Name:", customerName));
		section.add(Box.createVerticalStrut(10));

		// Payment Type with Edit button (for Loan and Partial only)
		section.add(createPaymentTypeRow());
		section.add(Box.createVerticalStrut(10));

		// Status
		section.add(createStatusRow());
		section.add(Box.createVerticalStrut(10));

		// Total Amount
		String totalAmount = currencyFormatter.format(currentPayment.getTotal()).replace("PHP", "₱");
		section.add(createInfoRow("Total Amount:", totalAmount));
		section.add(Box.createVerticalStrut(10));

		// Total Paid
		String totalPaid = currencyFormatter.format(currentPayment.getTotalPayment()).replace("PHP", "₱");
		section.add(createInfoRow("Total Paid:", totalPaid));
		section.add(Box.createVerticalStrut(10));

		// Remaining Balance
		double remainingBalance = currentPayment.getTotal() - currentPayment.getTotalPayment();
		String remainingBalanceStr = currencyFormatter.format(remainingBalance).replace("PHP", "₱");
		section.add(createInfoRow("Remaining Balance:", remainingBalanceStr));

		// Promise to Pay (only for Loan payments)
		if (TYPE_LOAN.equals(currentPayment.getPaymentType())) {
			section.add(Box.createVerticalStrut(10));
			section.add(createPromiseToPayRow());
		}

		return section;
	}

	/**
	 * Create payment type row with colored text and edit button
	 */
	private static JPanel createPaymentTypeRow() {
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.setBackground(SECTION_BG);
		row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

		// Label
		JLabel label = new JLabel("Payment Type:");
		label.setFont(new Font("Arial", Font.BOLD, 15));
		label.setForeground(TEXT_DARK);
		label.setPreferredSize(new Dimension(180, 25));
		row.add(label);

		// Value (colored based on payment type)
		String paymentType = currentPayment.getPaymentType();
		JLabel valueLabel = new JLabel(paymentType);
		valueLabel.setFont(new Font("Arial", Font.BOLD, 15));
		valueLabel.setForeground(getPaymentTypeColor(paymentType));
		row.add(valueLabel);

		// Edit button (only for Loan and Partial)
		if (TYPE_LOAN.equals(paymentType) || TYPE_PARTIAL.equals(paymentType)) {
			row.add(Box.createHorizontalStrut(10));
			JButton editBtn = createSmallEditButton();
			editBtn.addActionListener(e -> showEditPaymentTypeDialog());
			row.add(editBtn);
		}

		row.add(Box.createHorizontalGlue());

		return row;
	}

	/**
	 * Create status row with colored text
	 */
	private static JPanel createStatusRow() {
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.setBackground(SECTION_BG);
		row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

		// Label
		JLabel label = new JLabel("Status:");
		label.setFont(new Font("Arial", Font.BOLD, 15));
		label.setForeground(TEXT_DARK);
		label.setPreferredSize(new Dimension(180, 25));
		row.add(label);

		// Value (colored based on status)
		String status = currentPayment.getStatus();
		String displayStatus = status.substring(0, 1).toUpperCase() + status.substring(1);
		JLabel valueLabel = new JLabel(displayStatus);
		valueLabel.setFont(new Font("Arial", Font.BOLD, 15));
		valueLabel.setForeground(getStatusColor(status));
		row.add(valueLabel);

		row.add(Box.createHorizontalGlue());

		return row;
	}

	/**
	 * Create promise to pay row with date and edit button
	 */
	private static JPanel createPromiseToPayRow() {
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.setBackground(SECTION_BG);
		row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

		// Label
		JLabel label = new JLabel("Promise to Pay:");
		label.setFont(new Font("Arial", Font.BOLD, 15));
		label.setForeground(TEXT_DARK);
		label.setPreferredSize(new Dimension(180, 25));
		row.add(label);

		// Value
		String promiseToPayDate = currentPayment.getPromiseToPay() != null
				? dateFormatter.format(currentPayment.getPromiseToPay())
				: "Not set";
		JLabel valueLabel = new JLabel(promiseToPayDate);
		valueLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		valueLabel.setForeground(TEXT_DARK);
		row.add(valueLabel);

		// Edit button
		row.add(Box.createHorizontalStrut(10));
		JButton editBtn = createSmallEditButton();
		editBtn.addActionListener(e -> showEditPromiseToPayDialog());
		row.add(editBtn);

		row.add(Box.createHorizontalGlue());

		return row;
	}

	/**
	 * Create a generic info row (label + value)
	 */
	private static JPanel createInfoRow(String labelText, String valueText) {
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.setBackground(SECTION_BG);
		row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

		// Label
		JLabel label = new JLabel(labelText);
		label.setFont(new Font("Arial", Font.BOLD, 15));
		label.setForeground(TEXT_DARK);
		label.setPreferredSize(new Dimension(180, 25));
		row.add(label);

		// Value
		JLabel valueLabel = new JLabel(valueText);
		valueLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		valueLabel.setForeground(TEXT_DARK);
		row.add(valueLabel);

		row.add(Box.createHorizontalGlue());

		return row;
	}

	/**
	 * Get color for payment type
	 */
	private static Color getPaymentTypeColor(String paymentType) {
		switch (paymentType) {
		case TYPE_PAID_CASH:
			return COLOR_PAID_CASH;
		case TYPE_PAID_CHEQUE:
			return COLOR_PAID_CHEQUE;
		case TYPE_PARTIAL:
			return COLOR_PARTIAL;
		case TYPE_LOAN:
			return COLOR_LOAN;
		default:
			return TEXT_DARK;
		}
	}

	/**
	 * Get color for status
	 */
	private static Color getStatusColor(String status) {
		if (STATUS_PENDING.equalsIgnoreCase(status)) {
			return COLOR_PENDING;
		} else if (STATUS_COMPLETE.equalsIgnoreCase(status)) {
			return COLOR_COMPLETE;
		}
		return TEXT_DARK;
	}

	/**
	 * Create small edit button
	 */
	private static JButton createSmallEditButton() {
		JButton editBtn = new JButton("📝 Edit");
		editBtn.setFont(new Font("Arial", Font.PLAIN, 12));
		editBtn.setBackground(new Color(100, 100, 100));
		editBtn.setForeground(TEXT_LIGHT);
		editBtn.setFocusPainted(false);
		editBtn.setBorderPainted(false);
		editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		editBtn.setPreferredSize(new Dimension(70, 25));
		editBtn.setMaximumSize(new Dimension(70, 25));

		editBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (editBtn.isEnabled()) {
					editBtn.setBackground(new Color(130, 130, 130));
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				editBtn.setBackground(new Color(100, 100, 100));
			}
		});

		return editBtn;
	}

	/**
	 * Show payment history dialog
	 */
	private static void showPaymentHistoryDialog() {
		JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(mainPanel), "Payment History");
		dialog.setLayout(new BorderLayout());
		dialog.getContentPane().setBackground(CONTENT_BG);

		// Header
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(Color.WHITE);
		headerPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

		JLabel titleLabel = new JLabel("Payment History - Customer: " + currentPayment.getCustomerName());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(TEXT_DARK);
		headerPanel.add(titleLabel, BorderLayout.NORTH);

		JLabel subtitleLabel = new JLabel("All payments made for this transaction");
		subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		subtitleLabel.setForeground(new Color(100, 100, 100));
		subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
		headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

		dialog.add(headerPanel, BorderLayout.NORTH);

		// Table with payment history
		String[] columns = { "Amount Paid", "Paid At" };
		javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		// Mock data - TODO: Replace with actual database query
		// Example: Load payment history from database using customer_payment_id
		addMockPaymentHistoryData(model);

		javax.swing.JTable table = new javax.swing.JTable(model);
		table.setFont(new Font("Arial", Font.PLAIN, 14));
		table.setRowHeight(45);
		table.setShowGrid(true);
		table.setGridColor(new Color(220, 210, 200));
		table.setBackground(Color.WHITE);
		table.setSelectionBackground(new Color(245, 239, 231));
		table.setSelectionForeground(TEXT_DARK);

		// Set column widths
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(200);

		// Custom header
		javax.swing.table.JTableHeader header = table.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(SIDEBAR_ACTIVE);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));

		javax.swing.table.DefaultTableCellRenderer headerRenderer = new javax.swing.table.DefaultTableCellRenderer();
		headerRenderer.setBackground(SIDEBAR_ACTIVE);
		headerRenderer.setForeground(TEXT_LIGHT);
		headerRenderer.setFont(new Font("Arial", Font.BOLD, 14));
		headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}

		// Custom cell renderer for alternating rows and right-align amount
		table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
			@Override
			public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 245, 240));
					setForeground(TEXT_DARK);
				}

				if (column == 0) { // Amount column - right align
					setHorizontalAlignment(SwingConstants.RIGHT);
				} else {
					setHorizontalAlignment(SwingConstants.LEFT);
				}

				((JLabel) c).setBorder(new EmptyBorder(5, 15, 5, 15));
				return c;
			}
		});

		javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
		scrollPane.setPreferredSize(new Dimension(450, 300));

		dialog.add(scrollPane, BorderLayout.CENTER);

		// Footer with total
		JPanel footerPanel = new JPanel(new BorderLayout());
		footerPanel.setBackground(Color.WHITE);
		footerPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(220, 210, 200)),
				new EmptyBorder(15, 25, 15, 25)));

		// Calculate total from table
		double total = 0.0;
		for (int i = 0; i < model.getRowCount(); i++) {
			String amountStr = model.getValueAt(i, 0).toString().replace("₱", "").replace(",", "");
			total += Double.parseDouble(amountStr);
		}

		JLabel totalLabel = new JLabel("Total Payments:");
		totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
		totalLabel.setForeground(TEXT_DARK);
		footerPanel.add(totalLabel, BorderLayout.WEST);

		String totalStr = currencyFormatter.format(total).replace("PHP", "₱");
		JLabel totalValueLabel = new JLabel(totalStr);
		totalValueLabel.setFont(new Font("Arial", Font.BOLD, 18));
		totalValueLabel.setForeground(COLOR_COMPLETE);
		footerPanel.add(totalValueLabel, BorderLayout.EAST);

		dialog.add(footerPanel, BorderLayout.SOUTH);

		dialog.pack();
		dialog.setMinimumSize(new Dimension(500, 450));
		dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(mainPanel));
		dialog.setVisible(true);
	}

	/**
	 * Add mock payment history data TODO: Replace with actual database query
	 */
	private static void addMockPaymentHistoryData(javax.swing.table.DefaultTableModel model) {
		// Mock data - In real implementation, query payment_history table
		// SELECT amount, created_at FROM payment_history WHERE customer_payment_id = ?

		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");

		// Sample payment history entries
		model.addRow(new Object[] { currencyFormatter.format(5000.00).replace("PHP", "₱"),
				dateTimeFormat.format(new java.util.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L)) });

		model.addRow(new Object[] { currencyFormatter.format(3000.00).replace("PHP", "₱"),
				dateTimeFormat.format(new java.util.Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L)) });

		model.addRow(new Object[] { currencyFormatter.format(2000.00).replace("PHP", "₱"),
				dateTimeFormat.format(new java.util.Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000L)) });
	}

	/**
	 * Navigate to delivery details page
	 */
	private static void navigateToDeliveryDetails() {
		// TODO: This needs to be implemented based on your app's navigation pattern
		// You might need to call AppContext or main frame to switch to
		// DeliveryDetailsPage
		System.out.println("Navigate to Delivery Details - ID: " + currentPayment.getCustomerDeliveryId());
	}

	/**
	 * Show edit payment type dialog
	 */
	private static void showEditPaymentTypeDialog() {
		JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(mainPanel), "Edit Payment Type");
		dialog.setLayout(new GridBagLayout());
		dialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);

		JLabel titleLabel = new JLabel("Change Payment Type");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		dialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 10, 30);

		String currentType = currentPayment.getPaymentType();
		JLabel currentLabel = new JLabel("Current Type: " + currentType);
		currentLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		currentLabel.setForeground(getPaymentTypeColor(currentType));
		dialog.add(currentLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);

		JLabel instructionLabel = new JLabel("Select new payment type:");
		instructionLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		instructionLabel.setForeground(TEXT_DARK);
		dialog.add(instructionLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 20, 30);

		// Combo box with only Loan and Partial options
		String[] paymentTypes = { TYPE_LOAN, TYPE_PARTIAL };
		javax.swing.JComboBox<String> typeCombo = new javax.swing.JComboBox<>(paymentTypes);
		typeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
		typeCombo.setPreferredSize(new Dimension(250, 35));
		typeCombo.setBackground(Color.WHITE);
		typeCombo.setSelectedItem(currentType);
		dialog.add(typeCombo, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 20, 30);

		JLabel warningLabel = new JLabel(
				"<html><center>⚠️ Changing from Loan to Partial will<br>remove the Promise to Pay date</center></html>");
		warningLabel.setFont(new Font("Arial", Font.ITALIC, 12));
		warningLabel.setForeground(new Color(180, 100, 0));
		warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
		dialog.add(warningLabel, gbc);

		// Buttons
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 30, 20, 10);

		JButton cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> dialog.dispose());
		dialog.add(cancelBtn, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(20, 10, 20, 30);
		JButton saveBtn = createStyledButton("Save", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(120, 40));
		saveBtn.addActionListener(e -> {
			String newType = (String) typeCombo.getSelectedItem();

			// Check if type actually changed
			if (newType.equals(currentType)) {
				dialog.dispose();
				return;
			}

			// Confirmation if changing from Loan to Partial (loses Promise to Pay)
			if (currentType.equals(TYPE_LOAN) && newType.equals(TYPE_PARTIAL)) {
				int confirm = javax.swing.JOptionPane.showConfirmDialog(dialog,
						"Changing to Partial will remove the Promise to Pay date.\nAre you sure?", "Confirm Change",
						javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);

				if (confirm != javax.swing.JOptionPane.YES_OPTION) {
					return;
				}
			}

			// TODO: Update database with new payment type
			// For now, just show success message
			System.out.println("Changing payment type from " + currentType + " to " + newType);

			dialog.dispose();
			com.gierza_molases.molases_app.ui.components.ToastNotification
					.showSuccess(SwingUtilities.getWindowAncestor(mainPanel), "Payment type updated successfully!");

			// Refresh the page to show updated data
			// In real implementation, you would reload the payment data from DB
		});
		dialog.add(saveBtn, gbc);

		dialog.pack();
		dialog.setMinimumSize(new Dimension(450, 320));
		dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(mainPanel));
		dialog.setVisible(true);
	}

	/**
	 * Show edit promise to pay dialog
	 */
	private static void showEditPromiseToPayDialog() {
		JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(mainPanel), "Edit Promise to Pay");
		dialog.setLayout(new GridBagLayout());
		dialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);

		JLabel titleLabel = new JLabel("Set Promise to Pay Date");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		dialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);

		JLabel instructionLabel = new JLabel("Select a future date for payment:");
		instructionLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		instructionLabel.setForeground(TEXT_DARK);
		dialog.add(instructionLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 20, 30);

		// Date chooser
		com.toedter.calendar.JDateChooser dateChooser = new com.toedter.calendar.JDateChooser();
		dateChooser.setDateFormatString("MMM dd, yyyy");
		dateChooser.setPreferredSize(new Dimension(250, 35));
		dateChooser.setFont(new Font("Arial", Font.PLAIN, 14));

		// Set current promise to pay date if exists
		if (currentPayment.getPromiseToPay() != null) {
			dateChooser.setDate(currentPayment.getPromiseToPay());
		}

		dialog.add(dateChooser, gbc);

		// Buttons
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 30, 20, 10);

		JButton cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> dialog.dispose());
		dialog.add(cancelBtn, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(20, 10, 20, 30);
		JButton saveBtn = createStyledButton("Save", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(120, 40));
		saveBtn.addActionListener(e -> {
			java.util.Date selectedDate = dateChooser.getDate();

			// Validation: Check if date is selected
			if (selectedDate == null) {
				javax.swing.JOptionPane.showMessageDialog(dialog, "Please select a date.", "Validation Error",
						javax.swing.JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Validation: Check if date is not in the past
			java.util.Date today = new java.util.Date();
			// Reset time to start of day for comparison
			java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.setTime(today);
			cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
			cal.set(java.util.Calendar.MINUTE, 0);
			cal.set(java.util.Calendar.SECOND, 0);
			cal.set(java.util.Calendar.MILLISECOND, 0);
			today = cal.getTime();

			if (selectedDate.before(today)) {
				javax.swing.JOptionPane.showMessageDialog(dialog,
						"Cannot set a date in the past. Please select today or a future date.", "Invalid Date",
						javax.swing.JOptionPane.WARNING_MESSAGE);
				return;
			}

			// TODO: Update database with new promise to pay date
			// For now, just update the UI with mock data
			System.out.println("Saving Promise to Pay: " + dateFormatter.format(selectedDate));

			// Update current payment object (mock update)
			// In real implementation, this would be done after successful DB update

			dialog.dispose();
			com.gierza_molases.molases_app.ui.components.ToastNotification.showSuccess(
					SwingUtilities.getWindowAncestor(mainPanel), "Promise to Pay date updated successfully!");

			// Refresh the page to show updated data
			// In real implementation, you would reload the payment data from DB
		});
		dialog.add(saveBtn, gbc);

		dialog.pack();
		dialog.setMinimumSize(new Dimension(400, 250));
		dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(mainPanel));
		dialog.setVisible(true);
	}

	/**
	 * Create styled button
	 */
	private static JButton createStyledButton(String text, Color bgColor) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 14));
		button.setBackground(bgColor);
		button.setForeground(TEXT_LIGHT);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (button.isEnabled()) {
					button.setBackground(bgColor.brighter());
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setBackground(bgColor);
			}
		});

		return button;
	}
}