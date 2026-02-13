package com.gierza_molases.molases_app.ui.dialogs.Maintenance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class PreviewDialog extends JDialog {

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_MEDIUM = new Color(120, 90, 70);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);

	public PreviewDialog(Window parent, int count, int selectedYears) {
		super(parent, "Preview Results", ModalityType.APPLICATION_MODAL);
		initializeUI(count, selectedYears);
		setLocationRelativeTo(parent);
	}

	private void initializeUI(int count, int selectedYears) {
		setLayout(new BorderLayout());
		setSize(450, 250);

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 15, 0);
		gbc.anchor = GridBagConstraints.CENTER;

		// Icon and title
		JLabel titleLabel = new JLabel("Preview Results");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		titleLabel.setForeground(TEXT_DARK);
		contentPanel.add(titleLabel, gbc);

		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 10, 0);
		JLabel countLabel = new JLabel("<html><div style='text-align: center;'>Found <b>" + count
				+ " deliveries</b> eligible for deletion<br>(older than " + selectedYears + " year"
				+ (selectedYears > 1 ? "s" : "") + ")</div></html>");
		countLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		countLabel.setForeground(TEXT_DARK);
		contentPanel.add(countLabel, gbc);

		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 25, 0);
		JLabel infoLabel = new JLabel(
				"<html><div style='text-align: center;'>These deliveries are fully paid and<br>will be permanently removed.</div></html>");
		infoLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		infoLabel.setForeground(TEXT_MEDIUM);
		contentPanel.add(infoLabel, gbc);

		gbc.gridy = 3;
		gbc.insets = new Insets(0, 0, 0, 0);
		JButton closeButton = createStyledButton("Close", ACCENT_GOLD, Color.WHITE);
		closeButton.addActionListener(e -> dispose());
		contentPanel.add(closeButton, gbc);

		add(contentPanel, BorderLayout.CENTER);
	}

	private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 14));
		button.setForeground(fgColor);
		button.setBackground(bgColor);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
}