package com.gierza_molases.molases_app.UiController;

import java.sql.SQLException;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.service.AuthService;

public class LoginController {

	private final AuthService authService;

	/**
	 * Constructor for dependency injection (optional - for better testability)
	 */
	public LoginController(AuthService authService) {
		this.authService = authService;
	}

	/**
	 * Authenticate user credentials
	 * 
	 * @param username  Username input
	 * @param password  Password input
	 * @param onSuccess Callback on successful login
	 * @param onError   Callback on error (receives error message)
	 */
	public void authenticate(String username, String password, Runnable onSuccess, Consumer<String> onError) {
		// Validation: Check for empty fields (quick UI feedback)
		String validationError = validateInputs(username, password);
		if (validationError != null) {
			if (onError != null) {
				onError.accept(validationError);
			}
			return;
		}

		// Perform authentication in background thread
		new SwingWorker<Void, Void>() {
			private boolean authenticated = false;
			private String errorMessage = null;

			@Override
			protected Void doInBackground() {
				try {
					// Call AuthService to validate login
					authenticated = authService.validateLogin(username, password);

					// If service returns false (shouldn't happen with current implementation)
					if (!authenticated) {
						errorMessage = "Invalid username or password";
					}

				} catch (SecurityException e) {
					// Handle invalid username/password from DAO
					authenticated = false;
					errorMessage = e.getMessage(); // "Invalid username" or "Invalid password"

				} catch (SQLException e) {
					// Handle database errors
					authenticated = false;
					errorMessage = "Database connection error. Please try again later.";
					System.err.println("Database error during authentication: " + e.getMessage());
					e.printStackTrace();

				} catch (Exception e) {
					// Handle any other unexpected errors
					authenticated = false;
					errorMessage = "An unexpected error occurred. Please try again.";
					System.err.println("Unexpected error during authentication: " + e.getMessage());
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void done() {
				if (authenticated) {
					// Successful authentication
					if (onSuccess != null) {
						onSuccess.run();
					}
				} else {
					// Failed authentication
					if (onError != null) {
						onError.accept(errorMessage != null ? errorMessage : "Authentication failed");
					}
				}
			}
		}.execute();
	}

	/**
	 * Quick validation check (without database call) Can be used for instant UI
	 * feedback
	 * 
	 * @param username Username to validate
	 * @param password Password to validate
	 * @return Error message if invalid, null if valid
	 */
	public String validateInputs(String username, String password) {
		if (username == null || username.trim().isEmpty()) {
			return "Username is required";
		}

		if (password == null || password.trim().isEmpty()) {
			return "Password is required";
		}

		return null; // Valid
	}

	/**
	 * Check if username meets requirements
	 */
	public boolean isValidUsername(String username) {
		if (username == null || username.trim().isEmpty()) {
			return false;
		}

		return username.length() >= 3;
	}

	/**
	 * Check if password meets requirements
	 */
	public boolean isValidPassword(String password) {
		if (password == null || password.trim().isEmpty()) {
			return false;
		}

		return password.length() >= 4;
	}
}