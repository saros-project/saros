package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;

/**
 * This interface contains convenience API to perform a action using basic
 * widgets. You can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Tester} object in your junit-test. (How
 * to do it please look at the javadoc in class {@link TestPattern} or read the
 * user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * then you can use the object basic initialized in {@link Tester} to access the
 * API :), e.g.
 * 
 * <pre>
 * alice.basic.clickButton(&quot;Finish&quot;);
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface BasicComponent extends Remote {

    /**********************************************
     * 
     * basic widget: {@link SWTBotButton}.
     * 
     **********************************************/

    /**
     * clicks the button specified with the given mnemonicText.
     * 
     * @param mnemonicText
     *            the mnemonicText on the widget.
     */
    public void clickButton(String mnemonicText) throws RemoteException;

    /**
     * 
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return<tt>true</tt>, if the given button is enabled.
     * @throws RemoteException
     */
    public boolean isButtonEnabled(String mnemonicText) throws RemoteException;

    /**
     * Waits until the button is enabled.
     * 
     * @param mnemonicText
     *            the mnemonicText on the widget.
     */
    public void waitUntilButtonEnabled(String mnemonicText)
        throws RemoteException;

    /**
     * Waits until the button is enabled.
     * 
     * @param tooltipText
     *            the tooltip on the widget.
     */
    public void waitUnitButtonWithTooltipIsEnabled(String tooltipText)
        throws RemoteException;

    /**********************************************
     * 
     * basic widget: {@link SWTBotText}.
     * 
     **********************************************/

    /**
     * set the given text into the {@link SWTBotText} with the specified
     * 
     * @param text
     *            the text which you want to insert into the text field
     * @param label
     *            the label on the widget.
     * @throws RemoteException
     */
    public void setTextInTextWithLabel(String text, String label)
        throws RemoteException;

    /**
     * 
     * @param label
     *            the label on the widget.
     * @return the text in the given {@link SWTBotText}
     * @throws RemoteException
     */
    public String getTextInTextWithLabel(String label) throws RemoteException;

    /**********************************************
     * 
     * basic widget: {@link SWTBotLabel}.
     * 
     **********************************************/
    /**
     * 
     * @return the text of the first found {@link SWTBotLabel}
     * @throws RemoteException
     */
    public String geFirsttLabelText() throws RemoteException;

    /**
     * 
     * @param label
     *            the text of the label
     * @return<tt>true</tt>, if the given label exists.
     * @throws RemoteException
     */
    public boolean existsLabel(String label) throws RemoteException;
}
