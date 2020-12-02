package saros.lsp.extensions.client.dto;

/**
 * WorkDoneProgressBegin implementation of the language server protocol.
 *
 * @see https://microsoft.github.io/language-server-protocol/specification#workDoneProgressBegin
 */
public class WorkDoneProgressBegin {
  public String kind = "begin";

  /**
   * Mandatory title of the progress operation. Used to briefly inform about the kind of operation
   * being performed.
   *
   * <p>Examples: "Indexing" or "Linking dependencies".
   */
  public String title;

  /**
   * Controls if a cancel button should show to allow the user to cancel the long running operation.
   * Clients that don't support cancellation are allowed to ignore the setting.
   */
  public Boolean cancellable;

  /**
   * Optional, more detailed associated progress message. Contains complementary information to the
   * `title`.
   *
   * <p>Examples: "3/25 files", "project/src/module2", "node_modules/some_dep". If unset, the
   * previous progress message (if any) is still valid.
   */
  public String message;

  /**
   * Optional progress percentage to display (value 100 is considered 100%). If not provided
   * infinite progress is assumed and clients are allowed to ignore the `percentage` value in
   * subsequent in report notifications.
   *
   * <p>The value should be steadily rising. Clients are free to ignore values that are not
   * following this rule.
   */
  public Integer percentage;

  public WorkDoneProgressBegin(
      String title, String message, Integer percentage, Boolean cancellable) {
    this.title = title;
    this.message = message;
    this.percentage = percentage;
    this.cancellable = cancellable;
  }
}
