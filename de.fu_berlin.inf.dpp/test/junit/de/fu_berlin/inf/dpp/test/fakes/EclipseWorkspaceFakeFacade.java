package de.fu_berlin.inf.dpp.test.fakes;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * This facade provides functionality to create and delete instances of
 * {@link EclipseWorkspaceFake}.
 * 
 * @author cordes
 */
public class EclipseWorkspaceFakeFacade {

    private static Logger LOG = Logger.getLogger(EclipseWorkspaceFakeFacade.class);

    public static final String PATH_TO_TEST_WORKSPACES = "test/resources/workspaces_for_testsaros/";

    public static IWorkspace createWorkspace(String workspaceName) {
        String pathToWorkspace = PATH_TO_TEST_WORKSPACES + workspaceName + "/";

        // ensure to return a clean workspace
        deleteWorkspace(pathToWorkspace);

        return EclipseWorkspaceFake.getMockWorkspace(pathToWorkspace);
    }

    private static void deleteWorkspace(String pathTohWorkspace) {
        try {
            File file = new File(pathTohWorkspace);
            if (file.exists()) {
                FileUtils.forceDelete(file);
            }
        } catch (Exception e) {
            LOG.error("Error while deleting testWorkspace " + pathTohWorkspace,
                e);
        }
    }

    public static void deleteWorkspaces() {
        try {
            FileUtils.forceDelete(new File(PATH_TO_TEST_WORKSPACES));
        } catch (IOException e) {
            // only try do delete and do nothing
        }
    }

    // TODO could be outsourced in seperate files
    public static void addSomeProjectData(IProject project) {
        try {
            IFile file = project.getFile("src/Person.java");

            String personClass = "class Person {\n" + "\n"
                + "    public String name;\n"
                + "    public String firstname;\n" + "    public int age;\n"
                + "        \n" + "}";
            ByteArrayInputStream input = new ByteArrayInputStream(personClass
                .getBytes("UTF-8"));
            file.create(input, true,
                de.fu_berlin.inf.dpp.test.util.SarosTestUtils.submonitor());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (CoreException e) {
            e.printStackTrace();
        }

        try {
            String addressClass = "public class Address {\n" + "\n"
                + "\tprivate String street;\n"
                + "\tprivate int streetNumber;\n" + "    private String zip;\n"
                + "    private String city;\n" + "    \n" + "}";

            IFile file = project.getFile("src/Address.java");
            ByteArrayInputStream input = new ByteArrayInputStream(addressClass
                .getBytes("UTF-8"));
            file.create(input, true,
                de.fu_berlin.inf.dpp.test.util.SarosTestUtils.submonitor());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (CoreException e) {
            e.printStackTrace();
        }

    }
}
