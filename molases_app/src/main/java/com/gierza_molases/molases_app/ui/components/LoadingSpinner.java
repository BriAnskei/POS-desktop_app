package com.gierza_molases.molases_app.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Animated circular loading spinner
 */
public class LoadingSpinner extends JComponent implements ActionListener {
	private static final long serialVersionUID = 1L;

	private int angle = 0;
	private Timer timer;
	private Color color;
	private int size;

	public LoadingSpinner(int size, Color color) {
		this.size = size;
		this.color = color;

		setPreferredSize(new Dimension(size, size));
		setMinimumSize(new Dimension(size, size));
		setMaximumSize(new Dimension(size, size));

		// Rotate 6 degrees every 20ms = smooth 360° rotation in 1.2 seconds
		timer = new Timer(20, this);
	}

	public void start() {
		timer.start();
	}

	public void stop() {
		timer.stop();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		angle = (angle + 6) % 360;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;
		int radius = size / 2 - 4;

		// Draw arc
		g2.setColor(color);
		g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, angle, 300); // 300-degree arc

		g2.dispose();
	}
}
