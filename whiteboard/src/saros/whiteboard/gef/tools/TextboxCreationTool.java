package saros.whiteboard.gef.tools;

import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.jface.dialogs.InputDialog;
import saros.whiteboard.gef.commands.TextboxCreateCommand;
import saros.whiteboard.gef.editpolicy.XYLayoutWithFreehandEditPolicy;
import saros.whiteboard.gef.request.CreateTextBoxRequest;

public class TextboxCreationTool extends CreationToolWithoutSelection {

  @Override
  protected String getCommandName() {
    return XYLayoutWithFreehandEditPolicy.REQ_CREATE_TEXTBOX;
  }

  /** Creates a CreateTextBoxRequest. Is called if "getTargetRequest" is called the first time */
  @Override
  protected Request createTargetRequest() {
    log.trace("create CreateTextboxRequest " + getTargetEditPart());
    CreateTextBoxRequest request = new CreateTextBoxRequest();
    request.setFactory(getFactory());
    return request;
  }

  @Override
  protected void performCreation(int button) {
    InputDialog d = new InputDialog(null, "Enter Text", "Enter the text", "text", null);
    String val = "";
    if (d != null) {
      d.open();

      val = d.getValue();
      if (val == null || val.isEmpty()) val = "";
    }
    TextboxCreateCommand t = (TextboxCreateCommand) getCurrentCommand();
    if (t != null) {
      t.setText(val);
      setCurrentCommand(t);
    }

    // Only if a value was entered execute the command
    if (val != "") {
      super.performCreation(button);
    }
  }

  protected CreateTextBoxRequest getCreateTextBoxRequest() {
    return (CreateTextBoxRequest) getTargetRequest();
  }

  @Override
  protected CreateRequest getCreateRequest() {
    return getCreateTextBoxRequest();
  }

  @Override
  protected String getDebugName() {
    return "Textbox Creation Tool";
  }

  /** Adds the current location to the point list of the CreatePointlistRequest. */
  @Override
  protected void updateTargetRequest() {
    super.updateTargetRequest();
  }
}
