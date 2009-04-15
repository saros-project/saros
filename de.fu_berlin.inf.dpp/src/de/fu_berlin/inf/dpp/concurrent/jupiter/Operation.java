package de.fu_berlin.inf.dpp.concurrent.jupiter;

import java.util.List;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.activities.TextEditActivity;

/**
 * This interface must be implemented by all operations. An operation is
 * application dependent and therefore this interface does not contain any
 * specific methods at all.
 */
public interface Operation {

    List<TextEditActivity> toTextEdit(IPath path, String source);

}
