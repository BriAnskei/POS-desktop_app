package molases_appcom.gierza_molases.molases_app.ui.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.Context.AppContext;
import com.gierza_molases.molases_app.Context.ProductState;
import com.gierza_molases.molases_app.UiController.ProductsController;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;
import com.gierza_molases.molases_app.ui.components.ToastNotification;

public class ProductsPage {

	// Color Palette
	private static final Color SIDEBAR_BG = new Color(62, 39, 35);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color CARD_BG = Color.WHITE;
	private static final Color PROFIT_GREEN = new Color(34, 139, 34);
	private static final Color DELETE_RED = new Color(180, 50, 50);
	private static final Color CUSTOMER_BADGE_BG = new Color(230, 245, 255);
	private static final Color CUSTOMER_TEXT = new Color(60, 120, 180);

	// Controller reference
	private static final ProductsController productsController = AppContext.productsController;

	// UI component references
	private static JPanel cardsContainer;
	private static JPanel mainPanelRef;
	private static JTextField searchField;
	private static JComboBox<String> sortCombo;
	private static JPanel loadingOverlay;
	private static LoadingSpinner spinner;
	private static JLabel loadingLabel;

	/**
	 * Create the Products Page panel
	 */
	public static JPanel createPanel() {
		productsController.resetState();

		JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		mainPanelRef = mainPanel;

		// Top Section
		JPanel topSection = createTopSection();
		mainPanel.add(topSection, BorderLayout.NORTH);

		// Center Section with loading overlay
		JPanel cardsWrapper = new JPanel();
		cardsWrapper.setLayout(new OverlayLayout(cardsWrapper));
		cardsWrapper.setBackground(CONTENT_BG);

		JPanel overlay = createLoadingOverlay();
		cardsWrapper.add(overlay);

		JScrollPane scrollPane = createProductCardsSection();
		cardsWrapper.add(scrollPane);

		mainPanel.add(cardsWrapper, BorderLayout.CENTER);

		// Initial load
		loadProductsWithUI();

		return mainPanel;
	}

	/*
	 * ========================= CONTROLLER INTEGRATION =========================
	 */

	/**
	 * Load products with UI feedback - wrapper for controller.loadProducts
	 */
	private static void loadProductsWithUI() {
		showLoading("Loading products...");

		productsController.loadProducts(
				// onDone
				() -> {
					hideLoading();
					updateCardsFromState();
				},
				// onError
				(errorMsg) -> {
					hideLoading();
					showErrorInCards("Failed to load products: " + errorMsg);
				});
	}

	/**
	 * Perform search using controller
	 */
	private static void performSearch() {
		String searchText = searchField.getText().trim();
		showLoading("Searching...");

		productsController.search(searchText,
				// onDone
				() -> {
					hideLoading();
					updateCardsFromState();
				},
				// onError
				(errorMsg) -> {
					hideLoading();
					ToastNotification.showError(getParentWindow(), "Search failed: " + errorMsg);
				});
	}

	/**
	 * Change sort order using controller
	 */
	private static void handleSortChange(String newSortOrder) {
		ProductState state = productsController.getState();

		if (newSortOrder.equals(state.getSortOrder())) {
			return; // No change needed
		}

		showLoading("Sorting...");

		productsController.changeSortOrder(newSortOrder,
				// onDone
				() -> {
					hideLoading();
					updateCardsFromState();
				},
				// onError
				(errorMsg) -> {
					hideLoading();
					ToastNotification.showError(getParentWindow(), "Failed to sort: " + errorMsg);
				});
	}

	/**
	 * Clear filters using controller
	 */
	private static void clearFilters() {
		searchField.setText("");
		sortCombo.setSelectedIndex(0);
		showLoading("Clearing filters...");

		productsController.clearFilters(
				// onDone
				() -> {
					hideLoading();
					updateCardsFromState();
				},
				// onError
				(errorMsg) -> {
					hideLoading();
					ToastNotification.showError(getParentWindow(), "Failed to clear filters: " + errorMsg);
				});
	}

	/**
	 * Delete product using controller
	 */
	private static void deleteProductWithConfirmation(Product product, JButton deleteBtn, JButton cancelBtn,
			JDialog dialog) {
		deleteBtn.setEnabled(false);
		cancelBtn.setEnabled(false);
		deleteBtn.setText("Deleting...");

		productsController.deleteProduct(product.getId(),
				// onSuccess
				() -> {
					dialog.dispose();
					ToastNotification.showSuccess(getParentWindow(), "Product deleted successfully!");
					updateCardsFromState();
				},
				// onError
				(errorMsg) -> {
					deleteBtn.setEnabled(true);
					cancelBtn.setEnabled(true);
					deleteBtn.setText("Delete");
					ToastNotification.showError(getParentWindow(), "Failed to delete product: " + errorMsg);
				});
	}

	/**
	 * Update cards display from current state
	 */
	private static void updateCardsFromState() {
		if (cardsContainer == null)
			return;

		cardsContainer.removeAll();

		ProductState state = productsController.getState();
		List<Product> products = state.getProducts();

		if (products == null || products.isEmpty()) {
			String message = (state.getSearch() == null || state.getSearch().isEmpty()) ? "No products available"
					: "No products found for \"" + state.getSearch() + "\"";
			showEmptyState(message);
		} else {
			cardsContainer.setLayout(new GridLayout(0, 3, 20, 20));
			for (Product product : products) {
				cardsContainer.add(createProductCard(product));
			}
		}

		cardsContainer.revalidate();
		cardsContainer.repaint();
	}

	/*
	 * ========================= UI COMPONENTS =========================
	 */

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

		spinner = new LoadingSpinner(50, new Color(139, 90, 43));
		spinner.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		centerPanel.add(spinner);

		centerPanel.add(Box.createVerticalStrut(15));

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
	private static void showLoading(String message) {
		if (loadingOverlay != null) {
			if (loadingLabel != null) {
				loadingLabel.setText(message);
			}
			if (spinner != null) {
				spinner.start();
			}
			loadingOverlay.setVisible(true);

			setControlsEnabled(false);
		}
	}

	/**
	 * Hide loading indicator
	 */
	private static void hideLoading() {
		if (loadingOverlay != null) {
			if (spinner != null) {
				spinner.stop();
			}
			loadingOverlay.setVisible(false);

			setControlsEnabled(true);
		}
	}

	/**
	 * Enable/disable UI controls
	 */
	private static void setControlsEnabled(boolean enabled) {
		if (searchField != null)
			searchField.setEnabled(enabled);
		if (sortCombo != null)
			sortCombo.setEnabled(enabled);
	}

	/**
	 * Create top section with filters
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

		JLabel titleLabel = new JLabel("Product Management");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
		titleLabel.setForeground(TEXT_DARK);
		titleRow.add(titleLabel, BorderLayout.WEST);

		JButton addButton = createStyledButton("+ Add New", ACCENT_GOLD);
		addButton.addActionListener(e -> showAddProductDialog());
		titleRow.add(addButton, BorderLayout.EAST);

		topPanel.add(titleRow);
		topPanel.add(Box.createVerticalStrut(15));

		// Filters row
		topPanel.add(createFiltersRow());

		return topPanel;
	}

	/**
	 * Create filters row
	 */
	private static JPanel createFiltersRow() {
		JPanel filtersRow = new JPanel();
		filtersRow.setLayout(new BoxLayout(filtersRow, BoxLayout.X_AXIS));
		filtersRow.setBackground(CONTENT_BG);
		filtersRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		ProductState state = productsController.getState();

		// Search field
		searchField = new JTextField(20);
		searchField.setFont(new Font("Arial", Font.PLAIN, 14));
		searchField.setPreferredSize(new Dimension(300, 38));
		searchField.setMaximumSize(new Dimension(300, 38));
		searchField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));
		searchField.setText(state.getSearch() != null ? state.getSearch() : "");
		searchField.addActionListener(e -> performSearch());
		filtersRow.add(searchField);

		filtersRow.add(Box.createHorizontalStrut(10));

		// Search button
		JButton searchButton = createStyledButton("Search", SIDEBAR_ACTIVE);
		searchButton.addActionListener(e -> performSearch());
		filtersRow.add(searchButton);

		filtersRow.add(Box.createHorizontalStrut(15));

		// Sort controls
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
			handleSortChange(newSortOrder);
		});
		filtersRow.add(sortCombo);

		filtersRow.add(Box.createHorizontalGlue());

		// Clear filters button
		JButton clearFiltersButton = createStyledButton("Clear Filters", SIDEBAR_ACTIVE);
		clearFiltersButton.addActionListener(e -> clearFilters());
		filtersRow.add(clearFiltersButton);

		return filtersRow;
	}

	/**
	 * Create product cards section
	 */
	private static JScrollPane createProductCardsSection() {
		cardsContainer = new JPanel();
		cardsContainer.setBackground(CONTENT_BG);
		cardsContainer.setBorder(new EmptyBorder(10, 0, 10, 0));
		cardsContainer.setLayout(new GridLayout(0, 3, 20, 20));

		JScrollPane scrollPane = new JScrollPane(cardsContainer);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getViewport().setBackground(CONTENT_BG);

		return scrollPane;
	}

	/**
	 * Show empty state
	 */
	private static void showEmptyState(String message) {
		cardsContainer.setLayout(new BorderLayout());

		JPanel emptyPanel = new JPanel(new GridBagLayout());
		emptyPanel.setBackground(CONTENT_BG);

		JLabel emptyLabel = new JLabel(message);
		emptyLabel.setFont(new Font("Arial", Font.PLAIN, 18));
		emptyLabel.setForeground(new Color(120, 120, 120));

		emptyPanel.add(emptyLabel);
		cardsContainer.add(emptyPanel, BorderLayout.CENTER);
	}

	/**
	 * Show error state in cards
	 */
	private static void showErrorInCards(String errorMessage) {
		cardsContainer.removeAll();
		cardsContainer.setLayout(new BorderLayout());

		JPanel errorPanel = new JPanel(new GridBagLayout());
		errorPanel.setBackground(CONTENT_BG);

		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
		messagePanel.setOpaque(false);

		JLabel errorIcon = new JLabel("⚠️");
		errorIcon.setFont(new Font("Arial", Font.PLAIN, 48));
		errorIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
		messagePanel.add(errorIcon);

		messagePanel.add(Box.createVerticalStrut(10));

		JLabel errorLabel = new JLabel(errorMessage);
		errorLabel.setFont(new Font("Arial", Font.BOLD, 16));
		errorLabel.setForeground(DELETE_RED);
		errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		messagePanel.add(errorLabel);

		messagePanel.add(Box.createVerticalStrut(15));

		JButton retryButton = createStyledButton("Retry", ACCENT_GOLD);
		retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		retryButton.addActionListener(e -> loadProductsWithUI());
		messagePanel.add(retryButton);

		errorPanel.add(messagePanel);
		cardsContainer.add(errorPanel, BorderLayout.CENTER);

		cardsContainer.revalidate();
		cardsContainer.repaint();
	}

	/**
	 * Create individual product card
	 */
	private static JPanel createProductCard(Product product) {
		JPanel card = new JPanel(new BorderLayout(0, 8));
		card.setBackground(CARD_BG);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
				new EmptyBorder(15, 15, 15, 15)));

		Dimension cardSize = new Dimension(300, 260);
		card.setPreferredSize(cardSize);
		card.setMaximumSize(cardSize);
		card.setMinimumSize(cardSize);

		// Top section
		card.add(createCardTopSection(product), BorderLayout.NORTH);

		// Middle section
		card.add(createCardPriceSection(product), BorderLayout.CENTER);

		// Bottom section
		card.add(createCardButtonPanel(product), BorderLayout.SOUTH);

		return card;
	}

	/**
	 * Create card top section
	 */
	private static JPanel createCardTopSection(Product product) {
		JPanel topSection = new JPanel(new BorderLayout(15, 0));
		topSection.setBackground(CARD_BG);

		JPanel iconPanel = new MolassesIcon();
		iconPanel.setPreferredSize(new Dimension(60, 60));
		iconPanel.setBackground(CARD_BG);
		topSection.add(iconPanel, BorderLayout.WEST);

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
		namePanel.setBackground(CARD_BG);

		JLabel nameLabel = new JLabel("<html>" + product.getName() + "</html>");
		nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
		nameLabel.setForeground(TEXT_DARK);
		nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		namePanel.add(nameLabel);

		if (product.isProductAssociateWithCustomer()) {
			namePanel.add(Box.createVerticalStrut(6));
			JButton viewCustomersBtn = createViewCustomersButton(product.getAssociatedCount());
			viewCustomersBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
			viewCustomersBtn.addActionListener(e -> showCustomerAssociationDialog(product));
			namePanel.add(viewCustomersBtn);
		}

		topSection.add(namePanel, BorderLayout.CENTER);

		return topSection;
	}

	/**
	 * Create card price section
	 */
	private static JPanel createCardPriceSection(Product product) {
		JPanel priceSection = new JPanel();
		priceSection.setLayout(new BoxLayout(priceSection, BoxLayout.Y_AXIS));
		priceSection.setBackground(CARD_BG);
		priceSection.setBorder(new EmptyBorder(10, 0, 10, 0));

		priceSection.add(
				createPriceRow("Selling Price:", "₱" + String.format("%.2f", product.getSellingPrice()), ACCENT_GOLD));
		priceSection.add(Box.createVerticalStrut(8));

		priceSection.add(createPriceRow("Capital:", "₱" + String.format("%.2f", product.getCapital()), SIDEBAR_ACTIVE));
		priceSection.add(Box.createVerticalStrut(8));

		priceSection.add(createPriceRow("Profit:", "₱" + String.format("%.2f", product.getProfit()) + " ("
				+ String.format("%.1f", product.getProfitMargin()) + "%)", PROFIT_GREEN));

		return priceSection;
	}

	/**
	 * Create card button panel
	 */
	private static JPanel createCardButtonPanel(Product product) {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		buttonPanel.setBackground(CARD_BG);

		JButton updateBtn = createCardButton("✏️ Update", ACCENT_GOLD);
		updateBtn.addActionListener(e -> showUpdateProductDialog(product));
		buttonPanel.add(updateBtn);

		JButton deleteBtn = createCardButton("🗑️ Delete", DELETE_RED);
		deleteBtn.addActionListener(e -> showDeleteConfirmation(product));
		buttonPanel.add(deleteBtn);

		return buttonPanel;
	}

	/**
	 * Create price row
	 */
	private static JPanel createPriceRow(String label, String value, Color valueColor) {
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(CARD_BG);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

		JLabel labelComponent = new JLabel(label);
		labelComponent.setFont(new Font("Arial", Font.PLAIN, 14));
		labelComponent.setForeground(new Color(100, 100, 100));

		JLabel valueComponent = new JLabel(value);
		valueComponent.setFont(new Font("Arial", Font.BOLD, 14));
		valueComponent.setForeground(valueColor);

		row.add(labelComponent, BorderLayout.WEST);
		row.add(valueComponent, BorderLayout.EAST);

		return row;
	}

	/**
	 * Create view customers button
	 */
	private static JButton createViewCustomersButton(int count) {
		JButton button = new JButton("👥 View Associated (" + count + ")");
		button.setFont(new Font("Arial", Font.PLAIN, 11));
		button.setBackground(CUSTOMER_BADGE_BG);
		button.setForeground(CUSTOMER_TEXT);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(180, 210, 240), 1),
				new EmptyBorder(3, 8, 3, 8)));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

		button.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				button.setBackground(new Color(210, 235, 255));
			}

			public void mouseExited(MouseEvent e) {
				button.setBackground(CUSTOMER_BADGE_BG);
			}
		});

		return button;
	}

	/*
	 * ========================= DIALOGS =========================
	 */

	/**
	 * Show add product dialog
	 */
	private static void showAddProductDialog() {
		com.gierza_molases.molases_app.ui.dialogs.ProductDialogs.AddProductDialog.show(getParentWindow(), () -> {
			ToastNotification.showSuccess(getParentWindow(), "Product added successfully!");
			updateCardsFromState();
		}, productsController);
	}

	/**
	 * Show update product dialog
	 */
	private static void showUpdateProductDialog(Product product) {
		com.gierza_molases.molases_app.ui.dialogs.ProductDialogs.UpdateProductDialog.show(getParentWindow(), product,
				productsController, // Add the controller here
				() -> {
					// Remove the success toast from here since it's already shown in the dialog
					updateCardsFromState();
				});
	}

	/**
	 * Show customer association dialog
	 */
	private static void showCustomerAssociationDialog(Product product) {
		com.gierza_molases.molases_app.ui.dialogs.ProductDialogs.ProductCustomerAssociationDialog
				.show(getParentWindow(), product.getId(), product.getName(), () -> {
					updateCardsFromState();

				});

	}

	/**
	 * Show delete confirmation dialog
	 */
	private static void showDeleteConfirmation(Product product) {
		JDialog confirmDialog = new JDialog(getParentWindow(), "Confirm Delete");
		confirmDialog.setLayout(new GridBagLayout());
		confirmDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);

		JLabel messageLabel = new JLabel(
				"<html><center>Are you sure you want to delete<br><b>" + product.getName() + "</b>?</center></html>");
		messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		messageLabel.setForeground(TEXT_DARK);
		confirmDialog.add(messageLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 10, 30);
		JLabel warningLabel = new JLabel("This product may be used in active orders.");
		warningLabel.setFont(new Font("Arial", Font.ITALIC, 13));
		warningLabel.setForeground(new Color(180, 100, 50));
		confirmDialog.add(warningLabel, gbc);

		gbc.gridy++;
		JLabel warning2Label = new JLabel("This action cannot be undone.");
		warning2Label.setFont(new Font("Arial", Font.ITALIC, 13));
		warning2Label.setForeground(DELETE_RED);
		confirmDialog.add(warning2Label, gbc);

		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 30, 20, 10);

		JButton cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> confirmDialog.dispose());
		confirmDialog.add(cancelBtn, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(20, 10, 20, 30);
		JButton deleteBtn = createStyledButton("Delete", DELETE_RED);
		deleteBtn.setPreferredSize(new Dimension(120, 40));
		deleteBtn.addActionListener(e -> {
			deleteProductWithConfirmation(product, deleteBtn, cancelBtn, confirmDialog);
		});
		confirmDialog.add(deleteBtn, gbc);

		confirmDialog.pack();
		confirmDialog.setMinimumSize(new Dimension(450, 220));
		confirmDialog.setLocationRelativeTo(null);
		confirmDialog.setVisible(true);
	}

	/*
	 * ========================= BUTTON FACTORIES =========================
	 */

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
		button.setPreferredSize(new Dimension(150, 38));

		button.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				button.setBackground(bgColor.brighter());
			}

			public void mouseExited(MouseEvent e) {
				button.setBackground(bgColor);
			}
		});

		return button;
	}

	/**
	 * Create card button
	 */
	private static JButton createCardButton(String text, Color bgColor) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 12));
		button.setBackground(bgColor);
		button.setForeground(TEXT_LIGHT);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setPreferredSize(new Dimension(110, 35));

		button.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				button.setBackground(bgColor.brighter());
			}

			public void mouseExited(MouseEvent e) {
				button.setBackground(bgColor);
			}
		});

		return button;
	}

	/*
	 * ========================= HELPERS =========================
	 */

	/**
	 * Get parent window
	 */
	private static Window getParentWindow() {
		return SwingUtilities.getWindowAncestor(mainPanelRef);
	}

	/*
	 * ========================= CUSTOM COMPONENTS =========================
	 */

	/**
	 * Custom Molasses Icon
	 */
	private static class MolassesIcon extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			int w = getWidth();
			int h = getHeight();
			int centerX = w / 2;
			int centerY = h / 2;

			// Scale factor
			double scale = Math.min(w, h) / 60.0;

			// Draw barrel body (rounded rectangle)
			g2d.setColor(new Color(139, 90, 43));
			int barrelW = (int) (28 * scale);
			int barrelH = (int) (40 * scale);
			int barrelX = centerX - barrelW / 2;
			int barrelY = centerY - barrelH / 2;
			g2d.fillRoundRect(barrelX, barrelY, barrelW, barrelH, (int) (8 * scale), (int) (8 * scale));

			// Add wood texture lines (horizontal bands)
			g2d.setColor(new Color(100, 65, 30, 150));
			for (int i = 0; i < 5; i++) {
				int lineY = barrelY + (int) ((8 + i * 8) * scale);
				g2d.fillRect(barrelX, lineY, barrelW, (int) (2 * scale));
			}

			// Metal bands (top, middle, bottom)
			g2d.setColor(new Color(80, 80, 80));
			int bandH = (int) (3 * scale);
			g2d.fillRect(barrelX, barrelY + (int) (5 * scale), barrelW, bandH);
			g2d.fillRect(barrelX, centerY - bandH / 2, barrelW, bandH);
			g2d.fillRect(barrelX, barrelY + barrelH - (int) (8 * scale), barrelW, bandH);

			// Add shine/highlight on left side
			g2d.setColor(new Color(184, 134, 11, 100));
			g2d.fillRoundRect(barrelX + 2, barrelY + 2, (int) (6 * scale), barrelH - 4, (int) (4 * scale),
					(int) (4 * scale));

			// Label on barrel
			g2d.setColor(new Color(245, 239, 231));
			int labelW = (int) (20 * scale);
			int labelH = (int) (12 * scale);
			int labelX = centerX - labelW / 2;
			int labelY = centerY - labelH / 2;
			g2d.fillRoundRect(labelX, labelY, labelW, labelH, (int) (3 * scale), (int) (3 * scale));

			// "M" letter on label
			g2d.setColor(ACCENT_GOLD);
			g2d.setFont(new Font("Arial", Font.BOLD, (int) (10 * scale)));
			String text = "M";
			int textW = g2d.getFontMetrics().stringWidth(text);
			int textH = g2d.getFontMetrics().getAscent();
			g2d.drawString(text, centerX - textW / 2, centerY + textH / 3);

			// Barrel lid (top)
			g2d.setColor(new Color(100, 65, 30));
			g2d.fillOval(barrelX - 2, barrelY - (int) (4 * scale), barrelW + 4, (int) (8 * scale));

			// Lid highlight
			g2d.setColor(new Color(139, 90, 43, 150));
			g2d.fillOval(barrelX, barrelY - (int) (3 * scale), barrelW, (int) (6 * scale));
		}
	}
}