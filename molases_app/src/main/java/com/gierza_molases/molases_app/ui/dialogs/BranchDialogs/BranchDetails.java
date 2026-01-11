package com.gierza_molases.molases_app.ui.dialogs.BranchDialogs;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.model.BranchCustomerResponse;
import com.gierza_molases.molases_app.service.BranchService;

public class BranchDetails extends JDialog {

	// Color Palette - matching BranchesPage
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color BORDER_COLOR = new Color(200, 190, 180);

	BranchService branchService = AppContext.branchService;

	/**
	 * Constructor
	 */
	public BranchDetails(Window parent, int branchId) {
		super(parent, "Branch Details", ModalityType.APPLICATION_MODAL);

		BranchCustomerResponse branchCustomerData = branchService.getBanchDetails(branchId);

		initializeUI(branchCustomerData.getCustomer().getDisplayName(), branchCustomerData.getCustomer().getType(),
				branchCustomerData.getCustomer().getFormatttedNumber(), branchCustomerData.getBranch().getAddress(),
				branchCustomerData.getBranch().getNote());
	}

	/**
	 * Initialize the UI
	 */
	private void initializeUI(String customerName, String type, String contactNumber, String address, String note) {
		setLayout(new GridBagLayout());
		getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 30, 10, 30);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		// Title with icon
		JLabel titleLabel = new JLabel("📋 Branch Details");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		titleLabel.setForeground(TEXT_DARK);
		add(titleLabel, gbc);

		// Customer Name
		gbc.gridy++;
		gbc.insets = new Insets(20, 30, 5, 30);
		add(createFieldLabel("Customer Name"), gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 15, 30);
		add(createValuePanel(customerName), gbc);

		// Type
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);
		add(createFieldLabel("Type"), gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 15, 30);
		add(createValuePanel(type), gbc);

		// Contact Number
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);
		add(createFieldLabel("Contact Number"), gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 15, 30);
		add(createValuePanel(contactNumber), gbc);

		// Address
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);
		add(createFieldLabel("Address"), gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 15, 30);
		add(createValuePanel(address), gbc);

		// Note
		gbc.gridy++;
		gbc.insets = new Insets(10, 30, 5, 30);
		add(createFieldLabel("Note"), gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 20, 30);
		JPanel notePanel = createValuePanel(note);
		notePanel.setPreferredSize(new Dimension(450, 60)); // Taller for multi-line notes
		add(notePanel, gbc);

		// Close Button
		gbc.gridy++;
		gbc.insets = new Insets(20, 30, 20, 30);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		JButton closeBtn = createStyledButton("Close", ACCENT_GOLD);
		closeBtn.setPreferredSize(new Dimension(150, 40));
		closeBtn.addActionListener(e -> dispose());
		add(closeBtn, gbc);

		pack();
		setMinimumSize(new Dimension(550, 650));
		setLocationRelativeTo(getParent());
	}

	/**
	 * Create field label
	 */
	private JLabel createFieldLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(new Font("Arial", Font.BOLD, 14));
		label.setForeground(TEXT_DARK);
		return label;
	}

	/**
	 * Create value panel (styled like a disabled text field)
	 */
	private JPanel createValuePanel(String value) {

		if (value == null || value.isBlank()) {
			value = "<i>N/A</i>"; // You can customize this
		}

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(new Color(248, 245, 240)); // Light background
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1),
				new EmptyBorder(10, 10, 10, 10)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel valueLabel = new JLabel("<html>" + value.replace("\n", "<br>") + "</html>");
		valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		valueLabel.setForeground(TEXT_DARK);
		panel.add(valueLabel, gbc);

		panel.setPreferredSize(new Dimension(450, 40));
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
				button.setBackground(bgColor.brighter());
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
	public static void show(Window parent, int branchId) {
		BranchDetails dialog = new BranchDetails(parent, branchId);
		dialog.setVisible(true);
	}
}