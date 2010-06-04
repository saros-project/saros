package de.fu_berlin.inf.dpp.vcs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.ParseException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.patch.ApplyPatchOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.repo.SVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.resources.RemoteFolder;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.operations.CheckoutAsProjectOperation;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

public class SVNTest {

    public static void doStuff(final IProject project, IWorkbenchPart targetPart) {
        // testCreatePatch(project, targetPart);
        // testApplyPatch(project, targetPart);
        // determineRevision(project);
    }

    private static void determineRevision(final IProject project) {
        // SVNTeamProvider p = (SVNTeamProvider) RepositoryProvider
        // .getProvider(project);
        // IFile file = project.getFile("Test.java");

        try {
            IResource resource = project;
            ISVNLocalResource svnResource = SVNWorkspaceRoot
                .getSVNResourceFor(resource);
            if (svnResource == null)
                return;

            SVNRevision revision = svnResource.getRevision();
            System.out.println("current revision: " + revision);
        } catch (SVNException e) {
        }
    }

    public static void testCheckout() {
        ISVNRepositoryLocation loc;
        try {
            loc = SVNRepositoryLocation
                .fromString("https://svn.mi.fu-berlin.de/agse/students/haferburg");
            SVNRevision rev = SVNRevision.getRevision("7108");
            SVNUrl url = new SVNUrl(loc + "/test");

            ISVNRemoteFolder remote[] = { new RemoteFolder(loc, url,
                SVNRevision.HEAD) };
            IProject[] local = { SVNWorkspaceRoot.getProject("test3") };
            final CheckoutAsProjectOperation checkoutAsProjectOperation = new CheckoutAsProjectOperation(
                null, remote, local);
            checkoutAsProjectOperation.setSvnRevision(rev);
            Job job = new Job("checkout test") {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        checkoutAsProjectOperation.run(monitor);
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return Status.OK_STATUS;
                }

            };
            job.schedule();
        } catch (SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void testApplyPatch(final IProject project,
        IWorkbenchPart part, Plugin plugin) {
        // File inFile = new File(
        // "C:\\Documents and Settings\\haferbur\\Desktop\\patch.txt");

        IPath path = new Path(
            "D:\\haferbur\\workspace-alice-host\\test3\\patch.txt");
        IFile patch = ResourcesPlugin.getWorkspace().getRoot()
            .getFileForLocation(path);

        IResource target = project;
        CompareConfiguration configuration = new CompareConfiguration();
        ApplyPatchOperation op = new ApplyPatchOperation(part, patch, target,
            configuration);
        op.run();
        // try {
        // IFilePatch[] filepatch = ApplyPatchOperation.parsePatch(patch);
        // for (IFilePatch iFilePatch : filepatch) {
        // iFilePatch.getTargetPath(null);
        // IStorage contents;
        // iFilePatch.apply(contents, configuration, null);
        // }
        // } catch (CoreException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }

    public static void testCreatePatch(final IProject project,
        IWorkbenchPart part) {
        try {
            ISVNClientAdapter client = SVNWorkspaceRoot.getSVNResourceFor(
                project).getRepository().getSVNClient();
            File outFile = new File(
                "C:\\Documents and Settings\\haferbur\\Desktop\\patch.txt");// File.createTempFile("temp",
            // ".txt");
            if (outFile.exists())
                outFile.delete();
            outFile.createNewFile();
            System.out.println(outFile);
            File relativeToPath = project.getProject().getLocation().toFile();
            File[] files = { relativeToPath };
            boolean recurse = true;
            client.createPatch(files, relativeToPath, outFile, recurse);
            System.out.println("asdf");
        } catch (SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SVNClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    // private File[] getVersionedFiles() {
    // ArrayList versionedFileList = new ArrayList();
    // ArrayList unaddedResourceList = new ArrayList();
    // for (int i = 0; i < unaddedResources.length; i++)
    // unaddedResourceList.add(unaddedResources[i]);
    // for (int i = 0; i < resources.length; i++) {
    // if (!containsResource(unaddedResourceList, resources[i]) ||
    // containsResource(newFiles, resources[i]))
    // versionedFileList.add(new File(resources[i].getLocation().toOSString()));
    // }
    // File[] files = new File[versionedFileList.size()];
    // versionedFileList.toArray(files);
    // return files;
    // }

}
