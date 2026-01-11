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
import java.time.LocalDateTime;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

import com.gierza_molases.molases_app.UiController.CustomersController;
import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.context.CustomerState;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.ui.dialogs.CustomerDialogs.AddCustomerDialog;
import com.gierza_molases.molases_app.ui.dialogs.CustomerDialogs.UpdateCustomerDialog;
import com.gierza_molases.molases_app.ui.dialogs.CustomerDialogs.ViewBranchDialog;

public class CustomersPage {

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

	// Controller reference
	private static final CustomersController controller = AppContext.customersController;

	// UI component references
	private static JTextField searchField;
	private static JComboBox<String> sortCombo;
	private static JTable table;
	private static JLabel pageInfoLabel;
	private static JPanel mainPanelRef;

	private static JPanel paginationControlsPanel;

	private static JLabel loadingLabel;
	private static JPanel loadingOverlay;
	private static LoadingSpinner spinner;

	/**
	 * Create the Customer Page panel
	 */
	public static JPanel createPanel() {
		// Reset state when creating panel
		controller.resetState();

		JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		mainPanelRef = mainPanel;

		// Create UI sections
		JPanel topSection = createTopSection();
		mainPanel.add(topSection, BorderLayout.NORTH);

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

		JPanel bottomSection = createPaginationSection();
		mainPanel.add(bottomSection, BorderLayout.SOUTH);

		// Load data asynchronously WITH LOADING INDICATOR
		showLoading();
		controller.loadCustomers(() -> {
			hideLoading();
			refreshTable();
		}, () -> {
			hideLoading();
			JOptionPane.showMessageDialog(null, "Failed to load customers", "Error", JOptionPane.ERROR_MESSAGE);
		});

		return mainPanel;
	}

	/**
	 * Create top section with title, buttons, search, and sort
	 */
	private static JPanel createTopSection() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setBackground(CONTENT_BG);
		topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

		// Row 1: Title and Add Button
		JPanel titleRow = new JPanel(new BorderLayout());
		titleRow.setBackground(CONTENT_BG);
		titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		JLabel titleLabel = new JLabel("Customer Management");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
		titleLabel.setForeground(TEXT_DARK);
		titleRow.add(titleLabel, BorderLayout.WEST);

		JButton addButton = createStyledButton("+ Add New", ACCENT_GOLD);
		addButton.addActionListener(e -> {
			AddCustomerDialog dialog = new AddCustomerDialog(SwingUtilities.getWindowAncestor(addButton), () -> {
				refreshCustomerData();
				ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(addButton),
						"Customer saved successfully!");
			});
			dialog.setVisible(true);
		});
		titleRow.add(addButton, BorderLayout.EAST);

		topPanel.add(titleRow);
		topPanel.add(Box.createVerticalStrut(15));

		// Row 2: Filters
		JPanel filtersRow = new JPanel();
		filtersRow.setLayout(new BoxLayout(filtersRow, BoxLayout.X_AXIS));
		filtersRow.setBackground(CONTENT_BG);
		filtersRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		// Search field
		CustomerState state = controller.getState();
		searchField = new JTextField(20);
		searchField.setFont(new Font("Arial", Font.PLAIN, 14));
		searchField.setPreferredSize(new Dimension(300, 38));
		searchField.setMaximumSize(new Dimension(300, 38));
		searchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));

		filtersRow.add(searchField);

		filtersRow.add(Box.createHorizontalStrut(10));

		// Search button
		JButton searchButton = createStyledButton("Search", SIDEBAR_ACTIVE);
		searchButton.addActionListener(e -> performSearch());
		filtersRow.add(searchButton);

		filtersRow.add(Box.createHorizontalStrut(15));

		// Sort dropdown
		JLabel sortLabel = new JLabel("Sort by:");
		sortLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		sortLabel.setForeground(TEXT_DARK);
		filtersRow.add(sortLabel);
		filtersRow.add(Box.createHorizontalStrut(10));

		String[] sortOptions = { "Newest First", "Oldest First" };
		sortCombo = new JComboBox<>(sortOptions);
		sortCombo.setFont(new Font("Arial", Font.PLAIN, 14));
		sortCombo.setPreferredSize(new Dimension(150, 38));
		sortCombo.setMaximumSize(new Dimension(150, 38));
		sortCombo.setBackground(Color.WHITE);
		sortCombo.setSelectedIndex(state.getSortOrder().equals("DESC") ? 0 : 1);
		sortCombo.addActionListener(e -> {
			String selected = (String) sortCombo.getSelectedItem();
			String newSortOrder = selected.equals("Newest First") ? "DESC" : "ASC";

			if (!newSortOrder.equals(state.getSortOrder())) {
				showLoading();
				controller.updateSortOrder(newSortOrder, () -> {
					hideLoading();

					refreshTable();
				}, () -> {
					hideLoading();
					ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef),
							"Failed to update sort order");
				});
			}
		});
		filtersRow.add(sortCombo);

		filtersRow.add(Box.createHorizontalGlue());

		// Clear Filters button
		JButton clearFiltersButton = createStyledButton("Clear Filters", SIDEBAR_ACTIVE);
		clearFiltersButton.addActionListener(e -> clearFilters());
		filtersRow.add(clearFiltersButton);

		topPanel.add(filtersRow);

		return topPanel;
	}

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
			if (sortCombo != null)
				sortCombo.setEnabled(false);
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
			if (sortCombo != null)
				sortCombo.setEnabled(true);
		}
	}

	/**
	 * Perform search
	 */
	private static void performSearch() {
		String searchText = searchField.getText().trim();
		showLoading();

		controller.search(searchText, () -> {
			hideLoading();
			refreshTable();
		}, () -> {
			hideLoading();
			ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef), "Search failed");
		});
	}

	/**
	 * Clear all filters
	 */
	private static void clearFilters() {
		searchField.setText("");
		sortCombo.setSelectedIndex(0);

		showLoading();
		controller.clearFilters(() -> {
			hideLoading();
			refreshTable();
		}, () -> {
			hideLoading();
			ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef), "Failed to clear filters");
		});
	}

	/**
	 * Create table section
	 */
	private static JPanel createTableSection() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(CONTENT_BG);

		String[] columns = { "Name", "Contact Number", "Address", "Created At", "Actions" };

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
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.getColumnModel().getColumn(2).setPreferredWidth(300);
		table.getColumnModel().getColumn(3).setPreferredWidth(150);
		table.getColumnModel().getColumn(4).setPreferredWidth(150);

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

		// Alternating row colors
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
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

				CustomerState state = controller.getState();
				if (col == 4 && row >= 0 && row < state.getCustomers().size()) {
					Customer customer = state.getCustomers().get(row);
					showActionMenu(e.getComponent(), e.getX(), e.getY(), customer, row);
				}
			}
		});

		// Hand cursor on actions column
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
	 * Update table with current data from state
	 */
	private static void updateTableData(DefaultTableModel model) {
		model.setRowCount(0);

		CustomerState state = controller.getState();
		for (Customer c : state.getCustomers()) {
			model.addRow(
					new Object[] { c.getDisplayName(), c.getFormatttedNumber() != null ? c.getFormatttedNumber() : "",
							c.getAddress() != null ? c.getAddress() : "", formatDate(c.getCreatedAt()), "⚙ Actions" });
		}
	}

	/**
	 * Format date
	 */
	private static String formatDate(LocalDateTime date) {
		if (date == null)
			return "";
		return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
	}

	/**
	 * Show action menu
	 */
	private static void showActionMenu(Component parent, int x, int y, Customer customer, int row) {
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

		gbc.gridy++;
		gbc.insets = new Insets(15, 20, 5, 20);

		// Update button
		JButton updateBtn = createActionButton("✏️ Update Customer", ACCENT_GOLD);
		updateBtn.addActionListener(e -> {
			actionDialog.dispose();
			UpdateCustomerDialog dialog = new UpdateCustomerDialog(SwingUtilities.getWindowAncestor(updateBtn),
					customer, () -> {
						refreshCustomerData();
						ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(updateBtn),
								"Customer updated successfully!");
					});
			dialog.setVisible(true);
		});
		actionDialog.add(updateBtn, gbc);

		gbc.gridy++;
		// Branches button
		JButton branchesBtn = createActionButton("🏢 View Branches", SIDEBAR_ACTIVE);
		branchesBtn.addActionListener(e -> {
			actionDialog.dispose();
			ViewBranchDialog dialog = new ViewBranchDialog(SwingUtilities.getWindowAncestor(branchesBtn), customer);
			dialog.setVisible(true);
		});
		actionDialog.add(branchesBtn, gbc);

		gbc.gridy++;
		// Delete button
		JButton deleteBtn = createActionButton("🗑️ Delete Customer", new Color(180, 50, 50));
		deleteBtn.addActionListener(e -> {
			actionDialog.dispose();
			showDeleteConfirmation(customer);
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
	 * Show delete confirmation
	 */
	private static void showDeleteConfirmation(Customer customer) {
		JDialog confirmDialog = new JDialog(SwingUtilities.getWindowAncestor(table), "Confirm Delete");
		confirmDialog.setLayout(new GridBagLayout());
		confirmDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);

		JLabel messageLabel = new JLabel("<html><center>Are you sure you want to delete<br><b>"
				+ customer.getDisplayName() + "</b>?</center></html>");
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
			deleteBtn.setEnabled(false);

			controller.deleteCustomer(customer.getId(), () -> {
				refreshCustomerData();
				ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(table),
						"Customer deleted successfully!");
			}, ex -> {
				ToastNotification.showError(SwingUtilities.getWindowAncestor(table),
						ex.getMessage() != null ? ex.getMessage() : "Failed to delete customer!");
				deleteBtn.setEnabled(true);
			});

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

		// Page info label
		pageInfoLabel = new JLabel();
		updatePaginationInfo();
		pageInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		pageInfoLabel.setForeground(TEXT_DARK);
		paginationPanel.add(pageInfoLabel, BorderLayout.WEST);

		// Pagination controls - STORE REFERENCE
		paginationControlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		paginationControlsPanel.setBackground(CONTENT_BG);
		updatePaginationButtons();

		paginationPanel.add(paginationControlsPanel, BorderLayout.EAST);

		return paginationPanel;
	}

	/**
	 * Refresh customer data
	 */
	public static void refreshCustomerData() {
		showLoading();
		controller.loadCustomers(() -> {
			hideLoading();
			refreshTable();
		}, () -> {
			hideLoading();
			ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef), "Failed to refresh data");
		});
	}

	/**
	 * Update pagination buttons without recreating them
	 */
	private static void updatePaginationButtons() {
		if (paginationControlsPanel == null)
			return;

		// Remove old buttons
		paginationControlsPanel.removeAll();

		CustomerState state = controller.getState();
		int totalPages = Math.max(1, (int) Math.ceil((double) state.getTotalCustomers() / state.getItemsPerPage()));

		// Previous button
		JButton prevBtn = createPaginationButton("← Previous");
		prevBtn.setEnabled(state.getCurrentPage() > 1);
		prevBtn.addActionListener(e -> {
			if (state.getCurrentPage() > 1) {
				showLoading();
				controller.goToPage(state.getCurrentPage() - 1, () -> {
					hideLoading();
					refreshTable();
				}, () -> {
					hideLoading();
					ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef), "Failed to load page");
				});
			}
		});
		paginationControlsPanel.add(prevBtn);

		// Page numbers
		int startPage = Math.max(1, state.getCurrentPage() - 2);
		int endPage = Math.min(totalPages, state.getCurrentPage() + 2);

		for (int i = startPage; i <= endPage; i++) {
			final int pageNum = i;
			JButton pageBtn = createPaginationButton(String.valueOf(i));

			if (i == state.getCurrentPage()) {
				pageBtn.setBackground(ACCENT_GOLD);
				pageBtn.setForeground(TEXT_LIGHT);
			}

			pageBtn.addActionListener(e -> {
				showLoading();
				controller.goToPage(pageNum, () -> {
					hideLoading();
					refreshTable();
				}, () -> {
					hideLoading();
					ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef), "Failed to load page");
				});
			});
			paginationControlsPanel.add(pageBtn);
		}

		// Next button
		JButton nextBtn = createPaginationButton("Next →");
		nextBtn.setEnabled(state.getCurrentPage() < totalPages);
		nextBtn.addActionListener(e -> {
			if (state.getCurrentPage() < totalPages) {
				showLoading();
				controller.goToPage(state.getCurrentPage() + 1, () -> {
					hideLoading();
					refreshTable();
				}, () -> {
					hideLoading();
					ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef), "Failed to load page");
				});
			}
		});
		paginationControlsPanel.add(nextBtn);

		paginationControlsPanel.revalidate();
		paginationControlsPanel.repaint();
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

		// Update pagination info label
		updatePaginationInfo();

		// Update pagination buttons
		updatePaginationButtons();

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

		CustomerState state = controller.getState();
		int start = state.getTotalCustomers() > 0 ? ((state.getCurrentPage() - 1) * state.getItemsPerPage() + 1) : 0;
		int end = Math.min(state.getCurrentPage() * state.getItemsPerPage(), state.getTotalCustomers());

		String infoText = "Showing " + start + " to " + end + " of " + state.getTotalCustomers() + " entries";
		if (!state.getSearch().isEmpty()) {
			infoText += " (filtered by: \"" + state.getSearch() + "\")";
		}

		pageInfoLabel.setText(infoText);
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
		button.setPreferredSize(new Dimension(180, 38));

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
				if (!button.getBackground().equals(ACCENT_GOLD)) {
					button.setBackground(Color.WHITE);
				}
			}
		});

		return button;
	}
}