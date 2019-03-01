package de.fu_berlin.inf.dpp.whiteboard.gef.commands;

import de.fu_berlin.inf.dpp.whiteboard.gef.model.LayoutElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGTextBoxRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import java.util.List;
import org.apache.batik.util.SVGConstants;
import org.apache.log4j.Logger;

public class TextboxCreateCommand extends ElementRecordCreateCommand {

  Logger log = Logger.getLogger(TextboxCreateCommand.class);
  private String Text = null;

  public TextboxCreateCommand() {
    setChildName(SVGConstants.SVG_TEXT_TAG);
  }

  @Override
  public List<IRecord> getRecords() {
    return super.getRecords();
  }

  @Override
  protected List<IRecord> getAttributeRecords(LayoutElementRecord child) {
    ((SVGTextBoxRecord) getNewChild()).setText(this.Text);
    log.info("GetAttributeRecords, Text:" + this.Text);
    return super.getAttributeRecords(child);
  }

  public void setText(String text) {
    this.Text = text;
  }

  public String getText() {
    return this.Text;
  }
}
