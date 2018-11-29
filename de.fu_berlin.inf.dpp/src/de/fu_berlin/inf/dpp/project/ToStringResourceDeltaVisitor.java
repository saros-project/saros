package de.fu_berlin.inf.dpp.project;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * IResourceDeltaVisitor which collects all changes as full paths to the affected resource. Used for
 * debugging.
 */
public class ToStringResourceDeltaVisitor implements IResourceDeltaVisitor {
  private static final Map<Integer, String> map = new HashMap<Integer, String>();

  {
    map.put(IResourceDelta.CONTENT, "C");
    map.put(IResourceDelta.MOVED_FROM, "F");
    map.put(IResourceDelta.MOVED_TO, "T");
    map.put(IResourceDelta.COPIED_FROM, "f");
    map.put(IResourceDelta.OPEN, "O");
    map.put(IResourceDelta.TYPE, "Y");
    map.put(IResourceDelta.SYNC, "S");
    map.put(IResourceDelta.MARKERS, "M");
    map.put(IResourceDelta.REPLACED, "R");
    map.put(IResourceDelta.DESCRIPTION, "D");
    map.put(IResourceDelta.ENCODING, "E");
    map.put(IResourceDelta.LOCAL_CHANGED, "L");
    // if(Eclipse 3.6) {map.put(IResourceDelta.DERIVED_CHANGED, "d");}
  }

  private final StringBuilder sb = new StringBuilder(1024);

  @Override
  public boolean visit(IResourceDelta delta) throws CoreException {
    boolean result = true;
    switch (delta.getKind()) {
      case IResourceDelta.NO_CHANGE:
        sb.append("0");
        result = false;
        break;
      case IResourceDelta.CHANGED:
        sb.append("C(");
        int f = delta.getFlags();
        if (f == 0) {
          sb.append("0) ");
        } else {
          Set<Entry<Integer, String>> entrySet = map.entrySet();
          for (Entry<Integer, String> entry : entrySet) {
            if (0 != (f & entry.getKey())) sb.append(entry.getValue());
          }
          sb.append(") ");
        }

        break;
      case IResourceDelta.ADDED:
        sb.append("A ");
        break;
      case IResourceDelta.REMOVED:
        sb.append("R ");
        break;
      default:
        sb.append("? ");
    }
    IResource resource = delta.getResource();
    if (resource != null) {
      if (resource.isDerived()) sb.append("* ");

      if (resource.isPhantom()) sb.append("P ");

      if (resource.isHidden()) sb.append("H ");

      sb.append(resource.getFullPath().toPortableString());
    } else {
      sb.append("No resource");
    }
    sb.append("\n");

    return result;
  }

  @Override
  public String toString() {
    return sb.toString();
  }
}
