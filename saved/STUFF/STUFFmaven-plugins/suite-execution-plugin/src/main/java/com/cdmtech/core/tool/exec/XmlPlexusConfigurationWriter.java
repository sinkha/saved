package com.cdmtech.core.tool.exec;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

import java.io.IOException;
import java.io.Writer;

/**
 * Write a plexus configuration to a stream.
 * Note: This class was originally copied from plexus-container-default.
 * It is duplicated here to maintain compatibility with both Maven 2.x and
 * Maven 3.x.
 *
 */
public class XmlPlexusConfigurationWriter {

    public void write(PlexusConfiguration configuration,
                      Writer writer) throws IOException {
        PrettyPrintXMLWriter xmlWriter = new PrettyPrintXMLWriter(writer);

        write(configuration, xmlWriter);
    }

    private void write(PlexusConfiguration c,
                       XMLWriter w) throws IOException {
        int count = c.getChildCount();

        if (count == 0) {
            writeTag(c, w);
        } else {
            w.startElement(c.getName());
            writeAttributes(c, w);

            for (int i = 0; i < count; i++) {
                PlexusConfiguration child = c.getChild(i);

                write(child, w);
            }

            w.endElement();
        }
    }

    private void writeTag(PlexusConfiguration c,
                          XMLWriter w) throws IOException {
        w.startElement(c.getName());

        writeAttributes(c, w);

        String value = c.getValue(null);

        if (value != null) {
            w.writeText(value);
        }

        w.endElement();
    }

    private void writeAttributes(PlexusConfiguration c,
                                 XMLWriter w) throws IOException {
        String[] names = c.getAttributeNames();

        for (int i = 0; i < names.length; i++) {
            w.addAttribute(names[i], c.getAttribute(names[i], null));
        }
    }
}
