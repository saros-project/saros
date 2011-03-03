package de.fu_berlin.inf.dpp.ui.wizards.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;

import de.fu_berlin.inf.dpp.util.ArrayUtils;

/**
 * @author bkahlert
 */
public class WizardPageUtils {

    /**
     * Returns the {@link IWizardPage} of the given type from a given
     * {@link IWizard}.
     * 
     * @param wizard
     * 
     * @return the instance; <code>null</code> otherwise
     */
    public static <T extends IWizardPage> List<T> getPage(IWizard wizard,
        Class<T> wizardPageType) {
        if (wizard == null)
            return new ArrayList<T>();

        return ArrayUtils.getInstances(wizard.getPages(), wizardPageType);
    }

}
