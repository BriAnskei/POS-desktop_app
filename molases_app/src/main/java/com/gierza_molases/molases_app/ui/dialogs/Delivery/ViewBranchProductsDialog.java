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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import com.gierza_molases.molases_app.model.ProductWithQuantity;

public class ViewBranchProductsDialog extends JDialog {

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
	private JTable productTable;
	private DefaultTableModel productTableModel;
	private List<ProductWithQuantity> products = new ArrayList<>();
	private JButton addProductBtn;
	private JButton closeBtn;
	private JLabel branchAddressLabel;
	private JLabel totalProductsLabel;

	// Data
	private String branchAddress;
	private Consumer<List<ProductWithQuantity>> onProductsChangedCallback;

	// Controller
	private final NewDeliveryController newDeliveryController;
	private final int selectedCustomerId;

	/**
	 * Constructor
	 */
	public ViewBranchProductsDialog(Window parent, String branchAddress, List<ProductWithQuantity> existingProducts,
			Consumer<List<ProductWithQuantity>> onProductsChanged, NewDeliveryController newDeliveryController,
			int selectedCustomerId) {
		super(parent, "Branch Products", ModalityType.APPLICATION_MODAL);
		this.branchAddress = branchAddress;
		this.onProductsChangedCallback = onProductsChanged;
		this.newDeliveryController = newDeliveryController;
		this.selectedCustomerId = selectedCustomerId;

		// Load existing products
		if (existingProducts != null && !existingProducts.isEmpty()) {
			this.products = new ArrayList<>(existingProducts);
		}

		initializeUI();
		updateProductTable();
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

		// Header Section
		mainContent.add(createHeaderSection(), BorderLayout.NORTH);

		// Table Section
		mainContent.add(createTableSection(), BorderLayout.CENTER);

		// Button Panel
		mainContent.add(createButtonPanel(), BorderLayout.SOUTH);

		add(mainContent);

		// Dialog settings
		setSize(900, 600);
		setMinimumSize(new Dimension(900, 600));
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Create header section
	 */
	private JPanel createHeaderSection() {
		JPanel headerPanel = new JPanel(new BorderLayout(0, 15));
		headerPanel.setBackground(Color.WHITE);

		// Title
		JLabel titleLabel = new JLabel("Products in Branch");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(TEXT_DARK);
		headerPanel.add(titleLabel, BorderLayout.NORTH);

		// Branch Info Panel
		JPanel infoPanel = new JPanel(new GridBagLayout());
		infoPanel.setBackground(CONTENT_BG);
		infoPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(220, 210, 200), 1), new EmptyBorder(15, 15, 15, 15)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);

		JLabel addressTitle = new JLabel("Branch Address:");
		addressTitle.setFont(new Font("Arial", Font.BOLD, 14));
		addressTitle.setForeground(TEXT_DARK);
		infoPanel.add(addressTitle, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(5, 15, 5, 5);
		branchAddressLabel = new JLabel(branchAddress);
		branchAddressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		branchAddressLabel.setForeground(TEXT_DARK);
		infoPanel.add(branchAddressLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		JLabel totalTitle = new JLabel("Total Products:");
		totalTitle.setFont(new Font("Arial", Font.BOLD, 14));
		totalTitle.setForeground(TEXT_DARK);
		infoPanel.add(totalTitle, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(5, 15, 5, 5);
		totalProductsLabel = new JLabel("0");
		totalProductsLabel.setFont(new Font("Arial", Font.BOLD, 14));
		totalProductsLabel.setForeground(ACCENT_GOLD);
		infoPanel.add(totalProductsLabel, gbc);

		headerPanel.add(infoPanel, BorderLayout.CENTER);

		return headerPanel;
	}

	/**
	 * Create table section
	 */
	private JPanel createTableSection() {
		JPanel tableSection = new JPanel(new BorderLayout(0, 10));
		tableSection.setBackground(Color.WHITE);

		// Top panel with Add Product button
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		topPanel.setBackground(Color.WHITE);

		addProductBtn = createStyledButton("+ Add Product", ACCENT_GOLD);
		addProductBtn.setPreferredSize(new Dimension(150, 35));
		addProductBtn.addActionListener(e -> handleAddProduct());
		topPanel.add(addProductBtn);

		tableSection.add(topPanel, BorderLayout.NORTH);

		// Create table
		createProductTable();
		JScrollPane scrollPane = new JScrollPane(productTable);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));

		tableSection.add(scrollPane, BorderLayout.CENTER);

		return tableSection;
	}

	/**
	 * Create and configure product table
	 */
	private void createProductTable() {
		String[] columns = { "Product Name", "Selling Price", "Quantity", "Total Price", "Actions" };
		productTableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		productTable = new JTable(productTableModel);
		productTable.setFont(new Font("Arial", Font.PLAIN, 14));
		productTable.setRowHeight(50);
		productTable.setShowGrid(true);
		productTable.setGridColor(new Color(220, 210, 200));
		productTable.setBackground(TABLE_ROW_EVEN);
		productTable.setSelectionBackground(new Color(255, 235, 205));
		productTable.setSelectionForeground(TEXT_DARK);

		// Set column widths
		productTable.getColumnModel().getColumn(0).setPreferredWidth(300);
		productTable.getColumnModel().getColumn(1).setPreferredWidth(120);
		productTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		productTable.getColumnModel().getColumn(3).setPreferredWidth(120);
		productTable.getColumnModel().getColumn(4).setPreferredWidth(120);

		// Style table header
		JTableHeader header = productTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(TABLE_HEADER);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setBackground(TABLE_HEADER);
		headerRenderer.setForeground(TEXT_LIGHT);
		headerRenderer.setFont(new Font("Arial", Font.BOLD, 14));
		headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		for (int i = 0; i < productTable.getColumnCount(); i++) {
			productTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}

		// Alternating row colors and alignment
		productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				// Column 0: Product Name - Left aligned
				// Column 1, 2, 3: Prices and quantity - Right aligned
				// Column 4: Actions - Center aligned
				if (column == 4) {
					setHorizontalAlignment(SwingConstants.CENTER);
				} else if (column >= 1 && column <= 3) {
					setHorizontalAlignment(SwingConstants.RIGHT);
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

				if (col == 4 && row >= 0 && row < products.size()) {
					ProductWithQuantity product = products.get(row);
					showProductActionMenu(productTable, product, row);
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
	 * Update product table
	 */
	private void updateProductTable() {
		productTableModel.setRowCount(0);

		for (ProductWithQuantity product : products) {
			productTableModel.addRow(new Object[] { product.getProduct().getName(),
					String.format("₱%.2f", product.getProduct().getSellingPrice()), product.getQuantity(),
					String.format("₱%.2f", product.getTotalSellingPrice()), "⚙ Actions" });
		}

		// Update total products label
		totalProductsLabel.setText(String.valueOf(products.size()));
	}

	/**
	 * Handle add product button click
	 */
	private void handleAddProduct() {
		SelectProductDialog.show(this, result -> {
			// Check if product already exists
			for (ProductWithQuantity existingProduct : products) {
				if (existingProduct.getProduct().getId() == result.product.getId()) {
					JOptionPane.showMessageDialog(this,
							"This product is already added to this branch.\n"
									+ "Use 'Edit Quantity' to modify the quantity.",
							"Product Already Exists", JOptionPane.WARNING_MESSAGE);
					return;
				}
			}

			// Add new product with quantity
			ProductWithQuantity newProduct = new ProductWithQuantity(result.product, result.quantity);
			products.add(newProduct);
			updateProductTable();

			JOptionPane.showMessageDialog(this, "Product added successfully!", "Success",
					JOptionPane.INFORMATION_MESSAGE);
		}, this.newDeliveryController, this.selectedCustomerId);
	}

	/**
	 * Show action menu for product
	 */
	private void showProductActionMenu(Component parent, ProductWithQuantity product, int row) {
		JDialog actionDialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Actions");
		actionDialog.setLayout(new GridBagLayout());
		actionDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 20, 5, 20);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Actions for Product");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(TEXT_DARK);
		actionDialog.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 20, 5, 20);
		JLabel productNameLabel = new JLabel("<html>" + product.getProduct().getName() + "</html>");
		productNameLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		productNameLabel.setForeground(new Color(100, 100, 100));
		actionDialog.add(productNameLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(15, 20, 5, 20);

		JButton editQuantityBtn = createActionButton("✏️ Edit Quantity", ACCENT_GOLD);
		editQuantityBtn.addActionListener(e -> {
			actionDialog.dispose();
			handleEditQuantity(product, row);
		});
		actionDialog.add(editQuantityBtn, gbc);

		gbc.gridy++;
		JButton removeBtn = createActionButton("🗑️ Remove Product", new Color(180, 50, 50));
		removeBtn.addActionListener(e -> {
			actionDialog.dispose();
			handleRemoveProduct(row);
		});
		actionDialog.add(removeBtn, gbc);

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
	 * Handle edit quantity
	 */
	private void handleEditQuantity(ProductWithQuantity product, int row) {
		String input = JOptionPane.showInputDialog(this,
				"Enter new quantity for " + product.getProduct().getName() + ":", product.getQuantity());

		if (input != null && !input.trim().isEmpty()) {
			try {
				int newQuantity = Integer.parseInt(input.trim());
				if (newQuantity <= 0) {
					JOptionPane.showMessageDialog(this, "Quantity must be greater than 0", "Invalid Quantity",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				product.setQuantity(newQuantity);
				updateProductTable();

			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Please enter a valid number", "Invalid Input",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Handle remove product
	 */
	private void handleRemoveProduct(int row) {
		int confirm = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to remove this product from the branch?", "Confirm Remove",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (confirm == JOptionPane.YES_OPTION) {
			if (row >= 0 && row < products.size()) {
				products.remove(row);
				updateProductTable();
			}
		}
	}

	/**
	 * Create button panel
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(20, 0, 0, 0));

		closeBtn = createStyledButton("Save & Close", ACCENT_GOLD);
		closeBtn.setPreferredSize(new Dimension(140, 42));
		closeBtn.addActionListener(e -> handleSaveAndClose());

		panel.add(closeBtn);

		return panel;
	}

	/**
	 * Handle save and close
	 */
	private void handleSaveAndClose() {
		// Notify parent with updated products list
		if (onProductsChangedCallback != null) {
			onProductsChangedCallback.accept(new ArrayList<>(products));
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
	 * Get current products data
	 */
	public List<ProductWithQuantity> getProducts() {
		return new ArrayList<>(products);
	}

	/**
	 * Show the dialog
	 */
	public static void show(Window parent, String branchAddress, List<ProductWithQuantity> existingProducts,
			Consumer<List<ProductWithQuantity>> onProductsChanged, NewDeliveryController newDeliveryController,
			int selectedCustomerId) {
		ViewBranchProductsDialog dialog = new ViewBranchProductsDialog(parent, branchAddress, existingProducts,
				onProductsChanged, newDeliveryController, selectedCustomerId);
		dialog.setVisible(true);
	}
}