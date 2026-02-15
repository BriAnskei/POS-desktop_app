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
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.UiController.DeliveryController;
import com.gierza_molases.molases_app.context.AppContext;
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

	// Status colors
	private static final Color STATUS_SCHEDULED = new Color(255, 165, 0); // Orange
	private static final Color STATUS_DELIVERED = new Color(46, 125, 50); // Green
	private static final Color STATUS_CANCELLED = new Color(198, 40, 40); // Red

	// UI component references
	private static JTextField searchField;
	private static JDateChooser startDateChooser;
	private static JDateChooser endDateChooser;
	private static JTable table;
	private static JPanel mainPanelRef;
	private static JLabel loadingLabel;
	private static JPanel loadingOverlay;
	private static LoadingSpinner spinner;

	// Pagination components
	private static JButton prevButton;
	private static JButton nextButton;
	private static JLabel pageInfoLabel;

	// Controller reference

	private static DeliveryController controller = AppContext.deliveryController;

	private static java.util.function.Consumer<Integer> onViewDetailsCallback;

	/**
	 * Create the Deliveries Page panel
	 */
	public static JPanel createPanel(Runnable onAddNew, Runnable onRefresh,
			java.util.function.Consumer<Integer> onViewDetails) {

		onViewDetailsCallback = onViewDetails;

		controller.resetState();

		JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		mainPanelRef = mainPanel;

		// Top Section
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

		// Bottom Section - Pagination
		JPanel paginationSection = createPaginationSection();
		mainPanel.add(paginationSection, BorderLayout.SOUTH);

		// Initial load
		loadInitialData();

		return mainPanel;
	}

	/**
	 * Load initial data on page creation
	 */
	private static void loadInitialData() {
		showLoading();

		controller.loadDeliveries(false, () -> {
			SwingUtilities.invokeLater(() -> {
				refreshTable();
				updatePaginationUI();

				hideLoading();
			});
		}, () -> {
			SwingUtilities.invokeLater(() -> {
				hideLoading();

				ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef),
						"Failed to load deliveries. Please try again.");
			});
		});
	}

	/**
	 * Create pagination section
	 */
	private static JPanel createPaginationSection() {
		JPanel paginationPanel = new JPanel(new BorderLayout());
		paginationPanel.setBackground(CONTENT_BG);
		paginationPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

		// Page info label (left side)
		pageInfoLabel = new JLabel("Showing 0-0 of 0");
		pageInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		pageInfoLabel.setForeground(TEXT_DARK);
		paginationPanel.add(pageInfoLabel, BorderLayout.WEST);

		// Controls panel (right side)
		JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		controlsPanel.setBackground(CONTENT_BG);

		// Previous button
		prevButton = createPaginationButton("← Previous");
		prevButton.addActionListener(e -> goToPreviousPage());
		controlsPanel.add(prevButton);

		// Next button
		nextButton = createPaginationButton("Next →");
		nextButton.addActionListener(e -> goToNextPage());
		controlsPanel.add(nextButton);

		paginationPanel.add(controlsPanel, BorderLayout.EAST);

		return paginationPanel;
	}

	/**
	 * Go to next page
	 */
	private static void goToNextPage() {
		if (!controller.getState().hasNextPage())
			return;

		showLoading();
		controller.loadDeliveries(true, () -> {
			SwingUtilities.invokeLater(() -> {
				refreshTable();
				updatePaginationUI();
				hideLoading();
				scrollTableToTop();
			});
		}, () -> {
			SwingUtilities.invokeLater(() -> {
				hideLoading();
				ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef),
						"Failed to load next page. Please try again.");
			});
		});
	}

	/**
	 * Go to previous page
	 */
	private static void goToPreviousPage() {
		if (!controller.getState().hasPreviousPage())
			return;

		showLoading();
		controller.goToPreviousPage(() -> {
			SwingUtilities.invokeLater(() -> {
				refreshTable();
				updatePaginationUI();
				hideLoading();
				scrollTableToTop();
			});
		});
	}

	/**
	 * Scroll table to top
	 */
	private static void scrollTableToTop() {
		if (table != null && table.getRowCount() > 0) {
			table.scrollRectToVisible(table.getCellRect(0, 0, true));
		}
	}

	/**
	 * Update pagination UI components
	 */
	private static void updatePaginationUI() {
		if (prevButton != null && nextButton != null && pageInfoLabel != null) {
			List<Delivery> deliveries = controller.getState().getDeliveries();

			// Update button states
			prevButton.setEnabled(controller.getState().hasPreviousPage());
			nextButton.setEnabled(controller.getState().hasNextPage());

			// Update info label
			int totalItems = deliveries.size();
			if (totalItems == 0) {
				pageInfoLabel.setText("Showing 0-0 of 0");
			} else {
				pageInfoLabel.setText("Showing 1-" + totalItems + " of " + totalItems + "+");
			}
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
			if (prevButton != null)
				prevButton.setEnabled(false);
			if (nextButton != null)
				nextButton.setEnabled(false);
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

			// Update pagination button states properly
			updatePaginationUI();
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
				onAddNew.run();
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

		// Start date chooser
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

		// End date chooser
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
		String searchText = searchField.getText().trim();
		Date startDateUtil = startDateChooser.getDate();
		Date endDateUtil = endDateChooser.getDate();

		showLoading();

		// Apply all filters together
		controller.getState().setSearch(searchText);
		controller.getState().setStartAt(startDateUtil);
		controller.getState().setEndAt(endDateUtil);
		controller.getState().resetPagination();

		controller.loadDeliveries(false, () -> {
			SwingUtilities.invokeLater(() -> {
				refreshTable();
				updatePaginationUI();
				hideLoading();

			});
		}, () -> {
			SwingUtilities.invokeLater(() -> {
				hideLoading();

			});
		});
	}

	/**
	 * Clear all filters
	 */
	private static void clearFilters() {
		searchField.setText("");
		startDateChooser.setDate(null);
		endDateChooser.setDate(null);

		showLoading();

		controller.clearFilters(() -> {
			SwingUtilities.invokeLater(() -> {
				refreshTable();
				updatePaginationUI();
				hideLoading();
				ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(mainPanelRef), "Filters cleared");
			});
		}, () -> {
			SwingUtilities.invokeLater(() -> {
				hideLoading();
				ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef),
						"Failed to clear filters. Please try again.");
			});
		});
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

		// Custom cell renderer for status column
		table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);

					String status = value.toString().toLowerCase();
					if (status.equals("scheduled")) {
						setForeground(STATUS_SCHEDULED);
						setFont(new Font("Arial", Font.BOLD, 14));
					} else if (status.equals("delivered")) {
						setForeground(STATUS_DELIVERED);
						setFont(new Font("Arial", Font.BOLD, 14));
					} else if (status.equals("cancelled")) {
						setForeground(STATUS_CANCELLED);
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

				List<Delivery> deliveries = controller.getState().getDeliveries();
				if (col == 4 && row >= 0 && row < deliveries.size()) {
					Delivery delivery = deliveries.get(row);
					showActionMenu(e.getComponent(), e.getX(), e.getY(), delivery);
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
	 * Refresh table with current state data
	 */
	private static void refreshTable() {
		if (table == null)
			return;

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
		List<Delivery> deliveries = controller.getState().getDeliveries();

		for (Delivery d : deliveries) {
			String deliveryDate = d.getScheduleDate() != null ? d.getScheduleDate().format(formatter) : "";
			String customersInfo = d.getTotalCustomers() + " customers of " + d.getTotalBranches() + " branches";
			String status = d.getStatus();

			// Capitalize first letter of status
			status = status.substring(0, 1).toUpperCase() + status.substring(1);

			model.addRow(new Object[] { deliveryDate, d.getName(), customersInfo, status, "⚙ Actions" });
		}

		table.revalidate();
		table.repaint();
	}

	/**
	 * Show action menu
	 */
	private static void showActionMenu(Component parent, int x, int y, Delivery delivery) {
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
			// Call the callback with delivery ID
			if (onViewDetailsCallback != null) {
				onViewDetailsCallback.accept(delivery.getId());
			}
		});
		actionDialog.add(viewDetailsBtn, gbc);

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
		actionDialog.setMinimumSize(new Dimension(350, 200));
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
				{ "Overall Profit:", "₱" + String.format("%,.2f", delivery.getNetProfit()) },
				{ "Overall Capital:", "₱" + String.format("%,.2f", delivery.getTotalCapital()) },
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
			performDelete(delivery);
		});
		confirmDialog.add(deleteBtn, gbc);

		confirmDialog.pack();
		confirmDialog.setMinimumSize(new Dimension(400, 200));
		confirmDialog.setLocationRelativeTo(null);
		confirmDialog.setVisible(true);
	}

	/**
	 * Perform delete operation
	 */
	private static void performDelete(Delivery delivery) {
		showLoading();
		controller.deleteDelivery(delivery.getId(), () -> {
			SwingUtilities.invokeLater(() -> {
				// Refresh current page after delete
				controller.refreshCurrentPage(() -> {
					SwingUtilities.invokeLater(() -> {
						refreshTable();
						updatePaginationUI();
						hideLoading();
						ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(table),
								"Delivery ID " + delivery.getId() + " deleted successfully");
					});
				}, () -> {
					SwingUtilities.invokeLater(() -> {
						hideLoading();
						ToastNotification.showError(SwingUtilities.getWindowAncestor(table),
								"Failed to refresh after delete. Please reload the page.");
					});
				});
			});
		}, () -> {
			SwingUtilities.invokeLater(() -> {
				hideLoading();
				ToastNotification.showError(SwingUtilities.getWindowAncestor(table),
						"Failed to delete delivery. Please try again.");
			});
		});
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