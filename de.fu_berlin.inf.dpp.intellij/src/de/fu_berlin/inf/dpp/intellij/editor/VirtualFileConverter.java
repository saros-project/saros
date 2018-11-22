package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJProjectImplV2;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.annotations.Inject;

/**
 * Provides static methods to convert VirtualFiles to Saros resource objects.
 */
public class VirtualFileConverter {

    private static final Logger log = Logger
        .getLogger(VirtualFileConverter.class);

    @Inject
    private static Project project;

    static {
        SarosPluginContext.initComponent(new VirtualFileConverter());
    }

    private VirtualFileConverter() {
        //NOP
    }

    /**
     * Returns an <code>SPath</code> representing the given file.
     *
     * @param virtualFile file to get the <code>SPath</code> for
     * @return an <code>SPath</code> representing the given file or
     * <code>null</code> if given file does not exist, no module could be found
     * for the file or the found module can not be shared through saros, or the
     * relative path between the module root and the file could not be
     * constructed
     */
    @Nullable
    public static SPath convertToSPath(
        @NotNull
            VirtualFile virtualFile) {

        IResource resource = convertToResource(virtualFile);

        return resource == null ? null : new SPath(resource);
    }

    /**
     * Returns an <code>IResource</code> representing the given
     * <code>VirtualFile</code>.
     *
     * @param virtualFile file to get the <code>IResource</code> for
     * @return an <code>IResource</code> representing the given file or
     * <code>null</code> if given file does not exist, no module could be found
     * for the file or the found module can not be shared through saros, or the
     * relative path between the module root and the file could not be
     * constructed
     */
    @Nullable
    public static IResource convertToResource(
        @NotNull
            VirtualFile virtualFile) {

        Module module = ModuleUtil.findModuleForFile(virtualFile, project);

        if (module == null) {
            log.debug("Could not convert VirtualFile " + virtualFile
                + " as no module could be found for the file.");

            return null;
        }

        try {
            IntelliJProjectImplV2 wrappedModule = new IntelliJProjectImplV2(
                module);

            return wrappedModule.getResource(virtualFile);

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.debug("Could not convert VirtualFile " + virtualFile
                + " as the creation of an IProject object for its module "
                + module + " failed.", e);

            return null;
        }
    }

    /**
     * Returns an <code>IResource</code> for the passed VirtualFile and module.
     *
     * @param virtualFile file to get the <code>IResource</code> for
     * @param project     module the file belongs to
     * @return an <code>IResource</code> for the passed file or
     * <code>null</code> it does not belong to the passed module.
     */
    @Nullable
    public static IResource getResource(
        @NotNull
            VirtualFile virtualFile,
        @NotNull
            IProject project) {

        IntelliJProjectImplV2 module = (IntelliJProjectImplV2) project
            .getAdapter(IntelliJProjectImplV2.class);

        return module.getResource(virtualFile);
    }
}
