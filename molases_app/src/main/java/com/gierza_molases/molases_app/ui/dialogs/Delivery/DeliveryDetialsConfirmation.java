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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.context.DeliveryChangesCalculator;

/**
 * Dialog to show financial changes summary before marking delivery as delivered
 * Shows: Money Returned, Money Added (Revenue), New Expenses, Net Change
 */
public class DeliveryDetialsConfirmation extends JDialog {

	private static final long serialVersionUID = 1L;

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SECTION_BG = new Color(245, 242, 237);
	private static final Color PROFIT_GREEN = new Color(34, 139, 34);
	private static final Color LOSS_RED = new Color(180, 50, 50);
	private static final Color NEUTRAL_GRAY = new Color(120, 120, 120);

	private final DeliveryChangesCalculator.FinancialChanges changes;
	private final Runnable onConfirm;

	/**
	 * Show the confirmation dialog
	 */
	public static void show(Window parent, DeliveryChangesCalculator.FinancialChanges changes, Runnable onConfirm) {
		DeliveryDetialsConfirmation dialog = new DeliveryDetialsConfirmation(parent, changes, onConfirm);
		dialog.setVisible(true);
	}

	/**
	 * Constructor
	 */
	private DeliveryDetialsConfirmation(Window parent, DeliveryChangesCalculator.FinancialChanges changes,
			Runnable onConfirm) {
		super(parent, "Delivery Changes Summary", ModalityType.APPLICATION_MODAL);

		this.changes = changes;
		this.onConfirm = onConfirm;

		initializeUI();
	}

	/**
	 * Initialize the UI
	 */
	private void initializeUI() {
		setLayout(new BorderLayout(0, 20));
		getContentPane().setBackground(Color.WHITE);

		// Main content with padding
		JPanel mainContent = new JPanel(new BorderLayout(0, 20));
		mainContent.setBackground(Color.WHITE);
		mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

		mainContent.add(createHeaderPanel(), BorderLayout.NORTH);
		mainContent.add(createSummaryPanel(), BorderLayout.CENTER);
		mainContent.add(createButtonPanel(), BorderLayout.SOUTH);

		add(mainContent);

		// Dialog settings
		pack();
		setMinimumSize(new Dimension(850, 300));
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
		JLabel titleLabel = new JLabel("Delivery Changes Summary");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
		titleLabel.setForeground(TEXT_DARK);
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(titleLabel);

		panel.add(Box.createVerticalStrut(5));

		// Subtitle
		JLabel subtitleLabel = new JLabel("Review the financial impact of your changes before confirming");
		subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		subtitleLabel.setForeground(new Color(100, 100, 100));
		subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(subtitleLabel);

		return panel;
	}

	/**
	 * Create summary panel with 4 sections (horizontal layout)
	 */
	private JPanel createSummaryPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 2),
				new EmptyBorder(20, 15, 20, 15)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 10, 0, 10);

		// Money Returned
		panel.add(createFinancialCard("Money Returned", changes.moneyReturned, TEXT_DARK, false), gbc);

		// Money Added (Revenue)
		gbc.gridx = 1;
		panel.add(createFinancialCard("Money Added", changes.moneyAdded, TEXT_DARK, false), gbc);

		// New Expenses
		gbc.gridx = 2;
		panel.add(createFinancialCard("New Expenses", changes.newExpenses, new Color(180, 100, 50), false), gbc);

		// Net Change (with color coding)
		gbc.gridx = 3;
		Color netChangeColor = NEUTRAL_GRAY;
		if (changes.netChange > 0) {
			netChangeColor = PROFIT_GREEN;
		} else if (changes.netChange < 0) {
			netChangeColor = LOSS_RED;
		}
		panel.add(createFinancialCard("Net Change", changes.netChange, netChangeColor, true), gbc);

		return panel;
	}

	/**
	 * Create a financial card/section
	 */
	private JPanel createFinancialCard(String label, double amount, Color valueColor, boolean isNetChange) {
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(SECTION_BG);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 190, 180), 1),
				new EmptyBorder(15, 12, 15, 12)));

		// Label
		JLabel labelComponent = new JLabel(label);
		labelComponent.setFont(new Font("Arial", Font.BOLD, 14));
		labelComponent.setForeground(new Color(100, 100, 100));
		labelComponent.setAlignmentX(Component.CENTER_ALIGNMENT);
		card.add(labelComponent);

		card.add(Box.createVerticalStrut(10));

		// Amount
		String formattedAmount = String.format("₱%,.2f", Math.abs(amount));
		if (isNetChange && amount != 0) {
			formattedAmount = (amount > 0 ? "+" : "-") + formattedAmount;
		}

		JLabel amountComponent = new JLabel(formattedAmount);
		amountComponent.setFont(new Font("Arial", Font.BOLD, 24));
		amountComponent.setForeground(valueColor);
		amountComponent.setAlignmentX(Component.CENTER_ALIGNMENT);
		card.add(amountComponent);

		// Add some spacing at bottom
		card.add(Box.createVerticalStrut(5));

		// Optional: Add description for net change
		if (isNetChange) {
			String description = "";
			if (amount > 0) {
				description = "Profit Increase";
			} else if (amount < 0) {
				description = "Profit Decrease";
			} else {
				description = "No Change";
			}

			JLabel descLabel = new JLabel(description);
			descLabel.setFont(new Font("Arial", Font.ITALIC, 12));
			descLabel.setForeground(new Color(120, 120, 120));
			descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			card.add(descLabel);
		}

		return card;
	}

	/**
	 * Create button panel
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(10, 0, 0, 0));

		JButton cancelButton = createStyledButton("Cancel", NEUTRAL_GRAY);
		cancelButton.setPreferredSize(new Dimension(130, 42));
		cancelButton.addActionListener(e -> dispose());

		JButton confirmButton = createStyledButton("Confirm & Mark as Delivered", ACCENT_GOLD);
		confirmButton.setPreferredSize(new Dimension(250, 42));
		confirmButton.addActionListener(e -> {
			dispose();
			if (onConfirm != null) {
				onConfirm.run();
			}
		});

		panel.add(cancelButton);
		panel.add(confirmButton);

		return panel;
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