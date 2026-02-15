package com.gierza_molases.molases_app.ui.dialogs.Maintenance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.UiController.MaintenanceController;
import com.gierza_molases.molases_app.model.response.DeletionPreview;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;

/**
 * Detailed preview dialog that shows comprehensive deletion information Fetches
 * data internally and displays loading state
 */
public class DetailedPreviewDialog extends JDialog {

	// Color Palette
	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color PANEL_BG = Color.WHITE;
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_MEDIUM = new Color(120, 90, 70);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color INFO_BG = new Color(227, 242, 253);
	private static final Color INFO_BORDER = new Color(144, 202, 249);
	private static final Color WARNING_BG = new Color(255, 243, 224);
	private static final Color WARNING_BORDER = new Color(255, 167, 38);
	private static final Color DANGER_RED = new Color(211, 47, 47);
	private static final Color SUCCESS_GREEN = new Color(56, 142, 60);

	private DeletionPreview preview;
	private final int yearsOld;
	private final MaintenanceController controller;

	// UI Components
	private JPanel contentContainer;
	private JPanel loadingPanel;
	private JPanel dataPanel;
	private LoadingSpinner spinner;
	private JButton closeButton;

	public DetailedPreviewDialog(Window parent, MaintenanceController controller, int yearsOld) {
		super(parent, "Detailed Deletion Preview", ModalityType.APPLICATION_MODAL);
		this.controller = controller;
		this.yearsOld = yearsOld;

		initComponents();
		setSize(750, 650);
		setLocationRelativeTo(parent);

		// Start fetching data immediately after initialization
		fetchPreviewData();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		getContentPane().setBackground(CONTENT_BG);

		// Create content container that will switch between loading and data
		contentContainer = new JPanel(new BorderLayout());
		contentContainer.setBackground(CONTENT_BG);

		// Create loading panel
		loadingPanel = createLoadingPanel();

		// Initially show loading panel
		contentContainer.add(loadingPanel, BorderLayout.CENTER);

		add(contentContainer, BorderLayout.CENTER);

		// Button panel
		JPanel buttonPanel = createButtonPanel();
		add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * Create the loading panel with spinner
	 */
	private JPanel createLoadingPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(CONTENT_BG);
		panel.setBorder(new EmptyBorder(20, 20, 20, 20));

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBackground(CONTENT_BG);

		// Add vertical glue to center content
		centerPanel.add(Box.createVerticalGlue());

		// Spinner
		spinner = new LoadingSpinner(60, ACCENT_GOLD);
		spinner.setAlignmentX(0.5f);
		centerPanel.add(spinner);
		centerPanel.add(Box.createVerticalStrut(20));

		// Loading text
		JLabel loadingLabel = new JLabel("Fetching deletion preview...");
		loadingLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		loadingLabel.setForeground(TEXT_MEDIUM);
		loadingLabel.setAlignmentX(0.5f);
		centerPanel.add(loadingLabel);

		JLabel subLabel = new JLabel("This may take a moment");
		subLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		subLabel.setForeground(TEXT_MEDIUM);
		subLabel.setAlignmentX(0.5f);
		centerPanel.add(subLabel);

		centerPanel.add(Box.createVerticalGlue());

		panel.add(centerPanel, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Start loading - show spinner
	 */
	private void startLoading() {
		spinner.start();
		closeButton.setEnabled(false);
	}

	/**
	 * Fetch preview data from controller
	 */
	private void fetchPreviewData() {
		// Start loading state
		startLoading();

		// Fetch data using controller
		controller.getDetailedPreview(yearsOld, preview -> {
			// Show data when loaded (on EDT)
			SwingUtilities.invokeLater(() -> showData(preview));
		}, error -> {
			// Show error state (on EDT)
			SwingUtilities.invokeLater(() -> showError(error));
		});
	}

	/**
	 * Show the data after loading completes
	 */
	private void showData(DeletionPreview preview) {
		this.preview = preview;

		// Stop spinner
		spinner.stop();

		// Create data panel
		dataPanel = createDataPanel();

		// Switch from loading to data panel
		contentContainer.removeAll();
		contentContainer.add(dataPanel, BorderLayout.CENTER);
		contentContainer.revalidate();
		contentContainer.repaint();

		// Re-enable close button
		closeButton.setEnabled(true);
	}

	/**
	 * Show error message
	 */
	private void showError(String errorMessage) {
		// Stop spinner
		spinner.stop();

		// Create error panel
		JPanel errorPanel = createErrorPanel(errorMessage);

		// Switch to error panel
		contentContainer.removeAll();
		contentContainer.add(errorPanel, BorderLayout.CENTER);
		contentContainer.revalidate();
		contentContainer.repaint();

		// Re-enable close button
		closeButton.setEnabled(true);
	}

	/**
	 * Create error panel
	 */
	private JPanel createErrorPanel(String errorMessage) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(CONTENT_BG);
		panel.setBorder(new EmptyBorder(20, 20, 20, 20));

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBackground(CONTENT_BG);

		centerPanel.add(Box.createVerticalGlue());

		// Error icon
		JLabel iconLabel = new JLabel("❌");
		iconLabel.setFont(new Font("Arial", Font.PLAIN, 48));
		iconLabel.setAlignmentX(0.5f);
		centerPanel.add(iconLabel);
		centerPanel.add(Box.createVerticalStrut(20));

		// Error title
		JLabel titleLabel = new JLabel("Failed to Load Preview");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(DANGER_RED);
		titleLabel.setAlignmentX(0.5f);
		centerPanel.add(titleLabel);
		centerPanel.add(Box.createVerticalStrut(10));

		// Error message
		JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + errorMessage + "</div></html>");
		messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		messageLabel.setForeground(TEXT_MEDIUM);
		messageLabel.setAlignmentX(0.5f);
		centerPanel.add(messageLabel);

		centerPanel.add(Box.createVerticalGlue());

		panel.add(centerPanel, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Create the data panel with all preview information
	 */
	private JPanel createDataPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

		// Header
		JPanel headerPanel = createHeaderPanel();
		mainPanel.add(headerPanel);
		mainPanel.add(Box.createVerticalStrut(20));

		// Summary panel
		JPanel summaryPanel = createSummaryPanel();
		mainPanel.add(summaryPanel);
		mainPanel.add(Box.createVerticalStrut(15));

		// Breakdown panel
		JPanel breakdownPanel = createBreakdownPanel();
		mainPanel.add(breakdownPanel);
		mainPanel.add(Box.createVerticalStrut(15));

		// Warning panel
		if (preview.deliveryCount > 0) {
			JPanel warningPanel = createWarningPanel();
			mainPanel.add(warningPanel);
		}

		// Wrap in scroll pane
		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		JPanel container = new JPanel(new BorderLayout());
		container.setBackground(CONTENT_BG);
		container.add(scrollPane, BorderLayout.CENTER);

		return container;
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(CONTENT_BG);
		panel.setAlignmentX(0);

		JLabel titleLabel = new JLabel("📊 Detailed Deletion Preview");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setAlignmentX(0);

		JLabel subtitleLabel = new JLabel("Comprehensive breakdown of records to be deleted");
		subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		subtitleLabel.setForeground(TEXT_MEDIUM);
		subtitleLabel.setAlignmentX(0);

		panel.add(titleLabel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(subtitleLabel);

		return panel;
	}

	private JPanel createSummaryPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(INFO_BG);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(INFO_BORDER, 2, true),
				new EmptyBorder(20, 20, 20, 20)));
		// Removed setMaximumSize to allow proper content display

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(INFO_BG);

		// Criteria
		JLabel criteriaLabel = new JLabel("Search Criteria:");
		criteriaLabel.setFont(new Font("Arial", Font.BOLD, 14));
		criteriaLabel.setForeground(TEXT_DARK);
		criteriaLabel.setAlignmentX(0);
		contentPanel.add(criteriaLabel);
		contentPanel.add(Box.createVerticalStrut(8));

		JLabel criteriaValue = new JLabel(
				"Completed deliveries older than " + yearsOld + " year" + (yearsOld > 1 ? "s" : ""));
		criteriaValue.setFont(new Font("Arial", Font.PLAIN, 13));
		criteriaValue.setForeground(TEXT_DARK);
		criteriaValue.setAlignmentX(0);
		criteriaValue.setBorder(new EmptyBorder(0, 15, 0, 0));
		contentPanel.add(criteriaValue);
		contentPanel.add(Box.createVerticalStrut(15));

		// Total deliveries
		JLabel resultsLabel = new JLabel("Total Deliveries to Delete:");
		resultsLabel.setFont(new Font("Arial", Font.BOLD, 14));
		resultsLabel.setForeground(TEXT_DARK);
		resultsLabel.setAlignmentX(0);
		contentPanel.add(resultsLabel);
		contentPanel.add(Box.createVerticalStrut(8));

		JLabel countLabel = new JLabel(
				preview.deliveryCount + " completed " + (preview.deliveryCount == 1 ? "delivery" : "deliveries"));
		countLabel.setFont(new Font("Arial", Font.BOLD, 20));
		countLabel.setForeground(preview.deliveryCount > 0 ? DANGER_RED : SUCCESS_GREEN);
		countLabel.setAlignmentX(0);
		countLabel.setBorder(new EmptyBorder(0, 15, 0, 0));
		contentPanel.add(countLabel);

		panel.add(contentPanel, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createBreakdownPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(PANEL_BG);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(220, 210, 195), 2, true), new EmptyBorder(20, 20, 20, 20)));
		// Removed setMaximumSize to allow proper content display

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(PANEL_BG);

		JLabel titleLabel = new JLabel("Related Records to be Deleted:");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setAlignmentX(0);
		contentPanel.add(titleLabel);
		contentPanel.add(Box.createVerticalStrut(15));

		// Create breakdown items
		addBreakdownItem(contentPanel, "Customer Deliveries", preview.customerDeliveryCount);
		addBreakdownItem(contentPanel, "Branch Deliveries", preview.branchDeliveryCount);
		addBreakdownItem(contentPanel, "Product Deliveries", preview.productDeliveryCount);
		addBreakdownItem(contentPanel, "Payment Records", preview.paymentCount);
		addBreakdownItem(contentPanel, "Payment History Entries", preview.paymentHistoryCount);

		contentPanel.add(Box.createVerticalStrut(10));

		// Total records
		int totalRecords = preview.deliveryCount + preview.customerDeliveryCount + preview.branchDeliveryCount
				+ preview.productDeliveryCount + preview.paymentCount + preview.paymentHistoryCount;
		JLabel totalLabel = new JLabel("Total Records: " + totalRecords);
		totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
		totalLabel.setForeground(DANGER_RED);
		totalLabel.setAlignmentX(0);
		totalLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
		contentPanel.add(totalLabel);

		panel.add(contentPanel, BorderLayout.CENTER);

		return panel;
	}

	private void addBreakdownItem(JPanel parent, String label, int count) {
		JPanel itemPanel = new JPanel(new BorderLayout());
		itemPanel.setBackground(PANEL_BG);
		itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

		JLabel nameLabel = new JLabel("  • " + label + ":");
		nameLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		nameLabel.setForeground(TEXT_DARK);

		JLabel countLabel = new JLabel(String.valueOf(count));
		countLabel.setFont(new Font("Arial", Font.BOLD, 13));
		countLabel.setForeground(TEXT_DARK);

		itemPanel.add(nameLabel, BorderLayout.WEST);
		itemPanel.add(countLabel, BorderLayout.EAST);

		parent.add(itemPanel);
		parent.add(Box.createVerticalStrut(5));
	}

	private JPanel createWarningPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(WARNING_BG);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(WARNING_BORDER, 2, true),
				new EmptyBorder(15, 20, 15, 20)));
		// Removed setMaximumSize to allow proper content display

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(WARNING_BG);

		JLabel titleLabel = new JLabel("⚠️  Important Note");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
		titleLabel.setForeground(new Color(230, 81, 0));
		titleLabel.setAlignmentX(0);
		contentPanel.add(titleLabel);
		contentPanel.add(Box.createVerticalStrut(8));

		JLabel messageLabel = new JLabel(
				"<html>This is only a preview. No data has been deleted yet. To proceed with deletion, close this dialog and click 'Delete Records'.</html>");
		messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		messageLabel.setForeground(TEXT_DARK);
		messageLabel.setAlignmentX(0);
		contentPanel.add(messageLabel);

		panel.add(contentPanel, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
		panel.setBackground(CONTENT_BG);
		panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 210, 195)));

		closeButton = createStyledButton("Close", ACCENT_GOLD, Color.WHITE);
		closeButton.addActionListener(e -> dispose());

		panel.add(closeButton);

		return panel;
	}

	private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 14));
		button.setForeground(fgColor);
		button.setBackground(bgColor);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setPreferredSize(new Dimension(150, 40));

		// Hover effect
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				if (button.isEnabled()) {
					button.setBackground(bgColor.darker());
				}
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				button.setBackground(bgColor);
			}
		});

		return button;
	}
}