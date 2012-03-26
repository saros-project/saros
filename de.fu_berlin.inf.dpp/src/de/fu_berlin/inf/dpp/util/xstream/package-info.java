/**
 * The converters in this package can convert certain classes from objects
 * to string representations for serialization. They are typically used
 * in an annotation within a serializabe class, e.g.:
 * 
 * <code>
 * @XStreamConverter(JIDConverter.class)
 * private final JID theJID;
 * </code>
 */

package de.fu_berlin.inf.dpp.util.xstream;

import com.thoughtworks.xstream.annotations.XStreamConverter;

import de.fu_berlin.inf.dpp.util.xstream.JIDConverter;
