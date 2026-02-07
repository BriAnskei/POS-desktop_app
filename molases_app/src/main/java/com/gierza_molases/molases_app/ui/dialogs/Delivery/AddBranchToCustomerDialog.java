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

import com.gierza_molases.molases_app.UiController.NewDeliveryController;
import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.ui.components.ToastNotification;

public class AddBranchToCustomerDialog extends JDialog {

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);
	private static final Color CONTENT_BG = new Color(250, 247, 242);

	// UI Components
	private JPanel branchDetailsPanel;
	private JLabel branchAddressLabel;
	private JButton selectBranchBtn;
	private JButton addProductBtn;
	private JTable productTable;
	private DefaultTableModel productTableModel;

	// Data
	private Customer customer;
	private Branch selectedBranch = null;
	private List<ProductWithQuantity> selectedProducts = new ArrayList<>();
	private List<String> alreadyAddedBranchAddresses;

	// Buttons
	private JButton saveBtn;
	private JButton cancelBtn;

	// Callback
	private Runnable onSuccessCallback;

	// Controller
	private NewDeliveryController newDeliveryController = AppContext.newDeliveryController;

	/**
	 * Constructor
	 */
	public AddBranchToCustomerDialog(Window parent, Customer customer, List<String> alreadyAddedBranchAddresses,
			Runnable onSuccess) {
		super(parent, "Add Branch to Customer", ModalityType.APPLICATION_MODAL);
		this.customer = customer;
		this.alreadyAddedBranchAddresses = alreadyAddedBranchAddresses;
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
		JPanel leftPanel = createBranchSelectionSection();
		JPanel rightPanel = createProductsSection();

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
		setSize(1000, 550);
		setMinimumSize(new Dimension(1000, 550));
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Create branch selection section (LEFT SIDE)
	 */
	private JPanel createBranchSelectionSection() {
		JPanel section = new JPanel(new BorderLayout(0, 15));
		section.setBackground(Color.WHITE);
		section.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
								"Branch Selection", javax.swing.border.TitledBorder.LEFT,
								javax.swing.border.TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), TEXT_DARK),
						new EmptyBorder(15, 15, 15, 15)));
		section.setPreferredSize(new Dimension(420, 0));
		section.setMinimumSize(new Dimension(420, 0));

		// Branch details display panel
		branchDetailsPanel = new JPanel(new GridBagLayout());
		branchDetailsPanel.setBackground(CONTENT_BG);
		branchDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(220, 210, 200), 1), new EmptyBorder(20, 20, 20, 20)));
		branchDetailsPanel.setPreferredSize(new Dimension(0, 250));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 5, 5, 5);

		// Initial "No branch selected" message
		JLabel noBranchLabel = new JLabel("No branch selected");
		noBranchLabel.setFont(new Font("Arial", Font.ITALIC, 16));
		noBranchLabel.setForeground(new Color(150, 150, 150));
		branchDetailsPanel.add(noBranchLabel, gbc);

		section.add(branchDetailsPanel, BorderLayout.CENTER);

		// Select Branch Button
		selectBranchBtn = createStyledButton("Select Branch", ACCENT_GOLD);
		selectBranchBtn.setPreferredSize(new Dimension(180, 40));
		selectBranchBtn.addActionListener(e -> handleSelectBranch());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBackground(Color.WHITE);
		buttonPanel.add(selectBranchBtn);

		section.add(buttonPanel, BorderLayout.SOUTH);

		return section;
	}

	/**
	 * Create products section (RIGHT SIDE)
	 */
	private JPanel createProductsSection() {
		JPanel section = new JPanel(new BorderLayout(0, 15));
		section.setBackground(Color.WHITE);
		section.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
						"Products", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
						new Font("Arial", Font.BOLD, 16), TEXT_DARK),
				new EmptyBorder(15, 15, 15, 15)));
		section.setPreferredSize(new Dimension(480, 0));
		section.setMinimumSize(new Dimension(480, 0));

		// Add Product Button
		addProductBtn = createStyledButton("+ Add Product", ACCENT_GOLD);
		addProductBtn.setPreferredSize(new Dimension(150, 35));
		addProductBtn.setEnabled(false); // Disabled until branch is selected
		addProductBtn.addActionListener(e -> handleAddProduct());

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		topPanel.setBackground(Color.WHITE);
		topPanel.add(addProductBtn);

		section.add(topPanel, BorderLayout.NORTH);

		// Product Table
		createProductTable();
		JScrollPane scrollPane = new JScrollPane(productTable);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));

		section.add(scrollPane, BorderLayout.CENTER);

		return section;
	}

	/**
	 * Create and configure product table
	 */
	private void createProductTable() {
		String[] columns = { "Product Name", "Quantity", "Unit Price", "Total", "Actions" };
		productTableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		productTable = new JTable(productTableModel);
		productTable.setFont(new Font("Arial", Font.PLAIN, 13));
		productTable.setRowHeight(45);
		productTable.setShowGrid(true);
		productTable.setGridColor(new Color(220, 210, 200));
		productTable.setBackground(TABLE_ROW_EVEN);
		productTable.setSelectionBackground(new Color(255, 235, 205));
		productTable.setSelectionForeground(TEXT_DARK);

		// Set column widths
		productTable.getColumnModel().getColumn(0).setPreferredWidth(180);
		productTable.getColumnModel().getColumn(1).setPreferredWidth(80);
		productTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		productTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		productTable.getColumnModel().getColumn(4).setPreferredWidth(80);

		// Style table header
		JTableHeader header = productTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 13));
		header.setBackground(TABLE_HEADER);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setBackground(TABLE_HEADER);
		headerRenderer.setForeground(TEXT_LIGHT);
		headerRenderer.setFont(new Font("Arial", Font.BOLD, 13));
		headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		for (int i = 0; i < productTable.getColumnCount(); i++) {
			productTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}

		// Alternating row colors
		productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				if (column == 1 || column == 2 || column == 3) {
					setHorizontalAlignment(SwingConstants.CENTER);
				} else if (column == 4) {
					setHorizontalAlignment(SwingConstants.CENTER);
				} else {
					setHorizontalAlignment(SwingConstants.LEFT);
				}

				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		});

		// Mouse listener for actions column
		productTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = productTable.rowAtPoint(e.getPoint());
				int col = productTable.columnAtPoint(e.getPoint());

				if (col == 4 && row >= 0 && row < selectedProducts.size()) {
					showProductActionMenu(row);
				}
			}
		});

		productTable.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int col = productTable.columnAtPoint(e.getPoint());
				productTable.setCursor(new Cursor(col == 4 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
			}
		});
	}

	/**
	 * Handle select branch button click
	 */
	private void handleSelectBranch() {
		SelectBranchDialog.show(this, alreadyAddedBranchAddresses, selectedBranches -> {
			if (!selectedBranches.isEmpty()) {
				selectedBranch = selectedBranches.get(0); // Only take the first one
				updateBranchDetailsDisplay();

				// Clear products when changing branch
				selectedProducts.clear();
				updateProductTable();

				// Enable add product button
				addProductBtn.setEnabled(true);
			}
		}, customer.getId(), newDeliveryController, false); // false = single selection only
	}

	/**
	 * Update branch details display
	 */
	private void updateBranchDetailsDisplay() {
		branchDetailsPanel.removeAll();

		if (selectedBranch == null) {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.CENTER;

			JLabel noBranchLabel = new JLabel("No branch selected");
			noBranchLabel.setFont(new Font("Arial", Font.ITALIC, 16));
			noBranchLabel.setForeground(new Color(150, 150, 150));
			branchDetailsPanel.add(noBranchLabel, gbc);
		} else {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(8, 10, 8, 10);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;

			// Branch Address Title
			JLabel addressTitle = new JLabel("Branch Address:");
			addressTitle.setFont(new Font("Arial", Font.BOLD, 13));
			addressTitle.setForeground(TEXT_DARK);
			branchDetailsPanel.add(addressTitle, gbc);

			gbc.gridy++;
			branchAddressLabel = new JLabel("<html>" + selectedBranch.getAddress() + "</html>");
			branchAddressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
			branchAddressLabel.setForeground(TEXT_DARK);
			branchDetailsPanel.add(branchAddressLabel, gbc);

			// Customer Info (read-only)
			gbc.gridy++;
			gbc.insets = new Insets(15, 10, 8, 10);
			JLabel customerTitle = new JLabel("Customer:");
			customerTitle.setFont(new Font("Arial", Font.BOLD, 13));
			customerTitle.setForeground(TEXT_DARK);
			branchDetailsPanel.add(customerTitle, gbc);

			gbc.gridy++;
			gbc.insets = new Insets(8, 10, 8, 10);
			JLabel customerLabel = new JLabel(customer.getDisplayName());
			customerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
			customerLabel.setForeground(TEXT_DARK);
			branchDetailsPanel.add(customerLabel, gbc);
		}

		branchDetailsPanel.revalidate();
		branchDetailsPanel.repaint();
	}

	/**
	 * Handle add product button click
	 */
	private void handleAddProduct() {
		SelectProductDialog.show(this, result -> {
			// Check if product already exists
			for (ProductWithQuantity existing : selectedProducts) {
				if (existing.getProduct().getId() == result.product.getId()) {
					ToastNotification.showWarning(this, "Product already added. Use Edit to change quantity.");
					return;
				}
			}

			// Add new product
			ProductWithQuantity newProduct = new ProductWithQuantity(result.product, result.quantity);
			selectedProducts.add(newProduct);
			updateProductTable();

			ToastNotification.showSuccess(this, "Product added successfully!");
		}, newDeliveryController, customer.getId());
	}

	/**
	 * Update product table
	 */
	private void updateProductTable() {
		productTableModel.setRowCount(0);

		for (ProductWithQuantity product : selectedProducts) {
			productTableModel.addRow(new Object[] { product.getProduct().getName(), product.getQuantity(),
					String.format("₱%.2f", product.getProduct().getSellingPrice()),
					String.format("₱%.2f", product.getTotalSellingPrice()), "⚙ Actions" });
		}
	}

	/**
	 * Show product action menu
	 */
	private void showProductActionMenu(int rowIndex) {
		ProductWithQuantity product = selectedProducts.get(rowIndex);

		JDialog actionDialog = new JDialog(this, "Product Actions");
		actionDialog.setLayout(new GridBagLayout());
		actionDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 20, 5, 20);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Choose Action");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		actionDialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(15, 20, 5, 20);

		JButton editBtn = createActionButton("✏️ Edit Quantity", ACCENT_GOLD);
		editBtn.addActionListener(e -> {
			actionDialog.dispose();
			handleEditQuantity(product);
		});
		actionDialog.add(editBtn, gbc);

		gbc.gridy++;
		JButton removeBtn = createActionButton("🗑️ Remove Product", new Color(180, 50, 50));
		removeBtn.addActionListener(e -> {
			actionDialog.dispose();
			selectedProducts.remove(product);
			updateProductTable();
			ToastNotification.showSuccess(this, "Product removed successfully!");
		});
		actionDialog.add(removeBtn, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(10, 20, 15, 20);
		JButton cancelBtn = createActionButton("Cancel", new Color(120, 120, 120));
		cancelBtn.addActionListener(e -> actionDialog.dispose());
		actionDialog.add(cancelBtn, gbc);

		actionDialog.pack();
		actionDialog.setMinimumSize(new Dimension(320, 220));
		actionDialog.setLocationRelativeTo(null);
		actionDialog.setVisible(true);
	}

	/**
	 * Handle edit quantity
	 */
	private void handleEditQuantity(ProductWithQuantity product) {
		EditQuantityDialog.show(this, product, newQuantity -> {
			product.setQuantity(newQuantity);
			updateProductTable();
			ToastNotification.showSuccess(this, "Quantity updated successfully!");
		});
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

		saveBtn = createStyledButton("Add Branch", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(140, 42));
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
		if (selectedBranch == null) {
			ToastNotification.showError(this, "Please select a branch.");
			return;
		}

		if (selectedProducts.isEmpty()) {
			ToastNotification.showError(this, "Please add at least one product.");
			return;
		}

		// Save via controller
		AppContext.deliveryDetialsController.addBranchToCustomer(customer, selectedBranch, selectedProducts, () -> {
			SwingUtilities.invokeLater(() -> {
				ToastNotification.showSuccess(this, "Branch added successfully!");

				// Call success callback
				if (onSuccessCallback != null) {
					onSuccessCallback.run();
				}

				dispose();
			});
		}, error -> {
			SwingUtilities.invokeLater(() -> {
				ToastNotification.showError(this, "Failed to add branch: " + error);
			});
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
	public static void show(Window parent, Customer customer, List<String> alreadyAddedBranchAddresses,
			Runnable onSuccess) {
		AddBranchToCustomerDialog dialog = new AddBranchToCustomerDialog(parent, customer, alreadyAddedBranchAddresses,
				onSuccess);
		dialog.setVisible(true);
	}
}