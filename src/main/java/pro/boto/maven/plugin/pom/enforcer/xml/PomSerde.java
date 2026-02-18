package pro.boto.maven.plugin.pom.enforcer.xml;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class PomSerde {

    private final boolean keepBlankLines;
    private final int indentSpacesNumber;
    private final String lineSeparator;
    private final boolean formatSchemaLocation;
    private final String encoding;

    protected PomSerde(
            boolean keepBlankLines,
            int indentSpacesNumber,
            String lineSeparator,
            boolean formatSchemaLocation,
            String encoding) {
        this.keepBlankLines = keepBlankLines;
        this.indentSpacesNumber = indentSpacesNumber;
        this.lineSeparator = lineSeparator;
        this.formatSchemaLocation = formatSchemaLocation;
        this.encoding = encoding;
    }

    public static PomSerdeBuilder builder() {
        return new PomSerdeBuilder();
    }

    public static PomSerde defaults() {
        return new PomSerdeBuilder().build();
    }

    public Document deserialize(File pomFile) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        builder.setIgnoringBoundaryWhitespace(!keepBlankLines);
        builder.setIgnoringElementContentWhitespace(!keepBlankLines);

        return builder.build(pomFile);
    }

    public byte[] serialize(Document document) {
        try {
            Format format = Format.getPrettyFormat()
                    .setIndent(" ".repeat(indentSpacesNumber))
                    .setLineSeparator(lineSeparator)
                    .setEncoding(encoding);
            format.setSpecifiedAttributesOnly(formatSchemaLocation);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            XMLOutputter outputter = new XMLOutputter(new PomOutputProcessor());
            outputter.setFormat(format);
            outputter.output(document, output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error during XML serialization", e);
        }
    }
}
