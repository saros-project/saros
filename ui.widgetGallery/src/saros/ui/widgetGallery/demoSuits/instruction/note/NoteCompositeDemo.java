package saros.ui.widgetGallery.demoSuits.instruction.note;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MessageBox;
import saros.ui.util.LayoutUtils;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.NoteComposite;

@Demo("Different NoteComposites with various system icons and colors")
public class NoteCompositeDemo extends AbstractDemo {
  protected NoteComposite createNoteComposite(
      Composite parent, int iconId, int fgColorId, int bgColorId, final String text) {
    NoteComposite noteComposite =
        new NoteComposite(parent, SWT.BORDER) {

          Label label;

          @Override
          public Layout getContentLayout() {
            return LayoutUtils.createGridLayout(2, false, 0, 10);
          }

          @Override
          public void createContent(final Composite parent) {
            label = new Label(parent, SWT.WRAP);
            label.setText(text);

            Button button = new Button(illustratedComposite, SWT.PUSH);
            button.setText("Press Me...");
            button.addSelectionListener(
                new SelectionAdapter() {
                  @Override
                  public void widgetSelected(SelectionEvent e) {
                    MessageBox dialog =
                        new MessageBox(
                            parent.getShell(), SWT.PRIMARY_MODAL | SWT.OK | SWT.ICON_WORKING);
                    dialog.setText("Ouch...");
                    dialog.setMessage("You hit me!");
                    dialog.open();
                  }
                });
          }

          @Override
          public void setForeground(Color color) {
            super.setForeground(color);
            label.setForeground(color);
          }
        };

    /*
     * Foreground
     */
    if (fgColorId != SWT.DEFAULT) {
      Color color = parent.getDisplay().getSystemColor(fgColorId);
      noteComposite.setForeground(color);
    }

    /*
     * Background
     */
    if (bgColorId != SWT.DEFAULT) {
      Color color = parent.getDisplay().getSystemColor(bgColorId);
      noteComposite.setBackground(color);
    }

    /*
     * Icon
     */
    if (iconId != SWT.DEFAULT) noteComposite.setImage(iconId);

    return noteComposite;
  }

  @Override
  public void createDemo(Composite parent) {
    parent.setLayout(new GridLayout(2, false));

    int[] iconIds =
        new int[] {
          SWT.DEFAULT,
          SWT.ICON_CANCEL,
          SWT.ICON_ERROR,
          SWT.ICON_INFORMATION,
          SWT.ICON_QUESTION,
          SWT.ICON_SEARCH,
          SWT.ICON_WARNING,
          SWT.ICON_WORKING
        };
    int[] fgColorIds =
        new int[] {
          SWT.DEFAULT,
          SWT.COLOR_BLACK,
          SWT.COLOR_WHITE,
          SWT.COLOR_WHITE,
          SWT.COLOR_WHITE,
          SWT.COLOR_BLACK,
          SWT.COLOR_BLACK,
          SWT.COLOR_WHITE
        };
    int[] bgColorIds =
        new int[] {
          SWT.DEFAULT,
          SWT.COLOR_WIDGET_BACKGROUND,
          SWT.COLOR_WIDGET_BORDER,
          SWT.COLOR_WIDGET_DARK_SHADOW,
          SWT.COLOR_WIDGET_FOREGROUND,
          SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW,
          SWT.COLOR_WIDGET_LIGHT_SHADOW,
          SWT.COLOR_WIDGET_NORMAL_SHADOW
        };
    String[] texts =
        new String[] {
          "SWT.DEFAULT\nSWT.DEFAULT",
          "SWT.ICON_CANCEL\nSWT.COLOR_WIDGET_BACKGROUND",
          "SWT.ICON_ERROR\nSWT.COLOR_WIDGET_BORDER",
          "SWT.ICON_INFORMATION\nSWT.COLOR_WIDGET_DARK_SHADOW",
          "SWT.ICON_QUESTION\nSWT.COLOR_WIDGET_FOREGROUND",
          "SWT.ICON_SEARCH\nSWT.COLOR_WIDGET_HIGHLIGHT_SHADOW",
          "SWT.ICON_WARNING\nSWT.COLOR_WIDGET_LIGHT_SHADOW",
          "SWT.ICON_WORKING\nSWT.COLOR_WIDGET_NORMAL_SHADOW"
        };

    for (int i = 0; i < iconIds.length; i++) {
      Label label = new Label(parent, SWT.NONE);
      label.setText(texts[i] + ((i == iconIds.length - 1) ? "\nGridData.FILL" : ""));
      label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

      String text =
          "I'm a "
              + NoteComposite.class.getSimpleName()
              + " with the following options:\n"
              + texts[i]
              + ".";
      NoteComposite noteComposite =
          createNoteComposite(parent, iconIds[i], fgColorIds[i], bgColorIds[i], text);
      noteComposite.setLayoutData(
          (i == iconIds.length - 1)
              ? new GridData(SWT.FILL, SWT.FILL, true, true)
              : new GridData(SWT.FILL, SWT.CENTER, true, false));
    }
  }
}
