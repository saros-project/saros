package de.fu_berlin.inf.dpp.intellij.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class IntelliJResourceImpl implements IResource {

  protected IReferencePoint referencePoint;

  @Nullable
  @Override
  public Object getAdapter(@NotNull Class<? extends IResource> clazz) {
    return clazz.isInstance(this) ? this : null;
  }

  @Override
  public IReferencePoint getReferencePoint() {
    return referencePoint;
  }
}
