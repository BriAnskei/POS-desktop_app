package molases_appcom.gierza_molases.molases_app.ui.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.UiController.MaintenanceController;
import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.ui.dialogs.Maintenance.ConfirmationDialog;
import com.gierza_molases.molases_app.ui.dialogs.Maintenance.DetailedPreviewDialog;

public class MaintenancePage {

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

	private static int selectedYears = 2; // Default selection
	// Controller reference from AppContext (same pattern as BranchesPage)
	private static final MaintenanceController controller = AppContext.maintenanceController;
	private static JPanel mainPanel; // Store reference to main panel for getting parent window

	/**
	 * Get parent window from main panel
	 */
	private static Window getParentWindow() {
		if (mainPanel != null) {
			return SwingUtilities.getWindowAncestor(mainPanel);
		}
		return null;
	}

	/**
	 * Create the maintenance panel
	 */
	public static JPanel createPanel() {
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(CONTENT_BG);

		// Header
		JPanel headerPanel = createHeader();
		mainPanel.add(headerPanel, BorderLayout.NORTH);

		// Content area with scroll
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(CONTENT_BG);
		contentPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

		// Info panel
		JPanel infoPanel = createInfoPanel();
		contentPanel.add(infoPanel);
		contentPanel.add(Box.createVerticalStrut(20));

		// Action panel
		JPanel actionPanel = createActionPanel();
		contentPanel.add(actionPanel);
		contentPanel.add(Box.createVerticalStrut(20));

		// Warning panel
		JPanel warningPanel = createWarningPanel();
		contentPanel.add(warningPanel);

		// Wrap content panel in a scroll pane
		JScrollPane scrollPane = new JScrollPane(contentPanel);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		mainPanel.add(scrollPane, BorderLayout.CENTER);

		return mainPanel;
	}

	/**
	 * Create the header section
	 */
	private static JPanel createHeader() {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(CONTENT_BG);
		header.setBorder(new EmptyBorder(20, 40, 20, 40));

		JLabel titleLabel = new JLabel("MAINTENANCE");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
		titleLabel.setForeground(TEXT_DARK);

		JLabel subtitleLabel = new JLabel("System Database Cleanup");
		subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		subtitleLabel.setForeground(TEXT_MEDIUM);

		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
		titlePanel.setBackground(CONTENT_BG);
		titleLabel.setAlignmentX(0);
		subtitleLabel.setAlignmentX(0);
		titlePanel.add(titleLabel);
		titlePanel.add(Box.createVerticalStrut(5));
		titlePanel.add(subtitleLabel);

		header.add(titlePanel, BorderLayout.WEST);

		return header;
	}

	/**
	 * Create the information panel
	 */
	private static JPanel createInfoPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(INFO_BG);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(INFO_BORDER, 2, true),
				new EmptyBorder(20, 20, 20, 20)));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(INFO_BG);

		// Title with icon
		JLabel titleLabel = new JLabel("📋 About This Feature");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setAlignmentX(0);
		contentPanel.add(titleLabel);
		contentPanel.add(Box.createVerticalStrut(15));

		// Description
		JLabel descLabel = new JLabel(
				"<html>This maintenance tool helps keep your database running smoothly by removing old completed delivery records.</html>");
		descLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		descLabel.setForeground(TEXT_DARK);
		descLabel.setAlignmentX(0);
		contentPanel.add(descLabel);
		contentPanel.add(Box.createVerticalStrut(15));

		// What gets deleted section
		JLabel deletedTitle = new JLabel("What gets deleted:");
		deletedTitle.setFont(new Font("Arial", Font.BOLD, 14));
		deletedTitle.setForeground(TEXT_DARK);
		deletedTitle.setAlignmentX(0);
		contentPanel.add(deletedTitle);
		contentPanel.add(Box.createVerticalStrut(8));

		String[] deletedItems = { "• Deliveries with all payments marked as \"Complete\"",
				"• Associated customer deliveries, branch deliveries, and product deliveries",
				"• All payment records and payment history for those deliveries" };

		for (String item : deletedItems) {
			JLabel itemLabel = new JLabel(item);
			itemLabel.setFont(new Font("Arial", Font.PLAIN, 13));
			itemLabel.setForeground(TEXT_DARK);
			itemLabel.setAlignmentX(0);
			itemLabel.setBorder(new EmptyBorder(2, 15, 2, 0));
			contentPanel.add(itemLabel);
		}

		contentPanel.add(Box.createVerticalStrut(15));

		// What stays protected section
		JLabel protectedTitle = new JLabel("What stays protected:");
		protectedTitle.setFont(new Font("Arial", Font.BOLD, 14));
		protectedTitle.setForeground(SUCCESS_GREEN);
		protectedTitle.setAlignmentX(0);
		contentPanel.add(protectedTitle);
		contentPanel.add(Box.createVerticalStrut(8));

		String[] protectedItems = { "✓ Deliveries with any pending payments (automatically skipped)",
				"✓ Customer and branch master data (preserved)", "✓ Product definitions (preserved)" };

		for (String item : protectedItems) {
			JLabel itemLabel = new JLabel(item);
			itemLabel.setFont(new Font("Arial", Font.PLAIN, 13));
			itemLabel.setForeground(TEXT_DARK);
			itemLabel.setAlignmentX(0);
			itemLabel.setBorder(new EmptyBorder(2, 15, 2, 0));
			contentPanel.add(itemLabel);
		}

		panel.add(contentPanel, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Create the action panel with radio buttons and action buttons
	 */
	private static JPanel createActionPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(PANEL_BG);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(220, 210, 195), 2, true), new EmptyBorder(25, 25, 25, 25)));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(PANEL_BG);

		// Title with icon
		JLabel titleLabel = new JLabel("🗑️  Delete Old Records");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setAlignmentX(0);
		contentPanel.add(titleLabel);
		contentPanel.add(Box.createVerticalStrut(15));

		// Instructions
		JLabel instructionLabel = new JLabel("Select how far back to delete completed deliveries:");
		instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		instructionLabel.setForeground(TEXT_DARK);
		instructionLabel.setAlignmentX(0);
		contentPanel.add(instructionLabel);
		contentPanel.add(Box.createVerticalStrut(15));

		// Radio buttons
		ButtonGroup buttonGroup = new ButtonGroup();
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
		radioPanel.setBackground(PANEL_BG);
		radioPanel.setAlignmentX(0);

		for (int i = 1; i <= 5; i++) {
			final int years = i;
			JRadioButton radioButton = new JRadioButton("Older than " + i + " year" + (i > 1 ? "s" : ""));
			radioButton.setFont(new Font("Arial", Font.PLAIN, 14));
			radioButton.setForeground(TEXT_DARK);
			radioButton.setBackground(PANEL_BG);
			radioButton.setFocusPainted(false);
			radioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

			// Set default selection to 2 years
			if (i == 2) {
				radioButton.setSelected(true);
				selectedYears = 2;
			}

			radioButton.addActionListener(e -> selectedYears = years);

			buttonGroup.add(radioButton);
			radioPanel.add(radioButton);
			radioPanel.add(Box.createVerticalStrut(8));
		}

		contentPanel.add(radioPanel);
		contentPanel.add(Box.createVerticalStrut(20));

		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
		buttonPanel.setBackground(PANEL_BG);
		buttonPanel.setAlignmentX(0);

		// Preview button
		JButton previewButton = createStyledButton("Preview Deletion", new Color(33, 150, 243), Color.WHITE);
		previewButton.addActionListener(e -> showPreviewDialog());

		// Delete button
		JButton deleteButton = createStyledButton("Delete Records", DANGER_RED, Color.WHITE);
		deleteButton.addActionListener(e -> showPasswordDialog());

		buttonPanel.add(previewButton);
		buttonPanel.add(deleteButton);

		contentPanel.add(buttonPanel);

		panel.add(contentPanel, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Create the warning panel
	 */
	private static JPanel createWarningPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(WARNING_BG);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(WARNING_BORDER, 2, true),
				new EmptyBorder(15, 20, 15, 20)));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(WARNING_BG);

		JLabel titleLabel = new JLabel("⚠️  Warning");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		titleLabel.setForeground(new Color(230, 81, 0));
		titleLabel.setAlignmentX(0);
		contentPanel.add(titleLabel);
		contentPanel.add(Box.createVerticalStrut(8));

		JLabel messageLabel = new JLabel(
				"<html>This action cannot be undone. Make sure you have backed up your database before proceeding.</html>");
		messageLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		messageLabel.setForeground(TEXT_DARK);
		messageLabel.setAlignmentX(0);
		contentPanel.add(messageLabel);

		panel.add(contentPanel, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Create a styled button
	 */
	private static JButton createStyledButton(String text, Color bgColor, Color fgColor) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 14));
		button.setForeground(fgColor);
		button.setBackground(bgColor);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setPreferredSize(new Dimension(180, 40));

		// Hover effect
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				button.setBackground(bgColor.darker());
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				button.setBackground(bgColor);
			}
		});

		return button;
	}

	/**
	 * Show password dialog before deletion
	 */
	private static void showPasswordDialog() {
		Window parent = getParentWindow();

		JDialog dialog = new JDialog(parent, "Password Required", JDialog.ModalityType.APPLICATION_MODAL);
		dialog.setLayout(new BorderLayout());
		dialog.setSize(450, 250);
		dialog.setLocationRelativeTo(parent);

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 15, 0);
		gbc.anchor = GridBagConstraints.CENTER;

		// Title
		JLabel titleLabel = new JLabel("🔒 Authentication Required");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		titleLabel.setForeground(TEXT_DARK);
		contentPanel.add(titleLabel, gbc);

		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 20, 0);
		JLabel messageLabel = new JLabel("Please enter your password to proceed with deletion:");
		messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		messageLabel.setForeground(TEXT_DARK);
		contentPanel.add(messageLabel, gbc);

		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 25, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JPasswordField passwordField = new JPasswordField();
		passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
		passwordField.setPreferredSize(new Dimension(300, 35));
		contentPanel.add(passwordField, gbc);

		// Button panel
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 0, 0, 10);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;

		JButton cancelButton = createStyledButton("Cancel", new Color(158, 158, 158), Color.WHITE);
		cancelButton.setPreferredSize(new Dimension(120, 40));
		cancelButton.addActionListener(e -> dialog.dispose());
		contentPanel.add(cancelButton, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.WEST;
		JButton continueButton = createStyledButton("Continue", ACCENT_GOLD, Color.WHITE);
		continueButton.setPreferredSize(new Dimension(120, 40));

		continueButton.addActionListener(e -> {
			String password = new String(passwordField.getPassword());
			// TODO: Implement password validation in controller
			// For now, proceed if password is not empty
			if (!password.isEmpty()) {
				dialog.dispose();
				showDeleteConfirmationDialog();
			} else {
				ToastNotification.showError(parent, "Password is required");
			}
		});

		contentPanel.add(continueButton, gbc);

		// Enter key to submit
		passwordField.addActionListener(e -> continueButton.doClick());

		dialog.add(contentPanel, BorderLayout.CENTER);
		dialog.setVisible(true);
	}

	/**
	 * Show preview dialog
	 */
	private static void showPreviewDialog() {
		showDetailedPreviewDialog();
	}

	/**
	 * Show detailed preview dialog with loading state
	 */
	private static void showDetailedPreviewDialog() {
		Window parent = getParentWindow();

		if (controller == null) {
			ToastNotification.showError(parent, "Controller not initialized");
			return;
		}

		// Create and show dialog - it will handle loading and data fetching internally
		DetailedPreviewDialog dialog = new DetailedPreviewDialog(parent, controller, selectedYears);
		dialog.setVisible(true);
	}

	/**
	 * Show delete confirmation dialog
	 */
	private static void showDeleteConfirmationDialog() {
		Window parent = getParentWindow();

		if (controller == null) {
			ToastNotification.showError(parent, "Controller not initialized");
			return;
		}

		new ConfirmationDialog(parent, controller, selectedYears, null).setVisible(true);
	}
}