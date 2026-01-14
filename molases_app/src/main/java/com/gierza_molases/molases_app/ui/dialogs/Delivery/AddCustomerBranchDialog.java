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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.ui.dialogs.Delivery.SelectCustomerDialog.Customer;

public class AddCustomerBranchDialog extends JDialog {

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);
	private static final Color CONTENT_BG = new Color(250, 247, 242);

	private static class BranchData {
		String branchAddress;
		int productTypeCount;

		BranchData(String branchAddress, int productTypeCount) {
			this.branchAddress = branchAddress;
			this.productTypeCount = productTypeCount;
		}
	}

	// UI Components
	private JPanel customerDetailsPanel;
	private JLabel customerNameLabel;
	private JLabel customerAddressLabel;
	private JLabel customerContactLabel;
	private JButton selectCustomerBtn;
	private JButton addBranchBtn;
	private JTable branchTable;
	private DefaultTableModel branchTableModel;
	private List<BranchData> branches = new ArrayList<>();
	private Customer selectedCustomer = null;

	// Buttons
	private JButton saveBtn;
	private JButton cancelBtn;

	// Callback
	private Runnable onSuccessCallback;

	/**
	 * Constructor
	 */
	public AddCustomerBranchDialog(Window parent, Runnable onSuccess) {
		super(parent, "Add Customer & Branches", ModalityType.APPLICATION_MODAL);
		this.onSuccessCallback = onSuccess;
		initializeUI();
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

		// Create left and right panels
		JPanel leftPanel = createCustomerDetailsSection();
		JPanel rightPanel = createBranchesSection();

		// Combine panels horizontally
		JPanel formPanel = new JPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.X_AXIS));
		formPanel.setBackground(Color.WHITE);
		formPanel.add(leftPanel);
		formPanel.add(Box.createHorizontalStrut(20));
		formPanel.add(rightPanel);

		mainContent.add(formPanel, BorderLayout.CENTER);
		mainContent.add(createButtonPanel(), BorderLayout.SOUTH);

		add(mainContent);

		// Dialog settings
		setSize(1100, 550);
		setMinimumSize(new Dimension(1100, 550));
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Create customer details section (LEFT SIDE)
	 */
	private JPanel createCustomerDetailsSection() {
		JPanel section = new JPanel(new BorderLayout(0, 15));
		section.setBackground(Color.WHITE);
		section.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
								"Customer Details", javax.swing.border.TitledBorder.LEFT,
								javax.swing.border.TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), TEXT_DARK),
						new EmptyBorder(15, 15, 15, 15)));
		section.setPreferredSize(new Dimension(480, 0));
		section.setMinimumSize(new Dimension(480, 0));

		// Customer details display panel
		customerDetailsPanel = new JPanel(new GridBagLayout());
		customerDetailsPanel.setBackground(CONTENT_BG);
		customerDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(220, 210, 200), 1), new EmptyBorder(20, 20, 20, 20)));
		customerDetailsPanel.setPreferredSize(new Dimension(0, 250));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 5, 5, 5);

		// Initial "No customer selected" message
		JLabel noCustomerLabel = new JLabel("No customer selected");
		noCustomerLabel.setFont(new Font("Arial", Font.ITALIC, 16));
		noCustomerLabel.setForeground(new Color(150, 150, 150));
		customerDetailsPanel.add(noCustomerLabel, gbc);

		section.add(customerDetailsPanel, BorderLayout.CENTER);

		// Select Customer Button
		selectCustomerBtn = createStyledButton("Select Customer", ACCENT_GOLD);
		selectCustomerBtn.setPreferredSize(new Dimension(200, 40));
		selectCustomerBtn.addActionListener(e -> handleSelectCustomer());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBackground(Color.WHITE);
		buttonPanel.add(selectCustomerBtn);

		section.add(buttonPanel, BorderLayout.SOUTH);

		return section;
	}

	/**
	 * Create branches section (RIGHT SIDE)
	 */
	private JPanel createBranchesSection() {
		JPanel section = new JPanel(new BorderLayout(0, 15));
		section.setBackground(Color.WHITE);
		section.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
						"Branches", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
						new Font("Arial", Font.BOLD, 16), TEXT_DARK),
				new EmptyBorder(15, 15, 15, 15)));
		section.setPreferredSize(new Dimension(480, 0));
		section.setMinimumSize(new Dimension(480, 0));

		// Add Branch Button
		addBranchBtn = createStyledButton("+ Select Branch", ACCENT_GOLD);
		addBranchBtn.setPreferredSize(new Dimension(150, 35));
		addBranchBtn.setEnabled(false); // Disabled initially
		addBranchBtn.addActionListener(e -> handleAddBranch());

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		topPanel.setBackground(Color.WHITE);
		topPanel.add(addBranchBtn);

		section.add(topPanel, BorderLayout.NORTH);

		// Branch Table
		createBranchTable();
		JScrollPane scrollPane = new JScrollPane(branchTable);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));

		section.add(scrollPane, BorderLayout.CENTER);

		return section;
	}

	/**
	 * Create and configure branch table
	 */
	private void createBranchTable() {
		String[] columns = { "Branch Address", "Product Types", "Actions" };
		branchTableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		branchTable = new JTable(branchTableModel);
		branchTable.setFont(new Font("Arial", Font.PLAIN, 14));
		branchTable.setRowHeight(50);
		branchTable.setShowGrid(true);
		branchTable.setGridColor(new Color(220, 210, 200));
		branchTable.setBackground(TABLE_ROW_EVEN);
		branchTable.setSelectionBackground(new Color(255, 235, 205));
		branchTable.setSelectionForeground(TEXT_DARK);

		// Set column widths
		branchTable.getColumnModel().getColumn(0).setPreferredWidth(250);
		branchTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		branchTable.getColumnModel().getColumn(2).setPreferredWidth(100);

		// Style table header
		JTableHeader header = branchTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(TABLE_HEADER);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setBackground(TABLE_HEADER);
		headerRenderer.setForeground(TEXT_LIGHT);
		headerRenderer.setFont(new Font("Arial", Font.BOLD, 14));
		headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		for (int i = 0; i < branchTable.getColumnCount(); i++) {
			branchTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}

		// Alternating row colors
		branchTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				if (column == 2) {
					setHorizontalAlignment(SwingConstants.CENTER);
				} else {
					setHorizontalAlignment(SwingConstants.LEFT);
				}

				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		});

		// Mouse listener for actions column
		branchTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = branchTable.rowAtPoint(e.getPoint());
				int col = branchTable.columnAtPoint(e.getPoint());

				if (col == 2 && row >= 0 && row < branches.size()) {
					BranchData branch = branches.get(row);
					showActionMenu(branchTable, branch, row);
				}
			}
		});

		branchTable.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int col = branchTable.columnAtPoint(e.getPoint());
				branchTable.setCursor(new Cursor(col == 2 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
			}
		});
	}

	/**
	 * Handle select customer button click
	 */
	private void handleSelectCustomer() {
		SelectCustomerDialog.show(this, customer -> {
			selectedCustomer = customer;
			updateCustomerDetailsDisplay();
			addBranchBtn.setEnabled(true);
		});
	}

	/**
	 * Update customer details display
	 */
	private void updateCustomerDetailsDisplay() {
		customerDetailsPanel.removeAll();

		if (selectedCustomer == null) {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.CENTER;

			JLabel noCustomerLabel = new JLabel("No customer selected");
			noCustomerLabel.setFont(new Font("Arial", Font.ITALIC, 16));
			noCustomerLabel.setForeground(new Color(150, 150, 150));
			customerDetailsPanel.add(noCustomerLabel, gbc);
		} else {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(8, 10, 8, 10);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;

			// Customer Name
			JLabel nameTitle = new JLabel("Customer Name:");
			nameTitle.setFont(new Font("Arial", Font.BOLD, 13));
			nameTitle.setForeground(TEXT_DARK);
			customerDetailsPanel.add(nameTitle, gbc);

			gbc.gridy++;
			customerNameLabel = new JLabel(selectedCustomer.name);
			customerNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
			customerNameLabel.setForeground(TEXT_DARK);
			customerDetailsPanel.add(customerNameLabel, gbc);

			// Customer Address
			gbc.gridy++;
			gbc.insets = new Insets(15, 10, 8, 10);
			JLabel addressTitle = new JLabel("Address:");
			addressTitle.setFont(new Font("Arial", Font.BOLD, 13));
			addressTitle.setForeground(TEXT_DARK);
			customerDetailsPanel.add(addressTitle, gbc);

			gbc.gridy++;
			gbc.insets = new Insets(8, 10, 8, 10);
			customerAddressLabel = new JLabel("<html>" + selectedCustomer.address + "</html>");
			customerAddressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
			customerAddressLabel.setForeground(TEXT_DARK);
			customerDetailsPanel.add(customerAddressLabel, gbc);

			// Customer Contact
			gbc.gridy++;
			gbc.insets = new Insets(15, 10, 8, 10);
			JLabel contactTitle = new JLabel("Contact:");
			contactTitle.setFont(new Font("Arial", Font.BOLD, 13));
			contactTitle.setForeground(TEXT_DARK);
			customerDetailsPanel.add(contactTitle, gbc);

			gbc.gridy++;
			gbc.insets = new Insets(8, 10, 8, 10);
			customerContactLabel = new JLabel(selectedCustomer.contact);
			customerContactLabel.setFont(new Font("Arial", Font.PLAIN, 14));
			customerContactLabel.setForeground(TEXT_DARK);
			customerDetailsPanel.add(customerContactLabel, gbc);
		}

		customerDetailsPanel.revalidate();
		customerDetailsPanel.repaint();
	}

	/**
	 * Handle add branch button click
	 */
	private void handleAddBranch() {
		// Get list of already added branch addresses to filter them out
		List<String> alreadyAddedAddresses = new ArrayList<>();
		for (BranchData branch : branches) {
			alreadyAddedAddresses.add(branch.branchAddress);
		}

		// Show the SelectBranchDialog
		SelectBranchDialog.show(this, alreadyAddedAddresses, selectedBranches -> {
			// Add all selected branches with product count of 0
			for (SelectBranchDialog.Branch branch : selectedBranches) {
				BranchData newBranch = new BranchData(branch.address, 0);
				branches.add(newBranch);
			}
			updateBranchTable();
		});
	}

	/**
	 * Update branch table
	 */
	private void updateBranchTable() {
		branchTableModel.setRowCount(0);

		for (BranchData branch : branches) {
			branchTableModel.addRow(new Object[] { branch.branchAddress, branch.productTypeCount, "⚙ Actions" });
		}
	}

	/**
	 * Show action menu for branch
	 */
	private void showActionMenu(Component parent, BranchData branch, int row) {
		JDialog actionDialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Actions");
		actionDialog.setLayout(new GridBagLayout());
		actionDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 20, 5, 20);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Actions for Branch");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		actionDialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(15, 20, 5, 20);

		JButton viewProductsBtn = createActionButton("➕ Product Selection", new Color(70, 130, 180));
		viewProductsBtn.addActionListener(e -> {
			actionDialog.dispose();
			ViewBranchProductsDialog.show(SwingUtilities.getWindowAncestor(parent), branch.branchAddress, () -> {
				// Callback when products are changed
				// Update the product count in the table
				System.out.println("Products updated for branch: " + branch.branchAddress);
			});
		});
		actionDialog.add(viewProductsBtn, gbc);

		gbc.gridy++;
		JButton removeBtn = createActionButton("🗑️ Remove Branch", new Color(180, 50, 50));
		removeBtn.addActionListener(e -> {
			actionDialog.dispose();
			removeBranch(row);
		});
		actionDialog.add(removeBtn, gbc);

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
	 * Remove branch from list
	 */
	private void removeBranch(int row) {
		if (row >= 0 && row < branches.size()) {
			branches.remove(row);
			updateBranchTable();
		}
	}

	/**
	 * Create button panel
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(20, 0, 0, 0));

		cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 42));
		cancelBtn.addActionListener(e -> dispose());

		saveBtn = createStyledButton("Save", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(120, 42));
		saveBtn.addActionListener(e -> handleSave());

		panel.add(cancelBtn);
		panel.add(saveBtn);

		return panel;
	}

	/**
	 * Handle save button click
	 */
	private void handleSave() {
		// Validation
		if (selectedCustomer == null) {
			ToastNotification.showError(this, "Please select a customer.");
			return;
		}

		if (branches.isEmpty()) {
			ToastNotification.showError(this, "Please add at least one branch.");
			return;
		}

		// Check if all branches have at least one product
		for (int i = 0; i < branches.size(); i++) {
			if (branches.get(i).productTypeCount == 0) {
				ToastNotification.showError(this,
						"Branch '" + branches.get(i).branchAddress + "' has no products, please add products.");
				return;
			}
		}

		// If validation passes, save data and close
		System.out.println("=== Saving Customer & Branches Data ===");
		System.out.println("Customer: " + selectedCustomer.name);
		System.out.println("Total Branches: " + branches.size());
		for (int i = 0; i < branches.size(); i++) {
			BranchData branch = branches.get(i);
			System.out.println("  Branch " + (i + 1) + ": " + branch.branchAddress + " (Product Types: "
					+ branch.productTypeCount + ")");
		}

		if (onSuccessCallback != null) {
			onSuccessCallback.run();
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
	 * Create action button
	 */
	private JButton createActionButton(String text, Color bgColor) {
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
	public static void show(Window parent, Runnable onSuccess) {
		AddCustomerBranchDialog dialog = new AddCustomerBranchDialog(parent, onSuccess);
		dialog.setVisible(true);
	}

	/**
	 * Get the selected customer and branches data
	 */
	public CustomerBranchResult getResult() {
		if (selectedCustomer == null) {
			return null;
		}
		return new CustomerBranchResult(selectedCustomer, branches);
	}

	/**
	 * Result class to return data
	 */
	public static class CustomerBranchResult {
		public final Customer customer;
		public final List<BranchData> branches;

		public CustomerBranchResult(Customer customer, List<BranchData> branches) {
			this.customer = customer;
			this.branches = new ArrayList<>(branches);
		}
	}
}