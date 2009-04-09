package de.fu_berlin.inf.dpp.util.xstream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import de.fu_berlin.inf.dpp.util.Util;

/**
 * Converter for {@link IPath} objects to strings and back. Uses the portable
 * representation of {@link IPath} objects.
 */
public class IPathConverter extends AbstractSingleValueConverter {

    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert(Class clazz) {
        return clazz.equals(IPath.class);
    }

    @Override
    public Object fromString(String path) {
        return Path.fromPortableString(Util.urlUnescape(path));
    }

    @Override
    public String toString(Object obj) {
        return Util.urlEscape(((IPath) obj).toPortableString());
    }
}
