package com.team05.assetsrepo;

/**
 * Custom exception class for invalid (form) selections.
 */
public class InvalidSelection extends Exception {

  private static final long serialVersionUID = 1L;

  public InvalidSelection(String errorMessage) {
    super(errorMessage);
  }

}
