package com.gierza_molases.molases_app.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import com.gierza_molases.molases_app.Context.AppContext;

public class Main extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel mainContentArea;
	private JLabel currentPageLabel;

	// Color Palette - Professional Molasses Theme
	private static final Color SIDEBAR_BG = new Color(62, 39, 35); // Dark brown
	private static final Color SIDEBAR_HOVER = new Color(92, 64, 51); // Medium brown
	private static final Color SIDEBAR_ACTIVE = new Color(139, 90, 43); // Golden brown
	private static final Color HEADER_BG = new Color(245, 239, 231); // Cream
	private static final Color CONTENT_BG = new Color(250, 247, 242); // Light cream
	private static final Color TEXT_DARK = new Color(62, 39, 35); // Dark brown text
	private static final Color TEXT_LIGHT = new Color(245, 239, 231); // Light text
	private static final Color ACCENT_GOLD = new Color(184, 134, 11); // Golden accent

	private String currentPage = "Dashboard";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		AppContext.init();

		EventQueue.invokeLater(() -> {
			Main frame = new Main();
			frame.setVisible(true);
		});
	}

	/**
	 * Create the frame.
	 */
	public Main() {
		setTitle("Gierza Molasses - POS System");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH); // Start maximized

		contentPane = new JPanel();
		contentPane.setBackground(CONTENT_BG);
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		// Create sidebar
		JPanel sidebar = createSidebar();
		contentPane.add(sidebar, BorderLayout.WEST);

		// Create main panel (header + content)
		JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
		mainPanel.setBackground(CONTENT_BG);

		// Create header
		JPanel header = createHeader();
		mainPanel.add(header, BorderLayout.NORTH);

		// Create content area
		mainContentArea = new JPanel(new BorderLayout());
		mainContentArea.setBackground(CONTENT_BG);
		mainContentArea.setBorder(new EmptyBorder(30, 30, 30, 30));

		// Add default dashboard view
		showPage("Dashboard");

		mainPanel.add(mainContentArea, BorderLayout.CENTER);
		contentPane.add(mainPanel, BorderLayout.CENTER);

		setMinimumSize(new Dimension(1000, 600));
	}

	/**
	 * Create the sidebar navigation
	 */
	private JPanel createSidebar() {
		JPanel sidebar = new JPanel();
		sidebar.setBackground(SIDEBAR_BG);
		sidebar.setPreferredSize(new Dimension(250, 0));
		sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
		sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

		// Logo/Brand area with icon
		JPanel logoPanel = new JPanel();
		logoPanel.setBackground(SIDEBAR_BG);
		logoPanel.setMaximumSize(new Dimension(250, 150));
		logoPanel.setLayout(new BorderLayout());

		// Logo icon - increased size
		JPanel logoIconPanel = new DeliveryLogoIcon();
		logoIconPanel.setPreferredSize(new Dimension(120, 120));
		logoIconPanel.setMaximumSize(new Dimension(120, 120));
		logoIconPanel.setBackground(SIDEBAR_BG);

		JLabel subtitleLabel = new JLabel("Molasses POS", JLabel.CENTER);
		subtitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		subtitleLabel.setForeground(ACCENT_GOLD);

		JPanel logoContent = new JPanel();
		logoContent.setLayout(new BoxLayout(logoContent, BoxLayout.Y_AXIS));
		logoContent.setBackground(SIDEBAR_BG);
		logoIconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		logoContent.add(logoIconPanel);
		logoContent.add(Box.createVerticalStrut(10));
		logoContent.add(subtitleLabel);

		logoPanel.add(logoContent, BorderLayout.CENTER);
		sidebar.add(logoPanel);
		sidebar.add(Box.createVerticalStrut(30));

		// Navigation buttons with custom icons
		String[] navItems = { "Dashboard", "Delivery", "Customer", "Payments", "Branches", "Product", "Exit" };

		for (int i = 0; i < navItems.length; i++) {
			JPanel navButton = createNavButton(navItems[i]);
			sidebar.add(navButton);
			sidebar.add(Box.createVerticalStrut(5));
		}

		sidebar.add(Box.createVerticalGlue());

		return sidebar;
	}

	/**
	 * Create a navigation button
	 */
	private JPanel createNavButton(String text) {
		JPanel button = new JPanel();
		button.setLayout(new BorderLayout(12, 0));
		button.setBackground(SIDEBAR_BG);
		button.setMaximumSize(new Dimension(250, 50));
		button.setPreferredSize(new Dimension(250, 50));
		button.setBorder(new EmptyBorder(0, 25, 0, 25));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// Create icon based on navigation item (must be final for use in lambda)
		final JPanel iconPanel;
		switch (text) {
		case "Dashboard":
			iconPanel = new DashboardIcon();
			break;
		case "Delivery":
			iconPanel = new DeliveryIcon();
			break;
		case "Customer":
			iconPanel = new CustomerIcon();
			break;
		case "Payments":
			iconPanel = new PaymentsIcon();
			break;
		case "Branches":
			iconPanel = new BranchesIcon();
			break;
		case "Product":
			iconPanel = new ProductIcon();
			break;
		case "Exit":
			iconPanel = new ExitIcon();
			break;
		default:
			iconPanel = null;
			break;
		}

		// Wrapper panel to center icon vertically
		JPanel iconWrapper = new JPanel(new GridBagLayout());
		iconWrapper.setBackground(SIDEBAR_BG);
		if (iconPanel != null) {
			iconPanel.setPreferredSize(new Dimension(22, 22));
			iconPanel.setBackground(SIDEBAR_BG);
			iconWrapper.add(iconPanel);
		}

		JLabel textLabel = new JLabel(text);
		textLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		textLabel.setForeground(TEXT_LIGHT);

		button.add(iconWrapper, BorderLayout.WEST);
		button.add(textLabel, BorderLayout.CENTER);

		// Set initial active state for Dashboard
		if (text.equals("Dashboard")) {
			button.setBackground(SIDEBAR_ACTIVE);
			iconWrapper.setBackground(SIDEBAR_ACTIVE);
			if (iconPanel != null)
				iconPanel.setBackground(SIDEBAR_ACTIVE);
		}

		// Mouse listeners for hover and click effects
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (!text.equals(currentPage)) {
					button.setBackground(SIDEBAR_HOVER);
					iconWrapper.setBackground(SIDEBAR_HOVER);
					if (iconPanel != null)
						iconPanel.setBackground(SIDEBAR_HOVER);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!text.equals(currentPage)) {
					button.setBackground(SIDEBAR_BG);
					iconWrapper.setBackground(SIDEBAR_BG);
					if (iconPanel != null)
						iconPanel.setBackground(SIDEBAR_BG);
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (text.equals("Exit")) {
					System.exit(0);
				} else {
					handleNavigation(text);
					// Update icon background
					iconWrapper.setBackground(SIDEBAR_ACTIVE);
					if (iconPanel != null)
						iconPanel.setBackground(SIDEBAR_ACTIVE);
				}
			}
		});

		return button;
	}

	/**
	 * Handle navigation between pages
	 */
	private void handleNavigation(String pageName) {
		currentPage = pageName;

		// Update all nav buttons
		Component[] components = ((JPanel) contentPane.getComponent(0)).getComponents();
		for (Component comp : components) {
			if (comp instanceof JPanel) {
				JPanel panel = (JPanel) comp;
				if (panel.getLayout() instanceof BorderLayout && panel.getComponentCount() >= 2) {
					// This is a nav button
					boolean isActive = false;
					for (Component c : panel.getComponents()) {
						if (c instanceof JLabel) {
							JLabel label = (JLabel) c;
							if (label.getText().equals(pageName)) {
								isActive = true;
								break;
							}
						}
					}
					panel.setBackground(isActive ? SIDEBAR_ACTIVE : SIDEBAR_BG);
					// Update icon wrapper and icon background
					for (Component c : panel.getComponents()) {
						if (c instanceof JPanel) {
							JPanel wrapper = (JPanel) c;
							wrapper.setBackground(isActive ? SIDEBAR_ACTIVE : SIDEBAR_BG);
							// Update nested icon panel
							for (Component ic : wrapper.getComponents()) {
								if (ic instanceof JPanel) {
									ic.setBackground(isActive ? SIDEBAR_ACTIVE : SIDEBAR_BG);
								}
							}
						}
					}
				}
			}
		}

		showPage(pageName);
	}

	/**
	 * Display the selected page
	 */
	private void showPage(String pageName) {
		mainContentArea.removeAll();

		switch (pageName) {
		case "Customer":
			mainContentArea.add(molases_appcom.gierza_molases.molases_app.ui.pages.CustomersPage.createPanel(),
					BorderLayout.CENTER);
			break;
		case "Branches":
			mainContentArea.add(molases_appcom.gierza_molases.molases_app.ui.pages.BranchesPage.createPanel(),
					BorderLayout.CENTER);
			break;
		case "Product":
			mainContentArea.add(molases_appcom.gierza_molases.molases_app.ui.pages.ProductsPage.createPanel(),
					BorderLayout.CENTER);
			break;

		default:

			// Default page with title (for other pages)
			JPanel pagePanel = new JPanel(new GridBagLayout());
			pagePanel.setBackground(CONTENT_BG);

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new Insets(0, 0, 20, 0);

			// Page title
			JLabel titleLabel = new JLabel(pageName);
			titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
			titleLabel.setForeground(TEXT_DARK);
			pagePanel.add(titleLabel, gbc);

			gbc.gridy = 1;
			JLabel subtitleLabel = new JLabel("This is the " + pageName + " page");
			subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
			subtitleLabel.setForeground(new Color(120, 90, 70));
			pagePanel.add(subtitleLabel, gbc);

			mainContentArea.add(pagePanel, BorderLayout.CENTER);
			break;

		}

		mainContentArea.revalidate();
		mainContentArea.repaint();
	}

	/**
	 * Create the header with logo and date/time
	 */
	private JPanel createHeader() {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(HEADER_BG);
		header.setPreferredSize(new Dimension(0, 80));
		header.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 210, 195)),
				new EmptyBorder(15, 30, 15, 30)));

		// Left side - Business name
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setBackground(HEADER_BG);

		JLabel businessLabel = new JLabel("Gierza Molasses Business");
		businessLabel.setFont(new Font("Arial", Font.BOLD, 20));
		businessLabel.setForeground(TEXT_DARK);
		leftPanel.add(businessLabel, BorderLayout.WEST);

		header.add(leftPanel, BorderLayout.WEST);

		// Right side - Date and Time
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setBackground(HEADER_BG);

		JLabel dateLabel = new JLabel();
		dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
		dateLabel.setForeground(TEXT_DARK);
		dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		JLabel timeLabel = new JLabel();
		timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		timeLabel.setForeground(new Color(120, 90, 70));
		timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		rightPanel.add(dateLabel);
		rightPanel.add(Box.createVerticalStrut(3));
		rightPanel.add(timeLabel);

		header.add(rightPanel, BorderLayout.EAST);

		// Update time immediately and then every second
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
		Date now = new Date();
		dateLabel.setText(dateFormat.format(now));
		timeLabel.setText(timeFormat.format(now));

		Timer timer = new Timer(1000, e -> {
			Date currentTime = new Date();
			dateLabel.setText(dateFormat.format(currentTime));
			timeLabel.setText(timeFormat.format(currentTime));
		});
		timer.start();

		return header;
	}

	// ========== Custom Icon Classes ==========

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

	/**
	 * Dashboard Icon - Home icon
	 */
	private class DashboardIcon extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setColor(TEXT_LIGHT);

			// House roof
			int[] xPoints = { 12, 4, 20 };
			int[] yPoints = { 8, 16, 16 };
			g2d.fillPolygon(xPoints, yPoints, 3);

			// House body
			g2d.fillRect(7, 15, 10, 8);

			// Door
			g2d.setColor(SIDEBAR_BG);
			g2d.fillRect(10, 18, 4, 5);
		}
	}

	/**
	 * Delivery Icon - Truck icon
	 */
	private class DeliveryIcon extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setColor(TEXT_LIGHT);

			// Truck body
			g2d.fillRoundRect(6, 8, 12, 8, 2, 2);
			// Truck cab
			g2d.fillRoundRect(3, 10, 5, 6, 2, 2);
			// Wheels
			g2d.fillOval(5, 16, 3, 3);
			g2d.fillOval(14, 16, 3, 3);
		}
	}

	/**
	 * Customer Icon - People icon
	 */
	private class CustomerIcon extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setColor(TEXT_LIGHT);

			// First person
			g2d.fillOval(6, 6, 5, 5); // Head
			g2d.fillRoundRect(5, 12, 7, 8, 3, 3); // Body

			// Second person (slightly behind)
			g2d.fillOval(13, 8, 5, 5); // Head
			g2d.fillRoundRect(12, 14, 7, 6, 3, 3); // Body
		}
	}

	/**
	 * Payments Icon - Money/Cash icon
	 */
	private class PaymentsIcon extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setColor(TEXT_LIGHT);

			// Back bill (slightly offset)
			g2d.fillRoundRect(8, 7, 10, 6, 2, 2);

			// Front bill
			g2d.fillRoundRect(6, 9, 10, 6, 2, 2);

			// Dollar sign on front bill
			g2d.setColor(SIDEBAR_BG);
			g2d.setFont(new Font("Arial", Font.BOLD, 6));
			g2d.drawString("$", 9, 14);

			// Coins
			g2d.setColor(TEXT_LIGHT);
			g2d.fillOval(14, 13, 4, 4);
			g2d.fillOval(16, 15, 4, 4);
		}
	}

	/**
	 * Branches Icon - Building/Store icon
	 */
	private class BranchesIcon extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setColor(TEXT_LIGHT);

			// Building body
			g2d.fillRect(7, 8, 10, 12);

			// Roof
			int[] xPoints = { 7, 12, 17 };
			int[] yPoints = { 8, 4, 8 };
			g2d.fillPolygon(xPoints, yPoints, 3);

			// Windows (2x2 grid)
			g2d.setColor(SIDEBAR_BG);
			g2d.fillRect(9, 10, 2, 2);
			g2d.fillRect(13, 10, 2, 2);
			g2d.fillRect(9, 13, 2, 2);
			g2d.fillRect(13, 13, 2, 2);

			// Door
			g2d.fillRect(11, 16, 2, 4);
		}
	}

	/**
	 * Product Icon - Box icon
	 */
	private class ProductIcon extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setColor(TEXT_LIGHT);

			// Box body
			g2d.fillRect(6, 10, 12, 10);

			// Box top
			int[] xPoints = { 6, 12, 18, 12 };
			int[] yPoints = { 10, 6, 10, 14 };
			g2d.fillPolygon(xPoints, yPoints, 4);

			// Box lines
			g2d.drawLine(12, 6, 12, 20);
		}
	}

	/**
	 * Exit Icon - Door icon
	 */
	private class ExitIcon extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setColor(TEXT_LIGHT);

			// Door frame
			g2d.fillRoundRect(6, 5, 12, 15, 2, 2);

			// Door panel
			g2d.setColor(SIDEBAR_BG);
			g2d.fillRoundRect(8, 7, 8, 11, 2, 2);

			// Door knob
			g2d.setColor(TEXT_LIGHT);
			g2d.fillOval(14, 13, 2, 2);
		}
	}
}