package com.gierza_molases.molases_app.ui.dialogs.Delivery;

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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.gierza_molases.molases_app.UiController.NewDeliveryController;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;
import com.gierza_molases.molases_app.ui.components.ToastNotification;

public class SelectCustomerDialog extends JDialog {

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);

	// UI Components
	private JTable customerTable;
	private DefaultTableModel customerTableModel;
	private List<Customer> availableCustomers = new ArrayList<>();
	private JTextField searchField;
	private JButton searchButton;
	private JButton clearFiltersButton;
	private JButton selectButton;
	private JButton cancelButton;
	private Customer selectedCustomer = null;
	private int selectedRowIndex = -1;

	// Loading UI Components
	private JPanel loadingPanel;
	private LoadingSpinner loadingSpinner;
	private JPanel customerSelectionPanel;

	// Callback
	private Consumer<Customer> onCustomerSelectedCallback;

	// controller
	private NewDeliveryController newDeliveryController;

	/**
	 * Constructor
	 */
	public SelectCustomerDialog(Window parent, Consumer<Customer> onCustomerSelected,
			NewDeliveryController newDeliveryController) {
		super(parent, "Select Customer", ModalityType.APPLICATION_MODAL);
		this.onCustomerSelectedCallback = onCustomerSelected;
		this.newDeliveryController = newDeliveryController;

		initializeUI();
		loadCustomersAsync();
	}

	/**
	 * Initialize the UI
	 */
	private void initializeUI() {
		setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);

		// Main content panel with padding
		JPanel mainContent = new JPanel(new BorderLayout(0, 20));
		mainContent.setBackground(Color.WHITE);
		mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

		// Header
		mainContent.add(createHeaderSection(), BorderLayout.NORTH);

		// Center - Customer Selection (with loading overlay)
		mainContent.add(createCenterPanelWithLoading(), BorderLayout.CENTER);

		// Bottom - Buttons
		mainContent.add(createBottomSection(), BorderLayout.SOUTH);

		add(mainContent);

		// Dialog settings
		setSize(800, 600);
		setMinimumSize(new Dimension(800, 600));
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Create center panel with loading overlay capability
	 */
	private JPanel createCenterPanelWithLoading() {
		JPanel containerPanel = new JPanel(new java.awt.CardLayout());
		containerPanel.setBackground(Color.WHITE);

		// Customer selection panel
		customerSelectionPanel = createCustomerSelectionSection();
		customerSelectionPanel.setName("CUSTOMER_PANEL");

		// Loading panel
		loadingPanel = createLoadingPanel();
		loadingPanel.setName("LOADING_PANEL");

		// Add both panels with names
		containerPanel.add(loadingPanel, "LOADING_PANEL");
		containerPanel.add(customerSelectionPanel, "CUSTOMER_PANEL");

		return containerPanel;
	}

	/**
	 * Create loading panel
	 */
	private JPanel createLoadingPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.WHITE);
		panel.setOpaque(true);

		// Add vertical glue to center content
		panel.add(Box.createVerticalGlue());

		// Loading spinner
		loadingSpinner = new LoadingSpinner(60, SIDEBAR_ACTIVE);
		loadingSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(loadingSpinner);

		// Spacing
		panel.add(Box.createRigidArea(new Dimension(0, 20)));

		// Loading text
		JLabel loadingLabel = new JLabel("Loading customers...");
		loadingLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		loadingLabel.setForeground(TEXT_DARK);
		loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(loadingLabel);

		// Add vertical glue to center content
		panel.add(Box.createVerticalGlue());

		return panel;
	}

	/**
	 * Show loading state
	 */
	private void showLoading() {
		SwingUtilities.invokeLater(() -> {
			JPanel container = (JPanel) loadingPanel.getParent();
			java.awt.CardLayout cl = (java.awt.CardLayout) container.getLayout();
			cl.show(container, "LOADING_PANEL");
			loadingSpinner.start();

			// Disable buttons during loading
			searchButton.setEnabled(false);
			clearFiltersButton.setEnabled(false);
			selectButton.setEnabled(false);
			searchField.setEnabled(false);
		});
	}

	/**
	 * Hide loading state
	 */
	private void hideLoading() {
		SwingUtilities.invokeLater(() -> {
			loadingSpinner.stop();
			JPanel container = (JPanel) loadingPanel.getParent();
			java.awt.CardLayout cl = (java.awt.CardLayout) container.getLayout();
			cl.show(container, "CUSTOMER_PANEL");

			// Re-enable buttons
			searchButton.setEnabled(true);
			clearFiltersButton.setEnabled(true);
			searchField.setEnabled(true);
			updateSelectButtonState();
		});
	}

	/**
	 * Load customers asynchronously with loading UI
	 */
	private void loadCustomersAsync() {
		showLoading();

		newDeliveryController.load20CustomerData(() -> {
			SwingUtilities.invokeLater(() -> {

				this.availableCustomers = newDeliveryController.getCustomerState().getCustomers();
				updateCustomerTable();
				hideLoading();

				System.out.println("Loaded " + availableCustomers.size() + " customers"); // Debug
			});
		}, error -> {
			hideLoading();
			// showError(error);
		});

	}

	/**
	 * Perform search asynchronously
	 */
	private void performSearchAsync(String searchText) {

		newDeliveryController.getCustomerState().setSearch(searchText);
		this.loadCustomersAsync();
	}

	/**
	 * Create header section
	 */
	private JPanel createHeaderSection() {
		JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
		headerPanel.setBackground(Color.WHITE);

		// Title
		JLabel titleLabel = new JLabel("Select a Customer");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(TEXT_DARK);
		headerPanel.add(titleLabel, BorderLayout.NORTH);

		// Search Panel
		JPanel searchPanel = new JPanel(new GridBagLayout());
		searchPanel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 10);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;

		searchField = new JTextField();
		searchField.setFont(new Font("Arial", Font.PLAIN, 14));
		searchField.setPreferredSize(new Dimension(0, 38));
		searchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		searchField.addActionListener(e -> performSearch());
		searchPanel.add(searchField, gbc);

		gbc.gridx = 1;
		gbc.weightx = 0;
		gbc.insets = new Insets(0, 0, 0, 10);
		searchButton = createStyledButton("Search", SIDEBAR_ACTIVE);
		searchButton.setPreferredSize(new Dimension(100, 38));
		searchButton.addActionListener(e -> performSearch());
		searchPanel.add(searchButton, gbc);

		gbc.gridx = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		clearFiltersButton = createStyledButton("Clear Filters", new Color(120, 120, 120));
		clearFiltersButton.setPreferredSize(new Dimension(120, 38));
		clearFiltersButton.addActionListener(e -> clearFilters());
		searchPanel.add(clearFiltersButton, gbc);

		headerPanel.add(searchPanel, BorderLayout.CENTER);

		return headerPanel;
	}

	/**
	 * Clear all filters and reset the customer list
	 */
	private void clearFilters() {

		searchField.setText("");
		newDeliveryController.getCustomerState().setSearch(null);

		loadCustomersAsync();
	}

	/**
	 * Create customer selection section
	 */
	private JPanel createCustomerSelectionSection() {
		JPanel selectionPanel = new JPanel(new BorderLayout(0, 10));
		selectionPanel.setBackground(Color.WHITE);

		// Instructions
		JLabel instructionLabel = new JLabel("Select a customer from the list:");
		instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		instructionLabel.setForeground(TEXT_DARK);
		selectionPanel.add(instructionLabel, BorderLayout.NORTH);

		// Create customer table
		createCustomerTable();
		JScrollPane scrollPane = new JScrollPane(customerTable);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
		scrollPane.setPreferredSize(new Dimension(0, 400));

		selectionPanel.add(scrollPane, BorderLayout.CENTER);

		return selectionPanel;
	}

	/**
	 * Create customer table
	 */
	private void createCustomerTable() {
		String[] columns = { "", "Customer Name", "Address" };
		customerTableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int column) {
				if (column == 0)
					return Boolean.class;
				return Object.class;
			}
		};

		customerTable = new JTable(customerTableModel);
		customerTable.setFont(new Font("Arial", Font.PLAIN, 14));
		customerTable.setRowHeight(45);
		customerTable.setShowGrid(true);
		customerTable.setGridColor(new Color(220, 210, 200));
		customerTable.setBackground(TABLE_ROW_EVEN);
		customerTable.setSelectionBackground(new Color(255, 235, 205));
		customerTable.setSelectionForeground(TEXT_DARK);
		customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Set column widths
		customerTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		customerTable.getColumnModel().getColumn(0).setMaxWidth(50);
		customerTable.getColumnModel().getColumn(1).setPreferredWidth(250);
		customerTable.getColumnModel().getColumn(2).setPreferredWidth(450);

		// Style table header
		JTableHeader header = customerTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(TABLE_HEADER);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setBackground(TABLE_HEADER);
		headerRenderer.setForeground(TEXT_LIGHT);
		headerRenderer.setFont(new Font("Arial", Font.BOLD, 14));
		headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		for (int i = 0; i < customerTable.getColumnCount(); i++) {
			customerTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}

		// Custom renderer for checkbox column
		customerTable.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {
			private final JCheckBox checkBox = new JCheckBox();

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				checkBox.setSelected(value != null && (Boolean) value);
				checkBox.setHorizontalAlignment(SwingConstants.CENTER);
				checkBox.setBackground(
						isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD));
				checkBox.setEnabled(false);
				return checkBox;
			}
		});

		// Alternating row colors for other columns
		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				setHorizontalAlignment(SwingConstants.LEFT);
				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		};

		customerTable.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
		customerTable.getColumnModel().getColumn(2).setCellRenderer(cellRenderer);

		// Selection listener
		customerTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				int selectedRow = customerTable.getSelectedRow();
				handleRowSelection(selectedRow);
			}
		});

		// Mouse listener for clicking anywhere on the row
		customerTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = customerTable.rowAtPoint(e.getPoint());
				if (row >= 0) {
					// Toggle selection
					if (selectedRowIndex == row) {
						customerTable.clearSelection();
						handleRowSelection(-1);
					} else {
						customerTable.setRowSelectionInterval(row, row);
						handleRowSelection(row);
					}

					// Double-click behavior - select and close
					if (e.getClickCount() == 2 && row < availableCustomers.size()) {
						handleSelectCustomer();
					}
				}
			}
		});
	}

	/**
	 * Handle row selection and update checkboxes
	 */
	private void handleRowSelection(int selectedRow) {
		// Clear all checkboxes
		for (int i = 0; i < customerTableModel.getRowCount(); i++) {
			customerTableModel.setValueAt(false, i, 0);
		}

		// Set selected row
		if (selectedRow >= 0 && selectedRow < availableCustomers.size()) {
			selectedCustomer = availableCustomers.get(selectedRow);
			selectedRowIndex = selectedRow;
			customerTableModel.setValueAt(true, selectedRow, 0);
		} else {
			selectedCustomer = null;
			selectedRowIndex = -1;
		}

		updateSelectButtonState();
	}

	/**
	 * Perform search
	 */
	private void performSearch() {
		String searchText = searchField.getText().trim();

		if (searchText.isEmpty()) {
			loadCustomersAsync();
			return;
		}

		performSearchAsync(searchText);
	}

	/**
	 * Update customer table
	 */
	private void updateCustomerTable() {
		customerTableModel.setRowCount(0);

		for (Customer customer : availableCustomers) {
			customerTableModel.addRow(new Object[] { false, customer.getDisplayName(), customer.getAddress() });
		}

		selectedCustomer = null;
		selectedRowIndex = -1;
		updateSelectButtonState();
	}

	/**
	 * Create bottom section with buttons
	 */
	private JPanel createBottomSection() {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(Color.WHITE);

		cancelButton = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelButton.setPreferredSize(new Dimension(120, 42));
		cancelButton.addActionListener(e -> dispose());
		buttonPanel.add(cancelButton);

		selectButton = createStyledButton("Select", ACCENT_GOLD);
		selectButton.setPreferredSize(new Dimension(120, 42));
		selectButton.setEnabled(false);
		selectButton.addActionListener(e -> handleSelectCustomer());
		buttonPanel.add(selectButton);

		return buttonPanel;
	}

	/**
	 * Update select button state
	 */
	private void updateSelectButtonState() {
		selectButton.setEnabled(selectedCustomer != null);
	}

	/**
	 * Handle select customer
	 */
	private void handleSelectCustomer() {
		if (selectedCustomer == null) {
			ToastNotification.showError(this, "Please select a customer.");
			return;
		}

		if (onCustomerSelectedCallback != null) {
			onCustomerSelectedCallback.accept(selectedCustomer);
		}

		dispose();
	}

	/**
	 * Create styled button
	 */
	private JButton createStyledButton(String text, Color bgColor) {
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

	/**
	 * Show the dialog
	 */
	public static void show(Window parent, Consumer<Customer> onCustomerSelected,
			NewDeliveryController newDeliveryController) {
		SelectCustomerDialog dialog = new SelectCustomerDialog(parent, onCustomerSelected, newDeliveryController);
		dialog.setVisible(true);
	}
}