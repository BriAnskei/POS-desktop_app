package com.gierza_molases.molases_app.service;

import java.sql.SQLException;

import com.gierza_molases.molases_app.dao.AuthDao;

public class AuthService {

	private final AuthDao authDao;

	public AuthService(AuthDao authDao) {
		this.authDao = authDao;
	}

	/**
	 * Validates username + password login
	 * 
	 * @throws SQLException
	 * @throws SecurityException
	 */
	public boolean validateLogin(String userName, String password) throws SecurityException, SQLException {
		if (userName == null || password == null) {
			return false;
		}

		if (userName.isBlank() || password.isBlank()) {
			return false;
		}

		return authDao.validateLogin(userName, password);
	}

	public boolean verifyAdminPassword(String password) {
		if (password == null || password.isBlank()) {
			return false;
		}

		return authDao.verifyPasswordForAdmin(password);
	}
}
