/**
 * Aggregator for events.
 *
 * @export
 * @interface IEventAggregator
 */
export interface IEventAggregator {
  /**
   * Registers a callback for an event.
   *
   * @template TArgs
   * @param {string} event Event identifier
   * @param {TypedEventCallback<TArgs>} callback Callback that will be called
   *  when event is being published
   * @memberof IEventAggregator
   */
  subscribe<TArgs>(event: string, callback: TypedEventCallback<TArgs>): void;

  /**
   * Publishes the event.
   *
   * @template TArgs
   * @param {String} event Event identifier
   * @param {TArgs} args Event arguments
   * @memberof IEventAggregator
   */
  publish<TArgs>(event: string, args: TArgs): void;
}

type EventCallback = (args: any) => void;
type TypedEventCallback<TArgs> = (args: TArgs) => void;

/**
 * Aggregator for events.
 *
 * @export
 * @class EventAggregator
 * @implements {IEventAggregator}
 */
export class EventAggregator implements IEventAggregator {
    private _subscriber = new Map<string, EventCallback[]>();

    /**
     * Registers a callback for an event.
     *
     * @template TArgs
     * @param {string} event Event identifier
     * @param {TypedEventCallback<TArgs>} callback Callback that will be called
     *  when event is being published
     * @memberof EventAggregator
     */
    public subscribe<TArgs>(event: string, callback: TypedEventCallback<TArgs>)
      : void {
      if (!this._subscriber.has(event)) {
        this._subscriber.set(event, []);
      }

      const callbacks = this._subscriber.get(event);
        callbacks?.push(callback);
    }

    /**
     * Publishes the event.
     *
     * @template TArgs
     * @param {String} event Event identifier
     * @param {TArgs} args Event arguments
     * @memberof EventAggregator
     */
    public publish<TArgs>(event: string, args: TArgs): void {
      if (!this._subscriber.has(event)) {
        return;
      }

      const callbacks = this._subscriber.get(event);
        callbacks?.forEach((callback) => {
          callback(args);
        });
    }
}
