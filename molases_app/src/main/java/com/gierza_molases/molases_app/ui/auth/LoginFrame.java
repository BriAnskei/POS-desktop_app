package com.gierza_molases.molases_app.ui.auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.UiController.LoginController;
import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.ui.Main;
import com.gierza_molases.molases_app.ui.components.ToastNotification;

public class LoginFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	// Color Palette - Same as Main (Professional Molasses Theme)
	private static final Color SIDEBAR_BG = new Color(62, 39, 35); // Dark brown
	private static final Color HEADER_BG = new Color(245, 239, 231); // Cream
	private static final Color CONTENT_BG = new Color(250, 247, 242); // Light cream
	private static final Color TEXT_DARK = new Color(62, 39, 35); // Dark brown text
	private static final Color TEXT_LIGHT = new Color(245, 239, 231); // Light text
	private static final Color ACCENT_GOLD = new Color(184, 134, 11); // Golden accent
	private static final Color BUTTON_HOVER = new Color(139, 90, 43); // Golden brown
	private static final Color INPUT_BORDER = new Color(200, 180, 160); // Light border
	private static final Color INPUT_FOCUS = new Color(184, 134, 11); // Gold focus border

	private JTextField usernameField;
	private JPasswordField passwordField;
	private JButton loginButton;
	private LoginController loginController;

	public LoginFrame() {
		setTitle("Login - Gierza Molasses POS");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(900, 550);
		setResizable(false);
		setLocationRelativeTo(null); // Center on screen

		// Get controller from AppContext (already initialized with AuthService)
		loginController = AppContext.loginController;

		// Main container with horizontal split
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(CONTENT_BG);
		setContentPane(mainPanel);

		// LEFT PANEL - Branded section with logo and title
		JPanel leftPanel = createBrandedPanel();
		leftPanel.setPreferredSize(new Dimension(360, 550));

		// RIGHT PANEL - Login form
		JPanel rightPanel = createLoginFormPanel();

		mainPanel.add(leftPanel, BorderLayout.WEST);
		mainPanel.add(rightPanel, BorderLayout.CENTER);
	}

	/**
	 * Create the left branded panel with logo and company name
	 */
	private JPanel createBrandedPanel() {
		JPanel brandPanel = new JPanel();
		brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
		brandPanel.setBackground(SIDEBAR_BG);
		brandPanel.setBorder(new EmptyBorder(60, 40, 60, 40));

		// Add vertical glue to center content
		brandPanel.add(Box.createVerticalGlue());

		// Logo section
		JPanel logoPanel = new DeliveryLogoIcon();
		logoPanel.setPreferredSize(new Dimension(180, 180));
		logoPanel.setMaximumSize(new Dimension(180, 180));
		logoPanel.setAlignmentX(CENTER_ALIGNMENT);
		logoPanel.setBackground(SIDEBAR_BG);

		// Title text - larger and more prominent
		JLabel titleLabel = new JLabel(
				"<html><div style='text-align: center;'>GIERZA<br>AGRICULTURAL<br>MANAGEMENT</div></html>",
				SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
		titleLabel.setForeground(TEXT_LIGHT);
		titleLabel.setAlignmentX(CENTER_ALIGNMENT);

		JLabel subtitleLabel = new JLabel("Point of Sale System", SwingConstants.CENTER);
		subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		subtitleLabel.setForeground(ACCENT_GOLD);
		subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);

		// Add components
		brandPanel.add(logoPanel);
		brandPanel.add(Box.createVerticalStrut(30));
		brandPanel.add(titleLabel);
		brandPanel.add(Box.createVerticalStrut(15));
		brandPanel.add(subtitleLabel);
		brandPanel.add(Box.createVerticalGlue());

		return brandPanel;
	}

	/**
	 * Create the right panel with login form
	 */
	private JPanel createLoginFormPanel() {
		JPanel formContainer = new JPanel(new GridBagLayout());
		formContainer.setBackground(CONTENT_BG);
		formContainer.setBorder(new EmptyBorder(40, 60, 40, 60));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;

		// Welcome header
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 10, 0);
		JLabel welcomeLabel = new JLabel("Welcome Back", SwingConstants.LEFT);
		welcomeLabel.setFont(new Font("Arial", Font.BOLD, 34));
		welcomeLabel.setForeground(TEXT_DARK);
		formContainer.add(welcomeLabel, gbc);

		// Subtitle
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 40, 0);
		JLabel instructionLabel = new JLabel("Please login to your account", SwingConstants.LEFT);
		instructionLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		instructionLabel.setForeground(new Color(120, 100, 90));
		formContainer.add(instructionLabel, gbc);

		// Username label
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 8, 0);
		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setFont(new Font("Arial", Font.BOLD, 15));
		usernameLabel.setForeground(TEXT_DARK);
		formContainer.add(usernameLabel, gbc);

		// Username field - larger with better styling
		gbc.gridy = 3;
		gbc.insets = new Insets(0, 0, 25, 0);
		usernameField = createStyledTextField();
		formContainer.add(usernameField, gbc);

		// Password label
		gbc.gridy = 4;
		gbc.insets = new Insets(0, 0, 8, 0);
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setFont(new Font("Arial", Font.BOLD, 15));
		passwordLabel.setForeground(TEXT_DARK);
		formContainer.add(passwordLabel, gbc);

		// Password field - larger with better styling
		gbc.gridy = 5;
		gbc.insets = new Insets(0, 0, 35, 0);
		passwordField = createStyledPasswordField();
		formContainer.add(passwordField, gbc);

		// Login button - full width and prominent
		gbc.gridy = 6;
		gbc.insets = new Insets(0, 0, 0, 0);
		loginButton = createStyledButton("Login");
		loginButton.setPreferredSize(new Dimension(400, 42));
		loginButton.setMaximumSize(new Dimension(400, 42));

		// Login button action
		loginButton.addActionListener(e -> handleLogin());

		// Allow Enter key to submit
		passwordField.addActionListener(e -> handleLogin());

		formContainer.add(loginButton, gbc);

		return formContainer;
	}

	/**
	 * Create styled text field with modern design
	 */
	private JTextField createStyledTextField() {
		JTextField textField = new JTextField(20);
		textField.setFont(new Font("Arial", Font.PLAIN, 14));
		textField.setPreferredSize(new Dimension(400, 38));
		textField.setMaximumSize(new Dimension(400, 38));
		textField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1),
				new EmptyBorder(5, 10, 5, 10)));
		textField.setBackground(Color.WHITE);

		// Add focus effect
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				textField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(INPUT_FOCUS, 2),
						new EmptyBorder(4, 9, 4, 9)));
			}

			@Override
			public void focusLost(FocusEvent e) {
				textField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1),
						new EmptyBorder(5, 10, 5, 10)));
			}
		});

		return textField;
	}

	/**
	 * Create styled password field with modern design
	 */
	private JPasswordField createStyledPasswordField() {
		JPasswordField passwordField = new JPasswordField(20);
		passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
		passwordField.setPreferredSize(new Dimension(400, 38));
		passwordField.setMaximumSize(new Dimension(400, 38));
		passwordField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1),
				new EmptyBorder(5, 10, 5, 10)));
		passwordField.setBackground(Color.WHITE);

		// Add focus effect
		passwordField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				passwordField.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(INPUT_FOCUS, 2), new EmptyBorder(4, 9, 4, 9)));
			}

			@Override
			public void focusLost(FocusEvent e) {
				passwordField.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(INPUT_BORDER, 1), new EmptyBorder(5, 10, 5, 10)));
			}
		});

		return passwordField;
	}

	/**
	 * Create styled button matching the app theme
	 */
	private JButton createStyledButton(String text) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 14));
		button.setForeground(TEXT_LIGHT);
		button.setBackground(ACCENT_GOLD);
		button.setBorder(new EmptyBorder(12, 24, 12, 24));
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// Hover effect
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				button.setBackground(BUTTON_HOVER);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setBackground(ACCENT_GOLD);
			}
		});

		return button;
	}

	/**
	 * Handle login action - validates and authenticates user
	 */
	private void handleLogin() {
		String username = usernameField.getText();
		String password = new String(passwordField.getPassword());

		// Disable login button during authentication
		loginButton.setEnabled(false);
		loginButton.setText("Logging in...");

		// Use controller to authenticate
		loginController.authenticate(username, password, () -> {
			// SUCCESS: Show success toast and open main window
			ToastNotification.showSuccess(this, "Login successful! Welcome back.");

			// Small delay for user to see the success message
			javax.swing.Timer delayTimer = new javax.swing.Timer(800, e -> {
				// Set authentication flag BEFORE opening Main window
				Main.setAuthenticated(true);

				this.dispose();

				// Open main application window
				java.awt.EventQueue.invokeLater(() -> {
					Main mainFrame = new Main();
					mainFrame.setVisible(true);
				});
			});
			delayTimer.setRepeats(false);
			delayTimer.start();
		}, (errorMessage) -> {
			// ERROR: Show error toast and re-enable login button
			ToastNotification.showError(this, errorMessage);

			// Re-enable login button
			loginButton.setEnabled(true);
			loginButton.setText("Login");

			// Clear password field for security
			passwordField.setText("");
		});
	}

	/**
	 * Delivery Logo Icon - Reused from Main.java
	 */
	private class DeliveryLogoIcon extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			int width = getWidth();
			int height = getHeight();
			int centerX = width / 2;
			int centerY = height / 2;

			// Scale factor for responsive sizing
			double scale = Math.min(width, height) / 80.0;

			// Draw speed/motion lines behind truck (gives running effect)
			g2d.setColor(new Color(184, 134, 11, 60));
			g2d.setStroke(new java.awt.BasicStroke((float) (2.5 * scale), java.awt.BasicStroke.CAP_ROUND,
					java.awt.BasicStroke.JOIN_ROUND));

			// Multiple speed lines at different lengths for depth
			int[] lineLengths = { (int) (12 * scale), (int) (18 * scale), (int) (14 * scale) };
			int[] lineYOffsets = { -4, 0, 4 };

			for (int i = 0; i < 3; i++) {
				int lineStartX = (int) (centerX - 30 * scale);
				int lineEndX = lineStartX - lineLengths[i];
				int lineY = (int) (centerY + lineYOffsets[i] * scale);

				// Fade out as lines go back
				g2d.setColor(new Color(184, 134, 11, 80 - i * 20));
				g2d.drawLine(lineStartX, lineY, lineEndX, lineY);
			}

			g2d.setStroke(new java.awt.BasicStroke(1));

			// Add dust/smoke effect behind wheels
			g2d.setColor(new Color(139, 90, 43, 40));
			int[] dustX = { (int) (centerX - 18 * scale), (int) (centerX - 22 * scale), (int) (centerX - 20 * scale) };
			int[] dustY = { (int) (centerY + 14 * scale), (int) (centerY + 16 * scale), (int) (centerY + 12 * scale) };
			int[] dustSize = { (int) (8 * scale), (int) (10 * scale), (int) (6 * scale) };

			for (int i = 0; i < 3; i++) {
				g2d.fillOval(dustX[i], dustY[i], dustSize[i], dustSize[i]);
			}

			// Main truck body with gradient effect (gives 3D look)
			g2d.setColor(ACCENT_GOLD);

			// Truck cargo container (main body)
			int bodyX = (int) (centerX - 18 * scale);
			int bodyY = (int) (centerY - 8 * scale);
			int bodyW = (int) (28 * scale);
			int bodyH = (int) (16 * scale);
			g2d.fillRoundRect(bodyX, bodyY, bodyW, bodyH, (int) (4 * scale), (int) (4 * scale));

			// Add highlight to top of cargo (3D effect)
			g2d.setColor(new Color(220, 170, 50, 100));
			g2d.fillRoundRect(bodyX, bodyY, bodyW, (int) (4 * scale), (int) (4 * scale), (int) (4 * scale));

			// Truck cab
			int cabX = (int) (centerX - 23 * scale);
			int cabY = (int) (centerY - 4 * scale);
			int cabW = (int) (10 * scale);
			int cabH = (int) (12 * scale);
			g2d.setColor(ACCENT_GOLD);
			g2d.fillRoundRect(cabX, cabY, cabW, cabH, (int) (3 * scale), (int) (3 * scale));

			// Cab highlight (3D effect)
			g2d.setColor(new Color(220, 170, 50, 100));
			g2d.fillRoundRect(cabX, cabY, cabW, (int) (3 * scale), (int) (3 * scale), (int) (3 * scale));

			// Cab window with reflection/shine effect
			g2d.setColor(new Color(200, 220, 240, 180));
			int windowX = (int) (centerX - 21 * scale);
			int windowY = (int) (centerY - 2 * scale);
			int windowW = (int) (6 * scale);
			int windowH = (int) (5 * scale);
			g2d.fillRoundRect(windowX, windowY, windowW, windowH, (int) (2 * scale), (int) (2 * scale));

			// Window shine (top corner highlight)
			g2d.setColor(new Color(255, 255, 255, 200));
			g2d.fillOval(windowX + 1, windowY + 1, (int) (2 * scale), (int) (2 * scale));

			// Cargo lines for detail
			g2d.setColor(new Color(139, 90, 43, 120));
			g2d.setStroke(new java.awt.BasicStroke((float) (1.5 * scale)));
			for (int i = 0; i < 3; i++) {
				int lineX = (int) (bodyX + 5 * scale + i * 8 * scale);
				g2d.drawLine(lineX, bodyY + 2, lineX, bodyY + bodyH - 2);
			}
			g2d.setStroke(new java.awt.BasicStroke(1));

			// Wheels with motion blur effect (tilted for speed)
			int wheel1X = (int) (centerX - 15 * scale);
			int wheel2X = (int) (centerX + 5 * scale);
			int wheelY = (int) (centerY + 8 * scale);
			int wheelSize = (int) (8 * scale);

			// Draw motion-blurred wheels
			drawMotionWheel(g2d, wheel1X + wheelSize / 2, wheelY + wheelSize / 2, wheelSize);
			drawMotionWheel(g2d, wheel2X + wheelSize / 2, wheelY + wheelSize / 2, wheelSize);
		}

		private void drawMotionWheel(Graphics2D g2d, int centerX, int centerY, int size) {
			// Motion blur effect - multiple semi-transparent circles
			for (int i = 3; i >= 0; i--) {
				g2d.setColor(new Color(80, 50, 40, 40 + i * 20));
				int offset = i * 2;
				g2d.fillOval(centerX - size / 2 - offset, centerY - size / 2, size, size);
			}

			// Main wheel (tire)
			g2d.setColor(new Color(80, 50, 40));
			g2d.fillOval(centerX - size / 2, centerY - size / 2, size, size);

			// Inner wheel (rim) with metallic look
			g2d.setColor(new Color(140, 110, 90));
			int innerSize = size / 2;
			g2d.fillOval(centerX - innerSize / 2, centerY - innerSize / 2, innerSize, innerSize);

			// Rim highlight (makes it look metallic/shiny)
			g2d.setColor(new Color(180, 150, 120, 150));
			g2d.fillArc(centerX - innerSize / 2, centerY - innerSize / 2, innerSize, innerSize, 45, 90);

			// Center hub with shine
			g2d.setColor(ACCENT_GOLD);
			int hubSize = size / 5;
			g2d.fillOval(centerX - hubSize / 2, centerY - hubSize / 2, hubSize, hubSize);

			// Hub highlight
			g2d.setColor(new Color(255, 215, 100));
			g2d.fillOval(centerX - hubSize / 4, centerY - hubSize / 4, hubSize / 2, hubSize / 2);
		}
	}
}