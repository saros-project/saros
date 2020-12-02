package saros.lsp.extensions.server;

/**
 * A response for requests that indicate either success or failure and have a return value upon
 * success.
 *
 * @param <T> The response payload type
 */
public class SarosResultResponse<T> extends SarosResponse {
  public T result;

  public SarosResultResponse(T result) {
    super();
    this.result = result;
  }

  public SarosResultResponse(Throwable throwable) {
    super(throwable);
  }
}
