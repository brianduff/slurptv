package org.dubh.slurptv;

public class TaskFailedException extends Exception {
  private static final long serialVersionUID = -9070181755940083271L;

  public TaskFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  public TaskFailedException(Throwable cause) {
    super(cause);
  }

  public TaskFailedException(String message) {
    super(message);
  }
}
