package de.fu_berlin.inf.dpp.util.xstream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Converter for {@link IPath} objects (and {@link Path} objects too) to strings
 * and back. Uses the portable representation of {@link IPath} objects.
 * 
 * The string representation is usable as XML attribute value.
 */
public class IPathConverter extends AbstractSingleValueConverter {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean canConvert(Class c) {
        return c.isAssignableFrom(IPath.class);
    }

    @Override
    public Object fromString(String path) {
        return Path.fromPortableString(Utils.urlUnescape(path));
    }

    @Override
    public String toString(Object obj) {
        return Utils.urlEscape(((IPath) obj).toPortableString());
    }
}
