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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.toedter.calendar.JDateChooser;

public class PaymentsPage {

	// Color Palette
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);
	private static final Color TABLE_HOVER = new Color(245, 239, 231);

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

	// Pagination settings
	private static final int RECORDS_PER_PAGE = 10;

	// Mock data
	private static List<CustomerPayments> allPayments = new ArrayList<>();
	private static List<CustomerPayments> filteredPayments = new ArrayList<>();
	private static int currentPage = 0;

	// UI component references
	private static JTextField customerSearchField;
	private static JDateChooser fromDateChooser;
	private static JDateChooser toDateChooser;
	private static JComboBox<String> paymentTypeFilterCombo;
	private static JComboBox<String> statusFilterCombo;
	private static JTable table;
	private static JLabel pageInfoLabel;
	private static JButton prevBtn;
	private static JButton nextBtn;
	private static JPanel mainPanelRef;

	private static JLabel loadingLabel;
	private static JPanel loadingOverlay;
	private static LoadingSpinner spinner;

	// Currency formatter
	private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

	static {
		currencyFormatter.setMaximumFractionDigits(2);
		currencyFormatter.setMinimumFractionDigits(2);
	}

	/**
	 * Create the Payments Page panel
	 */
	public static JPanel createPanel() {
		// Initialize mock data
		initializeMockData();
		filteredPayments = new ArrayList<>(allPayments);
		currentPage = 0;

		JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		mainPanelRef = mainPanel;

		// Top Section
		JPanel topSection = createTopSection();
		mainPanel.add(topSection, BorderLayout.NORTH);

		// Center Section (table with overlay)
		JPanel tableSection = createTableSection();

		// WRAP TABLE IN A LAYERED PANEL FOR LOADING OVERLAY
		JPanel tableWrapper = new JPanel();
		tableWrapper.setLayout(new OverlayLayout(tableWrapper));
		tableWrapper.setBackground(CONTENT_BG);

		// Add loading overlay FIRST (so it appears on top)
		JPanel overlay = createLoadingOverlay();
		tableWrapper.add(overlay);

		// Add table section
		tableWrapper.add(tableSection);

		mainPanel.add(tableWrapper, BorderLayout.CENTER);

		// Bottom Section
		JPanel bottomSection = createPaginationSection();
		mainPanel.add(bottomSection, BorderLayout.SOUTH);

		return mainPanel;
	}

	/**
	 * Initialize mock payment data
	 */
	private static void initializeMockData() {
		allPayments.clear();

		String[] customerNames = { "Juan Dela Cruz", "Maria Santos", "Pedro Reyes", "Ana Garcia", "Jose Mendoza",
				"Carmen Lopez", "Ricardo Flores", "Sofia Ramos", "Miguel Torres", "Isabella Cruz", "Carlos Valdez",
				"Lucia Martinez", "Fernando Aguilar", "Gabriela Morales", "Antonio Diaz", "Valentina Ruiz",
				"Diego Hernandez", "Camila Castro", "Javier Ortiz", "Natalia Gutierrez", "Roberto Jimenez",
				"Elena Navarro", "Francisco Romero", "Paula Medina", "Andres Silva", "Victoria Perez", "Manuel Vargas",
				"Daniela Rojas", "Luis Fernandez", "Adriana Molina" };

		String[] deliveryNames = { "Express Delivery", "Standard Shipping", "Premium Service", "Quick Dispatch",
				"Fast Track", "Economy Shipping", "Priority Mail", "Overnight Express", "Same Day Delivery",
				"Next Day Service" };

		String[] paymentTypes = { TYPE_PAID_CASH, TYPE_PAID_CHEQUE, TYPE_PARTIAL, TYPE_LOAN };
		String[] statuses = { STATUS_PENDING, STATUS_COMPLETE };

		Calendar cal = Calendar.getInstance();

		for (int i = 1; i <= 30; i++) {
			// Random date within last 60 days for delivery date
			cal.setTime(new Date());
			cal.add(Calendar.DAY_OF_MONTH, -(int) (Math.random() * 60));
			Date deliveryDate = cal.getTime();

			// Random LocalDateTime for createdAt (within last 90 days)
			LocalDateTime createdAt = LocalDateTime.now().minusDays((long) (Math.random() * 90));

			// Random date for promiseToPay (future date for loans)
			cal.setTime(new Date());
			cal.add(Calendar.DAY_OF_MONTH, (int) (Math.random() * 30));
			Date promiseToPay = cal.getTime();

			// Random payment amount between 500 and 50000
			double total = 500 + (Math.random() * 49500);
			total = Math.round(total * 100.0) / 100.0;

			// Random total payment (could be less than total for partial payments)
			double totalPayment = total * (0.5 + Math.random() * 0.5);
			totalPayment = Math.round(totalPayment * 100.0) / 100.0;

			// Random payment type
			String paymentType = paymentTypes[(int) (Math.random() * paymentTypes.length)];

			// Random status
			String status = statuses[(int) (Math.random() * statuses.length)];

			// Random customer ID and delivery ID
			int customerId = i;
			int customerDeliveryId = i; // Now int, not Integer

			// Create CustomerPayments using the Payment Management list constructor
			CustomerPayments payment = new CustomerPayments(i, // id (Integer)
					customerId, // customerId (int)
					customerDeliveryId, // customerDeliveryId (int)
					paymentType, // paymentType
					status, // status
					total, // total
					totalPayment, // totalPayment
					promiseToPay, // promiseToPay (Date)
					createdAt, // createdAt (LocalDateTime)
					customerNames[i - 1], // customerName (joined)
					deliveryNames[(int) (Math.random() * deliveryNames.length)], // deliveryName (joined)
					deliveryDate // deliveryDate (joined)
			);

			allPayments.add(payment);
		}
	}

	/**
	 * Create loading overlay
	 */
	private static JPanel createLoadingOverlay() {
		loadingOverlay = new JPanel(new GridBagLayout());
		loadingOverlay.setBackground(new Color(250, 247, 242, 220));
		loadingOverlay.setVisible(false);

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setOpaque(false);

		// Create spinner
		spinner = new LoadingSpinner(50, new Color(139, 90, 43));
		spinner.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		centerPanel.add(spinner);

		centerPanel.add(Box.createVerticalStrut(15));

		// Loading text
		loadingLabel = new JLabel("Loading...");
		loadingLabel.setFont(new Font("Arial", Font.BOLD, 16));
		loadingLabel.setForeground(TEXT_DARK);
		loadingLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		centerPanel.add(loadingLabel);

		loadingOverlay.add(centerPanel);

		return loadingOverlay;
	}

	/**
	 * Create top section
	 */
	private static JPanel createTopSection() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setBackground(CONTENT_BG);
		topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

		// Row 1: Title and Payment Type/Status filters
		JPanel titleRow = new JPanel();
		titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.X_AXIS));
		titleRow.setBackground(CONTENT_BG);
		titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		JLabel titleLabel = new JLabel("Payment Management");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
		titleLabel.setForeground(TEXT_DARK);
		titleRow.add(titleLabel);

		titleRow.add(Box.createHorizontalGlue());

		// Payment Type filter
		String[] paymentTypeOptions = { "All", TYPE_PAID_CASH, TYPE_PAID_CHEQUE, TYPE_PARTIAL, TYPE_LOAN };
		paymentTypeFilterCombo = new JComboBox<>(paymentTypeOptions);
		paymentTypeFilterCombo.setFont(new Font("Arial", Font.PLAIN, 14));
		paymentTypeFilterCombo.setPreferredSize(new Dimension(140, 38));
		paymentTypeFilterCombo.setMaximumSize(new Dimension(140, 38));
		paymentTypeFilterCombo.setBackground(Color.WHITE);

		JLabel paymentTypeLabel = new JLabel("Payment Type:");
		paymentTypeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		paymentTypeLabel.setForeground(TEXT_DARK);

		titleRow.add(paymentTypeLabel);
		titleRow.add(Box.createHorizontalStrut(5));
		titleRow.add(paymentTypeFilterCombo);
		titleRow.add(Box.createHorizontalStrut(10));

		// Status filter
		String[] statusOptions = { "All", STATUS_PENDING, STATUS_COMPLETE };
		statusFilterCombo = new JComboBox<>(statusOptions);
		statusFilterCombo.setFont(new Font("Arial", Font.PLAIN, 14));
		statusFilterCombo.setPreferredSize(new Dimension(140, 38));
		statusFilterCombo.setMaximumSize(new Dimension(140, 38));
		statusFilterCombo.setBackground(Color.WHITE);

		JLabel statusLabel = new JLabel("Status:");
		statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		statusLabel.setForeground(TEXT_DARK);

		titleRow.add(statusLabel);
		titleRow.add(Box.createHorizontalStrut(5));
		titleRow.add(statusFilterCombo);

		topPanel.add(titleRow);
		topPanel.add(Box.createVerticalStrut(15));

		// Row 2: Search, dates, and action buttons
		JPanel filtersRow = new JPanel();
		filtersRow.setLayout(new BoxLayout(filtersRow, BoxLayout.X_AXIS));
		filtersRow.setBackground(CONTENT_BG);
		filtersRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		// Customer Name search
		customerSearchField = new JTextField(15);
		customerSearchField.setFont(new Font("Arial", Font.PLAIN, 14));
		customerSearchField.setPreferredSize(new Dimension(180, 38));
		customerSearchField.setMaximumSize(new Dimension(180, 38));
		customerSearchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		customerSearchField.setToolTipText("Search by customer or delivery name");

		JLabel customerLabel = new JLabel("Search:");
		customerLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		customerLabel.setForeground(TEXT_DARK);

		filtersRow.add(customerLabel);
		filtersRow.add(Box.createHorizontalStrut(5));
		filtersRow.add(customerSearchField);
		filtersRow.add(Box.createHorizontalStrut(15));

		// From Date filter
		fromDateChooser = new JDateChooser();
		fromDateChooser.setDateFormatString("MMM dd, yyyy");
		fromDateChooser.setPreferredSize(new Dimension(140, 38));
		fromDateChooser.setMaximumSize(new Dimension(140, 38));
		fromDateChooser.setFont(new Font("Arial", Font.PLAIN, 14));

		JLabel fromDateLabel = new JLabel("From Date:");
		fromDateLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		fromDateLabel.setForeground(TEXT_DARK);

		filtersRow.add(fromDateLabel);
		filtersRow.add(Box.createHorizontalStrut(5));
		filtersRow.add(fromDateChooser);
		filtersRow.add(Box.createHorizontalStrut(15));

		// To Date filter
		toDateChooser = new JDateChooser();
		toDateChooser.setDateFormatString("MMM dd, yyyy");
		toDateChooser.setPreferredSize(new Dimension(140, 38));
		toDateChooser.setMaximumSize(new Dimension(140, 38));
		toDateChooser.setFont(new Font("Arial", Font.PLAIN, 14));

		JLabel toDateLabel = new JLabel("To Date:");
		toDateLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		toDateLabel.setForeground(TEXT_DARK);

		filtersRow.add(toDateLabel);
		filtersRow.add(Box.createHorizontalStrut(5));
		filtersRow.add(toDateChooser);

		filtersRow.add(Box.createHorizontalGlue());

		// Search button
		JButton searchButton = createStyledButton("Search", SIDEBAR_ACTIVE);
		searchButton.addActionListener(e -> performSearch());
		filtersRow.add(searchButton);

		// Clear button
		filtersRow.add(Box.createHorizontalStrut(10));
		JButton clearButton = createStyledButton("Clear Filters", new Color(120, 120, 120));
		clearButton.addActionListener(e -> clearFilters());
		filtersRow.add(clearButton);

		topPanel.add(filtersRow);

		return topPanel;
	}

	/**
	 * Perform search/filter
	 */
	private static void performSearch() {
		String customerSearch = customerSearchField.getText().trim().toLowerCase();
		Date fromDate = fromDateChooser.getDate();
		Date toDate = toDateChooser.getDate();
		String selectedPaymentType = (String) paymentTypeFilterCombo.getSelectedItem();
		String selectedStatus = (String) statusFilterCombo.getSelectedItem();

		// Filter with AND logic
		filteredPayments = allPayments.stream().filter(payment -> {
			// Customer match
			boolean customerMatch = customerSearch.isEmpty()
					|| payment.getCustomerName().toLowerCase().contains(customerSearch)
					|| payment.getDeliveryName().toLowerCase().contains(customerSearch);

			// Date range match
			boolean dateMatch = true;
			if (fromDate != null || toDate != null) {
				Date paymentDate = payment.getDeliveryDate();

				if (paymentDate != null) {
					if (fromDate != null && toDate != null) {
						// Both dates specified - check if payment date is within range
						dateMatch = !paymentDate.before(fromDate) && !paymentDate.after(toDate);
					} else if (fromDate != null) {
						// Only from date - check if payment date is on or after from date
						dateMatch = !paymentDate.before(fromDate);
					} else {
						// Only to date - check if payment date is on or before to date
						dateMatch = !paymentDate.after(toDate);
					}
				} else {
					dateMatch = false;
				}
			}

			// Payment Type match
			boolean paymentTypeMatch = "All".equals(selectedPaymentType)
					|| payment.getPaymentType().equals(selectedPaymentType);

			// Status match
			boolean statusMatch = "All".equals(selectedStatus) || payment.getStatus().equals(selectedStatus);

			return customerMatch && dateMatch && paymentTypeMatch && statusMatch;
		}).collect(Collectors.toList());

		currentPage = 0;
		refreshTable();
	}

	/**
	 * Clear all filters
	 */
	private static void clearFilters() {
		customerSearchField.setText("");
		fromDateChooser.setDate(null);
		toDateChooser.setDate(null);
		paymentTypeFilterCombo.setSelectedIndex(0);
		statusFilterCombo.setSelectedIndex(0);

		filteredPayments = new ArrayList<>(allPayments);
		currentPage = 0;
		refreshTable();
	}

	/**
	 * Create table section
	 */
	private static JPanel createTableSection() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(CONTENT_BG);

		String[] columns = { "Customer Name", "Delivery Name", "Delivery Date", "Payment Type", "Status",
				"Total Payments", "Actions" };

		DefaultTableModel model = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		updateTableData(model);

		table = new JTable(model);
		table.setFont(new Font("Arial", Font.PLAIN, 14));
		table.setRowHeight(50);
		table.setShowGrid(true);
		table.setGridColor(new Color(220, 210, 200));
		table.setBackground(TABLE_ROW_EVEN);
		table.setSelectionBackground(TABLE_HOVER);
		table.setSelectionForeground(TEXT_DARK);

		// Set column widths
		table.getColumnModel().getColumn(0).setPreferredWidth(180);
		table.getColumnModel().getColumn(1).setPreferredWidth(180);
		table.getColumnModel().getColumn(2).setPreferredWidth(130);
		table.getColumnModel().getColumn(3).setPreferredWidth(120);
		table.getColumnModel().getColumn(4).setPreferredWidth(100);
		table.getColumnModel().getColumn(5).setPreferredWidth(130);
		table.getColumnModel().getColumn(6).setPreferredWidth(100);

		// Custom header
		JTableHeader header = table.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(TABLE_HEADER);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setBackground(TABLE_HEADER);
		headerRenderer.setForeground(TEXT_LIGHT);
		headerRenderer.setFont(new Font("Arial", Font.BOLD, 14));
		headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}

		// Custom renderer for payment type column (column 3) with colored text
		table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);

					String paymentType = value.toString();
					if (paymentType.equals(TYPE_PAID_CASH)) {
						setForeground(COLOR_PAID_CASH);
						setFont(new Font("Arial", Font.BOLD, 14));
					} else if (paymentType.equals(TYPE_PAID_CHEQUE)) {
						setForeground(COLOR_PAID_CHEQUE);
						setFont(new Font("Arial", Font.BOLD, 14));
					} else if (paymentType.equals(TYPE_PARTIAL)) {
						setForeground(COLOR_PARTIAL);
						setFont(new Font("Arial", Font.BOLD, 14));
					} else if (paymentType.equals(TYPE_LOAN)) {
						setForeground(COLOR_LOAN);
						setFont(new Font("Arial", Font.BOLD, 14));
					}
				}

				setHorizontalAlignment(SwingConstants.LEFT);
				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		});

		// Custom renderer for status column (column 4) with colored text
		table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);

					String status = value.toString();
					if (status.equals(STATUS_PENDING)) {
						setForeground(COLOR_PENDING);
						setFont(new Font("Arial", Font.BOLD, 14));
					} else if (status.equals(STATUS_COMPLETE)) {
						setForeground(COLOR_COMPLETE);
						setFont(new Font("Arial", Font.BOLD, 14));
					}
				}

				setHorizontalAlignment(SwingConstants.LEFT);
				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		});

		// Alternating row colors for other columns
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Don't override payment type and status column rendering
				if (column == 3) {
					return table.getColumnModel().getColumn(3).getCellRenderer().getTableCellRendererComponent(table,
							value, isSelected, hasFocus, row, column);
				}
				if (column == 4) {
					return table.getColumnModel().getColumn(4).getCellRenderer().getTableCellRendererComponent(table,
							value, isSelected, hasFocus, row, column);
				}

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
					setForeground(TEXT_DARK);
				}

				if (column == 5) {
					setHorizontalAlignment(SwingConstants.RIGHT);
				} else if (column == 6) {
					setHorizontalAlignment(SwingConstants.CENTER);
				} else {
					setHorizontalAlignment(SwingConstants.LEFT);
				}

				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		});

		// Mouse listener for actions
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());

				if (col == 6 && row >= 0) {
					int dataIndex = currentPage * RECORDS_PER_PAGE + row;
					if (dataIndex < filteredPayments.size()) {
						CustomerPayments payment = filteredPayments.get(dataIndex);
						showActionMenu(e.getComponent(), e.getX(), e.getY(), payment, row);
					}
				}
			}
		});

		table.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int col = table.columnAtPoint(e.getPoint());
				table.setCursor(new Cursor(col == 6 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
		scrollPane.getViewport().setBackground(TABLE_ROW_EVEN);

		tablePanel.add(scrollPane, BorderLayout.CENTER);

		return tablePanel;
	}

	/**
	 * Update table with current page data
	 */
	private static void updateTableData(DefaultTableModel model) {
		model.setRowCount(0);

		int startIndex = currentPage * RECORDS_PER_PAGE;
		int endIndex = Math.min(startIndex + RECORDS_PER_PAGE, filteredPayments.size());

		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");

		for (int i = startIndex; i < endIndex; i++) {
			CustomerPayments p = filteredPayments.get(i);

			String formattedAmount = currencyFormatter.format(p.getTotalPayment()).replace("PHP", "₱");
			String formattedDate = p.getDeliveryDate() != null ? dateFormat.format(p.getDeliveryDate()) : "N/A";

			model.addRow(new Object[] { p.getCustomerName(), p.getDeliveryName(), formattedDate, p.getPaymentType(),
					p.getStatus(), formattedAmount, "⚙ Actions" });
		}
	}

	/**
	 * Show action menu
	 */
	private static void showActionMenu(Component parent, int x, int y, CustomerPayments payment, int row) {
		JDialog actionDialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Actions");
		actionDialog.setLayout(new GridBagLayout());
		actionDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 20, 5, 20);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Choose Action for Payment ID: " + payment.getId());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		actionDialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(15, 20, 5, 20);

		JButton viewPaymentBtn = createActionButton("💰 View Payment", new Color(70, 130, 180));
		viewPaymentBtn.addActionListener(e -> {
			actionDialog.dispose();
			ToastNotification.showInfo(SwingUtilities.getWindowAncestor(parent),
					"View Payment functionality - Coming soon!");
		});
		actionDialog.add(viewPaymentBtn, gbc);

		gbc.gridy++;
		JButton viewDetailsBtn = createActionButton("👁️ View Details", ACCENT_GOLD);
		viewDetailsBtn.addActionListener(e -> {
			actionDialog.dispose();
			ToastNotification.showInfo(SwingUtilities.getWindowAncestor(parent),
					"View Details functionality - Coming soon!");
		});
		actionDialog.add(viewDetailsBtn, gbc);

		gbc.gridy++;
		JButton deleteBtn = createActionButton("🗑️ Delete", new Color(180, 50, 50));
		deleteBtn.addActionListener(e -> {
			actionDialog.dispose();
			showDeleteConfirmation(payment);
		});
		actionDialog.add(deleteBtn, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 20, 15, 20);
		JButton cancelBtn = createActionButton("Cancel", new Color(120, 120, 120));
		cancelBtn.addActionListener(e -> actionDialog.dispose());
		actionDialog.add(cancelBtn, gbc);

		actionDialog.pack();
		actionDialog.setMinimumSize(new Dimension(350, 280));
		actionDialog.setLocationRelativeTo(null);
		actionDialog.setVisible(true);
	}

	/**
	 * Show delete confirmation (UI only for now)
	 */
	private static void showDeleteConfirmation(CustomerPayments payment) {
		JDialog confirmDialog = new JDialog(SwingUtilities.getWindowAncestor(table), "Confirm Delete");
		confirmDialog.setLayout(new GridBagLayout());
		confirmDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);

		JLabel messageLabel = new JLabel("<html><center>Are you sure you want to delete this payment?<br><b>ID: "
				+ payment.getId() + "</b></center></html>");
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

		JButton cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> confirmDialog.dispose());
		confirmDialog.add(cancelBtn, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(20, 10, 20, 30);
		JButton deleteBtn = createStyledButton("Delete", new Color(180, 50, 50));
		deleteBtn.setPreferredSize(new Dimension(120, 40));
		deleteBtn.addActionListener(e -> {
			confirmDialog.dispose();
			ToastNotification.showInfo(SwingUtilities.getWindowAncestor(table), "Delete functionality - Coming soon!");
		});
		confirmDialog.add(deleteBtn, gbc);

		confirmDialog.pack();
		confirmDialog.setMinimumSize(new Dimension(400, 200));
		confirmDialog.setLocationRelativeTo(null);
		confirmDialog.setVisible(true);
	}

	/**
	 * Create pagination section
	 */
	private static JPanel createPaginationSection() {
		JPanel paginationPanel = new JPanel(new BorderLayout());
		paginationPanel.setBackground(CONTENT_BG);
		paginationPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

		pageInfoLabel = new JLabel();
		updatePaginationInfo();
		pageInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		pageInfoLabel.setForeground(TEXT_DARK);
		paginationPanel.add(pageInfoLabel, BorderLayout.WEST);

		JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		controlsPanel.setBackground(CONTENT_BG);

		prevBtn = createPaginationButton("← Previous");
		prevBtn.addActionListener(e -> goToPreviousPage());
		controlsPanel.add(prevBtn);

		nextBtn = createPaginationButton("Next →");
		nextBtn.addActionListener(e -> goToNextPage());
		controlsPanel.add(nextBtn);

		paginationPanel.add(controlsPanel, BorderLayout.EAST);

		updatePaginationControls();

		return paginationPanel;
	}

	/**
	 * Go to previous page
	 */
	private static void goToPreviousPage() {
		if (currentPage > 0) {
			currentPage--;
			refreshTable();
		}
	}

	/**
	 * Go to next page
	 */
	private static void goToNextPage() {
		int maxPage = (int) Math.ceil((double) filteredPayments.size() / RECORDS_PER_PAGE) - 1;
		if (currentPage < maxPage) {
			currentPage++;
			refreshTable();
		}
	}

	/**
	 * Refresh table and pagination
	 */
	private static void refreshTable() {
		if (table == null || mainPanelRef == null)
			return;

		// Update table data
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		updateTableData(model);

		// Update pagination info
		updatePaginationInfo();

		// Update pagination controls
		updatePaginationControls();

		// Refresh the table display
		table.revalidate();
		table.repaint();
	}

	/**
	 * Update pagination info label
	 */
	private static void updatePaginationInfo() {
		if (pageInfoLabel == null)
			return;

		int startIndex = currentPage * RECORDS_PER_PAGE + 1;
		int endIndex = Math.min((currentPage + 1) * RECORDS_PER_PAGE, filteredPayments.size());
		int total = filteredPayments.size();

		String infoText = "Showing " + startIndex + "-" + endIndex + " of " + total + " payments";

		pageInfoLabel.setText(infoText);
	}

	/**
	 * Update pagination controls
	 */
	private static void updatePaginationControls() {
		if (prevBtn != null) {
			prevBtn.setEnabled(currentPage > 0);
		}

		if (nextBtn != null) {
			int maxPage = (int) Math.ceil((double) filteredPayments.size() / RECORDS_PER_PAGE) - 1;
			nextBtn.setEnabled(currentPage < maxPage);
		}
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
		button.setPreferredSize(new Dimension(120, 38));

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

	/**
	 * Create action button
	 */
	private static JButton createActionButton(String text, Color bgColor) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 14));
		button.setBackground(bgColor);
		button.setForeground(TEXT_LIGHT);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setPreferredSize(new Dimension(280, 45));
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

	/**
	 * Create pagination button
	 */
	private static JButton createPaginationButton(String text) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.PLAIN, 14));
		button.setBackground(Color.WHITE);
		button.setForeground(TEXT_DARK);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 190, 180), 1),
				new EmptyBorder(8, 15, 8, 15)));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (button.isEnabled()) {
					button.setBackground(TABLE_HOVER);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setBackground(Color.WHITE);
			}
		});

		return button;
	}
}