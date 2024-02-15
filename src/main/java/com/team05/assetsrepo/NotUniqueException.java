package com.team05.assetsrepo;

/**
 * Exception thrown when a given data item is not unique, resulting in duplicates.
 */
public class NotUniqueException extends Exception {

  private static final long serialVersionUID = 1L;

  public NotUniqueException(String errorMessage) {
    super(errorMessage);
  }

}
