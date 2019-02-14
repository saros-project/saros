package de.fu_berlin.inf.dpp.intellij.ui.widgets.progress;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import org.picocontainer.annotations.Inject;

/** Creates independent progress monitor window */
// todo: use de.fu_berlin.inf.dpp.monitoring.IProgressMonitor in all IntelliJ classes
public class ProgressFrame implements IProgressMonitor {

  public static final String TITLE = "Progress monitor";
  public static final String BUTTON_CANCEL = "Cancel";

  private MonitorProgressBar monitorProgressBar;

  @Inject private Project project;

  private JFrame frmMain;
  private JButton btnCancel;

  /** Constructor with default title */
  public ProgressFrame() {
    this(TITLE);
  }

  /**
   * Constructor with explicit title
   *
   * @param title dialog title
   */
  public ProgressFrame(String title) {
    SarosPluginContext.initComponent(this);

    frmMain = new JFrame(title);
    frmMain.setSize(300, 160);
    frmMain.setLocationRelativeTo(WindowManager.getInstance().getFrame(project));

    Container pane = frmMain.getContentPane();
    pane.setLayout(null);

    frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    btnCancel = new JButton(BUTTON_CANCEL);
    btnCancel.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            setCanceled(true);
          }
        });

    JProgressBar progressBar =
        new JProgressBar(MonitorProgressBar.MIN_VALUE, MonitorProgressBar.MAX_VALUE);
    JLabel infoLabel = new JLabel(title);
    monitorProgressBar = new MonitorProgressBar(progressBar, infoLabel);

    pane.add(infoLabel);
    pane.add(btnCancel);

    pane.add(progressBar);

    infoLabel.setBounds(10, 15, 200, 15);
    progressBar.setBounds(10, 50, 280, 20);
    btnCancel.setBounds(100, 85, 100, 25);

    frmMain.setResizable(false);
    frmMain.setVisible(true);

    this.frmMain.repaint();
  }

  @Override
  public void done() {
    monitorProgressBar.done();
    frmMain.dispose();
  }

  @Override
  public void subTask(String name) {
    monitorProgressBar.subTask(name);
  }

  @Override
  public void setTaskName(String name) {
    monitorProgressBar.setTaskName(name);
  }

  @Override
  public void worked(int amount) {
    monitorProgressBar.worked(amount);
  }

  @Override
  public void setCanceled(boolean canceled) {
    monitorProgressBar.setCanceled(canceled);
  }

  @Override
  public boolean isCanceled() {
    return monitorProgressBar.isCanceled();
  }

  @Override
  public void beginTask(String name, int size) {
    monitorProgressBar.beginTask(name, size);
  }

  /** @param finishListener FinishListener */
  public void setFinishListener(MonitorProgressBar.FinishListener finishListener) {
    monitorProgressBar.setFinishListener(finishListener);
  }
}
