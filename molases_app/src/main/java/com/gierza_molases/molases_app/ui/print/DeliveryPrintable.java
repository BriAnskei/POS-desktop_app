package com.gierza_molases.molases_app.ui.print;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.context.AppContext;
import com.gierza_molases.molases_app.context.DeliveryDetailsState;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.ProductWithQuantity;

/**
 * Receipt-style printable for delivery reports Mimics supermarket receipt
 * format - simple, clean, and easy to read
 */
public class DeliveryPrintable implements Printable {

	// Receipt dimensions (in points, 72 points = 1 inch)
	private static final int RECEIPT_WIDTH = 216; // ~3 inches (80mm thermal paper width)
	private static final int LEFT_MARGIN = 10;
	private static final int RIGHT_MARGIN = 10;

	// Fonts - monospaced for receipt style
	private static final Font HEADER_FONT = new Font("Monospaced", Font.BOLD, 11);
	private static final Font SUBHEADER_FONT = new Font("Monospaced", Font.PLAIN, 9);
	private static final Font BODY_FONT = new Font("Monospaced", Font.PLAIN, 8);
	private static final Font SMALL_FONT = new Font("Monospaced", Font.PLAIN, 7);

	private DeliveryDetailsState state;
	private Delivery delivery;
	private boolean isDelivered;

	public DeliveryPrintable() {
		this.state = AppContext.deliveryDetialsController.getState();
		this.delivery = state.getDelivery();
		this.isDelivered = "delivered".equalsIgnoreCase(delivery.getStatus());
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		if (pageIndex > 0) {
			return NO_SUCH_PAGE;
		}

		Graphics2D g2d = (Graphics2D) graphics;
		g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

		int y = 10; // Start position

		// Print receipt header
		y = printReceiptHeader(g2d, y);
		y += 5;

		// Print separator
		y = printDashedLine(g2d, y);
		y += 5;

		// Print delivery info
		y = printDeliveryInfo(g2d, y);
		y += 5;

		// Print separator
		y = printDashedLine(g2d, y);
		y += 5;

		// Print customer deliveries
		y = printCustomerDeliveries(g2d, y);
		y += 5;

		// Print separator
		y = printDashedLine(g2d, y);
		y += 5;

		// Print financial summary
		y = printFinancialSummary(g2d, y);
		y += 5;

		// Print separator
		y = printDashedLine(g2d, y);
		y += 5;

		// Print expenses if any
		if (delivery.getExpenses() != null && !delivery.getExpenses().isEmpty()) {
			y = printExpenses(g2d, y);
			y += 5;
			y = printDashedLine(g2d, y);
			y += 5;
		}

		// Print footer
		y = printReceiptFooter(g2d, y);

		return PAGE_EXISTS;
	}

	/**
	 * Print the receipt header (company name and title)
	 */
	private int printReceiptHeader(Graphics2D g2d, int y) {
		g2d.setFont(HEADER_FONT);
		FontMetrics fm = g2d.getFontMetrics();

		// Company name - centered
		String companyName = "GIERZA AGRICULTURAL";
		int x = LEFT_MARGIN + (RECEIPT_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - fm.stringWidth(companyName)) / 2;
		g2d.drawString(companyName, x, y);
		y += fm.getHeight();

		String companyName2 = "MANAGEMENT";
		x = LEFT_MARGIN + (RECEIPT_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - fm.stringWidth(companyName2)) / 2;
		g2d.drawString(companyName2, x, y);
		y += fm.getHeight() + 3;

		// Receipt title
		g2d.setFont(SUBHEADER_FONT);
		fm = g2d.getFontMetrics();
		String title = "DELIVERY RECEIPT";
		x = LEFT_MARGIN + (RECEIPT_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - fm.stringWidth(title)) / 2;
		g2d.drawString(title, x, y);
		y += fm.getHeight() + 2;

		return y;
	}

	/**
	 * Print delivery information
	 */
	private int printDeliveryInfo(Graphics2D g2d, int y) {
		g2d.setFont(BODY_FONT);
		FontMetrics fm = g2d.getFontMetrics();

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm a");

		Date scheduleDate = Date.from(delivery.getScheduleDate().atZone(ZoneId.systemDefault()).toInstant());

		// Delivery name
		y = printLeftRightText(g2d, y, "Delivery:", delivery.getName());

		// Date
		y = printLeftRightText(g2d, y, "Date:", sdf.format(scheduleDate));

		// Status
		String status = capitalizeFirst(delivery.getStatus());
		y = printLeftRightText(g2d, y, "Status:", status);

		// Print timestamp
		y += 3;
		g2d.setFont(SMALL_FONT);
		fm = g2d.getFontMetrics();
		String timestamp = "Printed: " + sdf.format(new Date()) + " " + timeSdf.format(new Date());
		int x = LEFT_MARGIN + (RECEIPT_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - fm.stringWidth(timestamp)) / 2;
		g2d.drawString(timestamp, x, y);
		y += fm.getHeight() + 2;

		return y;
	}

	/**
	 * Print customer deliveries list
	 */
	private int printCustomerDeliveries(Graphics2D g2d, int y) {
		g2d.setFont(BODY_FONT);
		FontMetrics fm = g2d.getFontMetrics();

		// Section header
		g2d.setFont(SUBHEADER_FONT);
		g2d.drawString("CUSTOMER DELIVERIES", LEFT_MARGIN, y);
		y += fm.getHeight() + 3;

		g2d.setFont(BODY_FONT);
		fm = g2d.getFontMetrics();

		Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliveries = state.getMappedCustomerDeliveries();

		int customerNum = 1;
		for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> entry : customerDeliveries.entrySet()) {
			Customer customer = entry.getKey();
			Map<Branch, List<ProductWithQuantity>> branches = entry.getValue();

			// Get customer details
			String customerStatus = state.getCustomerDeliveryStatus(customer);
			String paymentType = isDelivered ? state.getPaymentType(customer) : "N/A";

			// Count non-cancelled branches
			int branchCount = 0;
			double totalSales = 0.0;
			double totalCapital = 0.0;

			for (Map.Entry<Branch, List<ProductWithQuantity>> branchEntry : branches.entrySet()) {
				Branch branch = branchEntry.getKey();
				String branchStatus = state.getBranchDeliveryStatus(branch);

				if (!"Cancelled".equalsIgnoreCase(branchStatus)) {
					branchCount++;
					List<ProductWithQuantity> products = branchEntry.getValue();
					for (ProductWithQuantity product : products) {
						totalSales += product.getTotalSellingPrice();
						totalCapital += product.getTotalCapital();
					}
				}
			}

			// Skip if customer is cancelled
			if ("Cancelled".equalsIgnoreCase(customerStatus)) {
				continue;
			}

			// Print customer info
			g2d.setFont(BODY_FONT);
			String customerLine = String.format("%d. %s", customerNum, truncateText(customer.getDisplayName(), 18));
			g2d.drawString(customerLine, LEFT_MARGIN, y);
			y += fm.getHeight();

			// Print status and payment
			String statusLine = String.format("   Status: %s", customerStatus);
			g2d.drawString(statusLine, LEFT_MARGIN, y);
			y += fm.getHeight();

			String paymentLine = String.format("   Payment: %s", paymentType);
			g2d.drawString(paymentLine, LEFT_MARGIN, y);
			y += fm.getHeight();

			// Print branches and sales
			y = printLeftRightText(g2d, y, "   Branches:", String.valueOf(branchCount), LEFT_MARGIN + 3);
			y = printLeftRightText(g2d, y, "   Sales:", formatCurrency(totalSales), LEFT_MARGIN + 3);

			y += 2; // Small space between customers
			customerNum++;
		}

		return y;
	}

	/**
	 * Print financial summary
	 */
	private int printFinancialSummary(Graphics2D g2d, int y) {
		g2d.setFont(BODY_FONT);
		FontMetrics fm = g2d.getFontMetrics();

		// Section header
		g2d.setFont(SUBHEADER_FONT);
		g2d.drawString("FINANCIAL SUMMARY", LEFT_MARGIN, y);
		y += fm.getHeight() + 3;

		g2d.setFont(BODY_FONT);

		// Financial details
		y = printLeftRightText(g2d, y, "Total Customers:", String.valueOf(delivery.getTotalCustomers()));
		y = printLeftRightText(g2d, y, "Total Branches:", String.valueOf(delivery.getTotalBranches()));

		y += 2;
		y = printLeftRightText(g2d, y, "Gross Sales:", formatCurrency(delivery.getTotalGross()));
		y = printLeftRightText(g2d, y, "Total Capital:", formatCurrency(delivery.getTotalCapital()));
		y = printLeftRightText(g2d, y, "Gross Profit:", formatCurrency(delivery.getGrossProfit()));
		y = printLeftRightText(g2d, y, "Total Expenses:", formatCurrency(delivery.getTotalExpenses()));

		y += 2;
		y = printDashedLine(g2d, y);
		y += 3;

		// Net profit - emphasized
		g2d.setFont(SUBHEADER_FONT);
		y = printLeftRightText(g2d, y, "NET PROFIT:", formatCurrency(delivery.getNetProfit()));

		return y;
	}

	/**
	 * Print expenses breakdown
	 */
	private int printExpenses(Graphics2D g2d, int y) {
		g2d.setFont(BODY_FONT);
		FontMetrics fm = g2d.getFontMetrics();

		// Section header
		g2d.setFont(SUBHEADER_FONT);
		g2d.drawString("EXPENSES", LEFT_MARGIN, y);
		y += fm.getHeight() + 3;

		g2d.setFont(BODY_FONT);

		Map<String, Double> expenses = delivery.getExpenses();
		for (Map.Entry<String, Double> expense : expenses.entrySet()) {
			String name = truncateText(expense.getKey(), 15);
			y = printLeftRightText(g2d, y, name + ":", formatCurrency(expense.getValue()));
		}

		return y;
	}

	/**
	 * Print receipt footer
	 */
	private int printReceiptFooter(Graphics2D g2d, int y) {
		g2d.setFont(SMALL_FONT);
		FontMetrics fm = g2d.getFontMetrics();

		String godBless = "God Bless!";
		int x = LEFT_MARGIN + (RECEIPT_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - fm.stringWidth(godBless)) / 2;
		g2d.drawString(godBless, x, y);
		y += fm.getHeight() + 2;

		String message = "To God Be The Glory";
		x = LEFT_MARGIN + (RECEIPT_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - fm.stringWidth(message)) / 2;
		g2d.drawString(message, x, y);
		y += fm.getHeight();

		return y;
	}

	/**
	 * Print a dashed line separator
	 */
	private int printDashedLine(Graphics2D g2d, int y) {
		g2d.setFont(SMALL_FONT);
		FontMetrics fm = g2d.getFontMetrics();

		StringBuilder dashes = new StringBuilder();
		int width = RECEIPT_WIDTH - LEFT_MARGIN - RIGHT_MARGIN;
		int dashWidth = fm.stringWidth("-");
		int dashCount = width / dashWidth;

		for (int i = 0; i < dashCount; i++) {
			dashes.append("-");
		}

		g2d.drawString(dashes.toString(), LEFT_MARGIN, y);
		y += fm.getHeight();

		return y;
	}

	/**
	 * Print text with left and right alignment on the same line
	 */
	private int printLeftRightText(Graphics2D g2d, int y, String leftText, String rightText) {
		return printLeftRightText(g2d, y, leftText, rightText, LEFT_MARGIN);
	}

	/**
	 * Print text with left and right alignment on the same line with custom left
	 * margin
	 */
	private int printLeftRightText(Graphics2D g2d, int y, String leftText, String rightText, int leftMargin) {
		FontMetrics fm = g2d.getFontMetrics();

		// Draw left text
		g2d.drawString(leftText, leftMargin, y);

		// Draw right text (right-aligned)
		int rightX = RECEIPT_WIDTH - RIGHT_MARGIN - fm.stringWidth(rightText);
		g2d.drawString(rightText, rightX, y);

		y += fm.getHeight();
		return y;
	}

	/**
	 * Format currency value
	 */
	private String formatCurrency(double value) {
		return String.format("P%.2f", value);
	}

	/**
	 * Truncate text to specified length
	 */
	private String truncateText(String text, int maxLength) {
		if (text == null) {
			return "";
		}
		if (text.length() <= maxLength) {
			return text;
		}
		return text.substring(0, maxLength - 2) + "..";
	}

	/**
	 * Capitalize first letter of string
	 */
	private String capitalizeFirst(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}