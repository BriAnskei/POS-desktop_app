package com.gierza_molases.molases_app.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class ToastNotification extends JDialog {

	// Toast types
	public enum Type {
		SUCCESS(new Color(76, 175, 80), "✓"), ERROR(new Color(244, 67, 54), "✕"), WARNING(new Color(255, 152, 0), "⚠"),
		INFO(new Color(33, 150, 243), "ℹ");

		private final Color color;
		private final String icon;

		Type(Color color, String icon) {
			this.color = color;
			this.icon = icon;
		}

		public Color getColor() {
			return color;
		}

		public String getIcon() {
			return icon;
		}
	}

	private static final int TOAST_WIDTH = 350;
	private static final int TOAST_HEIGHT = 70;
	private static final int DISPLAY_DURATION = 3000; // 3 seconds
	private static final int FADE_DURATION = 300; // 300ms fade

	private Timer fadeTimer;
	private float opacity = 0.0f;
	private boolean fadingIn = true;

	/**
	 * Private constructor
	 */
	private ToastNotification(Window parent, String message, Type type) {
		super(parent);

		setUndecorated(true);
		setLayout(new BorderLayout());
		setSize(TOAST_WIDTH, TOAST_HEIGHT);
		setBackground(new Color(0, 0, 0, 0));

		// Create content panel
		JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
		contentPanel.setBackground(type.getColor());
		contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

		// Icon label
		JLabel iconLabel = new JLabel(type.getIcon());
		iconLabel.setFont(new Font("Arial", Font.BOLD, 24));
		iconLabel.setForeground(Color.WHITE);
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		iconLabel.setPreferredSize(new Dimension(30, 30));

		// Message label
		JLabel messageLabel = new JLabel("<html><body style='width: 250px'>" + message + "</body></html>");
		messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		messageLabel.setForeground(Color.WHITE);

		contentPanel.add(iconLabel, BorderLayout.WEST);
		contentPanel.add(messageLabel, BorderLayout.CENTER);

		add(contentPanel);

		// Position toast at top-center of parent or screen
		positionToast(parent);

		// Make window semi-transparent and rounded (if supported)
		try {
			setOpacity(0.95f);
		} catch (Exception e) {
			// Opacity not supported, continue without it
		}
	}

	/**
	 * Position toast at top-center
	 */
	private void positionToast(Window parent) {
		if (parent != null && parent.isVisible()) {
			Point parentLocation = parent.getLocationOnScreen();
			Dimension parentSize = parent.getSize();

			int x = parentLocation.x + (parentSize.width - TOAST_WIDTH) / 2;
			int y = parentLocation.y + 80; // 80px from top

			setLocation(x, y);
		} else {
			// Center on screen
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (screenSize.width - TOAST_WIDTH) / 2;
			int y = 80;
			setLocation(x, y);
		}
	}

	/**
	 * Show the toast with fade animation
	 */
	private void showToast() {
		setVisible(true);

		// Start display timer
		Timer displayTimer = new Timer(DISPLAY_DURATION, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fadeOut();
			}
		});
		displayTimer.setRepeats(false);
		displayTimer.start();
	}

	/**
	 * Fade out animation
	 */
	private void fadeOut() {
		Timer closeTimer = new Timer(FADE_DURATION, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		closeTimer.setRepeats(false);
		closeTimer.start();
	}

	/**
	 * Show success toast
	 */
	public static void showSuccess(Window parent, String message) {
		show(parent, message, Type.SUCCESS);
	}

	/**
	 * Show error toast
	 */
	public static void showError(Window parent, String message) {
		show(parent, message, Type.ERROR);
	}

	/**
	 * Show warning toast
	 */
	public static void showWarning(Window parent, String message) {
		show(parent, message, Type.WARNING);
	}

	/**
	 * Show info toast
	 */
	public static void showInfo(Window parent, String message) {
		show(parent, message, Type.INFO);
	}

	/**
	 * Show toast with specified type
	 */
	public static void show(Window parent, String message, Type type) {
		SwingUtilities.invokeLater(() -> {
			ToastNotification toast = new ToastNotification(parent, message, type);
			toast.showToast();
		});
	}

	/**
	 * Override paint to add rounded corners
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Draw rounded rectangle background
		g2.setColor(getContentPane().getBackground());
		g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));

		g2.dispose();
		super.paint(g);
	}

	/**
	 * Test the toast notifications
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Toast Test");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(600, 400);
			frame.setLocationRelativeTo(null);

			JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 50));

			JButton successBtn = new JButton("Show Success");
			successBtn.addActionListener(e -> ToastNotification.showSuccess(frame, "Customer saved successfully!"));

			JButton errorBtn = new JButton("Show Error");
			errorBtn.addActionListener(e -> ToastNotification.showError(frame, "Please fill in all required fields."));

			JButton warningBtn = new JButton("Show Warning");
			warningBtn.addActionListener(e -> ToastNotification.showWarning(frame, "This action cannot be undone."));

			JButton infoBtn = new JButton("Show Info");
			infoBtn.addActionListener(e -> ToastNotification.showInfo(frame, "You have 5 new notifications."));

			panel.add(successBtn);
			panel.add(errorBtn);
			panel.add(warningBtn);
			panel.add(infoBtn);

			frame.add(panel);
			frame.setVisible(true);
		});
	}
}