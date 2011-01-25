package de.fu_berlin.inf.dpp.util.xstream;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.thoughtworks.xstream.converters.ErrorWriter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.AbstractPullReader;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;

/**
 * XStream reader that pulls from a given {@link XmlPullParser} which is already
 * within a document.
 * 
 * Based on {@link com.thoughtworks.xstream.io.xml.XppReader} by
 * <em>Joe Walnes</em>.
 */
public class XppReader extends AbstractPullReader {

    protected final XmlPullParser parser;

    /** Flag that tells if the reader is still "before" the first node. */
    protected boolean justStarted = true;

    public XppReader(XmlPullParser parser) {
        super(new XmlFriendlyReplacer());
        this.parser = parser;

        /*
         * As addition to the "hack" beyond we have to ensure that the parser works,
         * too, if XStream really pulls the very first
         * element from the underlying parser. ;)
         */
        if (parser.getName() == null)
            justStarted = false;

        moveDown();
    }

    @Override
    protected int pullNextEvent() {
        try {
            /*
             * This is the "hack" to make XStream think it pulls the very first
             * node from the underlying parser.
             */
            if (justStarted) {
                justStarted = false;
                return START_NODE;
            }

            switch (parser.next()) {
            case XmlPullParser.START_DOCUMENT:
            case XmlPullParser.START_TAG:
                return START_NODE;
            case XmlPullParser.END_DOCUMENT:
            case XmlPullParser.END_TAG:
                return END_NODE;
            case XmlPullParser.TEXT:
                return TEXT;
            case XmlPullParser.COMMENT:
                return COMMENT;
            default:
                return OTHER;
            }
        } catch (XmlPullParserException e) {
            throw new StreamException(e);
        } catch (IOException e) {
            throw new StreamException(e);
        }
    }

    @Override
    protected String pullElementName() {
        String name = parser.getName();
        return name;
    }

    @Override
    protected String pullText() {
        return parser.getText();
    }

    public String getAttribute(String name) {
        return parser.getAttributeValue(null, escapeXmlName(name));
    }

    public String getAttribute(int index) {
        return parser.getAttributeValue(index);
    }

    public int getAttributeCount() {
        return parser.getAttributeCount();
    }

    public String getAttributeName(int index) {
        return unescapeXmlName(parser.getAttributeName(index));
    }

    public void appendErrors(ErrorWriter errorWriter) {
        errorWriter.add("line number", String.valueOf(parser.getLineNumber()));
    }

    public void close() {
        /*
         * Do nothing because there is no reader to close. Closing the reader of
         * this.parser is done within the Smack library.
         */
    }
}
