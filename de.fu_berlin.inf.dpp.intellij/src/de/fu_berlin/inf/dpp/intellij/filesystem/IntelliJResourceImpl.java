package de.fu_berlin.inf.dpp.intellij.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class IntelliJResourceImpl implements IResource {

  @Nullable
  @Override
  public <T extends IResource> T adaptTo(@NotNull Class<T> clazz) {
    return clazz.isInstance(this) ? clazz.cast(this) : null;
  }
}
