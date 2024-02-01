package com.team05.assetsrepo;

/**
 * Exception thrown when a variable given is not unique, resulting in duplicate values.
 */
public class NotUnique extends Exception{

	private static final long serialVersionUID = 1L;
	
	public NotUnique(String errorMessage) {
		super(errorMessage);
	}

}
