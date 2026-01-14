package com.gierza_molases.molases_app.ui.components.delivery;


import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Factory class for creating styled UI components used across delivery form steps
 */
public class UIComponentFactory {
    
    private static final Color TEXT_LIGHT = new Color(245, 239, 231);
    
    /**
     * Creates a styled button with hover effect
     * @param text Button text
     * @param bgColor Background color
     * @return Styled JButton
     */
    public static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(TEXT_LIGHT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor.brighter());
                }
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
    
    /**
     * Creates a larger action button with left-aligned text and icon support
     * Used in action dialogs and menus
     * @param text Button text (can include emoji icons)
     * @param bgColor Background color
     * @return Styled action JButton
     */
    public static JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(TEXT_LIGHT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(280, 45));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
}