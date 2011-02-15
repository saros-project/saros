package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.noFinder;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class NoBotImp extends EclipseComponentImp implements NoBot {

    private static transient NoBotImp self;

    /**
     * {@link NoBotImp} is a singleton, but inheritance is possible.
     */
    public static NoBotImp getInstance() {
        if (self != null)
            return self;
        self = new NoBotImp();

        return self;
    }

    public String getFileContent(String... nodes) throws RemoteException,
        IOException, CoreException {
        IPath path = new Path(getPath(nodes));
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);

        log.info("Checking full path: \"" + file.getFullPath().toOSString()
            + "\"");
        return ConvertStreamToString(file.getContents());
    }

    public void waitUntilFileContentSame(final String otherClassContent,
        final String... fileNodes) throws RemoteException {

        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return getFileContent(fileNodes).equals(otherClassContent);
            }

            public String getFailureMessage() {
                return "The both contents are not" + " same.";
            }
        });

    }

}
