package molases_appcom.gierza_molases.molases_app.ui.pages.Delivery_detials;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.context.DeliveryChangesCalculator;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.ui.components.delivery.UIComponentFactory;
import com.gierza_molases.molases_app.ui.dialogs.Delivery.DeliveryDetialsConfirmation;

public class DeliveryDetailsPage {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);

	private static int deliveryId;
	private static JLayeredPane layeredPane;
	private static JPanel mainPanel;
	private static JPanel contentPanel;
	private static JPanel loadingOverlay;
	private static LoadingSpinner spinner;
	private static Runnable currentOnBack;

	// Tab buttons
	private static JButton overviewTab;
	private static JButton customersTab;
	private static int currentTab = 0; // 0 = Overview, 1 = Customers

	public static JLayeredPane createPanel(int deliveryId, Runnable onBack) {
		DeliveryDetailsPage.deliveryId = deliveryId;
		DeliveryDetailsPage.currentOnBack = onBack;
		currentTab = 0; // Always start at tab 1

		layeredPane = new JLayeredPane();
		layeredPane.setLayout(null);

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

		// Header with Back button
		mainPanel.add(createHeader(onBack), BorderLayout.NORTH);

		// Content area (will be swapped based on tab)
		contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBackground(CONTENT_BG);
		mainPanel.add(contentPanel, BorderLayout.CENTER);

		// Action buttons at bottom (sticky)
		mainPanel.add(createActionButtonsPanel(), BorderLayout.SOUTH);

		// Wrap mainPanel in layeredPane
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.add(mainPanel, BorderLayout.CENTER);
		layeredPane.add(wrapper, JLayeredPane.DEFAULT_LAYER);

		// Add component listener to wrapper for dynamic resizing
		layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				wrapper.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
			}
		});

		// Initialize loading overlay
		initializeLoadingOverlay();

		// Load data from controller
		loadDataFromController(onBack);

		return layeredPane;
	}

	private static JPanel createHeader(Runnable onBack) {
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(CONTENT_BG);
		headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

		// Title and Back button
		JPanel topRow = new JPanel(new BorderLayout());
		topRow.setBackground(CONTENT_BG);

		JLabel titleLabel = new JLabel("Delivery Details");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
		titleLabel.setForeground(TEXT_DARK);
		topRow.add(titleLabel, BorderLayout.WEST);

		JButton backButton = UIComponentFactory.createStyledButton("← Back to List", new Color(120, 120, 120));
		backButton.addActionListener(e -> {
			AppContext.deliveryDetialsController.resetState();
			onBack.run();
		});
		topRow.add(backButton, BorderLayout.EAST);

		headerPanel.add(topRow, BorderLayout.NORTH);

		// Tab navigation
		headerPanel.add(createTabNavigation(), BorderLayout.SOUTH);

		return headerPanel;
	}

	private static JPanel createTabNavigation() {
		JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tabPanel.setBackground(CONTENT_BG);
		tabPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

		overviewTab = createTabButton("Overview & Expenses", 0);
		customersTab = createTabButton("Customers", 1);

		tabPanel.add(overviewTab);
		tabPanel.add(customersTab);

		updateTabStyles();

		return tabPanel;
	}

	private static JButton createTabButton(String text, int tabIndex) {
		JButton tab = new JButton(text);
		tab.setFont(new Font("Arial", Font.BOLD, 15));
		tab.setFocusPainted(false);
		tab.setBorderPainted(true);
		tab.setCursor(new Cursor(Cursor.HAND_CURSOR));
		tab.setPreferredSize(new Dimension(200, 45));
		tab.setHorizontalAlignment(SwingConstants.CENTER);
		tab.setContentAreaFilled(false);
		tab.setOpaque(true);

		tab.addActionListener(e -> switchTab(tabIndex));

		tab.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (currentTab != tabIndex) {
					tab.setForeground(ACCENT_GOLD);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				updateTabStyles();
			}
		});

		return tab;
	}

	private static void updateTabStyles() {
		// Overview tab
		if (currentTab == 0) {
			overviewTab.setBackground(CONTENT_BG);
			overviewTab.setForeground(ACCENT_GOLD);
			overviewTab.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, ACCENT_GOLD));
		} else {
			overviewTab.setBackground(CONTENT_BG);
			overviewTab.setForeground(TEXT_DARK);
			overviewTab.setBorder(BorderFactory.createEmptyBorder(5, 15, 8, 15));
		}

		// Customers tab
		if (currentTab == 1) {
			customersTab.setBackground(CONTENT_BG);
			customersTab.setForeground(ACCENT_GOLD);
			customersTab.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, ACCENT_GOLD));
		} else {
			customersTab.setBackground(CONTENT_BG);
			customersTab.setForeground(TEXT_DARK);
			customersTab.setBorder(BorderFactory.createEmptyBorder(5, 15, 8, 15));
		}
	}

	private static void switchTab(int tabIndex) {
		if (currentTab == tabIndex)
			return;

		// Fade out effect
		Timer fadeOut = new Timer(10, null);
		final float[] opacity = { 1.0f };

		fadeOut.addActionListener(e -> {
			opacity[0] -= 0.1f;
			if (opacity[0] <= 0) {
				fadeOut.stop();

				// Switch content
				currentTab = tabIndex;
				updateTabStyles();
				loadTabContent();

				// Fade in effect
				Timer fadeIn = new Timer(10, null);
				fadeIn.addActionListener(e2 -> {
					opacity[0] += 0.1f;
					if (opacity[0] >= 1.0f) {
						fadeIn.stop();
					}
					contentPanel.repaint();
				});
				fadeIn.start();
			}
			contentPanel.repaint();
		});
		fadeOut.start();
	}

	private static void loadTabContent() {
		contentPanel.removeAll();

		if (currentTab == 0) {
			// Overview & Expenses tab
			contentPanel.add(DeliveryOverviewTab.createPanel());
		} else {
			// Customers tab
			contentPanel.add(CustomerDeliveriesTab.createPanel());
		}

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	private static void initializeLoadingOverlay() {
		loadingOverlay = new JPanel(new GridBagLayout());
		loadingOverlay.setBackground(new Color(250, 247, 242, 220)); // Light overlay matching CONTENT_BG
		loadingOverlay.setVisible(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;

		spinner = new LoadingSpinner(60, ACCENT_GOLD);
		loadingOverlay.add(spinner, gbc);

		gbc.gridy = 1;
		gbc.insets = new Insets(20, 0, 0, 0);
		JLabel loadingLabel = new JLabel("Loading delivery details...");
		loadingLabel.setFont(new Font("Arial", Font.BOLD, 16));
		loadingLabel.setForeground(TEXT_DARK); // Changed from TEXT_LIGHT to TEXT_DARK for visibility
		loadingOverlay.add(loadingLabel, gbc);

		layeredPane.add(loadingOverlay, JLayeredPane.PALETTE_LAYER);
	}

	private static void showLoading() {
		if (loadingOverlay != null && layeredPane != null) {
			SwingUtilities.invokeLater(() -> {
				loadingOverlay.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
				loadingOverlay.setVisible(true);
				spinner.start();
				layeredPane.repaint();
			});
		}
	}

	private static void hideLoading() {
		if (loadingOverlay != null && spinner != null) {
			SwingUtilities.invokeLater(() -> {
				spinner.stop();
				loadingOverlay.setVisible(false);
				layeredPane.repaint();
			});
		}
	}

	private static void loadDataFromController(Runnable onBack) {
		showLoading();

		AppContext.deliveryDetialsController.loadDeliveryData(deliveryId, () -> {
			SwingUtilities.invokeLater(() -> {
				// Initialize both tabs
				DeliveryOverviewTab.initialize();
				CustomerDeliveriesTab.initialize();

				// Load initial tab content
				loadTabContent();

				hideLoading();
			});
		}, (errorMsg) -> {
			SwingUtilities.invokeLater(() -> {
				hideLoading();
				ToastNotification.showError(SwingUtilities.getWindowAncestor(layeredPane),
						"Failed to load delivery: " + errorMsg);
				onBack.run();
			});
		});
	}

	private static JPanel createActionButtonsPanel() {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(CONTENT_BG);
		buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

		JButton printBtn = UIComponentFactory.createStyledButton("🖨️ Print", SIDEBAR_ACTIVE);
		printBtn.setPreferredSize(new Dimension(120, 40));
		printBtn.addActionListener(e -> printDelivery());
		buttonPanel.add(printBtn);

		Delivery delivery = AppContext.deliveryDetialsController.getState().getDelivery();
		String deliveryStatus = delivery != null ? delivery.getStatus() : "scheduled";

		if (!deliveryStatus.equalsIgnoreCase("Delivered") && !deliveryStatus.equalsIgnoreCase("Cancelled")) {
			JButton cancelBtn = UIComponentFactory.createStyledButton("❌ Mark as Cancelled", new Color(180, 50, 50));
			cancelBtn.setPreferredSize(new Dimension(180, 40));
			cancelBtn.addActionListener(e -> markAsCancelled());
			buttonPanel.add(cancelBtn);

			JButton deliveredBtn = UIComponentFactory.createStyledButton("✓ Mark as Delivered", ACCENT_GOLD);
			deliveredBtn.setPreferredSize(new Dimension(180, 40));
			deliveredBtn.addActionListener(e -> markAsDelivered());
			buttonPanel.add(deliveredBtn);
		}

		return buttonPanel;
	}

	private static void printDelivery() {
		java.awt.print.PrinterJob printerJob = java.awt.print.PrinterJob.getPrinterJob();
		java.awt.print.PageFormat pageFormat = printerJob.defaultPage();
		pageFormat.setOrientation(java.awt.print.PageFormat.PORTRAIT);

		printerJob.setPrintable(new java.awt.print.Printable() {
			@Override
			public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) {
				if (pageIndex > 0) {
					return java.awt.print.Printable.NO_SUCH_PAGE;
				}

				java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
				g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

				double scaleX = pageFormat.getImageableWidth() / contentPanel.getWidth();
				double scaleY = pageFormat.getImageableHeight() / contentPanel.getHeight();
				double scale = Math.min(scaleX, scaleY);

				g2d.scale(scale, scale);
				contentPanel.printAll(graphics);

				return java.awt.print.Printable.PAGE_EXISTS;
			}
		});

		if (printerJob.printDialog()) {
			try {
				printerJob.print();
				JOptionPane.showMessageDialog(null, "Printing completed successfully!", "Print",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (java.awt.print.PrinterException e) {
				JOptionPane.showMessageDialog(null, "Failed to print: " + e.getMessage(), "Print Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private static void markAsCancelled() {
		int result = JOptionPane.showConfirmDialog(null,
				"Are you sure you want to mark this delivery as CANCELLED?\nThis action cannot be undone.",
				"Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (result == JOptionPane.YES_OPTION) {
			JOptionPane.showMessageDialog(null, "Mark as Cancelled feature will be implemented later", "Info",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private static void markAsDelivered() {
		// First validate that all payments are set
		boolean allPaymentsSet = CustomerDeliveriesTab.validateAllPaymentsSet();

		if (!allPaymentsSet) {
			JOptionPane.showMessageDialog(null,
					"Please set payment type for all customers before marking as delivered.",
					"Missing Payment Information", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Calculate financial changes
		DeliveryChangesCalculator.FinancialChanges changes = DeliveryChangesCalculator
				.calculate(AppContext.deliveryDetialsController.getState());

		// Show confirmation dialog with changes summary
		DeliveryDetialsConfirmation.show(SwingUtilities.getWindowAncestor(layeredPane), changes, () -> {

			// For now, just show success message
			ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(layeredPane),
					"Delivery marked as delivered successfully! (Backend save pending implementation)");
		});
	}

	public static JLayeredPane getLayeredPane() {
		return layeredPane;
	}
}