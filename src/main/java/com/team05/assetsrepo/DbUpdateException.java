package com.team05.assetsrepo;

/**
 * Custom exception thrown in the case of errors whilst attempting to update the database.
 */
public class DbUpdateException extends Exception {

  private static final long serialVersionUID = 1L;

  public DbUpdateException(String errorMessage) {
    super(errorMessage);
  }

}
