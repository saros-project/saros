/**
 * Saros uses XML representations for transferring Activities (Java objects) through the network.
 * The serialization and deserialization is done by the XStream library, which provides both
 * pre-defined Converters for basic datatypes and a Converter interface for advanced conversions,
 * e.g. of complex datatypes.
 *
 * <p>Activity classes themselves use XStream annotations, but their fields may rely on one or more
 * of these converters:
 *
 * <ul>
 *   <li>UserConverter
 *   <li>SPathConverter
 * </ul>
 */
package saros.misc.xstream;
