/**
 * Code taken from {@link https://github.com/microsoft/vscode-extension-samples/blob/master/quickinput-sample/src/multiStepInput.ts vscode-extension-samples}
 * and modified for its purpose within this project.
 */
import {
  Disposable,
  window,
  QuickInputButtons,
  QuickInputButton,
  QuickInput,
  QuickPickItem,
} from 'vscode';

/**
 * A step of a wizard.
 *
 * @export
 * @interface WizardStep
 * @template T
 */
export interface WizardStep<T> {
  /**
   * Executes the step.
   *
   * @param {WizardContext<T>} context The current wizard context
   * @returns {Promise<void>} An awaitable promise without a result
   * @memberof WizardStep
   */
  execute(context: WizardContext<T>): Promise<void>;

  /**
   * Checks if step can be executed.
   *
   * @param {WizardContext<T>} context The current wizard context
   * @returns {boolean} true if step can be executed, false otherwise
   * @memberof WizardStep
   */
  canExecute(context: WizardContext<T>): boolean;
}

/**
 * Base class for wizard steps.
 *
 * @export
 * @abstract
 * @class WizardStepBase
 * @implements {WizardStep<T>}
 * @template T
 */
export abstract class WizardStepBase<T> implements WizardStep<T> {
  abstract execute(context: WizardContext<T>): Promise<void>;
  abstract canExecute(context: WizardContext<T>): boolean;

  /**
   * Checks if input is not empty.
   *
   * @protected
   * @param {string} input The input to validate
   * @return {(Promise<string|undefined>)} Awaitable result which is undefined
   *  if valid and contains the error message if not
   * @memberof WizardStepBase
   */
  protected notEmpty(input: string): Promise<string|undefined> {
    return Promise.resolve(input ? undefined : 'Value is obligatory');
  }

  /**
   * Returns undefined to indicate optional input.
   *
   * @protected
   * @param {string} _input The input to validate
   * @return {(Promise<string|undefined>)} Awaitable result which is undefined
   * @memberof WizardStepBase
   */
  protected optional(_input: string): Promise<string|undefined> {
    return Promise.resolve(undefined);
  }
}

/**
 * Context of the wizard that enables certain actions.
 *
 * @export
 * @interface WizardContext
 * @template T
 */
export interface WizardContext<T> {
  /**
   * Target of the wizard.
   *
   * @type {T}
   * @memberof WizardContext
   */
  target: T;

  /**
   * Shows an input box.
   *
   * @template P
   * @param {P} {value, prompt, validate, buttons, placeholder, password}
   *   Parameters of the input box
   * @return {
   *  (Promise<string | (P extends { buttons: (infer I)[] } ? I : never)>)
   * }
   *  Awaitable promise that returns the input or selected button
   *  when input box closes
   * @memberof WizardContext
   */
  showInputBox<P extends InputBoxParameters>(
    {value, prompt, validate, buttons, placeholder, password}: P
  ): Promise<string | (P extends { buttons: (infer I)[] } ? I : never)>;

  /**
   * Shows a quickpick.
   *
   * @template T
   * @template P
   * @param {P} {items, activeItem, placeholder, buttons}
   *   Parameters of the quickpick
   * @return {(Promise<T | (P extends { buttons: (infer I)[] } ? I : never)>)}
   *   Awaitable promise that returns the selected item or selected button
   *    when quickpick closes
   * @memberof WizardContext
   */
  showQuickPick<T extends QuickPickItem, P extends QuickPickParameters<T>>(
    {items, activeItem, placeholder, buttons}: P
  ): Promise<T | (P extends { buttons: (infer I)[] } ? I : never)>;
}

/**
 * Base parameters of an input box.
 *
 * @export
 * @interface InputBoxParameters
 */
export interface InputBoxParameters {
  value: string;
  prompt: string;
  placeholder: string | undefined;
  password: boolean;
  validate: (value: string) => Promise<string | undefined>;
  buttons?: QuickInputButton[];
}

/**
 * Base parameters of a quickpick.
 *
 * @export
 * @interface QuickPickParameters
 * @template T
 */
export interface QuickPickParameters<T extends QuickPickItem> {
  items: T[];
  activeItem?: T;
  placeholder: string;
  buttons?: QuickInputButton[];
}

/**
 * Flow of a wizard step.
 *
 * @class InputFlowAction
 */
class InputFlowAction {
  /**
   * Just for internal use.
   *
   * @memberof InputFlowAction
   */
  private constructor() { }
  static back = new InputFlowAction();
  static cancel = new InputFlowAction();
  static resume = new InputFlowAction();
}

/**
 * A wizard.
 *
 * @export
 * @class Wizard
 * @implements {WizardContext<T>}
 * @template T
 */
export class Wizard<T> implements WizardContext<T> {
  private _aborted = false;
  private _stepPointer = 0;
  private _current?: QuickInput;
  private _executed: boolean[];

  /**
   * true if wizard has been aborted and false otherwise.
   *
   * @readonly
   * @type {boolean}
   * @memberof Wizard
   */
  public get aborted(): boolean {
    return this._aborted;
  }

  /**
   * Calculates the total steps currently needed
   * to finish the wizard.
   *
   * @private
   * @return {number} Amount of total steps
   * @memberof Wizard
   */
  private calculateTotalSteps(): number {
    let c = this.calculateCurrentStep() - 1;
    for (let i = this._stepPointer; i < this._steps.length; i ++) {
      if (this._executed[i] || this._steps[i].canExecute(this)) {
        c ++;
      }
    }

    return c;
  }

  /**
   * Calculates the current step.
   *
   * @private
   * @return {number} Current step number
   * @memberof Wizard
   */
  private calculateCurrentStep(): number {
    let c = 1;
    for (let i = 0; i < this._stepPointer; i ++) {
      c += this._executed[i] ? 1 : 0;
    }

    return c;
  }

  /**
   * Creates an instance of Wizard.
   *
   * @param {T} target Target of the wizard
   * @param {string} _title Title of the wizard
   * @param {WizardStep<T>[]} _steps Steps of the wizard
   * @memberof Wizard
   */
  public constructor(
    public target: T,
    private _title: string,
        private _steps: WizardStep<T>[],
  ) {
    this._executed = this._steps.map(() => false);
  }

  /**
   * Executes the wizard.
   *
   * @return {Promise<T>} An awaitable result containing the
   *   result of the wizard.
   * @memberof Wizard
   */
  public async execute(): Promise<T> {
    for (;this._stepPointer < this._steps.length; this._stepPointer ++) {
      try {
        const step = this._steps[this._stepPointer];
        if (this._executed[this._stepPointer] || step.canExecute(this)) {
          await step.execute(this);
          this._executed[this._stepPointer] = true;
        }
      } catch (err) {
        if (err === InputFlowAction.back) {
          this._executed[this._stepPointer] = false;
          this._stepPointer = this._executed.lastIndexOf(true) - 1;
        } else if (err === InputFlowAction.resume) {
          this._stepPointer --;
        } else if (err === InputFlowAction.cancel) {
          this._aborted = true;
          break;
        } else {
          throw err;
        }
      }
    }

    if (this._current) {
      this._current.dispose();
    }

    return this.target;
  }

  /**
   * @see {@link WizardContext.showInputBox}
   */
  async showInputBox<P extends InputBoxParameters>(
      {value, prompt, validate, buttons, placeholder, password}: P,
  ): Promise<string | (P extends { buttons: (infer I)[] } ? I : never)> {
    const disposables: Disposable[] = [];
    try {
      return await
      new Promise<string|(P extends { buttons: (infer I)[] } ? I : never)>(
          (resolve, reject) => {
            const input = window.createInputBox();
            input.title = this._title;
            input.step = this.calculateCurrentStep();
            input.totalSteps = this.calculateTotalSteps();
            input.value = value || '';
            input.prompt = prompt;
            input.placeholder = placeholder;
            input.password = password;
            input.buttons = [
              ...(this.calculateCurrentStep() > 1 ?
                [QuickInputButtons.Back] :
                []),
              ...(buttons || []),
            ];
            let validating = validate('');
            disposables.push(
                input.onDidTriggerButton((item) => {
                  if (item === QuickInputButtons.Back) {
                    reject(InputFlowAction.back);
                  } else {
                    resolve(<any>item);
                  }
                }),
                input.onDidAccept(async () => {
                  const value = input.value;
                  input.enabled = false;
                  input.busy = true;
                  if (!(await validate(value))) {
                    resolve(value);
                  }
                  input.enabled = true;
                  input.busy = false;
                }),
                input.onDidChangeValue(async (text) => {
                  const current = validate(text);
                  validating = current;
                  const validationMessage = await current;
                  if (current === validating) {
                    input.validationMessage = validationMessage;
                  }
                }),
                input.onDidHide(() => {
                  (async () => {
                    reject(this._shouldResume && await this._shouldResume() ?
                      InputFlowAction.resume :
                      InputFlowAction.cancel);
                  })()
                      .catch(reject);
                }),
            );
            if (this._current) {
              this._current.dispose();
            }
            this._current = input;
            this._current.show();
          },
      );
    } finally {
      disposables.forEach((d) => d.dispose());
    }
  }

  /**
   * @see {@link WizardContext.showQuickPick}
   */
  async showQuickPick
  <T extends QuickPickItem, P extends QuickPickParameters<T>>(
      {items, activeItem, placeholder, buttons}: P,
  ) {
    const disposables: Disposable[] = [];
    try {
      return await
      new Promise<T|(P extends { buttons: (infer I)[] } ? I : never)>(
          (resolve, reject) => {
            const input = window.createQuickPick<T>();
            input.title = this._title;
            input.step = this.calculateCurrentStep();
            input.totalSteps = this.calculateTotalSteps();
            input.placeholder = placeholder;
            input.items = items;
            if (activeItem) {
              input.activeItems = [activeItem];
            }
            input.buttons = [
              ...(this.calculateCurrentStep() > 1 ?
              [QuickInputButtons.Back] :
              []),
              ...(buttons || []),
            ];
            disposables.push(
                input.onDidTriggerButton((item) => {
                  if (item === QuickInputButtons.Back) {
                    reject(InputFlowAction.back);
                  } else {
                    resolve(<any>item);
                  }
                }),
                input.onDidChangeSelection((items) => resolve(items[0])),
                input.onDidHide(() => {
                  (async () => {
                    reject(this._shouldResume && await this._shouldResume() ?
                    InputFlowAction.resume :
                    InputFlowAction.cancel);
                  })()
                      .catch(reject);
                }),
            );
            if (this._current) {
              this._current.dispose();
            }
            this._current = input;
            this._current.show();
          });
    } finally {
      disposables.forEach((d) => d.dispose());
    }
  }

  /**
   * Determines if wizard should be resumed or not after it has been closed.
   *
   * @private
   * @return {boolean} true if wizard should be shown again and false otherwise
   * @memberof Wizard
   */

  /**
   * Determines if wizard should be resumed or not after it has been closed.
   *
   * @private
   * @return {Thenable<boolean>} Awaitable thenable that returns after
   *  choice has been made
   * @memberof Wizard
   */
  private _shouldResume(): Thenable<boolean> {
    return window.showInformationMessage(
        `Wizard '${this._title}' has been closed. Resume?`,
        'Yes', 'No',
    )
        .then((option) => {
          if (option === 'Yes') {
            return true;
          } else {
            return false;
          }
        });
  }
}
