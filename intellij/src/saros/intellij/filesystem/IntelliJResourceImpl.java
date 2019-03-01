package saros.intellij.filesystem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IResource;

public abstract class IntelliJResourceImpl implements IResource {

  @Nullable
  @Override
  public <T extends IResource> T adaptTo(@NotNull Class<T> clazz) {
    return clazz.isInstance(this) ? clazz.cast(this) : null;
  }
}
