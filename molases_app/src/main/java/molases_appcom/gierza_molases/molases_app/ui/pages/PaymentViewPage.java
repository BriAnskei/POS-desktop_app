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
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.model.PaymentHistory;
import com.gierza_molases.molases_app.ui.components.delivery.UIComponentFactory;

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
	private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("MMM dd, yyyy hh:mm a");

	private static java.util.function.Consumer<Integer> currentOnNavigateToDelivery;

	static {
		currencyFormatter.setMaximumFractionDigits(2);
		currencyFormatter.setMinimumFractionDigits(2);
	}

	// State
	private static Runnable currentOnBack;
	private static JPanel mainPanel;
	private static JPanel contentContainer;

	/**
	 * Create the Payment View Page panel
	 */
	public static JPanel createPanel(CustomerPayments payment, Runnable onBack,
			java.util.function.Consumer<Integer> onNavigateToDelivery) {
		AppContext.customerPaymentViewController.setCustomerPayment(payment);
		currentOnBack = onBack;
		currentOnNavigateToDelivery = onNavigateToDelivery; // Store the callback

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

		// Header (fixed at top, not scrollable)
		mainPanel.add(createHeader(), BorderLayout.NORTH);

		// Create content container panel
		contentContainer = new JPanel(new BorderLayout());
		contentContainer.setBackground(CONTENT_BG);
		contentContainer.add(createContentPanel(), BorderLayout.CENTER);

		// Wrap content in scroll pane
		JScrollPane scrollPane = new JScrollPane(contentContainer);
		scrollPane.setBackground(CONTENT_BG);
		scrollPane.getViewport().setBackground(CONTENT_BG);
		scrollPane.setBorder(null);

		// Configure scroll pane
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// Improve scroll speed
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getVerticalScrollBar().setBlockIncrement(50);

		// Add scroll pane to main panel
		mainPanel.add(scrollPane, BorderLayout.CENTER);

		return mainPanel;
	}

	/**
	 * Create header with title and buttons
	 */
	private static JPanel createHeader() {
		CustomerPayments currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();
		if (currentPayment == null) {
			return new JPanel(); // Return empty panel if no payment data
		}

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

		// Back button
		JButton backButton = UIComponentFactory.createStyledButton("← Back", new Color(120, 120, 120));

		backButton.addActionListener(e -> {
			AppContext.customerPaymentViewController.resetState();
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

		// Two-column layout: Combined Info + Payment History
		JPanel twoColumnPanel = new JPanel(new GridBagLayout());
		twoColumnPanel.setBackground(CONTENT_BG);
		twoColumnPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.45;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 10);

		// Left: Combined Delivery + Payment Information
		twoColumnPanel.add(createCombinedInfoSection(), gbc);

		gbc.gridx = 1;
		gbc.weightx = 0.55;
		gbc.insets = new Insets(0, 10, 0, 0);

		// Right: Payment History
		twoColumnPanel.add(createPaymentHistorySection(), gbc);

		contentPanel.add(twoColumnPanel);

		// Add vertical glue to push content to top
		contentPanel.add(Box.createVerticalGlue());

		return contentPanel;
	}

	/**
	 * Create Combined Delivery and Payment Information Section
	 */
	private static JPanel createCombinedInfoSection() {
		CustomerPayments currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();
		if (currentPayment == null) {
			return new JPanel();
		}

		JPanel section = new JPanel();
		section.setBackground(SECTION_BG);
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SECTION_BORDER, 1),
				new EmptyBorder(20, 25, 20, 25)));
		section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

		// ========== DELIVERY INFORMATION ==========
		JLabel deliveryTitleLabel = new JLabel("📦 DELIVERY INFORMATION");
		deliveryTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		deliveryTitleLabel.setForeground(TEXT_DARK);
		deliveryTitleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		section.add(deliveryTitleLabel);

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

		// Separator
		section.add(Box.createVerticalStrut(25));
		JPanel separator = new JPanel();
		separator.setBackground(SECTION_BORDER);
		separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
		separator.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		section.add(separator);
		section.add(Box.createVerticalStrut(25));

		// ========== PAYMENT INFORMATION ==========
		JLabel paymentTitleLabel = new JLabel("💰 PAYMENT INFORMATION");
		paymentTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		paymentTitleLabel.setForeground(TEXT_DARK);
		paymentTitleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		section.add(paymentTitleLabel);

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
	 * Create Payment History Section
	 */
	private static JPanel createPaymentHistorySection() {
		CustomerPayments currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();
		if (currentPayment == null) {
			return new JPanel();
		}

		JPanel section = new JPanel(new BorderLayout());
		section.setBackground(SECTION_BG);
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SECTION_BORDER, 1),
				new EmptyBorder(20, 25, 20, 25)));

		// Header with title and Add Payment button (only for Partial payment type)
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(SECTION_BG);
		headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

		JLabel titleLabel = new JLabel("💰 PAYMENT HISTORY");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(TEXT_DARK);
		headerPanel.add(titleLabel, BorderLayout.WEST);

		// Add Payment button - ONLY show for Partial payment type
		if (TYPE_PARTIAL.equals(currentPayment.getPaymentType())) {
			JButton addPaymentBtn = createStyledButton("➕ Add Payment", ACCENT_GOLD);
			addPaymentBtn.setPreferredSize(new Dimension(140, 35));
			addPaymentBtn.addActionListener(e -> {
				com.gierza_molases.molases_app.ui.dialogs.Delivery.AddPaymentDialog
						.show(SwingUtilities.getWindowAncestor(mainPanel), () -> refreshPage());
			});
			headerPanel.add(addPaymentBtn, BorderLayout.EAST);
		}

		section.add(headerPanel, BorderLayout.NORTH);

		// Get payment history from state
		List<PaymentHistory> paymentHistory = AppContext.customerPaymentViewController.getState().getPaymentHistory();

		// Check if payment history is empty
		if (paymentHistory == null || paymentHistory.isEmpty()) {
			// Show "No payments yet" message
			JPanel emptyPanel = new JPanel(new GridBagLayout());
			emptyPanel.setBackground(SECTION_BG);

			JLabel emptyLabel = new JLabel("No payments yet");
			emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
			emptyLabel.setForeground(new Color(150, 150, 150));

			emptyPanel.add(emptyLabel);
			section.add(emptyPanel, BorderLayout.CENTER);
		} else {
			// Table with payment history
			String[] columns = { "Amount Paid", "Paid At" };
			javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columns, 0) {
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};

			// Load payment history from state
			for (PaymentHistory payment : paymentHistory) {
				model.addRow(new Object[] { currencyFormatter.format(payment.getAmount()).replace("PHP", "₱"),
						dateTimeFormatter.format(payment.getCreatedAt()) });
			}

			javax.swing.JTable table = new javax.swing.JTable(model);
			table.setFont(new Font("Arial", Font.PLAIN, 14));
			table.setRowHeight(45);
			table.setShowGrid(true);
			table.setGridColor(new Color(220, 210, 200));
			table.setBackground(Color.WHITE);
			table.setSelectionBackground(new Color(245, 239, 231));
			table.setSelectionForeground(TEXT_DARK);

			// Set column widths
			table.getColumnModel().getColumn(0).setPreferredWidth(150);
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

			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
			scrollPane.setPreferredSize(new Dimension(400, 300));

			section.add(scrollPane, BorderLayout.CENTER);

			// Footer with total
			JPanel footerPanel = new JPanel(new BorderLayout());
			footerPanel.setBackground(SECTION_BG);
			footerPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(220, 210, 200)),
					new EmptyBorder(15, 0, 0, 0)));

			// Calculate total from payment history
			double total = 0.0;
			for (PaymentHistory payment : paymentHistory) {
				total += payment.getAmount();
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

			section.add(footerPanel, BorderLayout.SOUTH);
		}

		return section;
	}

	/**
	 * Create payment type row with colored text and edit button
	 */
	private static JPanel createPaymentTypeRow() {
		CustomerPayments currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();
		if (currentPayment == null) {
			return new JPanel();
		}

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
	 * Create status row with colored text and edit button for Loan payments
	 */
	private static JPanel createStatusRow() {
		CustomerPayments currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();
		if (currentPayment == null) {
			return new JPanel();
		}

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

		// Edit button (only for Loan payment type)
		if (TYPE_LOAN.equals(currentPayment.getPaymentType())) {
			row.add(Box.createHorizontalStrut(10));
			JButton editBtn = createSmallEditButton();
			editBtn.addActionListener(e -> showEditStatusDialog());
			row.add(editBtn);
		}

		row.add(Box.createHorizontalGlue());

		return row;
	}

	private static void showEditStatusDialog() {
		CustomerPayments currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();
		if (currentPayment == null) {
			return;
		}

		// Create and show the dialog
		com.gierza_molases.molases_app.ui.dialogs.Delivery.EditStatusDialog dialog = new com.gierza_molases.molases_app.ui.dialogs.Delivery.EditStatusDialog(
				SwingUtilities.getWindowAncestor(mainPanel), currentPayment.getStatus(), () -> refreshPage());

		dialog.setVisible(true); // ← Changed from dialog.show()
	}

	/**
	 * Create promise to pay row with date and edit button Highlights in red if date
	 * is overdue
	 */
	private static JPanel createPromiseToPayRow() {
		CustomerPayments currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();
		if (currentPayment == null) {
			return new JPanel();
		}

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

		// Check if promise to pay date is overdue (past current date)
		boolean isOverdue = false;
		if (currentPayment.getPromiseToPay() != null) {
			java.util.Date today = new java.util.Date();
			java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.setTime(today);
			cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
			cal.set(java.util.Calendar.MINUTE, 0);
			cal.set(java.util.Calendar.SECOND, 0);
			cal.set(java.util.Calendar.MILLISECOND, 0);
			today = cal.getTime();

			if (currentPayment.getPromiseToPay().before(today)) {
				isOverdue = true;
			}
		}

		// Set color based on overdue status
		valueLabel.setForeground(isOverdue ? Color.RED : TEXT_DARK);
		if (isOverdue) {
			valueLabel.setFont(new Font("Arial", Font.BOLD, 15)); // Make it bold too
		}

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
	 * Refresh the entire page to show updated data
	 */
	private static void refreshPage() {
		if (contentContainer == null)
			return;

		SwingUtilities.invokeLater(() -> {
			// Remove all components from content container
			contentContainer.removeAll();

			// Recreate content
			contentContainer.add(createContentPanel(), BorderLayout.CENTER);

			// Refresh display
			contentContainer.revalidate();
			contentContainer.repaint();

			// Scroll to top after refresh
			if (mainPanel != null) {
				JScrollPane scrollPane = findScrollPane(mainPanel);
				if (scrollPane != null) {
					SwingUtilities.invokeLater(() -> {
						scrollPane.getVerticalScrollBar().setValue(0);
					});
				}
			}
		});
	}

	/**
	 * Helper method to find JScrollPane in component hierarchy
	 */
	private static JScrollPane findScrollPane(java.awt.Container container) {
		for (java.awt.Component comp : container.getComponents()) {
			if (comp instanceof JScrollPane) {
				return (JScrollPane) comp;
			} else if (comp instanceof java.awt.Container) {
				JScrollPane found = findScrollPane((java.awt.Container) comp);
				if (found != null)
					return found;
			}
		}
		return null;
	}

	/**
	 * Navigate to delivery details page
	 */
	private static void navigateToDeliveryDetails() {
		CustomerPayments currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();
		if (currentPayment == null) {
			return;
		}

		int deliveryId = currentPayment.getDeliveryId();

		if (deliveryId <= 0) {
			javax.swing.JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(mainPanel),
					"Delivery information not available.", "Navigation Error", javax.swing.JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Reset the payment view state
		AppContext.customerPaymentViewController.resetState();

		// Navigate to delivery details using the callback
		if (currentOnNavigateToDelivery != null) {
			currentOnNavigateToDelivery.accept(deliveryId);
		}
	}

	private static void showEditPaymentTypeDialog() {
		CustomerPayments currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();
		if (currentPayment == null) {
			return;
		}

		JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(mainPanel), "Edit Payment Type");
		dialog.setLayout(new GridBagLayout());
		dialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Title
		JLabel titleLabel = new JLabel("Change Payment Type");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		dialog.add(titleLabel, gbc);

		// Current type
		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 10, 30);

		String currentType = currentPayment.getPaymentType();
		JLabel currentLabel = new JLabel("Current Type: " + currentType);
		currentLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		currentLabel.setForeground(getPaymentTypeColor(currentType));
		dialog.add(currentLabel, gbc);

		// Payment type selector
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);
		gbc.fill = GridBagConstraints.NONE;

		JLabel instructionLabel = new JLabel("Select new payment type:");
		instructionLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		instructionLabel.setForeground(TEXT_DARK);
		dialog.add(instructionLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 15, 30);

		String[] paymentTypes = { TYPE_LOAN, TYPE_PARTIAL };
		javax.swing.JComboBox<String> typeCombo = new javax.swing.JComboBox<>(paymentTypes);
		typeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
		typeCombo.setPreferredSize(new Dimension(300, 35));
		typeCombo.setMaximumSize(new Dimension(300, 35));
		typeCombo.setBackground(Color.WHITE);
		typeCombo.setSelectedItem(currentType);
		dialog.add(typeCombo, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 30, 0, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1.0;
		gbc.weightx = 0.0;

		JPanel dynamicPanel = new JPanel();
		dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.Y_AXIS));
		dynamicPanel.setBackground(Color.WHITE);
		// NO FIXED SIZE - let it start at 0 height and grow as needed
		dialog.add(dynamicPanel, gbc);

		// Components that will be shown/hidden dynamically
		double remainingBalance = currentPayment.getTotal() - currentPayment.getTotalPayment();

		// Partial payment components (for Loan → Partial)
		JLabel remainingBalanceLabel = new JLabel(
				"Remaining Balance: " + currencyFormatter.format(remainingBalance).replace("PHP", "₱"));
		remainingBalanceLabel.setFont(new Font("Arial", Font.BOLD, 13));
		remainingBalanceLabel.setForeground(TEXT_DARK);
		remainingBalanceLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		remainingBalanceLabel.setBorder(new EmptyBorder(10, 0, 0, 0)); // Top padding when shown

		JLabel partialAmountLabel = new JLabel("Enter partial payment amount:");
		partialAmountLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		partialAmountLabel.setForeground(TEXT_DARK);
		partialAmountLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		partialAmountLabel.setBorder(new EmptyBorder(10, 0, 5, 0));

		JTextField partialAmountField = new JTextField();
		partialAmountField.setFont(new Font("Arial", Font.PLAIN, 14));
		partialAmountField.setPreferredSize(new Dimension(300, 35));
		partialAmountField.setMaximumSize(new Dimension(300, 35));
		partialAmountField.setAlignmentX(JTextField.LEFT_ALIGNMENT);

		JLabel partialWarningLabel = new JLabel(
				"<html><center>⚠️ Changing to Partial requires a partial payment amount.<br>"
						+ "Promise to Pay date will be removed.</center></html>");
		partialWarningLabel.setFont(new Font("Arial", Font.ITALIC, 11));
		partialWarningLabel.setForeground(new Color(180, 100, 0));
		partialWarningLabel.setHorizontalAlignment(SwingConstants.CENTER);
		partialWarningLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		partialWarningLabel.setBorder(new EmptyBorder(10, 0, 10, 0)); // Bottom padding when shown

		// Promise to pay components (for Partial → Loan)
		JLabel promiseLabel = new JLabel("Select Promise to Pay date:");
		promiseLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		promiseLabel.setForeground(TEXT_DARK);
		promiseLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		promiseLabel.setBorder(new EmptyBorder(10, 0, 0, 0)); // Top padding when shown

		com.toedter.calendar.JDateChooser dateChooser = new com.toedter.calendar.JDateChooser();
		dateChooser.setDateFormatString("MMM dd, yyyy");
		dateChooser.setPreferredSize(new Dimension(300, 35));
		dateChooser.setMaximumSize(new Dimension(300, 35));
		dateChooser.setFont(new Font("Arial", Font.PLAIN, 14));
		dateChooser.setAlignmentX(com.toedter.calendar.JDateChooser.LEFT_ALIGNMENT);
		if (currentPayment.getPromiseToPay() != null) {
			dateChooser.setDate(currentPayment.getPromiseToPay());
		}

		JLabel loanWarningLabel = new JLabel(
				"<html><center>⚠️ Changing to Loan requires a Promise to Pay date.</center></html>");
		loanWarningLabel.setFont(new Font("Arial", Font.ITALIC, 11));
		loanWarningLabel.setForeground(new Color(180, 100, 0));
		loanWarningLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loanWarningLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		loanWarningLabel.setBorder(new EmptyBorder(10, 0, 10, 0)); // Bottom padding when shown

		// Method to update dynamic panel based on selection
		Runnable updateDynamicPanel = () -> {
			dynamicPanel.removeAll();
			String selectedType = (String) typeCombo.getSelectedItem();

			if (!selectedType.equals(currentType)) {
				if (currentType.equals(TYPE_LOAN) && selectedType.equals(TYPE_PARTIAL)) {
					// Loan → Partial: Show partial payment input
					dynamicPanel.add(remainingBalanceLabel);
					dynamicPanel.add(partialAmountLabel);
					dynamicPanel.add(partialAmountField);
					dynamicPanel.add(partialWarningLabel);
				} else if (currentType.equals(TYPE_PARTIAL) && selectedType.equals(TYPE_LOAN)) {
					// Partial → Loan: Show date picker
					dynamicPanel.add(promiseLabel);
					dynamicPanel.add(Box.createVerticalStrut(5));
					dynamicPanel.add(dateChooser);
					dynamicPanel.add(loanWarningLabel);
				}
			}

			dynamicPanel.revalidate();
			dynamicPanel.repaint();

			// RESPONSIVE HEIGHT: Repack dialog to adjust to content size
			SwingUtilities.invokeLater(() -> {
				dialog.pack();
				// Maintain minimum width
				Dimension currentSize = dialog.getSize();
				if (currentSize.width < 400) {
					dialog.setSize(400, currentSize.height);
				}
				// Re-center after resizing
				dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(mainPanel));
			});
		};

		// Initial update
		updateDynamicPanel.run();

		// Add listener to combo box
		typeCombo.addActionListener(e -> updateDynamicPanel.run());

		// Buttons
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 30, 20, 10);
		gbc.weighty = 0;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;

		JButton cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> dialog.dispose());
		dialog.add(cancelBtn, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(20, 10, 20, 30);
		gbc.anchor = GridBagConstraints.EAST;
		JButton saveBtn = createStyledButton("Save", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(135, 40));
		saveBtn.addActionListener(e -> {
			String newType = (String) typeCombo.getSelectedItem();

			// Check if type actually changed
			if (newType.equals(currentType)) {
				dialog.dispose();
				return;
			}

			// Handle Loan → Partial
			if (currentType.equals(TYPE_LOAN) && newType.equals(TYPE_PARTIAL)) {
				String amountText = partialAmountField.getText().trim();

				// Validation: Check if amount is entered
				if (amountText.isEmpty()) {
					com.gierza_molases.molases_app.ui.components.ToastNotification.showError(dialog,
							"Please enter a partial payment amount.");
					return;
				}

				// Validation: Parse amount
				double partialAmount;
				try {
					partialAmount = Double.parseDouble(amountText);
				} catch (NumberFormatException ex) {
					com.gierza_molases.molases_app.ui.components.ToastNotification.showError(dialog,
							"Please enter a valid number.");
					return;
				}

				// Validation: Amount must be > 0
				if (partialAmount <= 0) {
					com.gierza_molases.molases_app.ui.components.ToastNotification.showError(dialog,
							"Partial payment amount must be greater than 0.");
					return;
				}

				// Validation: Amount must be < remaining balance (strictly less than)
				if (partialAmount >= remainingBalance) {
					com.gierza_molases.molases_app.ui.components.ToastNotification.showError(dialog,
							"Partial payment amount must be less than the remaining balance ("
									+ currencyFormatter.format(remainingBalance).replace("PHP", "₱") + ").");
					return;
				}

				// Disable buttons during processing
				saveBtn.setEnabled(false);
				cancelBtn.setEnabled(false);
				saveBtn.setText("Saving...");

				// Call controller
				AppContext.customerPaymentViewController.updatePaymentTypeWithPartialPayment(newType, partialAmount,
						() -> {
							SwingUtilities.invokeLater(() -> {
								dialog.dispose();
								com.gierza_molases.molases_app.ui.components.ToastNotification.showSuccess(
										SwingUtilities.getWindowAncestor(mainPanel),
										"Payment type updated and partial payment recorded successfully!");
								refreshPage();
							});
						}, (errorMsg) -> {
							SwingUtilities.invokeLater(() -> {
								saveBtn.setEnabled(true);
								cancelBtn.setEnabled(true);
								saveBtn.setText("Save");
								com.gierza_molases.molases_app.ui.components.ToastNotification.showError(dialog,
										"Failed to update payment type: " + errorMsg);
							});
						});
			}
			// Handle Partial → Loan
			else if (currentType.equals(TYPE_PARTIAL) && newType.equals(TYPE_LOAN)) {
				java.util.Date selectedDate = dateChooser.getDate();

				// Validation: Check if date is selected
				if (selectedDate == null) {
					com.gierza_molases.molases_app.ui.components.ToastNotification.showError(dialog,
							"Please select a Promise to Pay date.");
					return;
				}

				// Validation: Check if date is not in the past
				java.util.Date today = new java.util.Date();
				java.util.Calendar cal = java.util.Calendar.getInstance();
				cal.setTime(today);
				cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
				cal.set(java.util.Calendar.MINUTE, 0);
				cal.set(java.util.Calendar.SECOND, 0);
				cal.set(java.util.Calendar.MILLISECOND, 0);
				today = cal.getTime();

				if (selectedDate.before(today)) {
					com.gierza_molases.molases_app.ui.components.ToastNotification.showError(dialog,
							"Promise to Pay date cannot be in the past. Please select today or a future date.");
					return;
				}

				// Disable buttons during processing
				saveBtn.setEnabled(false);
				cancelBtn.setEnabled(false);
				saveBtn.setText("Saving...");

				// Call controller
				AppContext.customerPaymentViewController.updatePaymentTypeWithPromiseToPay(newType, selectedDate,
						() -> {
							SwingUtilities.invokeLater(() -> {
								dialog.dispose();
								com.gierza_molases.molases_app.ui.components.ToastNotification.showSuccess(
										SwingUtilities.getWindowAncestor(mainPanel),
										"Payment type updated and Promise to Pay date set successfully!");
								refreshPage();
							});
						}, (errorMsg) -> {
							SwingUtilities.invokeLater(() -> {
								saveBtn.setEnabled(true);
								cancelBtn.setEnabled(true);
								saveBtn.setText("Save");
								com.gierza_molases.molases_app.ui.components.ToastNotification.showError(dialog,
										"Failed to update payment type: " + errorMsg);
							});
						});
			}
		});
		dialog.add(saveBtn, gbc);

		// Initial pack - compact size with just basic elements
		dialog.pack();
		dialog.setMinimumSize(new Dimension(400, dialog.getHeight())); // Set min size based on packed height
		dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(mainPanel));
		dialog.setVisible(true);
	}

	/**
	 * Show edit promise to pay dialog
	 */
	private static void showEditPromiseToPayDialog() {
		CustomerPayments currentPayment = AppContext.customerPaymentViewController.getState().getCustomerPayment();
		if (currentPayment == null) {
			return;
		}

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
		gbc.weighty = 0; // Don't stretch buttons
		gbc.weightx = 0.0; // Don't expand horizontally <-- ADD THIS
		gbc.fill = GridBagConstraints.HORIZONTAL;

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

			// Disable buttons during processing
			saveBtn.setEnabled(false);
			cancelBtn.setEnabled(false);
			saveBtn.setText("Saving...");

			// Call controller to update promise to pay
			AppContext.customerPaymentViewController.updatePromiseToPay(selectedDate, () -> {
				// Success callback
				SwingUtilities.invokeLater(() -> {
					dialog.dispose();
					com.gierza_molases.molases_app.ui.components.ToastNotification.showSuccess(
							SwingUtilities.getWindowAncestor(mainPanel), "Promise to Pay date updated successfully!");

					// Refresh the page to show updated data
					refreshPage();
				});
			}, (errorMsg) -> {
				// Error callback
				SwingUtilities.invokeLater(() -> {
					// Re-enable buttons
					saveBtn.setEnabled(true);
					cancelBtn.setEnabled(true);
					saveBtn.setText("Save");

					com.gierza_molases.molases_app.ui.components.ToastNotification.showError(
							SwingUtilities.getWindowAncestor(mainPanel),
							"Failed to update Promise to Pay: " + errorMsg);
				});
			});
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