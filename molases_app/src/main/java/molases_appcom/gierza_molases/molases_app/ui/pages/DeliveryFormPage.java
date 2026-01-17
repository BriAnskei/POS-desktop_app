package molases_appcom.gierza_molases.molases_app.ui.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.UiController.NewDeliveryController;
import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.ui.components.delivery.DeliveryFormStep1;
import com.gierza_molases.molases_app.ui.components.delivery.DeliveryFormStep2;
import com.gierza_molases.molases_app.ui.components.delivery.DeliveryFormStep3;
import com.gierza_molases.molases_app.ui.components.delivery.UIComponentFactory;

public class DeliveryFormPage {

	private static final Color CONTENT_BG = new Color(250, 247, 242);
	private static final Color TEXT_DARK = new Color(62, 39, 35);
	private static final Color ACCENT_GOLD = new Color(184, 134, 11);
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43);
	private static final Color STEP_INACTIVE = new Color(200, 190, 180);

	// Step tracking
	private static int currentStep = 1;
	private static final int TOTAL_STEPS = 3;

	// Step instances
	private static DeliveryFormStep1 step1;
	private static DeliveryFormStep2 step2;
	private static DeliveryFormStep3 step3;

	// Data storage
	private static DeliveryFormStep1.Step1Data step1Data;
	private static DeliveryFormStep2.Step2Data step2Data;

	// Main panel reference for navigation
	private static JPanel mainContentPanel;
	private static JScrollPane mainScrollPane;

	// controller
	private static final NewDeliveryController newDeliveryController = AppContext.newDeliveryController;

	public static JPanel createPanel(Runnable onCancel, Runnable onSave) {
		// Reset to step 1
		currentStep = 1;
		step1Data = null;
		step2Data = null;

		// Initialize step instances
		step1 = new DeliveryFormStep1();
		step2 = new DeliveryFormStep2(newDeliveryController);
		step3 = new DeliveryFormStep3(newDeliveryController);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(CONTENT_BG);
		mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

		// Header
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(CONTENT_BG);
		headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

		JLabel titleLabel = new JLabel("Add New Delivery");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
		titleLabel.setForeground(TEXT_DARK);
		headerPanel.add(titleLabel, BorderLayout.WEST);

		JButton backButton = UIComponentFactory.createStyledButton("← Back to List", new Color(120, 120, 120));
		backButton.addActionListener(e -> onCancel.run());
		headerPanel.add(backButton, BorderLayout.EAST);

		mainPanel.add(headerPanel, BorderLayout.NORTH);

		// Content panel that will be replaced
		mainContentPanel = new JPanel(new BorderLayout());
		mainContentPanel.setBackground(CONTENT_BG);

		// Wrap the entire content panel in a scroll pane
		mainScrollPane = new JScrollPane(mainContentPanel);
		mainScrollPane.setBorder(null);
		mainScrollPane.getViewport().setBackground(CONTENT_BG);
		mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);

		mainPanel.add(mainScrollPane, BorderLayout.CENTER);

		// Load Step 1 initially
		loadStep1(onCancel, onSave);

		return mainPanel;
	}

	private static void loadStep1(Runnable onCancel, Runnable onSave) {
		currentStep = 1;
		mainContentPanel.removeAll();

		// Progress Indicator
		JPanel progressPanel = createProgressIndicator();
		mainContentPanel.add(progressPanel, BorderLayout.NORTH);

		// Form Content (Step 1)
		JPanel formPanel = step1.createPanel();

		// Load previous data if returning from step 2
		if (step1Data != null) {
			step1.loadData(step1Data);
		}

		mainContentPanel.add(formPanel, BorderLayout.CENTER);

		// Button Panel
		JPanel buttonPanel = createStep1ButtonPanel(onCancel, onSave);
		mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);

		mainContentPanel.revalidate();
		mainContentPanel.repaint();

		// Scroll to top when loading new step
		javax.swing.SwingUtilities.invokeLater(() -> {
			mainScrollPane.getVerticalScrollBar().setValue(0);
		});
	}

	private static void loadStep2(Runnable onCancel, Runnable onSave) {
		currentStep = 2;
		mainContentPanel.removeAll();

		// Progress Indicator
		JPanel progressPanel = createProgressIndicator();
		mainContentPanel.add(progressPanel, BorderLayout.NORTH);

		// Form Content (Step 2)
		JPanel formPanel = step2.createPanel();

		// Load previous data if returning from step 3
		if (step2Data != null) {
			step2.loadData(step2Data);
		}

		mainContentPanel.add(formPanel, BorderLayout.CENTER);

		// Button Panel
		JPanel buttonPanel = createStep2ButtonPanel(onCancel, onSave);
		mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);

		mainContentPanel.revalidate();
		mainContentPanel.repaint();

		// Scroll to top when loading new step
		javax.swing.SwingUtilities.invokeLater(() -> {
			mainScrollPane.getVerticalScrollBar().setValue(0);
		});
	}

	private static void loadStep3(Runnable onCancel, Runnable onSave) {
		currentStep = 3;
		mainContentPanel.removeAll();

		// Progress Indicator
		JPanel progressPanel = createProgressIndicator();
		mainContentPanel.add(progressPanel, BorderLayout.NORTH);

		// Form Content (Step 3 - Summary)
		JPanel formPanel = step3.createPanel(step1Data, step2Data);
		mainContentPanel.add(formPanel, BorderLayout.CENTER);

		// Button Panel
		JPanel buttonPanel = createStep3ButtonPanel(onCancel, onSave);
		mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);

		mainContentPanel.revalidate();
		mainContentPanel.repaint();

		// Scroll to top when loading new step
		javax.swing.SwingUtilities.invokeLater(() -> {
			mainScrollPane.getVerticalScrollBar().setValue(0);
		});
	}

	private static JPanel createProgressIndicator() {
		JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		progressPanel.setBackground(CONTENT_BG);
		progressPanel.setBorder(new EmptyBorder(10, 0, 20, 0));

		String[] stepLabels = { "Delivery Details", "Customer, Branch & Product Assignment", "Summary" };

		for (int i = 1; i <= TOTAL_STEPS; i++) {
			JPanel stepPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
			stepPanel.setBackground(CONTENT_BG);

			// Step number circle
			JLabel stepNumber = new JLabel(String.valueOf(i));
			stepNumber.setFont(new Font("Arial", Font.BOLD, 14));
			stepNumber.setPreferredSize(new Dimension(30, 30));
			stepNumber.setHorizontalAlignment(JLabel.CENTER);
			stepNumber.setOpaque(true);

			if (i == currentStep) {
				stepNumber.setBackground(ACCENT_GOLD);
				stepNumber.setForeground(Color.WHITE);
			} else if (i < currentStep) {
				stepNumber.setBackground(SIDEBAR_ACTIVE);
				stepNumber.setForeground(Color.WHITE);
			} else {
				stepNumber.setBackground(STEP_INACTIVE);
				stepNumber.setForeground(TEXT_DARK);
			}

			stepNumber.setBorder(BorderFactory.createLineBorder(
					i == currentStep ? ACCENT_GOLD : (i < currentStep ? SIDEBAR_ACTIVE : STEP_INACTIVE), 2));

			// Step label
			JLabel stepLabel = new JLabel(stepLabels[i - 1]);
			stepLabel.setFont(new Font("Arial", i == currentStep ? Font.BOLD : Font.PLAIN, 13));
			stepLabel.setForeground(i == currentStep ? TEXT_DARK : STEP_INACTIVE);

			stepPanel.add(stepNumber);
			stepPanel.add(stepLabel);

			progressPanel.add(stepPanel);

			// Add arrow between steps
			if (i < TOTAL_STEPS) {
				JLabel arrow = new JLabel("→");
				arrow.setFont(new Font("Arial", Font.PLAIN, 18));
				arrow.setForeground(STEP_INACTIVE);
				progressPanel.add(arrow);
			}
		}

		return progressPanel;
	}

	private static JPanel createStep1ButtonPanel(Runnable onCancel, Runnable onSave) {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(CONTENT_BG);
		buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

		JButton cancelBtn = UIComponentFactory.createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> onCancel.run());
		buttonPanel.add(cancelBtn);

		JButton nextBtn = UIComponentFactory.createStyledButton("Next →", ACCENT_GOLD);
		nextBtn.setPreferredSize(new Dimension(120, 40));
		nextBtn.addActionListener(e -> {
			if (step1.validate()) {
				step1Data = step1.getData();

				System.out.println("Step 1 Data Saved:");
				System.out.println("Delivery Name: " + step1Data.deliveryName);
				System.out.println("Delivery Date: " + step1Data.deliveryDate);
				System.out.println("Expenses: " + step1Data.expenses);

				newDeliveryController.getState().setStepOneForm(step1Data.deliveryName, step1Data.deliveryDate,
						step1Data.expenses);

				loadStep2(onCancel, onSave);
			}
		});
		buttonPanel.add(nextBtn);

		return buttonPanel;
	}

	private static JPanel createStep2ButtonPanel(Runnable onCancel, Runnable onSave) {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(CONTENT_BG);
		buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

		JButton backBtn = UIComponentFactory.createStyledButton("← Back", new Color(120, 120, 120));
		backBtn.setPreferredSize(new Dimension(120, 40));
		backBtn.addActionListener(e -> loadStep1(onCancel, onSave));
		buttonPanel.add(backBtn);

		JButton cancelBtn = UIComponentFactory.createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> onCancel.run());
		buttonPanel.add(cancelBtn);

		JButton nextBtn = UIComponentFactory.createStyledButton("Next →", ACCENT_GOLD);
		nextBtn.setPreferredSize(new Dimension(120, 40));
		nextBtn.addActionListener(e -> {
			if (step2.validate()) {
				step2Data = step2.getData();

				System.out.println("Step 2 Data Saved:");
//				System.out.println("Number of Customers/Branches: " + step2Data.customerBranches.size());
//				for (DeliveryFormStep2.CustomerBranchData data : step2Data.customerBranches) {
//					System.out.println("  - Customer: " + data.customerName + ", Branch: " + data.branchAddress
//							+ ", Product Types: " + data.productTypeCount);
//				}

				loadStep3(onCancel, onSave);
			}
		});
		buttonPanel.add(nextBtn);

		return buttonPanel;
	}

	private static JPanel createStep3ButtonPanel(Runnable onCancel, Runnable onSave) {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(CONTENT_BG);
		buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

		JButton backBtn = UIComponentFactory.createStyledButton("← Back", new Color(120, 120, 120));
		backBtn.setPreferredSize(new Dimension(120, 40));
		backBtn.addActionListener(e -> loadStep2(onCancel, onSave));
		buttonPanel.add(backBtn);

		JButton cancelBtn = UIComponentFactory.createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> onCancel.run());
		buttonPanel.add(cancelBtn);

		JButton printBtn = UIComponentFactory.createStyledButton("🖨️ Print", SIDEBAR_ACTIVE);
		printBtn.setPreferredSize(new Dimension(120, 40));
		printBtn.addActionListener(e -> {
			step3.printSummary();
		});
		buttonPanel.add(printBtn);

		JButton saveBtn = UIComponentFactory.createStyledButton("💾 Save Delivery", ACCENT_GOLD);
		saveBtn.setPreferredSize(new Dimension(160, 40));
		saveBtn.addActionListener(e -> {
			showSaveConfirmation(onSave);
		});
		buttonPanel.add(saveBtn);

		return buttonPanel;
	}

	private static void showSaveConfirmation(Runnable onSave) {
		JDialog confirmDialog = new JDialog(SwingUtilities.getWindowAncestor(mainContentPanel), "Confirm Save");
		confirmDialog.setModal(true);
		confirmDialog.setLayout(new GridBagLayout());
		confirmDialog.getContentPane().setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 30, 10, 30);

		JLabel messageLabel = new JLabel("<html><center>Are you sure you want to save<br><b>" + step1Data.deliveryName
				+ "</b>?</center></html>");
		messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		messageLabel.setForeground(TEXT_DARK);
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		confirmDialog.add(messageLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 30, 10, 30);
		JLabel warningLabel = new JLabel("This action will create a new delivery record.");
		warningLabel.setFont(new Font("Arial", Font.ITALIC, 13));
		warningLabel.setForeground(new Color(180, 50, 50));
		warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
		confirmDialog.add(warningLabel, gbc);

		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 30, 20, 10);

		JButton cancelBtn = UIComponentFactory.createStyledButton("Cancel", new Color(120, 120, 120));
		cancelBtn.setPreferredSize(new Dimension(120, 40));
		cancelBtn.addActionListener(e -> confirmDialog.dispose());
		confirmDialog.add(cancelBtn, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(20, 10, 20, 30);
		JButton saveButton = UIComponentFactory.createStyledButton("Save", ACCENT_GOLD);
		saveButton.setPreferredSize(new Dimension(120, 40));
		saveButton.addActionListener(e -> {
			// Disable button and show loading state
			saveButton.setEnabled(false);
			saveButton.setText("Saving...");

			// Call controller to save
			newDeliveryController.saveDelivery(
					// onSuccess
					() -> {
						SwingUtilities.invokeLater(() -> {
							confirmDialog.dispose();
							JOptionPane.showMessageDialog(null, "Delivery saved successfully!", "Success",
									JOptionPane.INFORMATION_MESSAGE);

							// Call the onSave callback (navigates back to list)
							onSave.run();
						});
					},
					// onError
					error -> {
						SwingUtilities.invokeLater(() -> {
							saveButton.setEnabled(true);
							saveButton.setText("Save");

							JOptionPane.showMessageDialog(confirmDialog, "Failed to save delivery:\n" + error, "Error",
									JOptionPane.ERROR_MESSAGE);
						});
					});
		});
		confirmDialog.add(saveButton, gbc);

		confirmDialog.pack();
		confirmDialog.setMinimumSize(new Dimension(400, 200));
		confirmDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(mainContentPanel));
		confirmDialog.setVisible(true);
	}
}