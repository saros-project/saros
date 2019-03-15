package saros.intellij.ui.widgets.progress;

import com.intellij.openapi.wm.WindowManager;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import saros.SarosPluginContext;
import saros.intellij.ui.util.UIProjectUtils;
import saros.monitoring.IProgressMonitor;
import saros.repackaged.picocontainer.annotations.Inject;

/** Creates independent progress monitor window */
// todo: use saros.monitoring.IProgressMonitor in all Intellij classes
public class ProgressFrame implements IProgressMonitor {

  private static final String TITLE = "Progress monitor";
  private static final String BUTTON_CANCEL = "Cancel";

  private MonitorProgressBar monitorProgressBar;

  @Inject private UIProjectUtils projectUtils;

  private JFrame frmMain;

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

    projectUtils.runWithProject(
        project -> frmMain.setLocationRelativeTo(WindowManager.getInstance().getFrame(project)));

    Container pane = frmMain.getContentPane();
    pane.setLayout(null);

    frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JButton btnCancel = new JButton(BUTTON_CANCEL);
    btnCancel.addActionListener(actionEvent -> setCanceled(true));

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
