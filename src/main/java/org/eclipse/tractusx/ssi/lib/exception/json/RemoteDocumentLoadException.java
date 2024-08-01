package org.eclipse.tractusx.ssi.lib.exception.json;

/**
 * Custom exception class for handling errors related to loading remote documents. This class
 * extends RuntimeException to allow it to be thrown directly or wrapped in a checked exception.
 */
public class RemoteDocumentLoadException extends RuntimeException {

  /**
   * Constructs a new RemoteDocumentLoadException with the specified detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method)
   */
  public RemoteDocumentLoadException(String message) {
    super(message);
  }

  /**
   * Constructs a new RemoteDocumentLoadException with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method)
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method)
   */
  public RemoteDocumentLoadException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new RemoteDocumentLoadException with the specified cause.
   *
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method)
   */
  public RemoteDocumentLoadException(Throwable cause) {
    super(cause);
  }
}
