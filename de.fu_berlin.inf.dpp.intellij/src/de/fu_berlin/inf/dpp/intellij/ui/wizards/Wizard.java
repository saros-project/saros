package de.fu_berlin.inf.dpp.intellij.ui.wizards;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.AbstractWizardPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.HeaderPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.NavigationPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.annotations.Inject;

/**
 * Class represents a wizard. Usage:
 *
 * <p>Wizard wiz = new Wizard("title"); wiz.registerPage(); wiz.create();
 *
 * <p>FIXME: Make layouts resizable
 */
public abstract class Wizard extends JDialog {

  private final WizardPageModel wizardPageModel;

  private final ActionListener navigationListener =
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

          if (wizardPageModel.getCurrentPage() == null) {
            return;
          }

          if (NavigationPanel.NEXT_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            wizardPageModel.getCurrentPage().actionNext();
            AbstractWizardPage nextPage = wizardPageModel.getNextPage();
            if (nextPage == null) {
              close();
            } else {
              goToPage(nextPage);
            }
          } else if (NavigationPanel.BACK_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            wizardPageModel.getCurrentPage().actionBack();
            goToPage(wizardPageModel.getBackPage());
          } else if (NavigationPanel.CANCEL_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            wizardPageModel.getCurrentPage().actionCancel();
            close();
          }
        }
      };

  private final JPanel cardPanel;

  private final HeaderPanel headerPanel;

  private final CardLayout cardLayout;

  private final NavigationPanel navigationPanel;

  @Inject protected Project project;

  /**
   * Constructor creates wizard structure.
   *
   * @param title window title
   * @param headerPanel
   */
  public Wizard(Window parent, String title, HeaderPanel headerPanel) {
    super(parent, title);
    SarosPluginContext.initComponent(this);

    this.headerPanel = headerPanel;

    wizardPageModel = new WizardPageModel();

    setMinimumSize(new Dimension(600, 400));
    setPreferredSize(new Dimension(600, 400));
    setResizable(true);

    navigationPanel = new NavigationPanel();
    navigationPanel.addActionListener(navigationListener);

    cardPanel = new JPanel();

    cardLayout = new CardLayout();
    cardPanel.setLayout(cardLayout);
  }

  /**
   * Sets new text for the top panel of the wizard.
   *
   * @param text new text of the top panel
   */
  void setTopPanelText(String text) {
    headerPanel.setText(text);
  }

  /** Creates UI. Must only be called explicitly after all pages for the wizard are registered. */
  public void create() {
    setLayout(new BorderLayout());

    getContentPane().add(headerPanel, BorderLayout.NORTH);

    cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
    cardPanel.setVisible(true);

    getContentPane().add(cardPanel, BorderLayout.CENTER);

    getContentPane().add(navigationPanel, BorderLayout.SOUTH);

    pack();

    setLocationRelativeTo(getParent());

    wizardPageModel.goToFirstPage();
  }

  /**
   * Registers pages used in wizard. Pages must be added before calling create().
   *
   * @param page AbstractWizardPage
   */
  public void registerPage(AbstractWizardPage page) {
    page.setWizard(this);
    cardPanel.add(page, page.getId());
    cardLayout.addLayoutComponent(page, page.getId());
    wizardPageModel.registerPage(page.getId(), page);
  }

  /**
   * This method proceeds to specified page, changing visibility of pages and modifying the
   * wizardPageModel.
   */
  protected void goToPage(AbstractWizardPage page) {
    if (page == null) {
      return;
    }

    wizardPageModel.setCurrentPagePosition(page);

    navigationPanel.setNextButtonText(page.getNextButtonTitle());

    NavigationPanel.Position position = NavigationPanel.Position.MIDDLE;

    if (wizardPageModel.getNextPage() == null) {
      position = NavigationPanel.Position.LAST;
    } else if (wizardPageModel.getBackPage() == null) {
      position = NavigationPanel.Position.FIRST;
    }

    navigationPanel.setPosition(position, page.isBackButtonEnabled(), page.isNextButtonEnabled());

    cardLayout.show(cardPanel, page.getId());
  }

  /**
   * Starts the given runnable in a separate thread and shows a modal, non-cancellable progress
   * dialog using {@link ProgressManager#run(com.intellij.openapi.progress.Task)}.
   */
  public void runTask(final Runnable runnable, String title) {
    ProgressManager.getInstance()
        .run(
            new Task.Modal(project, title, false) {

              @Override
              public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                runnable.run();
                indicator.stop();
              }
            });
  }

  public void close() {
    dispose();
  }

  public void open() {
    setVisible(true);
  }

  /**
   * Method used for changing the next page, for decision pages like the SelectProjectPage.
   *
   * @param page
   */
  public void setNextPage(AbstractWizardPage page) {
    wizardPageModel.setNextPage(page);
  }

  public void enableNextButtion() {
    navigationPanel.enableNextButton();
  }

  public void disableNextButton() {
    navigationPanel.disableNextButton();
  }

  /**
   * Default wizard model. Class keeps information about wizard position, acts as container for
   *
   * <p>FIXME: Replace back, current and next page fields by index of current page and calculation
   */
  private static class WizardPageModel {
    private final Map<Object, AbstractWizardPage> pageMap =
        new HashMap<Object, AbstractWizardPage>();
    private final List<AbstractWizardPage> pageList = new ArrayList<AbstractWizardPage>();

    private AbstractWizardPage backPage;
    private AbstractWizardPage currentPage;
    private AbstractWizardPage nextPage;

    public void registerPage(Object id, AbstractWizardPage panel) {
      pageMap.put(id, panel);
      pageList.add(panel);
    }

    /**
     * Return panel
     *
     * @return AbstractWizardPage
     */
    public AbstractWizardPage getBackPage() {
      return backPage;
    }

    public AbstractWizardPage getCurrentPage() {
      return currentPage;
    }

    public AbstractWizardPage getNextPage() {
      return nextPage;
    }

    public void setNextPage(AbstractWizardPage page) {
      nextPage = page;
    }

    /**
     * Back page
     *
     * @param backPage
     */
    public void setBackPage(AbstractWizardPage backPage) {
      this.backPage = backPage;
    }

    public void goToFirstPage() {
      setCurrentPagePosition(pageList.get(0));
    }

    /**
     * Called internally by framework to set current page
     *
     * @param page AbstractWizardPage
     */
    public void setCurrentPagePosition(AbstractWizardPage page) {
      if (page == null) {
        return;
      }
      currentPage = page;

      int index = pageList.indexOf(currentPage);
      if (index < 0) {
        throw new IllegalArgumentException(
            "WizardPageModel called with " + "illegal page it does not contain.");
      }
      if (index > 0) {
        backPage = pageList.get(index - 1);
      } else {
        backPage = null;
      }

      if (index < pageList.size() - 1) {
        nextPage = pageList.get(index + 1);
      } else {
        nextPage = null;
      }
    }
  }
}
