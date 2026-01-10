package com.gierza_molases.molases_app.ui.dialogs.CustomerDialogs;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.gierza_molases.molases_app.Context.AppContext;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.service.CustomerService;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.util.Listener;

public class AddCustomerDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final CustomerService customerService = AppContext.customerService;
	// Color Palette - matching Main.java
	private static final Color SIDEBAR_BG = new Color(62, 39, 35);
	private static final Color SIDEBAR_HOVER = new Color(92, 64, 51);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color HEADER_BG = new Color(245, 239, 231);
	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);

	// Customer type
	private JRadioButton individualRadio;
	private JRadioButton companyRadio;

	// Form fields - Individual
	private JPanel individualFieldsPanel;
	private JTextField firstNameField;
	private JTextField middleNameField;
	private JTextField lastNameField;

	// Form fields - Company
	private JPanel companyFieldsPanel;
	private JTextField companyNameField;

	// Common fields
	private JTextField addressField;
	private JTextField contactNumberField;

	// Branch fields
	private JCheckBox useCustomerAddressCheckbox;
	private JTextField branchAddressField;
	private JTextField branchNoteField;
	private JButton addBranchBtn;
	private JPanel branchesContainer;
	private List<Branch> branches;

	// listener
	private Listener listener;

	// Buttons (need reference to disable during save)
	private JButton saveBtn;
	private JButton cancelBtn;

	/**
	 * Constructor
	 */
	public AddCustomerDialog(Window parent, Listener listener) {

		super(parent, "Add New Customer", ModalityType.APPLICATION_MODAL);

		this.listener = listener;
		branches = new ArrayList<>();

		setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);

		// Main content panel with padding
		JPanel mainContent = new JPanel(new BorderLayout(0, 20));
		mainContent.setBackground(Color.WHITE);
		mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

		// Create sections
		JPanel customerInfoSection = createCustomerInfoSection();
		JPanel branchesSection = createBranchesSection();
		JPanel buttonPanel = createButtonPanel();

		// Combine customer info and branches in a scrollable panel
		JPanel formPanel = new JPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
		formPanel.setBackground(Color.WHITE);
		formPanel.add(customerInfoSection);
		formPanel.add(Box.createVerticalStrut(25));
		formPanel.add(branchesSection);

		JScrollPane scrollPane = new JScrollPane(formPanel);
		scrollPane.setBorder(null);
		scrollPane.getViewport().setBackground(Color.WHITE);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		mainContent.add(scrollPane, BorderLayout.CENTER);
		mainContent.add(buttonPanel, BorderLayout.SOUTH);

		add(mainContent);

		// Dialog settings
		setSize(700, 700);
		setMinimumSize(new Dimension(700, 700));
		setLocationRelativeTo(null); // Center on screen
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Create customer information section
	 */
	private JPanel createCustomerInfoSection() {
		JPanel section = new JPanel(new GridBagLayout());
		section.setBackground(Color.WHITE);
		section.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
						"Customer Information", javax.swing.border.TitledBorder.LEFT,
						javax.swing.border.TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), TEXT_DARK),
				new EmptyBorder(15, 15, 15, 15)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 5, 8, 5);

		// Row 0: Customer Type Radio Buttons
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		section.add(createCustomerTypePanel(), gbc);

		// Row 1: Individual Fields (First Name, Middle Name, Last Name)
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		individualFieldsPanel = createIndividualFieldsPanel();
		section.add(individualFieldsPanel, gbc);

		// Row 2: Company Field (Company Name)
		gbc.gridy = 2;
		companyFieldsPanel = createCompanyFieldsPanel();
		companyFieldsPanel.setVisible(false);
		section.add(companyFieldsPanel, gbc);

		// Row 3: Address (common for both types)
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		addressField = new JTextField();
		JPanel addressPanel = createLabeledField("Address *", addressField);
		section.add(addressPanel, gbc);

		// Add document listener to address field
		addressField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				updateCheckboxState();
			}

			public void removeUpdate(DocumentEvent e) {
				updateCheckboxState();
			}

			public void insertUpdate(DocumentEvent e) {
				updateCheckboxState();
			}
		});

		// Row 4: Contact Number
		gbc.gridy = 4;
		contactNumberField = new JTextField();
		section.add(createLabeledField("Contact Number *", contactNumberField), gbc);

		return section;
	}

	/**
	 * Create customer type selection panel
	 */
	private JPanel createCustomerTypePanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
		panel.setBackground(Color.WHITE);

		JLabel typeLabel = new JLabel("Customer Type:");
		typeLabel.setFont(new Font("Arial", Font.BOLD, 13));
		typeLabel.setForeground(TEXT_DARK);

		individualRadio = new JRadioButton("Individual");
		individualRadio.setFont(new Font("Arial", Font.PLAIN, 13));
		individualRadio.setBackground(Color.WHITE);
		individualRadio.setFocusPainted(false);
		individualRadio.setSelected(true);

		companyRadio = new JRadioButton("Company");
		companyRadio.setFont(new Font("Arial", Font.PLAIN, 13));
		companyRadio.setBackground(Color.WHITE);
		companyRadio.setFocusPainted(false);

		ButtonGroup group = new ButtonGroup();
		group.add(individualRadio);
		group.add(companyRadio);

		// Add listeners to toggle field visibility
		individualRadio.addActionListener(e -> toggleCustomerTypeFields());
		companyRadio.addActionListener(e -> toggleCustomerTypeFields());

		panel.add(typeLabel);
		panel.add(individualRadio);
		panel.add(companyRadio);

		return panel;
	}

	/**
	 * Create individual fields panel
	 */
	private JPanel createIndividualFieldsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 5, 8, 5);
		gbc.gridy = 0;

		// First Name
		gbc.gridx = 0;
		gbc.weightx = 0.33;
		firstNameField = new JTextField();
		panel.add(createLabeledField("First Name *", firstNameField), gbc);

		// Middle Name
		gbc.gridx = 1;
		middleNameField = new JTextField();
		panel.add(createLabeledField("Middle Name", middleNameField), gbc);

		// Last Name
		gbc.gridx = 2;
		lastNameField = new JTextField();
		panel.add(createLabeledField("Last Name *", lastNameField), gbc);

		return panel;
	}

	/**
	 * Create company fields panel
	 */
	private JPanel createCompanyFieldsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 5, 8, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;

		companyNameField = new JTextField();
		panel.add(createLabeledField("Company Name *", companyNameField), gbc);

		return panel;
	}

	/**
	 * Toggle between individual and company fields
	 */
	private void toggleCustomerTypeFields() {
		boolean isIndividual = individualRadio.isSelected();
		individualFieldsPanel.setVisible(isIndividual);
		companyFieldsPanel.setVisible(!isIndividual);

		// Revalidate and repaint to update layout
		individualFieldsPanel.revalidate();
		individualFieldsPanel.repaint();
		companyFieldsPanel.revalidate();
		companyFieldsPanel.repaint();
	}

	/**
	 * Create branches section
	 */
	private JPanel createBranchesSection() {
		JPanel section = new JPanel(new BorderLayout(0, 15));
		section.setBackground(Color.WHITE);
		section.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
								"Branch Locations", javax.swing.border.TitledBorder.LEFT,
								javax.swing.border.TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), TEXT_DARK),
						new EmptyBorder(15, 15, 15, 15)));

		// Top panel with checkbox
		JPanel topPanel = new JPanel(new BorderLayout(0, 10));
		topPanel.setBackground(Color.WHITE);

		// Checkbox to use customer address
		useCustomerAddressCheckbox = new JCheckBox("Use customer address as only branch location");
		useCustomerAddressCheckbox.setFont(new Font("Arial", Font.PLAIN, 13));
		useCustomerAddressCheckbox.setBackground(Color.WHITE);
		useCustomerAddressCheckbox.setFocusPainted(false);
		useCustomerAddressCheckbox.setEnabled(false);
		useCustomerAddressCheckbox.addActionListener(e -> toggleBranchInputs());

		topPanel.add(useCustomerAddressCheckbox, BorderLayout.NORTH);

		// Input panel for adding new branch
		JPanel inputPanel = new JPanel(new GridBagLayout());
		inputPanel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);

		// Address field
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.5;
		branchAddressField = new JTextField();
		inputPanel.add(createLabeledField("Address", branchAddressField), gbc);

		// Note field
		gbc.gridx = 1;
		branchNoteField = new JTextField();
		inputPanel.add(createLabeledField("Note", branchNoteField), gbc);

		// Add Branch button
		gbc.gridx = 2;
		gbc.weightx = 0;
		gbc.insets = new Insets(20, 10, 5, 5);
		addBranchBtn = createStyledButton("+ Add Branch", SIDEBAR_ACTIVE);
		addBranchBtn.setPreferredSize(new Dimension(140, 38));
		addBranchBtn.addActionListener(e -> addBranch());
		inputPanel.add(addBranchBtn, gbc);

		topPanel.add(inputPanel, BorderLayout.CENTER);
		section.add(topPanel, BorderLayout.NORTH);

		// Branches display container
		branchesContainer = new JPanel();
		branchesContainer.setLayout(new BoxLayout(branchesContainer, BoxLayout.Y_AXIS));
		branchesContainer.setBackground(Color.WHITE);

		JScrollPane branchesScroll = new JScrollPane(branchesContainer);
		branchesScroll.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 0, 0, 0),
				BorderFactory.createLineBorder(new Color(220, 210, 200), 1)));
		branchesScroll.setBackground(CONTENT_BG);
		branchesScroll.getViewport().setBackground(CONTENT_BG);
		branchesScroll.setPreferredSize(new Dimension(0, 200));
		branchesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		branchesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// Empty state message
		if (branches.isEmpty()) {
			JLabel emptyLabel = new JLabel("No branches added yet. Add a branch above.");
			emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
			emptyLabel.setForeground(new Color(150, 150, 150));
			emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
			emptyLabel.setBorder(new EmptyBorder(60, 20, 60, 20));
			branchesContainer.add(emptyLabel);
		}

		section.add(branchesScroll, BorderLayout.CENTER);

		return section;
	}

	/**
	 * Update checkbox state based on address field
	 */
	private void updateCheckboxState() {
		String address = addressField.getText().trim();
		useCustomerAddressCheckbox.setEnabled(!address.isEmpty());

		if (address.isEmpty() && useCustomerAddressCheckbox.isSelected()) {
			useCustomerAddressCheckbox.setSelected(false);
			toggleBranchInputs();
		}
	}

	/**
	 * Toggle branch input fields based on checkbox
	 */
	private void toggleBranchInputs() {
		boolean useCustomerAddress = useCustomerAddressCheckbox.isSelected();

		// Enable/disable manual branch input fields
		branchAddressField.setEnabled(!useCustomerAddress);
		branchNoteField.setEnabled(!useCustomerAddress);
		addBranchBtn.setEnabled(!useCustomerAddress);

		if (useCustomerAddress) {
			// Clear manual input fields
			branchAddressField.setText("");
			branchNoteField.setText("");

			// Clear manually added branches
			branches.clear();
			refreshBranchesDisplay();

			// add the customers address as branch
			String address = addressField.getText().trim();
			Branch branch = new Branch(address, null);
			branches.add(branch);
		} else {
			// Clear manually added branches
			branches.clear();
			refreshBranchesDisplay();
		}
	}

	/**
	 * Add a new branch
	 */
	private void addBranch() {
		String address = branchAddressField.getText().trim();
		String note = branchNoteField.getText().trim();

		if (address.isEmpty()) {
			ToastNotification.showError(this, "Branch address is required");
			return;
		}

		Branch branch = new Branch(address, note);
		branches.add(branch);

		// Clear input fields
		branchAddressField.setText("");
		branchNoteField.setText("");

		// Refresh branches display
		refreshBranchesDisplay();
	}

	/**
	 * Remove a branch
	 */
	private void removeBranch(Branch branch) {
		branches.remove(branch);
		refreshBranchesDisplay();
	}

	/**
	 * Refresh the branches display
	 */
	private void refreshBranchesDisplay() {
		branchesContainer.removeAll();

		if (branches.isEmpty()) {
			JLabel emptyLabel = new JLabel("No branches added yet. Add a branch above.");
			emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
			emptyLabel.setForeground(new Color(150, 150, 150));
			emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
			emptyLabel.setBorder(new EmptyBorder(60, 20, 60, 20));
			branchesContainer.add(emptyLabel);
		} else {
			for (Branch branch : branches) {
				JPanel branchCard = createBranchCard(branch);
				branchesContainer.add(branchCard);
				branchesContainer.add(Box.createVerticalStrut(10));
			}
		}

		branchesContainer.revalidate();
		branchesContainer.repaint();
	}

	/**
	 * Create a branch card/panel
	 */
	private JPanel createBranchCard(Branch branch) {
		JPanel card = new JPanel(new BorderLayout(15, 0));
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1),
				new EmptyBorder(12, 15, 12, 15)));
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

		// Content panel (address and note)
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(Color.WHITE);

		// Address
		JLabel addressLabel = new JLabel("📍 " + branch.getAddress());
		addressLabel.setFont(new Font("Arial", Font.BOLD, 14));
		addressLabel.setForeground(TEXT_DARK);
		addressLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		// Note
		JLabel noteLabel = new JLabel(branch.getNote().isEmpty() ? "No note" : branch.getNote());
		noteLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		noteLabel.setForeground(new Color(120, 90, 70));
		noteLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		contentPanel.add(addressLabel);
		contentPanel.add(Box.createVerticalStrut(4));
		contentPanel.add(noteLabel);

		card.add(contentPanel, BorderLayout.CENTER);

		// Remove button
		JButton removeBtn = new JButton("🗑");
		removeBtn.setFont(new Font("Arial", Font.PLAIN, 16));
		removeBtn.setBackground(new Color(220, 80, 80));
		removeBtn.setForeground(Color.WHITE);
		removeBtn.setFocusPainted(false);
		removeBtn.setBorderPainted(false);
		removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		removeBtn.setPreferredSize(new Dimension(45, 45));
		removeBtn.setToolTipText("Remove branch");

		removeBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				removeBtn.setBackground(new Color(200, 60, 60));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				removeBtn.setBackground(new Color(220, 80, 80));
			}
		});

		removeBtn.addActionListener(e -> removeBranch(branch));

		card.add(removeBtn, BorderLayout.EAST);

		return card;
	}

	/**
	 * Create labeled field
	 */
	private JPanel createLabeledField(String labelText, JTextField field) {
		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setBackground(Color.WHITE);

		JLabel label = new JLabel(labelText);
		label.setFont(new Font("Arial", Font.BOLD, 13));
		label.setForeground(TEXT_DARK);

		field.setFont(new Font("Arial", Font.PLAIN, 14));
		field.setPreferredSize(new Dimension(0, 38));
		field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 190, 180), 1),
				new EmptyBorder(5, 10, 5, 10)));

		panel.add(label, BorderLayout.NORTH);
		panel.add(field, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Create button panel (Save and Cancel)
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(20, 0, 0, 0));

		// Cancel button
		cancelBtn = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 42));
		cancelBtn.addActionListener(e -> dispose());

		// Save button
		saveBtn = createStyledButton("Save Customer", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(150, 42));
		saveBtn.addActionListener(e -> onSaveButtonClicked());

		panel.add(cancelBtn);
		panel.add(saveBtn);

		return panel;
	}

	/**
	 * Handle save button click
	 */
	private void onSaveButtonClicked() {
		// Validate form data FIRST (on UI thread - this is just reading text fields)
		String validationError = validateForm();
		if (validationError != null) {
			ToastNotification.showError(this, validationError);
			return;
		}

		// Collect data to save (on UI thread - just reading values)
		final String firstName = firstNameField.getText().trim();
		final String middleName = middleNameField.getText().trim();
		final String lastName = lastNameField.getText().trim();
		final String companyName = companyNameField.getText().trim();
		final boolean isIndividual = individualRadio.isSelected();
		final String address = addressField.getText().trim();
		final String contactNumber = contactNumberField.getText().trim();
		final List<Branch> branchesToSave = new ArrayList<>(branches);

		// Disable buttons during save
		saveBtn.setEnabled(false);
		cancelBtn.setEnabled(false);
		saveBtn.setText("Saving...");

		// Save on background thread
		saveCustomerAsync(isIndividual, firstName, middleName, lastName, companyName, contactNumber, address,
				branchesToSave);
	}

	/**
	 * Validate form (UI thread only - just reading text fields)
	 * 
	 * @return error message if validation fails, null if valid
	 */
	private String validateForm() {
		boolean isIndividual = individualRadio.isSelected();

		// Validate name fields based on customer type
		if (isIndividual) {
			if (firstNameField.getText().trim().isEmpty()) {
				firstNameField.requestFocus();
				return "First Name is required";
			}
			if (lastNameField.getText().trim().isEmpty()) {
				lastNameField.requestFocus();
				return "Last Name is required";
			}
		} else {
			if (companyNameField.getText().trim().isEmpty()) {
				companyNameField.requestFocus();
				return "Company Name is required";
			}
		}

		// Validate address
		if (addressField.getText().trim().isEmpty()) {
			addressField.requestFocus();
			return "Address is required";
		}

		// Validate contact number
		if (contactNumberField.getText().trim().isEmpty()) {
			contactNumberField.requestFocus();
			return "Contact Number is required";
		}

		// Validate branches
		boolean hasCustomerAddressBranch = useCustomerAddressCheckbox.isSelected();
		if (!hasCustomerAddressBranch && branches.isEmpty()) {
			return "At least one branch is required";
		}

		return null; // Valid
	}

	/**
	 * Save customer asynchronously (BACKGROUND THREAD)
	 */
	private void saveCustomerAsync(boolean isIndividual, String firstName, String middleName, String lastName,
			String companyName, String contactNumber, String address, List<Branch> branchesToSave) {
		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			private Exception savedException = null;

			@Override
			protected Void doInBackground() throws Exception {
				// This runs on background thread ✔
				try {
					if (isIndividual) {
						customerService.addCustomerAsIndividualType(firstName, middleName, lastName, contactNumber,
								address, branchesToSave);
					} else {
						customerService.addCustomerAsCompanyType(companyName, contactNumber, address, branchesToSave);
					}
				} catch (Exception e) {
					savedException = e;
				}
				return null;
			}

			@Override
			protected void done() {
				// This runs on UI thread ✔
				// Re-enable buttons
				saveBtn.setEnabled(true);
				cancelBtn.setEnabled(true);
				saveBtn.setText("Save Customer");

				if (savedException != null) {
					// Save failed
					ToastNotification.showError(AddCustomerDialog.this,
							"Failed to save customer: " + savedException.getMessage());
					savedException.printStackTrace();
				} else {
					// Save successful
					// Notify listener (which will refresh the table and show toast)
					if (listener != null) {
						listener.onAction();
					}
					dispose();
				}
			}
		};
		worker.execute();
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