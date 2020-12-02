/**
 * Awaitable promise that resolves after a certain time.
 *
 * @param {number} ms Time in milliseconds after that the promise
 *  resolves
 * @return {Promise<void>} Awaitable promise that resolves
 *  after the given time
 */
export function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

type Callback = () => void;
type Func<T> = () => T;

/**
 * Tries to get a value from a function and retries
 * on failure until a time threshold is hit.
 *
 * @template T Return value of the function
 * @param {Func<T>} func Function that returns a value
 * @param {Callback|null} isLateAction Optional callback that is being
 *  called once half of the time threshold is reached
 * @return {T|null} Return value of the function if it
 *  could be retrieved, null otherwise
 */
export async function timeout<T>(func: Func<T>,
    isLateAction: Callback|null = null): Promise<T|null> {
  const maxRetryWaitMs = 1000 * 10;
  const waitBetweenRetriesMs = 100;
  let lateActionExecuted = false;
  const lateActionAfterMs = maxRetryWaitMs * 0.5;
  for (let retryWaitedInMs = 0;
    retryWaitedInMs <= maxRetryWaitMs;
    retryWaitedInMs += waitBetweenRetriesMs) {
    try {
      return func();
    } catch {
      if (!lateActionExecuted && retryWaitedInMs >= lateActionAfterMs) {
        if (isLateAction != null) {
          isLateAction();
        }
        lateActionExecuted = true;
      }
      await sleep(waitBetweenRetriesMs);
    }
  }

  return null;
}
