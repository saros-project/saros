package de.fu_berlin.inf.dpp.test.fakes;

import java.io.File;

import org.eclipse.core.resources.IFolder;

/**
 * Fake-Implementation of {@link org.eclipse.core.resources.IFolder}.
 * 
 * Wrappes a {@link java.io.File} to delegate the the most functionality.
 * 
 * If you call a functionality which is not implemented you will get a
 * {@link UnsupportedOperationException}.
 * 
 * @see de.fu_berlin.inf.dpp.test.util.EclipseWorkspaceFakeFacadeTest
 * @author cordes
 */
public class EclipseFolderFake extends EclipseContainerFake implements IFolder {
    protected EclipseFolderFake(File wrappedFile) {
        super(wrappedFile);
    }
}
