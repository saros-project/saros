package de.fu_berlin.inf.dpp.intellij.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.fu_berlin.inf.dpp.filesystem.IResource;

public abstract class IntelliJResourceImplV2 implements IResource {

    protected IReferencePoint referencePoint;

    @Nullable
    @Override
    public Object getAdapter(@NotNull Class<? extends IResource> clazz) {
        return clazz.isInstance(this) ? this : null;
    }

    @Override
    public IReferencePoint getReferencePoint()
    {
        return referencePoint;
    }
}
