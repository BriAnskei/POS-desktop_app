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

import com.gierza_molases.molases_app.Context.AppContext;
import com.gierza_molases.molases_app.Context.BranchState;
import com.gierza_molases.molases_app.UiController.BranchesController;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.ui.dialogs.BranchDialogs.AddBranchDialog;
import com.gierza_molases.molases_app.ui.dialogs.BranchDialogs.BranchDetails;
import com.gierza_molases.molases_app.ui.dialogs.BranchDialogs.UpdateBranchDialog;

public class BranchesPage {

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
	private static final BranchesController controller = AppContext.branchesController;

	// UI component references
	private static JTextField searchField;
	private static JTable table;
	private static JLabel pageInfoLabel;
	private static JButton prevBtn;
	private static JButton nextBtn;
	private static JPanel mainPanelRef;

	private static JLabel loadingLabel;
	private static JPanel loadingOverlay;
	private static LoadingSpinner spinner;

	/**
	 * Create the Branches Page panel
	 */
	public static JPanel createPanel() {
		// Reset state when creating panel
		controller.resetState();

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

		// Load initial data with loading indicator
		showLoading();
		controller.loadBranches(false, () -> {
			hideLoading();
			refreshTable();
		}, () -> {
			hideLoading();
			ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef), "Failed to load branches");
		});

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
			if (prevBtn != null)
				prevBtn.setEnabled(false);
			if (nextBtn != null)
				nextBtn.setEnabled(false);
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

			updatePaginationControls();
		}
	}

	/**
	 * Create top section
	 */
	private static JPanel createTopSection() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setBackground(CONTENT_BG);
		topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

		// Title row
		JPanel titleRow = new JPanel(new BorderLayout());
		titleRow.setBackground(CONTENT_BG);
		titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		JLabel titleLabel = new JLabel("Branch Management");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
		titleLabel.setForeground(TEXT_DARK);
		titleRow.add(titleLabel, BorderLayout.WEST);

		JButton addButton = createStyledButton("+ Add New", ACCENT_GOLD);
		addButton.addActionListener(e -> {
			AddBranchDialog dialog = new AddBranchDialog(SwingUtilities.getWindowAncestor(addButton), controller,
					() -> {
						refreshBranchData();
						ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(addButton),
								"Branch saved successfully!");
					});
			dialog.setVisible(true);
		});
		titleRow.add(addButton, BorderLayout.EAST);

		topPanel.add(titleRow);
		topPanel.add(Box.createVerticalStrut(15));

		// Search row
		JPanel filtersRow = new JPanel();
		filtersRow.setLayout(new BoxLayout(filtersRow, BoxLayout.X_AXIS));
		filtersRow.setBackground(CONTENT_BG);
		filtersRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		BranchState state = controller.getState();
		searchField = new JTextField(20);
		searchField.setFont(new Font("Arial", Font.PLAIN, 14));
		searchField.setPreferredSize(new Dimension(300, 38));
		searchField.setMaximumSize(new Dimension(300, 38));
		searchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		searchField.setText(state.getSearch());
		filtersRow.add(searchField);

		filtersRow.add(Box.createHorizontalStrut(10));

		JButton searchButton = createStyledButton("Search", SIDEBAR_ACTIVE);
		searchButton.addActionListener(e -> performSearch());
		filtersRow.add(searchButton);

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

		showLoading();
		controller.clearSearch(() -> {
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

		String[] columns = { "Customer Name", "Address", "Note", "Last Delivery", "Actions" };

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
		table.getColumnModel().getColumn(1).setPreferredWidth(300);
		table.getColumnModel().getColumn(2).setPreferredWidth(200);
		table.getColumnModel().getColumn(3).setPreferredWidth(150);
		table.getColumnModel().getColumn(4).setPreferredWidth(100);

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

				BranchState state = controller.getState();
				if (col == 4 && row >= 0 && row < state.getBranches().size()) {
					Branch branch = state.getBranches().get(row);
					showActionMenu(e.getComponent(), e.getX(), e.getY(), branch, row);
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
	 * Update table with current data from state
	 */
	private static void updateTableData(DefaultTableModel model) {
		model.setRowCount(0);

		BranchState state = controller.getState();
		for (Branch b : state.getBranches()) {
			String createdAt = b.getCreatedAt() != null ? b.getCreatedAt() : "";

			model.addRow(new Object[] { b.getCustomerName(), b.getAddress() != null ? b.getAddress() : "",
					b.getNote() != null ? b.getNote() : "", createdAt, "⚙ Actions" });
		}
	}

	/**
	 * Show action menu
	 */
	private static void showActionMenu(Component parent, int x, int y, Branch branch, int row) {
		JDialog actionDialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Actions");
		actionDialog.setLayout(new GridBagLayout());
		actionDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 20, 5, 20);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Choose Action for Branch ID: " + branch.getId());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		actionDialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(15, 20, 5, 20);

		JButton viewDetailsBtn = createActionButton("👁️ View Details", new Color(70, 130, 180));
		viewDetailsBtn.addActionListener(e -> {
			actionDialog.dispose();
			BranchDetails.show(SwingUtilities.getWindowAncestor(parent), branch.getId());
		});
		actionDialog.add(viewDetailsBtn, gbc);

		gbc.gridy++;
		JButton updateBtn = createActionButton("✏️ Update Branch", ACCENT_GOLD);
		updateBtn.addActionListener(e -> {
			actionDialog.dispose();
			UpdateBranchDialog dialog = new UpdateBranchDialog(SwingUtilities.getWindowAncestor(parent), branch.getId(),
					controller, () -> {
						refreshBranchData();
						ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(parent),
								"Branch updated successfully!");
					});
			dialog.setVisible(true);
		});
		actionDialog.add(updateBtn, gbc);

		gbc.gridy++;
		JButton deleteBtn = createActionButton("🗑️ Delete Branch", new Color(180, 50, 50));
		deleteBtn.addActionListener(e -> {
			actionDialog.dispose();
			showDeleteConfirmation(branch);
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
	private static void showDeleteConfirmation(Branch branch) {
		JDialog confirmDialog = new JDialog(SwingUtilities.getWindowAncestor(table), "Confirm Delete");
		confirmDialog.setLayout(new GridBagLayout());
		confirmDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);

		JLabel messageLabel = new JLabel("<html><center>Are you sure you want to delete this branch?<br><b>ID: "
				+ branch.getId() + "</b></center></html>");
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

			controller.deleteBranch(branch.getId(), () -> {
				refreshBranchData();
				ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(table), "Branch deleted successfully!");
			}, () -> {
				ToastNotification.showError(SwingUtilities.getWindowAncestor(table), "Failed to delete branch!");
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

		return paginationPanel;
	}

	/**
	 * Go to previous page
	 */
	private static void goToPreviousPage() {
		controller.goToPreviousPage(() -> {
			refreshTable();
		});
	}

	/**
	 * Go to next page
	 */
	private static void goToNextPage() {
		showLoading();
		controller.loadBranches(true, () -> {
			hideLoading();
			refreshTable();
		}, () -> {
			hideLoading();
			ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef), "Failed to load next page");
		});
	}

	/**
	 * Refresh branch data
	 */
	public static void refreshBranchData() {
		showLoading();
		controller.refreshCurrentPage(() -> {
			hideLoading();
			refreshTable();
		}, () -> {
			hideLoading();
			ToastNotification.showError(SwingUtilities.getWindowAncestor(mainPanelRef), "Failed to refresh data");
		});
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

		BranchState state = controller.getState();
		String infoText = "Showing " + state.getBranches().size() + " branches";
		if (!state.getSearch().isEmpty()) {
			infoText += " (filtered by: \"" + state.getSearch() + "\")";
		}

		pageInfoLabel.setText(infoText);
	}

	/**
	 * Update pagination controls
	 */
	private static void updatePaginationControls() {
		BranchState state = controller.getState();

		if (prevBtn != null) {
			prevBtn.setEnabled(!state.getPageHistory().isEmpty());
		}

		if (nextBtn != null) {
			nextBtn.setEnabled(state.hasNextPage());
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