package com.team05.assetsrepo;

/**
 * Exception class thrown when an invalid selection in made.
 */
public class InvalidSelection extends Exception{

	private static final long serialVersionUID = 1L;
	
	public InvalidSelection(String errorMessage) {
		super(errorMessage);
	}

}
