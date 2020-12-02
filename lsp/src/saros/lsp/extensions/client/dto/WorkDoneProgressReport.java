package saros.lsp.extensions.client.dto;

/**
 * WorkDoneProgressReport implementation of the language server protocol.
 *
 * @see https://microsoft.github.io/language-server-protocol/specification#workDoneProgressReport
 */
public class WorkDoneProgressReport {
  public String kind = "report";

  /**
   * Controls enablement state of a cancel button. This property is only valid if a cancel button
   * got requested in the `WorkDoneProgressStart` payload.
   *
   * <p>Clients that don't support cancellation or don't support control the button's enablement
   * state are allowed to ignore the setting.
   */
  Boolean cancellable;

  /**
   * Optional, more detailed associated progress message. Contains complementary information to the
   * `title`.
   *
   * <p>Examples: "3/25 files", "project/src/module2", "node_modules/some_dep". If unset, the
   * previous progress message (if any) is still valid.
   */
  String message;

  /**
   * Optional progress percentage to display (value 100 is considered 100%). If not provided
   * infinite progress is assumed and clients are allowed to ignore the `percentage` value in
   * subsequent in report notifications.
   *
   * <p>The value should be steadily rising. Clients are free to ignore values that are not
   * following this rule.
   */
  Integer percentage;

  public WorkDoneProgressReport(String message, Integer percentage, Boolean cancellable) {
    this.message = message;
    this.percentage = percentage;
    this.cancellable = cancellable;
  }
}
