package com.gierza_molases.molases_app.ui.components.delivery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.toedter.calendar.JDateChooser;

public class DeliveryFormStep1 {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color STEP_INACTIVE = new Color(200, 190, 180);
	private static final Color PLACEHOLDER_COLOR = new Color(150, 150, 150);

	private JTextField deliveryNameField;
	private JDateChooser deliveryDateChooser;
	private JTextField expenseNameInput;
	private JTextField expenseAmountInput;
	private DefaultTableModel expenseTableModel;
	private JTable expenseTable;
	private JLabel totalLabel;

	public JPanel createPanel() {
		JPanel formPanel = new JPanel(new GridBagLayout());
		formPanel.setBackground(CONTENT_BG);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Top Section: Delivery Name and Date side by side
		JPanel topSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
		topSection.setBackground(CONTENT_BG);

		// Delivery Name
		JLabel nameLabel = new JLabel("Delivery Name: *");
		nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
		nameLabel.setForeground(TEXT_DARK);
		topSection.add(nameLabel);

		deliveryNameField = createPlaceholderTextField("e.g., Manila Delivery #001", 20);
		deliveryNameField.setPreferredSize(new Dimension(250, 35));
		topSection.add(deliveryNameField);

		// Delivery Date
		JLabel dateLabel = new JLabel("Date: *");
		dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
		dateLabel.setForeground(TEXT_DARK);
		topSection.add(dateLabel);

		deliveryDateChooser = new JDateChooser();
		deliveryDateChooser.setDateFormatString("MM/dd/yyyy");
		deliveryDateChooser.setPreferredSize(new Dimension(200, 35));
		deliveryDateChooser.setFont(new Font("Arial", Font.PLAIN, 14));
		topSection.add(deliveryDateChooser);

		gbc.gridwidth = 2;
		formPanel.add(topSection, gbc);

		// Expenses Section Title
		gbc.gridy++;
		gbc.insets = new Insets(30, 10, 10, 10);
		JLabel expensesTitle = new JLabel("Expenses *");
		expensesTitle.setFont(new Font("Arial", Font.BOLD, 16));
		expensesTitle.setForeground(TEXT_DARK);
		formPanel.add(expensesTitle, gbc);

		// Expenses Table
		gbc.gridy++;
		gbc.insets = new Insets(10, 10, 10, 10);
		JPanel tablePanel = createExpensesTable();
		formPanel.add(tablePanel, gbc);

		// Input Row for Adding Expenses
		gbc.gridy++;
		gbc.insets = new Insets(10, 10, 10, 10);
		JPanel inputPanel = createExpenseInputPanel();
		formPanel.add(inputPanel, gbc);

		// Add vertical glue to push everything to the top
		gbc.gridy++;
		gbc.weighty = 1.0;
		formPanel.add(Box.createVerticalGlue(), gbc);

		return formPanel;
	}

	private JPanel createExpensesTable() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(CONTENT_BG);
		tablePanel.setBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1));
		tablePanel.setPreferredSize(new Dimension(700, 250));

		// Create table model
		String[] columnNames = { "Expense Name", "Amount", "Action" };
		expenseTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 2;
			}
		};

		expenseTable = new JTable(expenseTableModel);
		expenseTable.setFont(new Font("Arial", Font.PLAIN, 14));
		expenseTable.setRowHeight(35);
		expenseTable.setBackground(Color.WHITE);
		expenseTable.setGridColor(STEP_INACTIVE);

		// Style table header
		JTableHeader header = expenseTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(new Color(240, 235, 225));
		header.setForeground(TEXT_DARK);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));

		// Set column widths
		expenseTable.getColumnModel().getColumn(0).setPreferredWidth(300);
		expenseTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		expenseTable.getColumnModel().getColumn(2).setPreferredWidth(150);

		// Add Remove button renderer and editor
		expenseTable.getColumnModel().getColumn(2).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
			JButton removeBtn = UIComponentFactory.createStyledButton("Remove", new Color(180, 50, 50));
			removeBtn.setPreferredSize(new Dimension(90, 28));
			return removeBtn;
		});

		expenseTable.getColumnModel().getColumn(2).setCellEditor(new javax.swing.DefaultCellEditor(new JTextField()) {
			private JButton button = UIComponentFactory.createStyledButton("Remove", new Color(180, 50, 50));

			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
					int column) {
				button.addActionListener(e -> {
					removeExpenseRow(row);
					fireEditingStopped();
				});
				return button;
			}

			@Override
			public Object getCellEditorValue() {
				return "";
			}
		});

		JScrollPane scrollPane = new JScrollPane(expenseTable);
		scrollPane.setBorder(null);
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		// Total Panel
		JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
		totalPanel.setBackground(new Color(240, 235, 225));
		totalPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, STEP_INACTIVE));

		JLabel totalTextLabel = new JLabel("Total:");
		totalTextLabel.setFont(new Font("Arial", Font.BOLD, 15));
		totalTextLabel.setForeground(TEXT_DARK);
		totalPanel.add(totalTextLabel);

		totalLabel = new JLabel("₱0.00");
		totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
		totalLabel.setForeground(ACCENT_GOLD);
		totalPanel.add(totalLabel);

		tablePanel.add(totalPanel, BorderLayout.SOUTH);

		return tablePanel;
	}

	private JPanel createExpenseInputPanel() {
		JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
		inputPanel.setBackground(CONTENT_BG);

		JLabel nameInputLabel = new JLabel("Expense Name:");
		nameInputLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		nameInputLabel.setForeground(TEXT_DARK);
		inputPanel.add(nameInputLabel);

		expenseNameInput = createPlaceholderTextField("e.g., Fuel, Toll Fee, Meals", 20);
		expenseNameInput.setPreferredSize(new Dimension(250, 35));
		inputPanel.add(expenseNameInput);

		JLabel amountInputLabel = new JLabel("Amount:");
		amountInputLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		amountInputLabel.setForeground(TEXT_DARK);
		inputPanel.add(amountInputLabel);

		expenseAmountInput = createPlaceholderTextField("0.00", 12);
		expenseAmountInput.setPreferredSize(new Dimension(150, 35));
		inputPanel.add(expenseAmountInput);

		JButton addBtn = UIComponentFactory.createStyledButton("+ Add Expense", ACCENT_GOLD);
		addBtn.setPreferredSize(new Dimension(140, 35));
		addBtn.addActionListener(e -> addExpenseToTable());
		inputPanel.add(addBtn);

		return inputPanel;
	}

	private JTextField createPlaceholderTextField(String placeholder, int columns) {
		JTextField textField = new JTextField(columns);
		textField.setFont(new Font("Arial", Font.PLAIN, 14));
		textField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 190, 180), 1), new EmptyBorder(5, 10, 5, 10)));

		textField.setText(placeholder);
		textField.setForeground(PLACEHOLDER_COLOR);

		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (textField.getText().equals(placeholder)) {
					textField.setText("");
					textField.setForeground(TEXT_DARK);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (textField.getText().trim().isEmpty()) {
					textField.setText(placeholder);
					textField.setForeground(PLACEHOLDER_COLOR);
				}
			}
		});

		return textField;
	}

	private String getTextFieldValue(JTextField textField, String placeholder) {
		String text = textField.getText();
		if (text.equals(placeholder)) {
			return "";
		}
		return text.trim();
	}

	private void addExpenseToTable() {
		String name = getTextFieldValue(expenseNameInput, "e.g., Fuel, Toll Fee, Meals");
		String amountStr = getTextFieldValue(expenseAmountInput, "0.00");

		if (name.isEmpty() || amountStr.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Please fill in both expense name and amount", "Input Required",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			double amount = Double.parseDouble(amountStr);
			if (amount <= 0) {
				JOptionPane.showMessageDialog(null, "Amount must be greater than 0", "Invalid Amount",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			expenseTableModel.addRow(new Object[] { name, String.format("₱%.2f", amount), "Remove" });

			expenseNameInput.setText("e.g., Fuel, Toll Fee, Meals");
			expenseNameInput.setForeground(PLACEHOLDER_COLOR);
			expenseAmountInput.setText("0.00");
			expenseAmountInput.setForeground(PLACEHOLDER_COLOR);
			expenseNameInput.requestFocus();

			updateTotal();

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Please enter a valid number for amount", "Invalid Amount",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void removeExpenseRow(int row) {
		expenseTableModel.removeRow(row);
		updateTotal();
	}

	private void updateTotal() {
		double total = 0.0;

		if (expenseTableModel.getColumnCount() > 0) {
			for (int i = 0; i < expenseTableModel.getRowCount(); i++) {
				String amountStr = expenseTableModel.getValueAt(i, 1).toString();
				amountStr = amountStr.replace("₱", "").replace(",", "").trim();
				total += Double.parseDouble(amountStr);
			}
		}

		totalLabel.setText(String.format("₱%.2f", total));
	}

	public boolean validate() {
		String deliveryName = getTextFieldValue(deliveryNameField, "e.g., Manila Delivery #001");
		if (deliveryName.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Please enter a delivery name", "Validation Error",
					JOptionPane.ERROR_MESSAGE);
			deliveryNameField.requestFocus();
			return false;
		}

		if (deliveryDateChooser.getDate() == null) {
			JOptionPane.showMessageDialog(null, "Please select a delivery date", "Validation Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (expenseTableModel.getRowCount() == 0) {
			JOptionPane.showMessageDialog(null, "Please add at least one expense", "Validation Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	public Step1Data getData() {
		Step1Data data = new Step1Data();
		data.deliveryName = getTextFieldValue(deliveryNameField, "e.g., Manila Delivery #001");
		data.deliveryDate = deliveryDateChooser.getDate();
		data.expenses = new HashMap<>();

		for (int i = 0; i < expenseTableModel.getRowCount(); i++) {
			String name = expenseTableModel.getValueAt(i, 0).toString();
			String amountStr = expenseTableModel.getValueAt(i, 1).toString();
			amountStr = amountStr.replace("₱", "").replace(",", "").trim();
			double amount = Double.parseDouble(amountStr);
			data.expenses.put(name, amount);
		}

		return data;
	}

	public void loadData(Step1Data data) {
		if (data == null)
			return;

		deliveryNameField.setText(data.deliveryName);
		deliveryNameField.setForeground(TEXT_DARK);
		deliveryDateChooser.setDate(data.deliveryDate);

		// Clear and reload expenses
		expenseTableModel.setRowCount(0);
		for (Map.Entry<String, Double> entry : data.expenses.entrySet()) {
			expenseTableModel
					.addRow(new Object[] { entry.getKey(), String.format("₱%.2f", entry.getValue()), "Remove" });
		}
		updateTotal();
	}

	public static class Step1Data {
		public String deliveryName;
		public java.util.Date deliveryDate;
		public Map<String, Double> expenses;
	}
}