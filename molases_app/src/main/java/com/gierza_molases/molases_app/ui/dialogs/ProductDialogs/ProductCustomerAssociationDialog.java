package com.gierza_molases.molases_app.ui.dialogs.ProductDialogs;

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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.Context.AppContext;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.service.ProductAssociationService;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.util.UiSwingWorker;

public class ProductCustomerAssociationDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);
	private static final Color TABLE_HOVER = new Color(245, 239, 231);
	private static final Color DELETE_RED = new Color(180, 50, 50);

	// Dialog data
	private final int productId;
	private final String productName;
	private final Runnable onChangeCallback;

	// UI Components
	private JTable customerTable;
	private DefaultTableModel tableModel;
	private JPanel tableContainer;
	private LoadingSpinner loadingSpinner;
	private JScrollPane tableScrollPane;
	private JLabel countLabel;
	private JButton addBtn;
	private JButton closeBtn;

	// Data
	private List<Customer> associatedCustomers = new ArrayList<>();

	// Service
	private final ProductAssociationService productAssociationService = AppContext.productAssociationService;

	/**
	 * Show dialog
	 */
	public static void show(Window parent, int productId, String productName, Runnable onChangeCallback) {
		ProductCustomerAssociationDialog dialog = new ProductCustomerAssociationDialog(parent, productId, productName,
				onChangeCallback);
		dialog.setVisible(true);
	}

	/**
	 * Constructor
	 */
	private ProductCustomerAssociationDialog(Window parent, int productId, String productName,
			Runnable onChangeCallback) {
		super(parent, "Customer Associations", ModalityType.APPLICATION_MODAL);

		this.productId = productId;
		this.productName = productName;
		this.onChangeCallback = onChangeCallback;

		initializeUI();
		loadCustomerData();
	}

	/**
	 * Initialize the UI
	 */
	private void initializeUI() {
		setLayout(new BorderLayout(0, 15));
		getContentPane().setBackground(Color.WHITE);

		// Main content with padding
		JPanel mainContent = new JPanel(new BorderLayout(0, 15));
		mainContent.setBackground(Color.WHITE);
		mainContent.setBorder(new EmptyBorder(20, 25, 20, 25));

		mainContent.add(createHeaderPanel(), BorderLayout.NORTH);
		mainContent.add(createTablePanel(), BorderLayout.CENTER);
		mainContent.add(createButtonPanel(), BorderLayout.SOUTH);

		add(mainContent);

		// Dialog settings
		setSize(750, 550);
		setMinimumSize(new Dimension(750, 550));
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Create header panel
	 */
	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.WHITE);

		// Title
		JLabel titleLabel = new JLabel("Customer Associations");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(titleLabel);

		panel.add(Box.createVerticalStrut(8));

		// Product info
		JLabel productLabel = new JLabel("Product: " + productName + " (ID: " + productId + ")");
		productLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		productLabel.setForeground(new Color(100, 100, 100));
		productLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(productLabel);

		panel.add(Box.createVerticalStrut(5));

		// Customer count (will be updated)
		countLabel = new JLabel("Loading...");
		countLabel.setFont(new Font("Arial", Font.BOLD, 13));
		countLabel.setForeground(new Color(70, 130, 180));
		countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(countLabel);

		return panel;
	}

	/**
	 * Create table panel with loading state
	 */
	private JPanel createTablePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);

		// Table container with CardLayout for switching between table and loading
		tableContainer = new JPanel(new java.awt.CardLayout());
		tableContainer.setBackground(Color.WHITE);

		// Create table view
		createCustomerTable();
		tableScrollPane = new JScrollPane(customerTable);
		tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
		tableScrollPane.getViewport().setBackground(TABLE_ROW_EVEN);

		// Create loading view
		loadingSpinner = new LoadingSpinner(50, TABLE_HEADER);
		JPanel loadingPanel = new JPanel(new GridBagLayout());
		loadingPanel.setBackground(Color.WHITE);
		loadingPanel.add(loadingSpinner);

		// Add both views to container
		tableContainer.add(tableScrollPane, "TABLE");
		tableContainer.add(loadingPanel, "LOADING");

		panel.add(tableContainer, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Create and configure customer table
	 */
	private void createCustomerTable() {
		String[] columns = { "Customer Name", "Contact Number", "Actions" };

		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		customerTable = new JTable(tableModel);
		customerTable.setFont(new Font("Arial", Font.PLAIN, 14));
		customerTable.setRowHeight(50);
		customerTable.setShowGrid(true);
		customerTable.setGridColor(new Color(220, 210, 200));
		customerTable.setBackground(TABLE_ROW_EVEN);
		customerTable.setSelectionBackground(TABLE_HOVER);
		customerTable.setSelectionForeground(TEXT_DARK);

		// Set column widths
		customerTable.getColumnModel().getColumn(0).setPreferredWidth(250);
		customerTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		customerTable.getColumnModel().getColumn(2).setPreferredWidth(150);
		customerTable.getColumnModel().getColumn(2).setMaxWidth(150);

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

		// Alternating row colors
		customerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				setHorizontalAlignment(column == 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));

				// Style the action column
				if (column == 2) {
					setForeground(DELETE_RED);
					setFont(getFont().deriveFont(Font.BOLD));
				} else {
					setForeground(TEXT_DARK);
					setFont(new Font("Arial", Font.PLAIN, 14));
				}

				return c;
			}
		});

		// Mouse listener for actions
		customerTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = customerTable.rowAtPoint(e.getPoint());
				int col = customerTable.columnAtPoint(e.getPoint());

				if (col == 2 && row >= 0 && row < associatedCustomers.size()) {
					Customer customer = associatedCustomers.get(row);
					showRemoveConfirmation(customer);
				}
			}
		});

		customerTable.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int col = customerTable.columnAtPoint(e.getPoint());
				customerTable.setCursor(new Cursor(col == 2 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
			}
		});
	}

	/**
	 * Create button panel
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(15, 0, 0, 0));

		// Left side: Add Customer button
		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		leftPanel.setBackground(Color.WHITE);

		addBtn = createStyledButton("+ Add Customer", ACCENT_GOLD);
		addBtn.setPreferredSize(new Dimension(150, 42));
		addBtn.addActionListener(e -> handleAddCustomer());
		leftPanel.add(addBtn);

		// Right side: Close button
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		rightPanel.setBackground(Color.WHITE);

		closeBtn = createStyledButton("Close", new Color(120, 120, 120));
		closeBtn.setPreferredSize(new Dimension(120, 42));
		closeBtn.addActionListener(e -> dispose());
		rightPanel.add(closeBtn);

		panel.add(leftPanel, BorderLayout.WEST);
		panel.add(rightPanel, BorderLayout.EAST);

		return panel;
	}

	/**
	 * Show/hide loading state
	 */
	private void showLoading(boolean loading) {
		java.awt.CardLayout cardLayout = (java.awt.CardLayout) tableContainer.getLayout();

		if (loading) {
			loadingSpinner.start();
			cardLayout.show(tableContainer, "LOADING");
			addBtn.setEnabled(false);
		} else {
			loadingSpinner.stop();
			cardLayout.show(tableContainer, "TABLE");
			addBtn.setEnabled(true);
		}
	}

	/**
	 * Load customer data from service
	 */
	private void loadCustomerData() {
		showLoading(true);

		new UiSwingWorker<List<Customer>, Void>() {
			@Override
			protected List<Customer> doInBackground() throws Exception {
				return productAssociationService.fetchByProductId(productId);
			}

			@Override
			protected void onSuccess(List<Customer> result) {
				associatedCustomers = result;
				updateTable();
				updateCountLabel();
				showLoading(false);
			}

			@Override
			protected void onError(Exception e) {
				ToastNotification.showError(ProductCustomerAssociationDialog.this,
						"Error loading customers: " + e.getMessage());
				e.printStackTrace();
				associatedCustomers.clear();
				updateTable();
				updateCountLabel();
				showLoading(false);
			}
		}.execute();
	}

	/**
	 * Update table with current customer data
	 */
	private void updateTable() {
		tableModel.setRowCount(0);

		if (associatedCustomers.isEmpty()) {
			// Show empty state in the table
			showEmptyState();
		} else {
			for (Customer customer : associatedCustomers) {
				tableModel.addRow(new Object[] { customer.getDisplayName(),
						customer.getContactNumber() != null ? customer.getContactNumber() : "N/A", "🗑️ Remove" });
			}
		}
	}

	/**
	 * Show empty state when no customers are associated
	 */
	private void showEmptyState() {
		tableModel.addRow(new Object[] { "No customers associated with this product", "", "" });
	}

	/**
	 * Update customer count label
	 */
	private void updateCountLabel() {
		int count = associatedCustomers.size();
		countLabel.setText(count + " customer" + (count == 1 ? "" : "s") + " associated");
	}

	/**
	 * Handle add customer button click
	 */
	private void handleAddCustomer() {
		AddCustomerAssociationDialog.show(this, productId, productName, () -> {
			loadCustomerData();

			// Notify parent if callback exists
			if (onChangeCallback != null) {
				onChangeCallback.run();
			}
		});
	}

	/**
	 * Show remove confirmation dialog
	 */
	private void showRemoveConfirmation(Customer customer) {
		// Don't show confirmation for empty state row
		if (associatedCustomers.isEmpty()) {
			return;
		}

		JDialog confirmDialog = new JDialog(this, "Confirm Remove", true);
		confirmDialog.setLayout(new GridBagLayout());
		confirmDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(25, 35, 10, 35);

		JLabel messageLabel = new JLabel("<html><center>Remove customer association?<br><b>" + customer.getDisplayName()
				+ "</b></center></html>");
		messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		messageLabel.setForeground(TEXT_DARK);
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		confirmDialog.add(messageLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 35, 15, 35);
		JLabel warningLabel = new JLabel("This will remove the association between customer and product.");
		warningLabel.setFont(new Font("Arial", Font.ITALIC, 13));
		warningLabel.setForeground(new Color(180, 100, 50));
		warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
		confirmDialog.add(warningLabel, gbc);

		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 35, 25, 10);

		JButton cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> confirmDialog.dispose());
		confirmDialog.add(cancelBtn, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(20, 10, 25, 35);
		JButton removeBtn = createStyledButton("Remove", DELETE_RED);
		removeBtn.setPreferredSize(new Dimension(120, 40));
		removeBtn.addActionListener(e -> {
			confirmDialog.dispose();
			handleRemoveCustomer(customer);
		});
		confirmDialog.add(removeBtn, gbc);

		confirmDialog.pack();
		confirmDialog.setMinimumSize(new Dimension(480, 220));
		confirmDialog.setLocationRelativeTo(this);
		confirmDialog.setVisible(true);
	}

	/**
	 * Handle customer removal
	 */
	private void handleRemoveCustomer(Customer customer) {
		showLoading(true);

		// Use the controller instead of service directly
		AppContext.productsController.removeCustomerAssociation(productId, customer.getId(),
				// onSuccess
				() -> {
					ToastNotification.showSuccess(ProductCustomerAssociationDialog.this,
							"Customer association removed successfully!");

					// Reload data in this dialog
					loadCustomerData();

					// Notify parent if callback exists (this will also update the products page)
					if (onChangeCallback != null) {
						onChangeCallback.run();
					}
				},
				// onError
				(errorMsg) -> {
					ToastNotification.showError(ProductCustomerAssociationDialog.this,
							"Failed to remove association: " + errorMsg);
					showLoading(false);
				});
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
}