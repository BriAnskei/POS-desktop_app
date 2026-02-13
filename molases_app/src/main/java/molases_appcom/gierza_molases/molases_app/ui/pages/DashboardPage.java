package molases_appcom.gierza_molases.molases_app.ui.pages;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.UiController.DashboardController;
import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.context.DashboardState;
import com.gierza_molases.molases_app.context.DashboardState.UpcomingDelivery;
import com.gierza_molases.molases_app.context.DashboardState.UpcomingLoanPayment;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.toedter.calendar.JDateChooser;

public class DashboardPage {

	// ── Colours ───────────────────────────────────────────────────────────────
	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color TEXT_MUTED = new Color(120, 90, 70);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = Color.WHITE;
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);
	private static final Color TABLE_HOVER = new Color(245, 239, 231);
	private static final Color BORDER_COLOR = new Color(220, 210, 200);

	// Metric card accents
	private static final Color CARD_GOLD = new Color(184, 134, 11);
	private static final Color CARD_GREEN = new Color(46, 125, 50);
	private static final Color CARD_RED = new Color(198, 40, 40);
	private static final Color CARD_BLUE = new Color(25, 118, 210);
	private static final Color CARD_ORANGE = new Color(230, 119, 0);
	private static final Color CARD_PURPLE = new Color(106, 27, 154);
	private static final Color CARD_TEAL = new Color(0, 131, 143);
	private static final Color CARD_BROWN = new Color(109, 76, 65);

	// Status / payment type colours (match PaymentsPage)
	private static final Color COLOR_PENDING = new Color(255, 152, 0);
	private static final Color COLOR_COMPLETE = new Color(76, 175, 80);
	private static final Color COLOR_OVERDUE = new Color(198, 40, 40);
	private static final Color COLOR_DUE_SOON = new Color(255, 152, 0);
	private static final Color COLOR_SCHEDULED = new Color(25, 118, 210);
	private static final Color COLOR_IN_TRANSIT = new Color(46, 125, 50);
	private static final Color COLOR_PAID_CASH = new Color(46, 125, 50);
	private static final Color COLOR_PAID_CHEQUE = new Color(25, 118, 210);
	private static final Color COLOR_PARTIAL = new Color(245, 124, 0);
	private static final Color COLOR_LOAN = new Color(123, 31, 162);

	// Currency formatter
	private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"));
	static {
		CURRENCY.setMaximumFractionDigits(2);
		CURRENCY.setMinimumFractionDigits(2);
	}

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

	// ── Mutable UI refs ───────────────────────────────────────────────────────
	private static JPanel mainPanelRef;
	private static JPanel metricsGrid;
	private static JPanel countCardsPanel; // Added reference for count cards panel
	private static BarChartPanel barChart;
	private static JTable loanTable;
	private static JTable upcomingDelivTable;
	private static JPanel loadingOverlay;
	private static LoadingSpinner spinner;
	private static JLabel filterRangeLabel;

	// Date filter pickers
	private static JDateChooser fromDateChooser;
	private static JDateChooser toDateChooser;

	// Nav callbacks
	private static Runnable onViewDeliveries;
	private static Runnable onViewPayments;

	private static final DashboardController controller = AppContext.dashboardController;

	// =========================================================================
	// Public factory
	// =========================================================================

	public static JPanel createPanel(Runnable onViewDeliveries, Runnable onViewPayments) {
		DashboardPage.onViewDeliveries = onViewDeliveries;
		DashboardPage.onViewPayments = onViewPayments;

		// Null out stale table refs
		loanTable = upcomingDelivTable = null;
		barChart = null;
		countCardsPanel = null; // Reset count cards panel reference

		JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		mainPanelRef = mainPanel;

		// Title + filter row (fixed at top, never scrolls)
		mainPanel.add(buildTitleFilterRow(), BorderLayout.NORTH);

		// ── Scrollable body ───────────────────────────────────────────────────
		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setBackground(CONTENT_BG);
		body.setBorder(new EmptyBorder(16, 0, 24, 0));

		// Row 1: 3 financial cards (Revenue, Net Profit, Expenses) this month
		metricsGrid = buildMetricsGrid();
		body.add(metricsGrid);
		body.add(Box.createVerticalStrut(20));

		// Row 2: Bar chart (left) + count cards stack (right)
		body.add(buildChartAndCountsRow());
		body.add(Box.createVerticalStrut(20));

		// Row 3: Upcoming Loan Payments (left) + Upcoming Deliveries (right)
		body.add(buildUpcomingSection());

		JScrollPane scrollPane = new JScrollPane(body);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setBackground(CONTENT_BG);
		scrollPane.getViewport().setBackground(CONTENT_BG);

		// Loading overlay wrapper
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new OverlayLayout(wrapper));
		wrapper.setBackground(CONTENT_BG);
		wrapper.add(buildLoadingOverlay());
		wrapper.add(scrollPane);

		mainPanel.add(wrapper, BorderLayout.CENTER);

		// Initial load
		showLoading();
		controller.loadDashboard(() -> {
			hideLoading();
			refreshUI();
		}, err -> {
			hideLoading();
			showError(err);
		});

		return mainPanel;
	}

	// =========================================================================
	// Title + filter row
	// =========================================================================

	private static JPanel buildTitleFilterRow() {
		JPanel row = new JPanel(new BorderLayout(0, 8));
		row.setBackground(CONTENT_BG);
		row.setBorder(new EmptyBorder(0, 0, 12, 0));

		// Left: title
		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		left.setBackground(CONTENT_BG);

		JLabel title = new JLabel("Dashboard");
		title.setFont(new Font("Arial", Font.BOLD, 28));
		title.setForeground(TEXT_DARK);
		left.add(title);

		filterRangeLabel = new JLabel("Showing: Current Month");
		filterRangeLabel.setFont(new Font("Arial", Font.ITALIC, 12));
		filterRangeLabel.setForeground(TEXT_MUTED);
		left.add(filterRangeLabel);
		row.add(left, BorderLayout.WEST);

		// Right: date pickers + buttons
		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
		right.setBackground(CONTENT_BG);

		JLabel fromLbl = new JLabel("From:");
		fromLbl.setFont(new Font("Arial", Font.PLAIN, 13));
		fromLbl.setForeground(TEXT_DARK);

		fromDateChooser = new JDateChooser();
		fromDateChooser.setDateFormatString("MMM dd, yyyy");
		fromDateChooser.setPreferredSize(new Dimension(140, 36));
		fromDateChooser.setMaximumSize(new Dimension(140, 36));
		fromDateChooser.setFont(new Font("Arial", Font.PLAIN, 13));

		JLabel toLbl = new JLabel("To:");
		toLbl.setFont(new Font("Arial", Font.PLAIN, 13));
		toLbl.setForeground(TEXT_DARK);

		toDateChooser = new JDateChooser();
		toDateChooser.setDateFormatString("MMM dd, yyyy");
		toDateChooser.setPreferredSize(new Dimension(140, 36));
		toDateChooser.setMaximumSize(new Dimension(140, 36));
		toDateChooser.setFont(new Font("Arial", Font.PLAIN, 13));

		JButton applyBtn = createStyledButton("Apply", SIDEBAR_ACTIVE, 90, 36);
		applyBtn.addActionListener(e -> applyDateFilter());

		JButton clearBtn = createStyledButton("Clear", new Color(120, 120, 120), 80, 36);
		clearBtn.addActionListener(e -> clearDateFilter());

		JButton refreshBtn = createStyledButton("↻ Refresh", new Color(80, 80, 80), 100, 36);
		refreshBtn.addActionListener(e -> {
			showLoading();
			controller.refresh(() -> {
				hideLoading();
				refreshUI();
			}, err -> {
				hideLoading();
				showError(err);
			});
		});

		right.add(fromLbl);
		right.add(Box.createHorizontalStrut(6));
		right.add(fromDateChooser);
		right.add(Box.createHorizontalStrut(12));
		right.add(toLbl);
		right.add(Box.createHorizontalStrut(6));
		right.add(toDateChooser);
		right.add(Box.createHorizontalStrut(10));
		right.add(applyBtn);
		right.add(Box.createHorizontalStrut(6));
		right.add(clearBtn);
		right.add(Box.createHorizontalStrut(6));
		right.add(refreshBtn);

		row.add(right, BorderLayout.EAST);
		return row;
	}

	private static void applyDateFilter() {
		java.util.Date from = fromDateChooser.getDate();
		java.util.Date to = toDateChooser.getDate();

		LocalDate lfrom = from == null ? null : from.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
		LocalDate lto = to == null ? null : to.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

		if (lfrom != null && lto != null && lfrom.isAfter(lto)) {
			ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef),
					"\"From\" date cannot be after \"To\" date");
			return;
		}

		updateFilterLabel(lfrom, lto);
		showLoading();
		controller.applyFilter(lfrom, lto, () -> {
			hideLoading();
			refreshUI();
		}, err -> {
			hideLoading();
			showError(err);
		});
	}

	private static void clearDateFilter() {
		fromDateChooser.setDate(null);
		toDateChooser.setDate(null);
		updateFilterLabel(null, null);
		showLoading();
		controller.clearFilter(() -> {
			hideLoading();
			refreshUI();
		}, err -> {
			hideLoading();
			showError(err);
		});
	}

	private static void updateFilterLabel(LocalDate from, LocalDate to) {
		if (filterRangeLabel == null)
			return;
		if (from == null && to == null) {
			filterRangeLabel.setText("Showing: Current Month");
		} else if (from != null && to != null) {
			filterRangeLabel.setText("Showing: " + from.format(DATE_FMT) + "  →  " + to.format(DATE_FMT));
		} else if (from != null) {
			filterRangeLabel.setText("Showing: From " + from.format(DATE_FMT));
		} else {
			filterRangeLabel.setText("Showing: Up to " + to.format(DATE_FMT));
		}
	}

	// =========================================================================
	// Loading overlay
	// =========================================================================

	private static JPanel buildLoadingOverlay() {
		loadingOverlay = new JPanel(new GridBagLayout());
		loadingOverlay.setBackground(new Color(250, 247, 242, 210));
		loadingOverlay.setVisible(false);

		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.setOpaque(false);

		spinner = new LoadingSpinner(50, SIDEBAR_ACTIVE);
		spinner.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		center.add(spinner);
		center.add(Box.createVerticalStrut(15));

		JLabel lbl = new JLabel("Loading...");
		lbl.setFont(new Font("Arial", Font.BOLD, 16));
		lbl.setForeground(TEXT_DARK);
		lbl.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		center.add(lbl);

		loadingOverlay.add(center);
		return loadingOverlay;
	}

	private static void showLoading() {
		if (loadingOverlay == null)
			return;
		if (spinner != null)
			spinner.start();
		loadingOverlay.setVisible(true);
	}

	private static void hideLoading() {
		if (loadingOverlay == null)
			return;
		if (spinner != null)
			spinner.stop();
		loadingOverlay.setVisible(false);
	}

	// =========================================================================
	// Refresh all UI from state
	// =========================================================================

	private static void refreshUI() {
		DashboardState s = controller.getState();
		rebuildMetricCards(s);
		rebuildCountCards(s); // Added: rebuild count cards on refresh
		if (barChart != null)
			barChart.setData(s.getMonthlyIncome());
		updateLoanTable(s.getUpcomingLoanPayments());
		updateUpcomingDelivTable(s.getUpcomingDeliveries());
		if (mainPanelRef != null) {
			mainPanelRef.revalidate();
			mainPanelRef.repaint();
		}
	}

	// =========================================================================
	// Row 1 — 3 financial metric cards
	// =========================================================================

	private static JPanel buildMetricsGrid() {
		JPanel grid = new JPanel(new GridBagLayout());
		grid.setBackground(CONTENT_BG);
		grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
		grid.setAlignmentX(Component.LEFT_ALIGNMENT);
		metricsGrid = grid;
		rebuildMetricCards(new DashboardState());
		return grid;
	}

	private static void rebuildMetricCards(DashboardState s) {
		if (metricsGrid == null)
			return;
		metricsGrid.removeAll();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.weightx = 1.0;
		gbc.gridy = 0;

		gbc.gridx = 0;
		gbc.insets = new Insets(0, 0, 0, 14);
		metricsGrid.add(buildCard("Total Revenue (Period)", formatPeso(s.getTotalRevenueMonth()), "💰", CARD_GOLD,
				"Revenue within the selected date range"), gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(0, 0, 0, 14);
		metricsGrid.add(buildCard("Net Profit (Period)", formatPeso(s.getNetProfitMonth()), "📈", CARD_GREEN,
				"Revenue minus expenses"), gbc);

		gbc.gridx = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		metricsGrid.add(buildCard("Expenses (Period)", formatPeso(s.getExpensesMonth()), "📉", CARD_RED,
				"Total expenses for the period"), gbc);

		metricsGrid.revalidate();
		metricsGrid.repaint();
	}

	// =========================================================================
	// Row 2 — Bar chart (left 60%) + count cards (right 40%)
	// =========================================================================

	private static JPanel buildChartAndCountsRow() {
		JPanel row = new JPanel(new GridBagLayout());
		row.setBackground(CONTENT_BG);
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.gridy = 0;

		// Bar chart panel (60%)
		gbc.gridx = 0;
		gbc.weightx = 0.60;
		gbc.insets = new Insets(0, 0, 0, 14);
		row.add(buildChartPanel(), gbc);

		// Count cards (40%)
		gbc.gridx = 1;
		gbc.weightx = 0.40;
		gbc.insets = new Insets(0, 0, 0, 0);
		row.add(buildCountCardsColumn(), gbc);

		return row;
	}

	private static JPanel buildChartPanel() {
		JPanel wrapper = new JPanel(new BorderLayout(0, 8));
		wrapper.setBackground(CONTENT_BG);

		JLabel title = new JLabel("Monthly Income  (Last 12 Months)");
		title.setFont(new Font("Arial", Font.BOLD, 15));
		title.setForeground(TEXT_DARK);
		wrapper.add(title, BorderLayout.NORTH);

		barChart = new BarChartPanel();
		barChart.setData(controller.getState().getMonthlyIncome());
		barChart.setPreferredSize(new Dimension(0, 240));
		wrapper.add(barChart, BorderLayout.CENTER);

		return wrapper;
	}

	private static JPanel buildCountCardsColumn() {
		countCardsPanel = new JPanel(new GridBagLayout()); // Store reference
		countCardsPanel.setBackground(CONTENT_BG);

		rebuildCountCards(controller.getState()); // Use rebuild method

		return countCardsPanel;
	}

	// New method to rebuild count cards dynamically
	private static void rebuildCountCards(DashboardState s) {
		if (countCardsPanel == null)
			return;
		countCardsPanel.removeAll();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx = 0;
		gbc.insets = new Insets(0, 0, 10, 0);

		gbc.gridy = 0;
		countCardsPanel.add(buildSmallCard("Total Deliveries", String.valueOf(s.getTotalDeliveries()), "📦", CARD_BLUE),
				gbc);
		gbc.gridy = 1;
		countCardsPanel.add(
				buildSmallCard("Pending Deliveries", String.valueOf(s.getPendingDeliveries()), "⏳", CARD_ORANGE), gbc);
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 10, 0);
		countCardsPanel.add(buildSmallCard("Total Customers", String.valueOf(s.getTotalCustomers()), "👥", CARD_PURPLE),
				gbc);
		gbc.gridy = 3;
		gbc.insets = new Insets(0, 0, 0, 0);
		countCardsPanel.add(buildSmallCard("Total Products", String.valueOf(s.getTotalProducts()), "🧴", CARD_TEAL),
				gbc);

		countCardsPanel.revalidate();
		countCardsPanel.repaint();
	}

	// =========================================================================
	// Row 3 — Upcoming Loans + Upcoming Deliveries
	// =========================================================================

	private static JPanel buildUpcomingSection() {
		JPanel section = new JPanel(new GridBagLayout());
		section.setBackground(CONTENT_BG);
		section.setAlignmentX(Component.LEFT_ALIGNMENT);
		section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.gridy = 0;

		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 0, 12);
		section.add(buildLoanPaymentsPanel(), gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(0, 12, 0, 0);
		section.add(buildUpcomingDeliveriesPanel(), gbc);

		return section;
	}

	// ── Upcoming Loan Payments ────────────────────────────────────────────────

	private static JPanel buildLoanPaymentsPanel() {
		JPanel panel = new JPanel(new BorderLayout(0, 8));
		panel.setBackground(CONTENT_BG);

		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(CONTENT_BG);
		JLabel title = new JLabel("Upcoming Loan Due Payments");
		title.setFont(new Font("Arial", Font.BOLD, 15));
		title.setForeground(TEXT_DARK);
		header.add(title, BorderLayout.WEST);
		JButton viewAll = createLinkButton("View All →");
		viewAll.addActionListener(e -> {
			if (onViewPayments != null)
				onViewPayments.run();
		});
		header.add(viewAll, BorderLayout.EAST);
		panel.add(header, BorderLayout.NORTH);

		String[] cols = { "#", "Customer", "Delivery", "Amount Due", "Due Date", "Status" };
		DefaultTableModel model = new DefaultTableModel(cols, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		loanTable = buildStyledTable(model);

		loanTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		loanTable.getColumnModel().getColumn(1).setPreferredWidth(130);
		loanTable.getColumnModel().getColumn(2).setPreferredWidth(110);
		loanTable.getColumnModel().getColumn(3).setPreferredWidth(110);
		loanTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		loanTable.getColumnModel().getColumn(5).setPreferredWidth(90);

		loanTable.getColumnModel().getColumn(3).setCellRenderer(amountRenderer());
		loanTable.getColumnModel().getColumn(5).setCellRenderer(loanStatusRenderer());

		panel.add(wrapInScroll(loanTable, 220), BorderLayout.CENTER);
		return panel;
	}

	private static void updateLoanTable(List<UpcomingLoanPayment> list) {
		if (loanTable == null)
			return;
		DefaultTableModel model = (DefaultTableModel) loanTable.getModel();
		model.setRowCount(0);
		if (list == null || list.isEmpty()) {
			model.addRow(new Object[] { "", "No upcoming loan payments", "", "", "", "" });
			return;
		}
		int i = 1;
		for (UpcomingLoanPayment p : list) {
			model.addRow(new Object[] { i++, p.getCustomerName(), p.getDeliveryName(), formatPeso(p.getAmountDue()),
					p.getDueDate() != null ? p.getDueDate().format(DATE_FMT) : "N/A", p.getStatus() });
		}
	}

	// ── Upcoming Deliveries ───────────────────────────────────────────────────

	private static JPanel buildUpcomingDeliveriesPanel() {
		JPanel panel = new JPanel(new BorderLayout(0, 8));
		panel.setBackground(CONTENT_BG);

		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(CONTENT_BG);
		JLabel title = new JLabel("Upcoming Deliveries");
		title.setFont(new Font("Arial", Font.BOLD, 15));
		title.setForeground(TEXT_DARK);
		header.add(title, BorderLayout.WEST);
		JButton viewAll = createLinkButton("View All →");
		viewAll.addActionListener(e -> {
			if (onViewDeliveries != null)
				onViewDeliveries.run();
		});
		header.add(viewAll, BorderLayout.EAST);
		panel.add(header, BorderLayout.NORTH);

		String[] cols = { "#", "Delivery", "Customer", "Branch", "Date", "Status" };
		DefaultTableModel model = new DefaultTableModel(cols, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		upcomingDelivTable = buildStyledTable(model);

		upcomingDelivTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		upcomingDelivTable.getColumnModel().getColumn(1).setPreferredWidth(110);
		upcomingDelivTable.getColumnModel().getColumn(2).setPreferredWidth(120);
		upcomingDelivTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		upcomingDelivTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		upcomingDelivTable.getColumnModel().getColumn(5).setPreferredWidth(90);

		upcomingDelivTable.getColumnModel().getColumn(5).setCellRenderer(delivStatusRenderer());

		panel.add(wrapInScroll(upcomingDelivTable, 220), BorderLayout.CENTER);
		return panel;
	}

	private static void updateUpcomingDelivTable(List<UpcomingDelivery> list) {
		if (upcomingDelivTable == null)
			return;
		DefaultTableModel model = (DefaultTableModel) upcomingDelivTable.getModel();
		model.setRowCount(0);
		if (list == null || list.isEmpty()) {
			model.addRow(new Object[] { "", "No upcoming deliveries", "", "", "", "" });
			return;
		}
		int i = 1;
		for (UpcomingDelivery d : list) {
			model.addRow(new Object[] { i++, d.getDeliveryName(), d.getCustomerName(), d.getBranchName(),
					d.getScheduledDate() != null ? d.getScheduledDate().format(DATE_FMT) : "N/A", d.getStatus() });
		}
	}

	// =========================================================================
	// Metric card builders
	// =========================================================================

	private static JPanel buildCard(String label, String value, String emoji, Color accent, String tooltip) {
		JPanel card = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.setColor(accent);
				g2.fillRoundRect(0, 0, 6, getHeight(), 6, 6);
				g2.fillRect(3, 0, 3, getHeight());
			}
		};
		card.setOpaque(false);
		card.setBorder(new EmptyBorder(18, 22, 18, 18));
		card.setPreferredSize(new Dimension(0, 105));
		card.setToolTipText(tooltip);

		JLabel iconLabel = new JLabel(emoji);
		iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);
		iconLabel.setBorder(new EmptyBorder(0, 0, 0, 14));
		card.add(iconLabel, BorderLayout.WEST);

		JPanel textPanel = new JPanel();
		textPanel.setOpaque(false);
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

		JLabel lbl = new JLabel(label);
		lbl.setFont(new Font("Arial", Font.PLAIN, 12));
		lbl.setForeground(TEXT_MUTED);

		JLabel val = new JLabel(value);
		val.setFont(new Font("Arial", Font.BOLD, 22));
		val.setForeground(TEXT_DARK);

		textPanel.add(lbl);
		textPanel.add(Box.createVerticalStrut(3));
		textPanel.add(val);
		card.add(textPanel, BorderLayout.CENTER);

		card.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent, 1, true),
						new EmptyBorder(17, 21, 17, 17)));
				card.repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				card.setBorder(new EmptyBorder(18, 22, 18, 18));
				card.repaint();
			}
		});
		return card;
	}

	private static JPanel buildSmallCard(String label, String value, String emoji, Color accent) {
		JPanel card = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				g2.setColor(accent);
				g2.fillRoundRect(0, 0, 5, getHeight(), 5, 5);
				g2.fillRect(3, 0, 2, getHeight());
			}
		};
		card.setOpaque(false);
		card.setBorder(new EmptyBorder(12, 16, 12, 12));

		JLabel iconLabel = new JLabel(emoji);
		iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);
		iconLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
		card.add(iconLabel, BorderLayout.WEST);

		JPanel textPanel = new JPanel();
		textPanel.setOpaque(false);
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

		JLabel lbl = new JLabel(label);
		lbl.setFont(new Font("Arial", Font.PLAIN, 11));
		lbl.setForeground(TEXT_MUTED);

		JLabel val = new JLabel(value);
		val.setFont(new Font("Arial", Font.BOLD, 18));
		val.setForeground(TEXT_DARK);

		textPanel.add(lbl);
		textPanel.add(val);
		card.add(textPanel, BorderLayout.CENTER);

		return card;
	}

	// =========================================================================
	// Bar chart (pure Swing — no external library)
	// =========================================================================

	private static class BarChartPanel extends JPanel {
		private Map<String, BigDecimal> data;
		private int hoveredBar = -1;

		public void setData(Map<String, BigDecimal> data) {
			this.data = data;
			repaint();
		}

		public BarChartPanel() {
			setBackground(Color.WHITE);
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1),
					new EmptyBorder(14, 16, 14, 16)));
			addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					int prev = hoveredBar;
					hoveredBar = getBarIndexAt(e.getX());
					if (hoveredBar != prev)
						repaint();
				}
			});
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(MouseEvent e) {
					hoveredBar = -1;
					repaint();
				}
			});
		}

		private int getBarIndexAt(int x) {
			if (data == null || data.isEmpty())
				return -1;
			int pad = 16;
			int w = getWidth() - pad * 2;
			int n = data.size();
			int gap = 4;
			int barW = (w - gap * (n - 1)) / n;
			int idx = 0;
			for (int i = 0; i < n; i++) {
				int bx = pad + i * (barW + gap);
				if (x >= bx && x <= bx + barW)
					return idx;
				idx++;
			}
			return -1;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (data == null || data.isEmpty()) {
				g.setColor(TEXT_MUTED);
				g.setFont(new Font("Arial", Font.ITALIC, 13));
				g.drawString("No data", getWidth() / 2 - 28, getHeight() / 2);
				return;
			}

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			int padL = 60; // left (Y-axis labels)
			int padR = 16;
			int padT = 16;
			int padB = 42; // bottom (X-axis labels)
			int width = getWidth();
			int height = getHeight();
			int chartW = width - padL - padR;
			int chartH = height - padT - padB;

			// Max value for scaling
			BigDecimal maxVal = data.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
			if (maxVal.compareTo(BigDecimal.ZERO) == 0)
				maxVal = BigDecimal.ONE;

			// Gridlines + Y-axis labels
			int gridLines = 5;
			g2.setFont(new Font("Arial", Font.PLAIN, 10));
			g2.setColor(new Color(230, 222, 212));
			for (int i = 0; i <= gridLines; i++) {
				int y = padT + chartH - (int) ((double) i / gridLines * chartH);
				g2.setColor(new Color(235, 228, 218));
				g2.drawLine(padL, y, padL + chartW, y);
				BigDecimal labelVal = maxVal.multiply(BigDecimal.valueOf((double) i / gridLines));
				String labelText = formatPesoShort(labelVal);
				g2.setColor(TEXT_MUTED);
				FontMetrics fm = g2.getFontMetrics();
				g2.drawString(labelText, padL - fm.stringWidth(labelText) - 5, y + fm.getAscent() / 2 - 1);
			}

			// Bars
			List<String> keys = new ArrayList<>(data.keySet());
			int n = keys.size();
			int gap = 4;
			int barW = (chartW - gap * (n - 1)) / n;

			for (int i = 0; i < n; i++) {
				BigDecimal val = data.get(keys.get(i));
				double ratio = val.doubleValue() / maxVal.doubleValue();
				int barH = (int) (ratio * chartH);
				int bx = padL + i * (barW + gap);
				int by = padT + chartH - barH;
				boolean hovered = (i == hoveredBar);

				// Bar gradient
				Color top = hovered ? SIDEBAR_ACTIVE.brighter() : SIDEBAR_ACTIVE;
				Color bottom = hovered ? new Color(90, 60, 30) : new Color(109, 71, 35);
				GradientPaint gp = new GradientPaint(bx, by, top, bx, by + barH, bottom);
				g2.setPaint(gp);
				g2.fillRoundRect(bx, by, barW, barH, 4, 4);

				// Value label on hover
				if (hovered) {
					g2.setColor(TEXT_DARK);
					g2.setFont(new Font("Arial", Font.BOLD, 11));
					String txt = formatPesoShort(val);
					FontMetrics fm = g2.getFontMetrics();
					int tx = bx + barW / 2 - fm.stringWidth(txt) / 2;
					int ty = by - 4;
					g2.drawString(txt, tx, ty);
				}

				// X-axis label
				g2.setColor(TEXT_MUTED);
				g2.setFont(new Font("Arial", Font.PLAIN, 10));
				String xLabel = keys.get(i);
				FontMetrics fm = g2.getFontMetrics();
				int lx = bx + barW / 2 - fm.stringWidth(xLabel) / 2;
				g2.drawString(xLabel, lx, padT + chartH + 16);
			}

			// X and Y axis lines
			g2.setColor(new Color(200, 190, 180));
			g2.setStroke(new BasicStroke(1.5f));
			g2.drawLine(padL, padT, padL, padT + chartH);
			g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);
		}

		private String formatPesoShort(BigDecimal val) {
			double d = val.doubleValue();
			if (d >= 1_000_000)
				return String.format("₱%.1fM", d / 1_000_000);
			if (d >= 1_000)
				return String.format("₱%.0fK", d / 1_000);
			return String.format("₱%.0f", d);
		}
	}

	// =========================================================================
	// Shared table helpers
	// =========================================================================

	private static JTable buildStyledTable(DefaultTableModel model) {
		JTable table = new JTable(model);
		table.setFont(new Font("Arial", Font.PLAIN, 13));
		table.setRowHeight(40);
		table.setShowGrid(true);
		table.setGridColor(new Color(220, 210, 200));
		table.setBackground(TABLE_ROW_EVEN);
		table.setSelectionBackground(TABLE_HOVER);
		table.setSelectionForeground(TEXT_DARK);
		table.setFocusable(false);

		JTableHeader header = table.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 13));
		header.setBackground(TABLE_HEADER);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
		header.setReorderingAllowed(false);

		DefaultTableCellRenderer hr = new DefaultTableCellRenderer();
		hr.setBackground(TABLE_HEADER);
		hr.setForeground(TEXT_LIGHT);
		hr.setFont(new Font("Arial", Font.BOLD, 13));
		hr.setHorizontalAlignment(SwingConstants.LEFT);
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setHeaderRenderer(hr);
		}

		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
					int row, int col) {
				Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
					setForeground(TEXT_DARK);
				}
				setHorizontalAlignment(SwingConstants.LEFT);
				((JLabel) c).setBorder(new EmptyBorder(4, 10, 4, 10));
				return c;
			}
		});
		return table;
	}

	private static JScrollPane wrapInScroll(JTable table, int height) {
		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
		scroll.getViewport().setBackground(TABLE_ROW_EVEN);
		scroll.setPreferredSize(new Dimension(0, height));
		return scroll;
	}

	// ── Cell renderers ────────────────────────────────────────────────────────

	private static DefaultTableCellRenderer loanStatusRenderer() {
		return new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
					int row, int col) {
				Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
					String s = value == null ? "" : value.toString();
					if (s.equals("Overdue"))
						setForeground(COLOR_OVERDUE);
					else if (s.equals("Due Soon"))
						setForeground(COLOR_DUE_SOON);
					else
						setForeground(TEXT_DARK);
					setFont(new Font("Arial", Font.BOLD, 13));
				}
				setHorizontalAlignment(SwingConstants.LEFT);
				((JLabel) c).setBorder(new EmptyBorder(4, 10, 4, 10));
				return c;
			}
		};
	}

	private static DefaultTableCellRenderer delivStatusRenderer() {
		return new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
					int row, int col) {
				Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
					String s = value == null ? "" : value.toString();
					if (s.equals("In Transit"))
						setForeground(COLOR_IN_TRANSIT);
					else if (s.equals("Scheduled"))
						setForeground(COLOR_SCHEDULED);
					else
						setForeground(TEXT_DARK);
					setFont(new Font("Arial", Font.BOLD, 13));
				}
				setHorizontalAlignment(SwingConstants.LEFT);
				((JLabel) c).setBorder(new EmptyBorder(4, 10, 4, 10));
				return c;
			}
		};
	}

	private static DefaultTableCellRenderer amountRenderer() {
		return new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
					int row, int col) {
				Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
					setForeground(TEXT_DARK);
				}
				setHorizontalAlignment(SwingConstants.RIGHT);
				((JLabel) c).setBorder(new EmptyBorder(4, 10, 4, 10));
				return c;
			}
		};
	}

	// =========================================================================
	// Button helpers
	// =========================================================================

	private static JButton createStyledButton(String text, Color bgColor, int w, int h) {
		JButton btn = new JButton(text);
		btn.setFont(new Font("Arial", Font.BOLD, 13));
		btn.setBackground(bgColor);
		btn.setForeground(TEXT_LIGHT);
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.setPreferredSize(new Dimension(w, h));
		btn.setMaximumSize(new Dimension(w, h));
		btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (btn.isEnabled())
					btn.setBackground(bgColor.brighter());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				btn.setBackground(bgColor);
			}
		});
		return btn;
	}

	private static JButton createLinkButton(String text) {
		JButton btn = new JButton(text);
		btn.setFont(new Font("Arial", Font.PLAIN, 13));
		btn.setForeground(SIDEBAR_ACTIVE);
		btn.setBackground(CONTENT_BG);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				btn.setFont(new Font("Arial", Font.BOLD, 13));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				btn.setFont(new Font("Arial", Font.PLAIN, 13));
			}
		});
		return btn;
	}

	// =========================================================================
	// Utilities
	// =========================================================================

	private static void showError(String msg) {
		ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef), msg);
	}

	private static String formatPeso(BigDecimal amount) {
		if (amount == null)
			return "₱0.00";
		return CURRENCY.format(amount).replace("PHP", "₱");
	}
}