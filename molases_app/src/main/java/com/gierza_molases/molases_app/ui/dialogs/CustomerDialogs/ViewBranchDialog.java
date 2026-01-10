
package com.gierza_molases.molases_app.ui.dialogs.CustomerDialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.Context.AppContext;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.service.BranchService;

public class ViewBranchDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	// Color Palette - matching the application theme
	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);

	private Customer customer;
	private List<Branch> branches;
	private JPanel branchesContainer;
	private javax.swing.border.TitledBorder branchesBorder;

	private final BranchService branchService = AppContext.branchService;

	/**
	 * Constructor
	 */
	public ViewBranchDialog(Window parent, Customer customer) {
		super(parent, "	 Branches - " + customer.getDisplayName(), ModalityType.APPLICATION_MODAL);

		this.customer = customer;
		this.branches = new ArrayList<>();

		setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);

		// Main content panel with padding
		JPanel mainContent = new JPanel(new BorderLayout(0, 20));
		mainContent.setBackground(Color.WHITE);
		mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

		// Header section
		JPanel headerSection = createHeaderSection();
		mainContent.add(headerSection, BorderLayout.NORTH);

		// Branches section
		JPanel branchesSection = createBranchesSection();
		mainContent.add(branchesSection, BorderLayout.CENTER);

		// Button panel
		JPanel buttonPanel = createButtonPanel();
		mainContent.add(buttonPanel, BorderLayout.SOUTH);

		add(mainContent);

		// Load branches data
		loadBranchesData();

		// Dialog settings
		setSize(700, 600);
		setMinimumSize(new Dimension(700, 600));
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void loadBranchesData() {
		try {

			branches = branchService.getBranchesByCustomerId(customer.getId());

			refreshBranchesDisplay();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * Create header section with customer info
	 */
	private JPanel createHeaderSection() {
		JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
		headerPanel.setBackground(Color.WHITE);
		headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

		// Title
		JLabel titleLabel = new JLabel("Branch Locations");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(TEXT_DARK);
		headerPanel.add(titleLabel, BorderLayout.NORTH);

		// Customer info
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		infoPanel.setBackground(Color.WHITE);

		JLabel customerLabel = new JLabel("Customer: " + customer.getDisplayName());
		customerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		customerLabel.setForeground(new Color(100, 80, 60));
		customerLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		JLabel contactLabel = new JLabel(
				"Contact: " + (customer.getFormatttedNumber() != null ? customer.getFormatttedNumber() : "N/A"));
		contactLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		contactLabel.setForeground(new Color(100, 80, 60));
		contactLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		infoPanel.add(customerLabel);
		infoPanel.add(Box.createVerticalStrut(3));
		infoPanel.add(contactLabel);

		headerPanel.add(infoPanel, BorderLayout.CENTER);

		return headerPanel;
	}

	/**
	 * Create branches section with scrollable list
	 */
	private JPanel createBranchesSection() {
		JPanel section = new JPanel(new BorderLayout());
		section.setBackground(Color.WHITE);

		branchesBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
				"All Branches (" + branches.size() + ")", javax.swing.border.TitledBorder.LEFT,
				javax.swing.border.TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), TEXT_DARK);

		section.setBorder(BorderFactory.createCompoundBorder(branchesBorder, new EmptyBorder(15, 15, 15, 15)));

		// Branches display container
		branchesContainer = new JPanel();
		branchesContainer.setLayout(new BoxLayout(branchesContainer, BoxLayout.Y_AXIS));
		branchesContainer.setBackground(Color.WHITE);

		JScrollPane branchesScroll = new JScrollPane(branchesContainer);
		branchesScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
		branchesScroll.setBackground(CONTENT_BG);
		branchesScroll.getViewport().setBackground(CONTENT_BG);
		branchesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		branchesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		section.add(branchesScroll, BorderLayout.CENTER);

		return section;
	}

	/**
	 * Refresh the branches display
	 */
	private void refreshBranchesDisplay() {
		branchesContainer.removeAll();

		if (branches.isEmpty()) {
			// Empty state
			JLabel emptyLabel = new JLabel("No branches found for this customer.");
			emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
			emptyLabel.setForeground(new Color(150, 150, 150));
			emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
			emptyLabel.setBorder(new EmptyBorder(80, 20, 80, 20));
			branchesContainer.add(emptyLabel);
		} else {
			// Display each branch as a card
			for (int i = 0; i < branches.size(); i++) {
				Branch branch = branches.get(i);
				JPanel branchCard = createBranchCard(branch, i + 1);
				branchesContainer.add(branchCard);

				// Add spacing between cards
				if (i < branches.size() - 1) {
					branchesContainer.add(Box.createVerticalStrut(12));
				}
			}
		}

		// Update the border title with count
		updateBranchCountTitle();

		branchesContainer.revalidate();
		branchesContainer.repaint();
	}

	/**
	 * Update the section title with branch count
	 */
	private void updateBranchCountTitle() {
		branchesBorder.setTitle("All Branches (" + branches.size() + ")");
		branchesContainer.getParent().getParent().repaint(); // Repaint the section
	}

	/**
	 * Create a branch card/panel (read-only)
	 */
	private JPanel createBranchCard(Branch branch, int index) {
		JPanel card = new JPanel(new BorderLayout(15, 0));
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
				new EmptyBorder(15, 18, 15, 18)));
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

		// Index badge
		JPanel badgePanel = new JPanel(new BorderLayout());
		badgePanel.setBackground(SIDEBAR_ACTIVE);
		badgePanel.setPreferredSize(new Dimension(45, 45));
		badgePanel.setBorder(BorderFactory.createLineBorder(SIDEBAR_ACTIVE, 2));

		JLabel indexLabel = new JLabel(String.valueOf(index));
		indexLabel.setFont(new Font("Arial", Font.BOLD, 18));
		indexLabel.setForeground(TEXT_LIGHT);
		indexLabel.setHorizontalAlignment(SwingConstants.CENTER);
		badgePanel.add(indexLabel, BorderLayout.CENTER);

		card.add(badgePanel, BorderLayout.WEST);

		// Content panel (address and note)
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(Color.WHITE);

		// Address
		JLabel addressLabel = new JLabel("📍 " + branch.getAddress());
		addressLabel.setFont(new Font("Arial", Font.BOLD, 15));
		addressLabel.setForeground(TEXT_DARK);
		addressLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		// Note
		String noteText = (branch.getNote() == null || branch.getNote().trim().isEmpty()) ? "No additional notes"
				: branch.getNote();
		JLabel noteLabel = new JLabel("📝 " + noteText);
		noteLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		noteLabel.setForeground(new Color(120, 90, 70));
		noteLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		contentPanel.add(addressLabel);
		contentPanel.add(Box.createVerticalStrut(6));
		contentPanel.add(noteLabel);

		card.add(contentPanel, BorderLayout.CENTER);

		return card;
	}

	/**
	 * Create button panel with Close button
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(20, 0, 0, 0));

		// Close button
		JButton closeBtn = createStyledButton("Close", ACCENT_GOLD);
		closeBtn.setPreferredSize(new Dimension(150, 42));
		closeBtn.addActionListener(e -> dispose());

		panel.add(closeBtn);

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
}