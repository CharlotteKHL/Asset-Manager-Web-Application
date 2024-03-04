package com.team05.assetsrepo;

/**
 * Exception class thrown when an invalid login is performed.
 */
public class InvalidLogin extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidLogin(String errorMessage) {
		super(errorMessage);
	}

}
