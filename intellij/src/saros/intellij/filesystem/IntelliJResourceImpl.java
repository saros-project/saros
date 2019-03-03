package saros.intellij.filesystem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;

public abstract class IntelliJResourceImpl implements IResource {

  protected IReferencePoint referencePoint;

  @Nullable
  @Override
  public <T extends IResource> T adaptTo(@NotNull Class<T> clazz) {
    return clazz.isInstance(this) ? clazz.cast(this) : null;
  }

  @Override
  public IReferencePoint getReferencePoint() {
    return referencePoint;
  }
}
