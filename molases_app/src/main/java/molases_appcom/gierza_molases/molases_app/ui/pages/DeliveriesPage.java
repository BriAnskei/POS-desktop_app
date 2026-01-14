package molases_appcom.gierza_molases.molases_app.ui.pages;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.toedter.calendar.JDateChooser;

public class DeliveriesPage {

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
	private static final Color STATUS_SCHEDULED = new Color(255, 165, 0); // Orange
	private static final Color STATUS_COMPLETE = new Color(46, 125, 50); // Green

	// UI component references
	private static JTextField searchField;
	private static JDateChooser startDateChooser;
	private static JDateChooser endDateChooser;
	private static JTable table;
	private static JPanel mainPanelRef;
	private static JLabel loadingLabel;
	private static JPanel loadingOverlay;
	private static LoadingSpinner spinner;

	// Mock data
	private static List<Delivery> mockDeliveries = new ArrayList<>();
	private static List<Delivery> filteredDeliveries = new ArrayList<>();

	/**
	 * Initialize mock data
	 */
	private static void initMockData() {
		mockDeliveries.clear();

		// Create mock expenses
		Map<String, Double> expenses1 = new HashMap<>();
		expenses1.put("Gas", 500.0);
		expenses1.put("Toll", 150.0);

		Map<String, Double> expenses2 = new HashMap<>();
		expenses2.put("Gas", 600.0);
		expenses2.put("Maintenance", 200.0);

		Map<String, Double> expenses3 = new HashMap<>();
		expenses3.put("Gas", 450.0);

		Map<String, Double> expenses4 = new HashMap<>();
		expenses4.put("Gas", 550.0);
		expenses4.put("Toll", 100.0);
		expenses4.put("Parking", 50.0);

		Map<String, Double> expenses5 = new HashMap<>();
		expenses5.put("Gas", 700.0);
		expenses5.put("Toll", 200.0);

		// Create mock deliveries
		mockDeliveries.add(new Delivery(1, LocalDateTime.of(2026, 1, 15, 9, 0), "Morning Route A", expenses1,
				"scheduled", 2500.0, 5000.0, null, LocalDateTime.of(2026, 1, 10, 14, 30), 5, 3));

		mockDeliveries.add(new Delivery(2, LocalDateTime.of(2026, 1, 14, 14, 0), "Afternoon Route B", expenses2,
				"complete", 3200.0, 6000.0, null, LocalDateTime.of(2026, 1, 9, 10, 15), 8, 5));

		mockDeliveries.add(new Delivery(3, LocalDateTime.of(2026, 1, 16, 8, 30), "Downtown Delivery", expenses3,
				"scheduled", 1800.0, 4000.0, null, LocalDateTime.of(2026, 1, 11, 16, 45), 3, 2));

		mockDeliveries.add(new Delivery(4, LocalDateTime.of(2026, 1, 13, 10, 0), "Express Route", expenses4, "complete",
				4100.0, 7500.0, null, LocalDateTime.of(2026, 1, 8, 11, 20), 12, 7));

		mockDeliveries.add(new Delivery(5, LocalDateTime.of(2026, 1, 17, 13, 0), "Weekend Special", expenses5,
				"scheduled", 2900.0, 5500.0, null, LocalDateTime.of(2026, 1, 12, 9, 30), 6, 4));

		mockDeliveries.add(new Delivery(6, LocalDateTime.of(2026, 1, 12, 15, 30), "Evening Route C", expenses1,
				"complete", 2200.0, 4500.0, null, LocalDateTime.of(2026, 1, 7, 13, 10), 4, 3));

		mockDeliveries.add(new Delivery(7, LocalDateTime.of(2026, 1, 18, 7, 0), "Early Bird Route", expenses2,
				"scheduled", 3500.0, 6500.0, null, LocalDateTime.of(2026, 1, 13, 15, 0), 9, 6));

		mockDeliveries.add(new Delivery(8, LocalDateTime.of(2026, 1, 11, 11, 0), "Midday Express", expenses3,
				"complete", 2700.0, 5200.0, null, LocalDateTime.of(2026, 1, 6, 12, 45), 7, 4));

		// Initially show all deliveries
		filteredDeliveries = new ArrayList<>(mockDeliveries);
	}

	/**
	 * Create the Deliveries Page panel
	 */
	public static JPanel createPanel(Runnable onAddNew, Runnable onRefresh) {
		initMockData();

		JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		mainPanelRef = mainPanel;

		// Top Section - pass the callback
		JPanel topSection = createTopSection(onAddNew);
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

		// Show brief loading animation on initial load
		showLoading();
		Timer timer = new Timer(800, e -> {
			hideLoading();
		});
		timer.setRepeats(false);
		timer.start();

		return mainPanel;
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
		loadingLabel = new JLabel("Loading deliveries...");
		loadingLabel.setFont(new Font("Arial", Font.BOLD, 16));
		loadingLabel.setForeground(TEXT_DARK);
		loadingLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		centerPanel.add(loadingLabel);

		loadingOverlay.add(centerPanel);

		return loadingOverlay;
	}

	/**
	 * Show loading indicator
	 */
	private static void showLoading() {
		if (loadingOverlay != null && table != null) {
			if (spinner != null)
				spinner.start();
			loadingOverlay.setVisible(true);
			table.setEnabled(false);

			if (searchField != null)
				searchField.setEnabled(false);
			if (startDateChooser != null)
				startDateChooser.setEnabled(false);
			if (endDateChooser != null)
				endDateChooser.setEnabled(false);
		}
	}

	/**
	 * Hide loading indicator
	 */
	private static void hideLoading() {
		if (loadingOverlay != null && table != null) {
			if (spinner != null)
				spinner.stop();
			loadingOverlay.setVisible(false);
			table.setEnabled(true);

			if (searchField != null)
				searchField.setEnabled(true);
			if (startDateChooser != null)
				startDateChooser.setEnabled(true);
			if (endDateChooser != null)
				endDateChooser.setEnabled(true);
		}
	}

	/**
	 * Create top section
	 */
	private static JPanel createTopSection(Runnable onAddNew) {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setBackground(CONTENT_BG);
		topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

		// Title row
		JPanel titleRow = new JPanel(new BorderLayout());
		titleRow.setBackground(CONTENT_BG);
		titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		JLabel titleLabel = new JLabel("Delivery Management");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
		titleLabel.setForeground(TEXT_DARK);
		titleRow.add(titleLabel, BorderLayout.WEST);

		JButton addButton = createStyledButton("+ Add New", ACCENT_GOLD);
		addButton.addActionListener(e -> {
			if (onAddNew != null) {
				onAddNew.run(); // Call the navigation callback
			}
		});
		titleRow.add(addButton, BorderLayout.EAST);

		topPanel.add(titleRow);
		topPanel.add(Box.createVerticalStrut(15));

		// Filters row
		JPanel filtersRow = new JPanel();
		filtersRow.setLayout(new BoxLayout(filtersRow, BoxLayout.X_AXIS));
		filtersRow.setBackground(CONTENT_BG);
		filtersRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		// Search by name
		JLabel searchLabel = new JLabel("Search Name:");
		searchLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		searchLabel.setForeground(TEXT_DARK);
		filtersRow.add(searchLabel);

		filtersRow.add(Box.createHorizontalStrut(10));

		searchField = new JTextField(15);
		searchField.setFont(new Font("Arial", Font.PLAIN, 14));
		searchField.setPreferredSize(new Dimension(200, 38));
		searchField.setMaximumSize(new Dimension(200, 38));
		searchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		filtersRow.add(searchField);

		filtersRow.add(Box.createHorizontalStrut(20));

		// Date range filter
		JLabel dateLabel = new JLabel("Delivery Date:");
		dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		dateLabel.setForeground(TEXT_DARK);
		filtersRow.add(dateLabel);

		filtersRow.add(Box.createHorizontalStrut(10));

		// Start date chooser with calendar popup
		startDateChooser = new JDateChooser();
		startDateChooser.setFont(new Font("Arial", Font.PLAIN, 14));
		startDateChooser.setPreferredSize(new Dimension(160, 38));
		startDateChooser.setMaximumSize(new Dimension(160, 38));
		startDateChooser.setDateFormatString("yyyy-MM-dd");
		startDateChooser.setBorder(BorderFactory.createLineBorder(new Color(200, 190, 180), 1));
		filtersRow.add(startDateChooser);

		filtersRow.add(Box.createHorizontalStrut(5));

		JLabel toLabel = new JLabel("to");
		toLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		toLabel.setForeground(TEXT_DARK);
		filtersRow.add(toLabel);

		filtersRow.add(Box.createHorizontalStrut(5));

		// End date chooser with calendar popup
		endDateChooser = new JDateChooser();
		endDateChooser.setFont(new Font("Arial", Font.PLAIN, 14));
		endDateChooser.setPreferredSize(new Dimension(160, 38));
		endDateChooser.setMaximumSize(new Dimension(160, 38));
		endDateChooser.setDateFormatString("yyyy-MM-dd");
		endDateChooser.setBorder(BorderFactory.createLineBorder(new Color(200, 190, 180), 1));
		filtersRow.add(endDateChooser);

		filtersRow.add(Box.createHorizontalStrut(10));

		// Filter button
		JButton filterButton = createStyledButton("Filter", SIDEBAR_ACTIVE);
		filterButton.addActionListener(e -> performFilter());
		filtersRow.add(filterButton);

		// Clear button
		filtersRow.add(Box.createHorizontalStrut(10));
		JButton clearButton = createStyledButton("Clear Filters", new Color(120, 120, 120));
		clearButton.addActionListener(e -> clearFilters());
		filtersRow.add(clearButton);

		filtersRow.add(Box.createHorizontalGlue());

		topPanel.add(filtersRow);

		return topPanel;
	}

	/**
	 * Perform filter
	 */
	private static void performFilter() {
		String searchText = searchField.getText().trim().toLowerCase();

		filteredDeliveries.clear();

		// Get dates from date choosers
		java.util.Date startDateUtil = startDateChooser.getDate();
		java.util.Date endDateUtil = endDateChooser.getDate();

		// Convert to LocalDateTime
		LocalDateTime startDate = null;
		LocalDateTime endDate = null;

		if (startDateUtil != null) {
			startDate = LocalDateTime.ofInstant(startDateUtil.toInstant(), java.time.ZoneId.systemDefault()).withHour(0)
					.withMinute(0).withSecond(0);
		}

		if (endDateUtil != null) {
			endDate = LocalDateTime.ofInstant(endDateUtil.toInstant(), java.time.ZoneId.systemDefault()).withHour(23)
					.withMinute(59).withSecond(59);
		}

		// Filter deliveries
		for (Delivery delivery : mockDeliveries) {
			boolean matchesSearch = searchText.isEmpty() || delivery.getName().toLowerCase().contains(searchText);

			boolean matchesDateRange = true;
			if (startDate != null && delivery.getScheduleDate().isBefore(startDate)) {
				matchesDateRange = false;
			}
			if (endDate != null && delivery.getScheduleDate().isAfter(endDate)) {
				matchesDateRange = false;
			}

			if (matchesSearch && matchesDateRange) {
				filteredDeliveries.add(delivery);
			}
		}

		refreshTable();
		ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(mainPanelRef),
				"Showing " + filteredDeliveries.size() + " deliveries");
	}

	/**
	 * Clear all filters
	 */
	private static void clearFilters() {
		searchField.setText("");

		// Clear date choosers
		startDateChooser.setDate(null);
		endDateChooser.setDate(null);

		filteredDeliveries = new ArrayList<>(mockDeliveries);
		refreshTable();
		ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(mainPanelRef), "Filters cleared");
	}

	/**
	 * Create table section
	 */
	private static JPanel createTableSection() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(CONTENT_BG);

		String[] columns = { "Delivery Date", "Name", "Customers/Branches", "Status", "Actions" };

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
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(250);
		table.getColumnModel().getColumn(2).setPreferredWidth(200);
		table.getColumnModel().getColumn(3).setPreferredWidth(150);
		table.getColumnModel().getColumn(4).setPreferredWidth(120);

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

		// Custom cell renderer for status column with colors
		table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);

					// Color based on status
					String status = value.toString().toLowerCase();
					if (status.equals("scheduled")) {
						setForeground(STATUS_SCHEDULED);
						setFont(new Font("Arial", Font.BOLD, 14));
					} else if (status.equals("complete")) {
						setForeground(STATUS_COMPLETE);
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

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
					setForeground(TEXT_DARK);
				}

				setHorizontalAlignment(column == 4 ? SwingConstants.CENTER : SwingConstants.LEFT);
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

				if (col == 4 && row >= 0 && row < filteredDeliveries.size()) {
					Delivery delivery = filteredDeliveries.get(row);
					showActionMenu(e.getComponent(), e.getX(), e.getY(), delivery, row);
				}
			}
		});

		table.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int col = table.columnAtPoint(e.getPoint());
				table.setCursor(new Cursor(col == 4 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
		scrollPane.getViewport().setBackground(TABLE_ROW_EVEN);

		tablePanel.add(scrollPane, BorderLayout.CENTER);

		return tablePanel;
	}

	/**
	 * Update table with current filtered data
	 */
	private static void updateTableData(DefaultTableModel model) {
		model.setRowCount(0);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

		for (Delivery d : filteredDeliveries) {
			String deliveryDate = d.getScheduleDate() != null ? d.getScheduleDate().format(formatter) : "";
			String customersInfo = d.getTotalCustomers() + " customers of " + d.getTotalBranches() + " branches";
			String status = d.getStatus();

			// Capitalize first letter of status
			status = status.substring(0, 1).toUpperCase() + status.substring(1);

			model.addRow(new Object[] { deliveryDate, d.getName(), customersInfo, status, "⚙ Actions" });
		}
	}

	/**
	 * Show action menu
	 */
	private static void showActionMenu(Component parent, int x, int y, Delivery delivery, int row) {
		JDialog actionDialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Actions");
		actionDialog.setLayout(new GridBagLayout());
		actionDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 20, 5, 20);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Choose Action for Delivery ID: " + delivery.getId());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		actionDialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(15, 20, 5, 20);

		JButton viewDetailsBtn = createActionButton("👁️ View Details", new Color(70, 130, 180));
		viewDetailsBtn.addActionListener(e -> {
			actionDialog.dispose();
			showDeliveryDetails(delivery);
		});
		actionDialog.add(viewDetailsBtn, gbc);

		gbc.gridy++;
		JButton updateBtn = createActionButton("✏️ Update Delivery", ACCENT_GOLD);
		updateBtn.addActionListener(e -> {
			actionDialog.dispose();
			ToastNotification.showInfo(SwingUtilities.getWindowAncestor(parent),
					"Update dialog for Delivery ID " + delivery.getId() + " - Coming soon!");
		});
		actionDialog.add(updateBtn, gbc);

		gbc.gridy++;
		JButton deleteBtn = createActionButton("🗑️ Delete Delivery", new Color(180, 50, 50));
		deleteBtn.addActionListener(e -> {
			actionDialog.dispose();
			showDeleteConfirmation(delivery);
		});
		actionDialog.add(deleteBtn, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 20, 15, 20);
		JButton cancelBtn = createActionButton("Cancel", new Color(120, 120, 120));
		cancelBtn.addActionListener(e -> actionDialog.dispose());
		actionDialog.add(cancelBtn, gbc);

		actionDialog.pack();
		actionDialog.setMinimumSize(new Dimension(350, 250));
		actionDialog.setLocationRelativeTo(null);
		actionDialog.setVisible(true);
	}

	/**
	 * Show delivery details dialog
	 */
	private static void showDeliveryDetails(Delivery delivery) {
		JDialog detailsDialog = new JDialog(SwingUtilities.getWindowAncestor(table), "Delivery Details");
		detailsDialog.setLayout(new GridBagLayout());
		detailsDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 20, 30);
		gbc.anchor = GridBagConstraints.WEST;

		JLabel titleLabel = new JLabel("Delivery Details - ID: " + delivery.getId());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		titleLabel.setForeground(TEXT_DARK);
		detailsDialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 30, 5, 10);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

		String[][] details = { { "Name:", delivery.getName() },
				{ "Schedule Date:", delivery.getScheduleDate().format(formatter) },
				{ "Status:", delivery.getStatus().substring(0, 1).toUpperCase() + delivery.getStatus().substring(1) },
				{ "Customers:", String.valueOf(delivery.getTotalCustomers()) },
				{ "Branches:", String.valueOf(delivery.getTotalBranches()) },
				{ "Overall Profit:", "₱" + String.format("%,.2f", delivery.getOverAllProfit()) },
				{ "Overall Capital:", "₱" + String.format("%,.2f", delivery.getOverAllCapital()) },
				{ "Total Expenses:", "₱" + String.format("%,.2f", delivery.getTotalExpenses()) },
				{ "Created At:", delivery.getCreatedAt().format(formatter) } };

		for (String[] detail : details) {
			JLabel keyLabel = new JLabel(detail[0]);
			keyLabel.setFont(new Font("Arial", Font.BOLD, 14));
			keyLabel.setForeground(TEXT_DARK);
			detailsDialog.add(keyLabel, gbc);

			gbc.gridx = 1;
			gbc.insets = new Insets(10, 10, 5, 30);
			JLabel valueLabel = new JLabel(detail[1]);
			valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
			valueLabel.setForeground(TEXT_DARK);
			detailsDialog.add(valueLabel, gbc);

			gbc.gridx = 0;
			gbc.gridy++;
			gbc.insets = new Insets(10, 30, 5, 10);
		}

		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 20, 30);
		gbc.anchor = GridBagConstraints.CENTER;

		JButton closeBtn = createStyledButton("Close", SIDEBAR_ACTIVE);
		closeBtn.setPreferredSize(new Dimension(150, 40));
		closeBtn.addActionListener(e -> detailsDialog.dispose());
		detailsDialog.add(closeBtn, gbc);

		detailsDialog.pack();
		detailsDialog.setMinimumSize(new Dimension(500, 450));
		detailsDialog.setLocationRelativeTo(null);
		detailsDialog.setVisible(true);
	}

	/**
	 * Show delete confirmation
	 */
	private static void showDeleteConfirmation(Delivery delivery) {
		JDialog confirmDialog = new JDialog(SwingUtilities.getWindowAncestor(table), "Confirm Delete");
		confirmDialog.setLayout(new GridBagLayout());
		confirmDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);

		JLabel messageLabel = new JLabel("<html><center>Are you sure you want to delete this delivery?<br><b>ID: "
				+ delivery.getId() + " - " + delivery.getName() + "</b></center></html>");
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
			// Mock delete - just show toast
			ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(table),
					"Delivery ID " + delivery.getId() + " deleted (mock operation)");
		});
		confirmDialog.add(deleteBtn, gbc);

		confirmDialog.pack();
		confirmDialog.setMinimumSize(new Dimension(400, 200));
		confirmDialog.setLocationRelativeTo(null);
		confirmDialog.setVisible(true);
	}

	/**
	 * Refresh table
	 */
	private static void refreshTable() {
		if (table == null || mainPanelRef == null)
			return;

		// Update table data
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		updateTableData(model);

		// Refresh the table display
		table.revalidate();
		table.repaint();
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
}