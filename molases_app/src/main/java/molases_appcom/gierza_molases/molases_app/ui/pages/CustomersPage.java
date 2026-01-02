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
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.service.CustomerService;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.ui.dialogs.AddCustomerDialog;
import com.gierza_molases.molases_app.ui.dialogs.UpdateCustomerDialog;
import com.gierza_molases.molases_app.ui.dialogs.ViewBranchDialog;

public class CustomersPage {

	// Color Palette - matching Main.java
	private static final Color SIDEBAR_BG = new Color(62, 39, 35);
	private static final Color SIDEBAR_HOVER = new Color(92, 64, 51);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color HEADER_BG = new Color(245, 239, 231);
	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);
	private static final Color TABLE_HOVER = new Color(245, 239, 231);

	private static int currentPage = 1;
	private static int itemsPerPage = 20;
	private static int totalCustomers = 0;

	private static final CustomerService customerService = new CustomerService();

	private static List<Customer> customers = new ArrayList<>();

	private static String currentSearch = "";
	private static String currentSortOrder = "DESC"; // "DESC" or "ASC"

	// References to UI components
	private static JTextField searchField;
	private static JComboBox<String> sortCombo;
	private static JTable table;
	private static JLabel pageInfoLabel;

	/**
	 * Create the Customer Page panel
	 */
	public static JPanel createPanel() {
		// Reset to defaults when creating panel
		currentPage = 1;
		currentSearch = "";
		currentSortOrder = "DESC";

		// Load data from database
		loadCustomerData();

		JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

		// Top Section (Header with buttons and filters)
		JPanel topSection = createTopSection();
		mainPanel.add(topSection, BorderLayout.NORTH);

		// Center Section (Table)
		JPanel tableSection = createTableSection();
		mainPanel.add(tableSection, BorderLayout.CENTER);

		// Bottom Section (Pagination)
		JPanel bottomSection = createPaginationSection();
		mainPanel.add(bottomSection, BorderLayout.SOUTH);

		return mainPanel;
	}

	/**
	 * Load customer data from database with current filters
	 */
	private static void loadCustomerData() {
		try {
			// Get total count first (you'll need to add this method to your DAO/Service)
			totalCustomers = customerService.getTotalCustomerCount(currentSearch);

			// Fetch customers for current page with filters
			customers = customerService.fetchAll(currentPage, itemsPerPage, currentSearch, currentSortOrder);

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to load customers: " + e.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);

			customers = new ArrayList<>();
			totalCustomers = 0;
		}
	}

	/**
	 * Create top section with title, buttons, search, and sort
	 */
	private static JPanel createTopSection() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setBackground(CONTENT_BG);
		topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

		// Row 1: Title and Add New Button
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

				Window parent = SwingUtilities.getWindowAncestor(addButton);

				ToastNotification.showSuccess(parent, "Customer saved successfully!");
			});
			dialog.setVisible(true);
		});
		titleRow.add(addButton, BorderLayout.EAST);

		topPanel.add(titleRow);
		topPanel.add(Box.createVerticalStrut(15));

		// Row 2: Filters with custom spacing
		JPanel filtersRow = new JPanel();
		filtersRow.setLayout(new BoxLayout(filtersRow, BoxLayout.X_AXIS));
		filtersRow.setBackground(CONTENT_BG);
		filtersRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		// Search field
		searchField = new JTextField(20);
		searchField.setFont(new Font("Arial", Font.PLAIN, 14));
		searchField.setPreferredSize(new Dimension(300, 38));
		searchField.setMaximumSize(new Dimension(300, 38));
		searchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		searchField.setText(currentSearch);
		searchField.addActionListener(e -> performSearch());
		filtersRow.add(searchField);

		filtersRow.add(Box.createHorizontalStrut(10));

		// Search button
		JButton searchButton = createStyledButton("Search", SIDEBAR_ACTIVE);
		searchButton.addActionListener(e -> performSearch());
		filtersRow.add(searchButton);

		filtersRow.add(Box.createHorizontalStrut(15));

		// Sort label and dropdown
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
		sortCombo.setSelectedIndex(currentSortOrder.equals("DESC") ? 0 : 1);
		sortCombo.addActionListener(e -> {
			String selected = (String) sortCombo.getSelectedItem();
			currentSortOrder = selected.equals("Newest First") ? "DESC" : "ASC";
			currentPage = 1;
			loadCustomerData();
			refreshTable();
		});
		filtersRow.add(sortCombo);

		// Add flexible space
		filtersRow.add(Box.createHorizontalGlue());

		// Clear Filters button
		JButton clearFiltersButton = createStyledButton("Clear Filters", SIDEBAR_ACTIVE);
		clearFiltersButton.addActionListener(e -> clearFilters());
		filtersRow.add(clearFiltersButton);

		topPanel.add(filtersRow);

		return topPanel;
	}

	/**
	 * Perform search operation
	 */
	private static void performSearch() {
		currentSearch = searchField.getText().trim();
		currentPage = 1; // Reset to first page when searching
		loadCustomerData();
		refreshTable();
	}

	/**
	 * Clear all filters and reset to default state
	 */
	private static void clearFilters() {
		// Reset search
		currentSearch = "";
		searchField.setText("");

		// Reset sort to default (Newest First)
		currentSortOrder = "DESC";
		sortCombo.setSelectedIndex(0);

		// Reset to first page
		currentPage = 1;

		// Reload data and refresh table
		refreshCustomerData();
	}

	/**
	 * Create table section with customer data
	 */
	private static JPanel createTableSection() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(CONTENT_BG);

		// Table columns
		String[] columns = { "Name", "Contact Number", "Address", "Created At", "Actions" };

		// Create table model (non-editable)
		DefaultTableModel model = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		// Populate with current page data
		updateTableData(model);

		// Create table
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

		// Custom header renderer
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
					if (row % 2 == 0) {
						c.setBackground(TABLE_ROW_EVEN);
					} else {
						c.setBackground(TABLE_ROW_ODD);
					}
				}

				if (column == 4) { // Actions column
					setHorizontalAlignment(SwingConstants.CENTER);
				} else {
					setHorizontalAlignment(SwingConstants.LEFT);
				}

				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		});

		// Add mouse listener for action buttons
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());

				if (col == 4 && row >= 0) { // Actions column
					Customer customer = customers.get(row);
					showActionMenu(e.getComponent(), e.getX(), e.getY(), customer, row);
				}
			}
		});

		// Make actions column show hand cursor
		table.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int col = table.columnAtPoint(e.getPoint());
				if (col == 4) {
					table.setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else {
					table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
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

		for (Customer c : customers) {
			model.addRow(
					new Object[] { c.getDisplayName(), c.getFormatttedNumber() != null ? c.getFormatttedNumber() : "",
							c.getAddress() != null ? c.getAddress() : "", formatDate(c.getCreatedAt()), "⚙ Actions" });
		}
	}

	/**
	 * Format LocalDateTime to display string
	 */
	private static String formatDate(LocalDateTime date) {
		if (date == null)
			return "";
		return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
	}

	/**
	 * Show action menu/dialog when actions column is clicked
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

		// Update button with icon
		JButton updateBtn = createActionButton("✏️ Update Customer", ACCENT_GOLD);
		updateBtn.addActionListener(e -> {
			actionDialog.dispose();

			UpdateCustomerDialog dialog = new UpdateCustomerDialog(SwingUtilities.getWindowAncestor(updateBtn),
					customer, () -> {
						ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(updateBtn),
								"Customer updated successfully!");
						refreshCustomerData();
					});
			dialog.setVisible(true);
		});
		actionDialog.add(updateBtn, gbc);

		gbc.gridy++;
		// Branches button with icon
		JButton branchesBtn = createActionButton("🏢 View Branches", SIDEBAR_ACTIVE);
		branchesBtn.addActionListener(e -> {
			actionDialog.dispose();
			ViewBranchDialog dialog = new ViewBranchDialog(SwingUtilities.getWindowAncestor(branchesBtn), customer);
			dialog.setVisible(true);
		});
		actionDialog.add(branchesBtn, gbc);

		gbc.gridy++;
		// Delete button with icon
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
		actionDialog.setLocationRelativeTo(null); // Center on screen
		actionDialog.setVisible(true);
	}

	/**
	 * Show delete confirmation dialog
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
			try {
				customerService.delete(customer.getId());

				// Refresh data
				refreshCustomerData();

				ToastNotification.showSuccess(confirmDialog, "Customer deleted successfully!");
			} catch (Exception ex) {
				ToastNotification.showError(confirmDialog, "ailed to delete customer: " + ex.getMessage());

			}
		});
		confirmDialog.add(deleteBtn, gbc);

		confirmDialog.pack();
		confirmDialog.setMinimumSize(new Dimension(400, 200));
		confirmDialog.setLocationRelativeTo(null); // Center on screen
		confirmDialog.setVisible(true);
	}

	/**
	 * Create pagination section
	 */
	private static JPanel createPaginationSection() {
		JPanel paginationPanel = new JPanel(new BorderLayout());
		paginationPanel.setBackground(CONTENT_BG);
		paginationPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

		// Info label (left side)
		int start = totalCustomers > 0 ? ((currentPage - 1) * itemsPerPage + 1) : 0;
		int end = Math.min(currentPage * itemsPerPage, totalCustomers);

		String infoText = "Showing " + start + " to " + end + " of " + totalCustomers + " entries";
		if (!currentSearch.isEmpty()) {
			infoText += " (filtered by: \"" + currentSearch + "\")";
		}

		pageInfoLabel = new JLabel(infoText);
		pageInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		pageInfoLabel.setForeground(TEXT_DARK);
		paginationPanel.add(pageInfoLabel, BorderLayout.WEST);

		// Pagination controls (right side)
		JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		controlsPanel.setBackground(CONTENT_BG);

		int totalPages = Math.max(1, (int) Math.ceil((double) totalCustomers / itemsPerPage));

		// Previous button
		JButton prevBtn = createPaginationButton("← Previous");
		prevBtn.setEnabled(currentPage > 1);
		prevBtn.addActionListener(e -> {
			if (currentPage > 1) {
				currentPage--;
				loadCustomerData();
				refreshTable();
			}
		});
		controlsPanel.add(prevBtn);

		// Page numbers (show current page and neighbors)
		int startPage = Math.max(1, currentPage - 2);
		int endPage = Math.min(totalPages, currentPage + 2);

		for (int i = startPage; i <= endPage; i++) {
			final int pageNum = i;
			JButton pageBtn = createPaginationButton(String.valueOf(i));

			if (i == currentPage) {
				pageBtn.setBackground(ACCENT_GOLD);
				pageBtn.setForeground(TEXT_LIGHT);
			}

			pageBtn.addActionListener(e -> {
				currentPage = pageNum;
				loadCustomerData();
				refreshTable();
			});
			controlsPanel.add(pageBtn);
		}

		// Next button
		JButton nextBtn = createPaginationButton("Next →");
		nextBtn.setEnabled(currentPage < totalPages);
		nextBtn.addActionListener(e -> {
			if (currentPage < totalPages) {
				currentPage++;
				loadCustomerData();
				refreshTable();
			}
		});
		controlsPanel.add(nextBtn);

		paginationPanel.add(controlsPanel, BorderLayout.EAST);

		return paginationPanel;
	}

	/**
	 * Refresh customer data (called after adding/updating/deleting)
	 */
	public static void refreshCustomerData() {
		loadCustomerData();
		refreshTable();
	}

	/**
	 * Refresh table and pagination
	 */
	private static void refreshTable() {
		// Find the main panel (traverse up the component tree)
		Component component = table;
		JPanel mainPanel = null;

		while (component != null) {
			component = component.getParent();
			if (component instanceof JPanel) {
				JPanel panel = (JPanel) component;
				if (panel.getLayout() instanceof BorderLayout && panel.getComponentCount() == 3) {
					// This should be our main panel with NORTH, CENTER, SOUTH
					mainPanel = panel;
					break;
				}
			}
		}

		if (mainPanel != null) {
			mainPanel.removeAll();

			JPanel topSection = createTopSection();
			mainPanel.add(topSection, BorderLayout.NORTH);

			JPanel tableSection = createTableSection();
			mainPanel.add(tableSection, BorderLayout.CENTER);

			JPanel bottomSection = createPaginationSection();
			mainPanel.add(bottomSection, BorderLayout.SOUTH);

			mainPanel.revalidate();
			mainPanel.repaint();
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
	 * Create action button (for dialogs)
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