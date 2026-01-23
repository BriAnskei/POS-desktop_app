package molases_appcom.gierza_molases.molases_app.ui.pages.Delivery_detials;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.ui.components.ToastNotification;
import com.gierza_molases.molases_app.ui.components.delivery.UIComponentFactory;

public class DeliveryOverviewTab {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color STEP_INACTIVE = new Color(200, 190, 180);
	private static final Color SECTION_BG = new Color(245, 242, 237);
	private static final Color PROFIT_GREEN = new Color(34, 139, 34);
	private static final Color PLACEHOLDER_COLOR = new Color(150, 150, 150);
	private static final Color STATUS_SCHEDULED = new Color(255, 140, 0);
	private static final Color STATUS_DELIVERED = new Color(34, 139, 34);
	private static final Color STATUS_CANCELLED = new Color(180, 50, 50);

	private static String deliveryName;
	private static Date deliveryDate;
	private static String deliveryStatus;
	private static Map<String, Double> expenses;

	private static JTable expenseTable;
	private static DefaultTableModel expenseTableModel;
	private static JLabel totalExpensesLabel;
	private static JPanel financialSummaryPanel;
	private static JTextField expenseNameInput;
	private static JTextField expenseAmountInput;

	public static void initialize() {
		Delivery delivery = AppContext.deliveryDetialsController.getState().getDelivery();

		if (delivery == null) {
			return;
		}

		deliveryName = delivery.getName();
		deliveryDate = Date.from(delivery.getScheduleDate().atZone(ZoneId.systemDefault()).toInstant());
		deliveryStatus = capitalizeStatus(delivery.getStatus());
		expenses = new LinkedHashMap<>(delivery.getExpenses());
	}

	public static JPanel createPanel() {
		JPanel mainContentPanel = new JPanel(new GridBagLayout());
		mainContentPanel.setBackground(CONTENT_BG);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.65;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(0, 5, 10, 5);

		// TOP ROW - LEFT: Delivery Details
		mainContentPanel.add(createDeliveryDetailsSection(), gbc);

		// TOP ROW - RIGHT: Financial Summary
		gbc.gridx = 1;
		gbc.weightx = 0.35;
		financialSummaryPanel = createFinancialSummaryPanel();
		mainContentPanel.add(financialSummaryPanel, gbc);

		// MIDDLE ROW - Expenses (FULL WIDTH)
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 5, 10, 5);
		mainContentPanel.add(createExpensesSection(), gbc);

		// Wrap in scroll pane
		JScrollPane scrollPane = new JScrollPane(mainContentPanel);
		scrollPane.setBorder(null);
		scrollPane.getViewport().setBackground(CONTENT_BG);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.add(scrollPane, BorderLayout.CENTER);

		return wrapper;
	}

	private static String capitalizeStatus(String status) {
		if (status == null || status.isEmpty()) {
			return "Scheduled";
		}
		return status.substring(0, 1).toUpperCase() + status.substring(1);
	}

	private static JPanel createDeliveryDetailsSection() {
		JPanel section = new JPanel(new BorderLayout());
		section.setBackground(SECTION_BG);
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1),
				new EmptyBorder(12, 12, 12, 12)));

		JLabel titleLabel = new JLabel("Delivery Details");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(SIDEBAR_ACTIVE);
		titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
		section.add(titleLabel, BorderLayout.NORTH);

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBackground(SECTION_BG);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(3, 0, 3, 0);

		JPanel firstRow = new JPanel(new BorderLayout());
		firstRow.setBackground(SECTION_BG);

		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		namePanel.setBackground(SECTION_BG);

		JLabel nameLabel = new JLabel("Delivery Name: ");
		nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
		nameLabel.setForeground(TEXT_DARK);
		namePanel.add(nameLabel);

		JLabel nameValue = new JLabel(deliveryName);
		nameValue.setFont(new Font("Arial", Font.PLAIN, 14));
		nameValue.setForeground(TEXT_DARK);
		namePanel.add(nameValue);

		firstRow.add(namePanel, BorderLayout.WEST);

		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		statusPanel.setBackground(SECTION_BG);

		JLabel statusLabel = new JLabel("Status: ");
		statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
		statusLabel.setForeground(TEXT_DARK);
		statusPanel.add(statusLabel);

		Color statusColor = STATUS_SCHEDULED;
		if (deliveryStatus.equalsIgnoreCase("Delivered")) {
			statusColor = STATUS_DELIVERED;
		} else if (deliveryStatus.equalsIgnoreCase("Cancelled")) {
			statusColor = STATUS_CANCELLED;
		}

		JLabel statusValue = new JLabel(deliveryStatus);
		statusValue.setFont(new Font("Arial", Font.BOLD, 14));
		statusValue.setForeground(statusColor);
		statusPanel.add(statusValue);

		firstRow.add(statusPanel, BorderLayout.EAST);

		contentPanel.add(firstRow, gbc);

		gbc.gridy++;
		JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		datePanel.setBackground(SECTION_BG);

		JLabel dateLabel = new JLabel("Date: ");
		dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
		dateLabel.setForeground(TEXT_DARK);
		datePanel.add(dateLabel);

		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
		JLabel dateValue = new JLabel(sdf.format(deliveryDate));
		dateValue.setFont(new Font("Arial", Font.PLAIN, 14));
		dateValue.setForeground(TEXT_DARK);
		datePanel.add(dateValue);

		contentPanel.add(datePanel, gbc);

		section.add(contentPanel, BorderLayout.CENTER);

		return section;
	}

	private static JPanel createFinancialSummaryPanel() {
		Delivery delivery = AppContext.deliveryDetialsController.getState().getDelivery();

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(SECTION_BG);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ACCENT_GOLD, 2),
				new EmptyBorder(12, 12, 12, 12)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 10, 0);

		JLabel titleLabel = new JLabel("Financial Summary");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(SIDEBAR_ACTIVE);
		titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
		panel.add(titleLabel, gbc);

		JPanel rowsPanel = new JPanel(new GridBagLayout());
		rowsPanel.setBackground(SECTION_BG);

		GridBagConstraints rowGbc = new GridBagConstraints();
		rowGbc.gridx = 0;
		rowGbc.gridy = 0;
		rowGbc.anchor = GridBagConstraints.WEST;
		rowGbc.fill = GridBagConstraints.HORIZONTAL;
		rowGbc.weightx = 1.0;
		rowGbc.insets = new Insets(5, 0, 5, 0);

		rowsPanel.add(createFinancialRow("Gross Sales:", delivery.getTotalGross(), TEXT_DARK), rowGbc);

		rowGbc.gridy++;
		rowsPanel.add(createFinancialRow("Total Capital:", delivery.getTotalCapital(), SIDEBAR_ACTIVE), rowGbc);

		rowGbc.gridy++;
		rowsPanel.add(createFinancialRow("Gross Profit:", delivery.getGrossProfit(), PROFIT_GREEN), rowGbc);

		rowGbc.gridy++;
		rowGbc.insets = new Insets(5, 0, 10, 0);
		rowsPanel.add(createFinancialRow("Total Expenses:", delivery.getTotalExpenses(), new Color(180, 100, 50)),
				rowGbc);

		rowGbc.gridy++;
		rowGbc.insets = new Insets(10, 0, 0, 0);
		JPanel netProfitPanel = new JPanel(new BorderLayout());
		netProfitPanel.setBackground(SECTION_BG);
		netProfitPanel.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, ACCENT_GOLD));
		netProfitPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);

		JPanel netProfitRow = createFinancialRow("Net Profit:", delivery.getNetProfit(),
				delivery.getNetProfit() >= 0 ? PROFIT_GREEN : new Color(180, 50, 50));
		netProfitPanel.add(netProfitRow, BorderLayout.CENTER);

		rowsPanel.add(netProfitPanel, rowGbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 0, 0);
		panel.add(rowsPanel, gbc);

		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(Box.createVerticalGlue(), gbc);

		return panel;
	}

	private static JPanel createFinancialRow(String label, double value, Color valueColor) {
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(SECTION_BG);

		JLabel labelComponent = new JLabel(label);
		labelComponent.setFont(new Font("Arial", Font.BOLD, 16));
		labelComponent.setForeground(TEXT_DARK);

		JLabel valueComponent = new JLabel(String.format("₱%,.2f", value));
		valueComponent.setFont(new Font("Arial", Font.BOLD, 18));
		valueComponent.setForeground(valueColor);

		row.add(labelComponent, BorderLayout.WEST);
		row.add(valueComponent, BorderLayout.EAST);

		return row;
	}

	private static JPanel createExpensesSection() {
		JPanel section = new JPanel(new BorderLayout());
		section.setBackground(SECTION_BG);
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1),
				new EmptyBorder(12, 12, 12, 12)));

		JLabel titleLabel = new JLabel("Expenses");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(SIDEBAR_ACTIVE);
		titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
		section.add(titleLabel, BorderLayout.NORTH);

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBackground(SECTION_BG);

		JPanel tablePanel = createExpensesTable();
		contentPanel.add(tablePanel, BorderLayout.CENTER);

		if (!deliveryStatus.equalsIgnoreCase("Delivered")) {
			JPanel expenseInputPanel = createExpenseInputPanel();
			contentPanel.add(expenseInputPanel, BorderLayout.SOUTH);
		}

		section.add(contentPanel, BorderLayout.CENTER);

		return section;
	}

	private static JPanel createExpensesTable() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(SECTION_BG);
		tablePanel.setBorder(BorderFactory.createLineBorder(STEP_INACTIVE, 1));

		String[] columnNames = { "Expense Name", "Amount", "Action" };
		expenseTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 2 && !deliveryStatus.equalsIgnoreCase("Delivered");
			}
		};

		for (Map.Entry<String, Double> entry : expenses.entrySet()) {
			expenseTableModel
					.addRow(new Object[] { entry.getKey(), String.format("₱%.2f", entry.getValue()), "Remove" });
		}

		expenseTable = new JTable(expenseTableModel);
		expenseTable.setFont(new Font("Arial", Font.PLAIN, 14));
		expenseTable.setRowHeight(32);
		expenseTable.setBackground(Color.WHITE);
		expenseTable.setGridColor(STEP_INACTIVE);
		expenseTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader header = expenseTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(new Color(240, 235, 225));
		header.setForeground(TEXT_DARK);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));

		expenseTable.getColumnModel().getColumn(0).setPreferredWidth(450);
		expenseTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		expenseTable.getColumnModel().getColumn(2).setPreferredWidth(150);

		if (!deliveryStatus.equalsIgnoreCase("Delivered")) {
			expenseTable.getColumnModel().getColumn(2)
					.setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
						JButton removeBtn = UIComponentFactory.createStyledButton("Remove", new Color(180, 50, 50));
						removeBtn.setPreferredSize(new Dimension(90, 26));
						return removeBtn;
					});

			expenseTable.getColumnModel().getColumn(2)
					.setCellEditor(new javax.swing.DefaultCellEditor(new JTextField()) {
						private JButton button = UIComponentFactory.createStyledButton("Remove",
								new Color(180, 50, 50));
						private ActionListener currentListener = null;

						@Override
						public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
								int row, int column) {
							if (currentListener != null) {
								button.removeActionListener(currentListener);
							}

							currentListener = new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									fireEditingStopped();
									removeExpenseRow(row);
								}
							};

							button.addActionListener(currentListener);
							return button;
						}

						@Override
						public Object getCellEditorValue() {
							return "";
						}
					});
		}

		JScrollPane scrollPane = new JScrollPane(expenseTable);
		scrollPane.setBorder(null);
		scrollPane.setPreferredSize(new Dimension(0, 250));
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
		totalPanel.setBackground(new Color(240, 235, 225));
		totalPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, STEP_INACTIVE));

		JLabel totalTextLabel = new JLabel("Total:");
		totalTextLabel.setFont(new Font("Arial", Font.BOLD, 15));
		totalTextLabel.setForeground(TEXT_DARK);
		totalPanel.add(totalTextLabel);

		totalExpensesLabel = new JLabel(String.format("₱%.2f", calculateTotalExpenses()));
		totalExpensesLabel.setFont(new Font("Arial", Font.BOLD, 16));
		totalExpensesLabel.setForeground(ACCENT_GOLD);
		totalPanel.add(totalExpensesLabel);

		tablePanel.add(totalPanel, BorderLayout.SOUTH);

		return tablePanel;
	}

	private static JPanel createExpenseInputPanel() {
		JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
		inputPanel.setBackground(SECTION_BG);
		inputPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

		JLabel nameInputLabel = new JLabel("Expense Name:");
		nameInputLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		nameInputLabel.setForeground(TEXT_DARK);
		inputPanel.add(nameInputLabel);

		expenseNameInput = createPlaceholderTextField("e.g., Fuel, Toll Fee", 15);
		expenseNameInput.setPreferredSize(new Dimension(180, 32));
		inputPanel.add(expenseNameInput);

		JLabel amountInputLabel = new JLabel("Amount:");
		amountInputLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		amountInputLabel.setForeground(TEXT_DARK);
		inputPanel.add(amountInputLabel);

		expenseAmountInput = createPlaceholderTextField("0.00", 10);
		expenseAmountInput.setPreferredSize(new Dimension(100, 32));
		inputPanel.add(expenseAmountInput);

		JButton addBtn = UIComponentFactory.createStyledButton("+ Add", ACCENT_GOLD);
		addBtn.setPreferredSize(new Dimension(100, 32));
		addBtn.addActionListener(e -> addExpenseToTable());
		inputPanel.add(addBtn);

		return inputPanel;
	}

	private static JTextField createPlaceholderTextField(String placeholder, int columns) {
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

	private static String getTextFieldValue(JTextField textField, String placeholder) {
		String text = textField.getText();
		if (text.equals(placeholder)) {
			return "";
		}
		return text.trim();
	}

	private static void addExpenseToTable() {
		String name = getTextFieldValue(expenseNameInput, "e.g., Fuel, Toll Fee");
		String amountStr = getTextFieldValue(expenseAmountInput, "0.00");

		if (name.isEmpty() || amountStr.isEmpty()) {
			ToastNotification.showWarning(SwingUtilities.getWindowAncestor(expenseNameInput),
					"Please fill in both expense name and amount");
			return;
		}

		try {
			double amount = Double.parseDouble(amountStr);
			if (amount <= 0) {
				ToastNotification.showError(SwingUtilities.getWindowAncestor(expenseNameInput),
						"Amount must be greater than 0");
				return;
			}

			AppContext.deliveryDetialsController.addExpense(name, amount, () -> {
				expenseTableModel.addRow(new Object[] { name, String.format("₱%.2f", amount), "Remove" });
				expenses.put(name, amount);

				expenseNameInput.setText("e.g., Fuel, Toll Fee");
				expenseNameInput.setForeground(PLACEHOLDER_COLOR);
				expenseAmountInput.setText("0.00");
				expenseAmountInput.setForeground(PLACEHOLDER_COLOR);
				expenseNameInput.requestFocus();

				updateTotal();
				updateFinancialSummary();

				ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(expenseNameInput),
						"Expense added successfully");

				// Notify customer tab to update its financials
				CustomerDeliveriesTab.refreshFinancials();
			}, (error) -> {
				ToastNotification.showError(SwingUtilities.getWindowAncestor(expenseNameInput),
						"Failed to add expense: " + error);
			});

		} catch (NumberFormatException e) {
			ToastNotification.showError(SwingUtilities.getWindowAncestor(expenseNameInput),
					"Please enter a valid number for amount");
		}
	}

	private static void removeExpenseRow(int row) {
		String expenseName = expenseTableModel.getValueAt(row, 0).toString();

		AppContext.deliveryDetialsController.removeExpense(expenseName, () -> {
			expenses.remove(expenseName);
			expenseTableModel.removeRow(row);
			updateTotal();
			updateFinancialSummary();

			ToastNotification.showSuccess(SwingUtilities.getWindowAncestor(expenseTable),
					"Expense removed successfully");

			// Notify customer tab to update its financials
			CustomerDeliveriesTab.refreshFinancials();
		}, (error) -> {
			ToastNotification.showError(SwingUtilities.getWindowAncestor(expenseTable), "Failed: " + error);
		});
	}

	private static void updateTotal() {
		double total = 0.0;

		for (int i = 0; i < expenseTableModel.getRowCount(); i++) {
			String amountStr = expenseTableModel.getValueAt(i, 1).toString();
			amountStr = amountStr.replace("₱", "").replace(",", "").trim();
			total += Double.parseDouble(amountStr);
		}

		totalExpensesLabel.setText(String.format("₱%.2f", total));
	}

	private static double calculateTotalExpenses() {
		return expenses.values().stream().mapToDouble(Double::doubleValue).sum();
	}

	public static void updateFinancialSummary() {
		Delivery delivery = AppContext.deliveryDetialsController.getState().getDelivery();

		if (delivery != null && financialSummaryPanel != null) {
			Container parent = financialSummaryPanel.getParent();

			if (parent != null && parent.getLayout() instanceof GridBagLayout) {
				GridBagLayout layout = (GridBagLayout) parent.getLayout();
				GridBagConstraints constraints = layout.getConstraints(financialSummaryPanel);

				parent.remove(financialSummaryPanel);
				financialSummaryPanel = createFinancialSummaryPanel();
				parent.add(financialSummaryPanel, constraints);

				parent.revalidate();
				parent.repaint();
			}
		}
	}
}