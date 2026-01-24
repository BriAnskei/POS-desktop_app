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
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.gierza_molases.molases_app.UiController.NewDeliveryController;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.ui.components.LoadingSpinner;
import com.gierza_molases.molases_app.ui.components.ToastNotification;

public class SelectBranchDialog extends JDialog {

	// Color Palette
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color TEXT_LIGHT = new Color(245, 239, 231);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color TABLE_HEADER = new Color(139, 90, 43);
	private static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
	private static final Color TABLE_ROW_ODD = new Color(248, 245, 240);

	// UI Components
	private JTable branchTable;
	private DefaultTableModel branchTableModel;
	private List<Branch> availableBranches = new ArrayList<>();
	private List<Branch> selectedBranches = new ArrayList<>();
	private JButton addBranchButton;
	private JButton cancelButton;

	// Loading components
	private JPanel loadingOverlay;
	private LoadingSpinner loadingSpinner;
	private JLabel loadingLabel;
	private JPanel mainContent;

	private final int selectedCustomerId;

	// Callback
	private Consumer<List<Branch>> onBranchesSelectedCallback;

	// controller
	private final NewDeliveryController newDeliveryController;

	/**
	 * Constructor - now accepts the list of branches to display
	 */
	public SelectBranchDialog(Window parent, List<String> alreadyAddedBranchAddresses,
			Consumer<List<Branch>> onBranchesSelected, int selectedCustomerId,
			NewDeliveryController newDeliveryController) {
		super(parent, "Select Branches", ModalityType.APPLICATION_MODAL);
		this.onBranchesSelectedCallback = onBranchesSelected;
		this.newDeliveryController = newDeliveryController;
		this.selectedCustomerId = selectedCustomerId;
		initializeUI();
		loadBranches(alreadyAddedBranchAddresses);
	}

	/**
	 * Initialize the UI
	 */
	private void initializeUI() {
		setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);

		// Main content panel with padding
		mainContent = new JPanel(new BorderLayout(0, 20));
		mainContent.setBackground(Color.WHITE);
		mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

		// Header
		mainContent.add(createHeaderSection(), BorderLayout.NORTH);

		// Center - Branch Selection
		mainContent.add(createBranchSelectionSection(), BorderLayout.CENTER);

		// Bottom - Buttons
		mainContent.add(createBottomSection(), BorderLayout.SOUTH);

		// Create layered pane for overlay support
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setLayout(null); // Use null layout for manual positioning

		// Add components
		layeredPane.add(mainContent, JLayeredPane.DEFAULT_LAYER);

		// Create loading overlay
		createLoadingOverlay();
		layeredPane.add(loadingOverlay, JLayeredPane.POPUP_LAYER);

		add(layeredPane, BorderLayout.CENTER);

		// Dialog settings
		setSize(700, 550);
		setMinimumSize(new Dimension(700, 550));
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Set bounds after sizing
		layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				mainContent.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
				loadingOverlay.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
			}
		});
	}

	/**
	 * Create header section
	 */
	private JPanel createHeaderSection() {
		JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
		headerPanel.setBackground(Color.WHITE);

		// Title
		JLabel titleLabel = new JLabel("Select Branches");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(TEXT_DARK);
		headerPanel.add(titleLabel, BorderLayout.NORTH);

		// Instructions
		JLabel instructionLabel = new JLabel("Select one or more branches to add:");
		instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		instructionLabel.setForeground(TEXT_DARK);
		headerPanel.add(instructionLabel, BorderLayout.CENTER);

		return headerPanel;
	}

	/**
	 * Create branch selection section
	 */
	private JPanel createBranchSelectionSection() {
		JPanel selectionPanel = new JPanel(new BorderLayout());
		selectionPanel.setBackground(Color.WHITE);

		// Create branch table
		createBranchTable();
		JScrollPane scrollPane = new JScrollPane(branchTable);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
		scrollPane.setPreferredSize(new Dimension(0, 350));

		selectionPanel.add(scrollPane, BorderLayout.CENTER);

		return selectionPanel;
	}

	/**
	 * Create branch table
	 */
	private void createBranchTable() {
		String[] columns = { "", "Branch Address" };
		branchTableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int column) {
				if (column == 0)
					return Boolean.class;
				return Object.class;
			}
		};

		branchTable = new JTable(branchTableModel);
		branchTable.setFont(new Font("Arial", Font.PLAIN, 14));
		branchTable.setRowHeight(45);
		branchTable.setShowGrid(true);
		branchTable.setGridColor(new Color(220, 210, 200));
		branchTable.setBackground(TABLE_ROW_EVEN);
		branchTable.setSelectionBackground(new Color(255, 235, 205));
		branchTable.setSelectionForeground(TEXT_DARK);
		branchTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// Set column widths
		branchTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		branchTable.getColumnModel().getColumn(0).setMaxWidth(50);
		branchTable.getColumnModel().getColumn(1).setPreferredWidth(600);

		// Style table header
		JTableHeader header = branchTable.getTableHeader();
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setBackground(TABLE_HEADER);
		header.setForeground(TEXT_LIGHT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setBackground(TABLE_HEADER);
		headerRenderer.setForeground(TEXT_LIGHT);
		headerRenderer.setFont(new Font("Arial", Font.BOLD, 14));
		headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		for (int i = 0; i < branchTable.getColumnCount(); i++) {
			branchTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}

		// Custom renderer for checkbox column
		branchTable.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {
			private final JCheckBox checkBox = new JCheckBox();

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				checkBox.setSelected(value != null && (Boolean) value);
				checkBox.setHorizontalAlignment(SwingConstants.CENTER);
				checkBox.setBackground(
						isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD));
				checkBox.setEnabled(false); // Visual only, clicks handled by mouse listener
				return checkBox;
			}
		});

		// Alternating row colors for address column
		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
				}

				setHorizontalAlignment(SwingConstants.LEFT);
				((JLabel) c).setBorder(new EmptyBorder(5, 10, 5, 10));
				return c;
			}
		};

		branchTable.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);

		// Mouse listener for checkbox toggling
		branchTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = branchTable.rowAtPoint(e.getPoint());
				if (row >= 0 && row < availableBranches.size()) {
					toggleBranchSelection(row);
				}
			}
		});
	}

	/**
	 * Toggle branch selection
	 */
	private void toggleBranchSelection(int row) {
		boolean currentValue = (Boolean) branchTableModel.getValueAt(row, 0);
		boolean newValue = !currentValue;
		branchTableModel.setValueAt(newValue, row, 0);

		Branch branch = availableBranches.get(row);
		if (newValue) {
			if (!selectedBranches.contains(branch)) {
				selectedBranches.add(branch);
			}
		} else {
			selectedBranches.remove(branch);
		}

		updateAddButtonState();
	}

	private void loadBranches(List<String> alreadyAddedBranchAddresses) {

		availableBranches.clear();

		SwingUtilities.invokeLater(() -> showLoading("Loading branches..."));

		this.newDeliveryController.loadCustomerBranches(this.selectedCustomerId, () -> {
			SwingUtilities.invokeLater(() -> {
				List<Branch> customerBranches = this.newDeliveryController.getBranchesState().getBranches();

				availableBranches.clear();

				for (Branch branch : customerBranches) {
					if (!alreadyAddedBranchAddresses.contains(branch.getAddress())) {
						availableBranches.add(branch);
					}
				}
				hideLoading();
				updateBranchTable();

			});
		}, err -> {
			SwingUtilities.invokeLater(() -> {
				hideLoading();
				ToastNotification.showError(this, "Failed to fetch branches");
			});
		});
	}

	/**
	 * Update branch table
	 */
	private void updateBranchTable() {
		branchTableModel.setRowCount(0);

		for (Branch branch : availableBranches) {
			branchTableModel.addRow(new Object[] { false, // Checkbox unchecked by default
					branch.getAddress() });
		}

		selectedBranches.clear();
		updateAddButtonState();
	}

	/**
	 * Create bottom section with buttons
	 */
	private JPanel createBottomSection() {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(Color.WHITE);

		cancelButton = createStyledButton("Cancel", new Color(120, 120, 120));
		cancelButton.setPreferredSize(new Dimension(120, 42));
		cancelButton.addActionListener(e -> dispose());
		buttonPanel.add(cancelButton);

		addBranchButton = createStyledButton("Add Branch", ACCENT_GOLD);
		addBranchButton.setPreferredSize(new Dimension(120, 42));
		addBranchButton.setEnabled(false);
		addBranchButton.addActionListener(e -> handleAddBranches());
		buttonPanel.add(addBranchButton);

		return buttonPanel;
	}

	/**
	 * Update add button state
	 */
	private void updateAddButtonState() {
		addBranchButton.setEnabled(!selectedBranches.isEmpty());
	}

	/**
	 * Handle add branches
	 */
	private void handleAddBranches() {
		if (selectedBranches.isEmpty()) {
			ToastNotification.showError(this, "Please select at least one branch.");
			return;
		}

		// Call callback with selected branches
		if (onBranchesSelectedCallback != null) {
			onBranchesSelectedCallback.accept(new ArrayList<>(selectedBranches));
		}

		dispose();
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

	/**
	 * Create loading overlay
	 */
	/**
	 * Create loading overlay
	 */
	private void createLoadingOverlay() {
		loadingOverlay = new JPanel();
		loadingOverlay.setLayout(new GridBagLayout());
		loadingOverlay.setBackground(new Color(255, 255, 255, 240));
		loadingOverlay.setVisible(false);

		// Container for spinner and label
		JPanel loadingContainer = new JPanel(new BorderLayout(0, 15));
		loadingContainer.setOpaque(false);

		// Spinner
		loadingSpinner = new LoadingSpinner(50, ACCENT_GOLD);
		JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		spinnerPanel.setOpaque(false);
		spinnerPanel.add(loadingSpinner);
		loadingContainer.add(spinnerPanel, BorderLayout.CENTER);

		// Label
		loadingLabel = new JLabel("Loading...");
		loadingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		loadingLabel.setForeground(TEXT_DARK);
		loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loadingContainer.add(loadingLabel, BorderLayout.SOUTH);

		GridBagConstraints gbc = new GridBagConstraints();
		loadingOverlay.add(loadingContainer, gbc);
	}

	/**
	 * Show loading overlay
	 * 
	 * @param message Optional custom loading message (null for default
	 *                "Loading...")
	 */
	public void showLoading(String message) {
		if (message != null && !message.isEmpty()) {
			loadingLabel.setText(message);
		} else {
			loadingLabel.setText("Loading...");
		}

		loadingSpinner.start();
		loadingOverlay.setVisible(true);
		mainContent.setEnabled(false);

		// Disable buttons
		addBranchButton.setEnabled(false);
		cancelButton.setEnabled(false);
		branchTable.setEnabled(false);

		// Bring overlay to front
		getContentPane().setComponentZOrder(loadingOverlay, 0);

		revalidate();
		repaint();
	}

	/**
	 * Show loading overlay with default message
	 */
	public void showLoading() {
		showLoading(null);
	}

	/**
	 * Hide loading overlay
	 */
	public void hideLoading() {
		loadingSpinner.stop();
		loadingOverlay.setVisible(false);
		mainContent.setEnabled(true);

		// Re-enable components
		cancelButton.setEnabled(true);
		branchTable.setEnabled(true);
		updateAddButtonState(); // This will properly enable/disable based on selection

		revalidate();
		repaint();
	}

	/**
	 * Show the dialog
	 */
	public static void show(Window parent, List<String> alreadyAddedBranchAddresses,
			Consumer<List<Branch>> onBranchesSelected, int selectedCustomerId,
			NewDeliveryController newDeliveryController) {
		System.out.println("Fetching cutomer bracnehs: " + selectedCustomerId);
		SelectBranchDialog dialog = new SelectBranchDialog(parent, alreadyAddedBranchAddresses, onBranchesSelected,
				selectedCustomerId, newDeliveryController);
		dialog.setVisible(true);
	}
}