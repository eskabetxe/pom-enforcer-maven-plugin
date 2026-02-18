package pro.boto.maven.plugin.pom.enforcer.xml;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

public class PomSerde {

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final boolean DEFAULT_FORMAT_SCHEMA_LOCATION = true;
    public static final boolean DEFAULT_INDENT_SCHEMA_LOCATION = true;
    public static final int DEFAULT_INDENT_SPACES_NUMBER = 4;
    public static final boolean DEFAULT_KEEP_BLANK_LINES = false;
    public static final String DEFAULT_LINE_SEPARATOR = "\n";

    private String encoding;
    private boolean formatSchemaLocation;
    private boolean indentSchemaLocation;
    private int indentSpacesNumber;
    private boolean keepBlankLines;
    private String lineSeparator;

    public PomSerde() {
        withEncoding(DEFAULT_ENCODING);
        withFormatSchemaLocation(DEFAULT_FORMAT_SCHEMA_LOCATION);
        withIndentSchemaLocation(DEFAULT_INDENT_SCHEMA_LOCATION);
        withIndentSpacesNumber(DEFAULT_INDENT_SPACES_NUMBER);
        withKeepBlankLines(DEFAULT_KEEP_BLANK_LINES);
        withLineSeparator(DEFAULT_LINE_SEPARATOR);
    }

    public PomSerde withKeepBlankLines(Boolean keepBlankLines) {
        if (keepBlankLines != null) {
            this.keepBlankLines = keepBlankLines;
        }
        return this;
    }

    public PomSerde withIndentSpacesNumber(Integer indentSpacesNumber) {
        if (indentSpacesNumber != null) {
            if (indentSpacesNumber < 0) {
                throw new IllegalArgumentException("Indent spaces number cannot be negative");
            }
            this.indentSpacesNumber = indentSpacesNumber;
        }
        return this;
    }

    public PomSerde withLineSeparator(String lineSeparator) {
        if (lineSeparator != null && !lineSeparator.isEmpty()) {
            this.lineSeparator = lineSeparator;
        }
        return this;
    }

    public PomSerde withFormatSchemaLocation(Boolean formatSchemaLocation) {
        if (formatSchemaLocation != null) {
            this.formatSchemaLocation = formatSchemaLocation;
        }
        return this;
    }

    public PomSerde withEncoding(String encoding) {
        if (encoding != null && !encoding.isBlank()) {
            this.encoding = encoding;
        }
        return this;
    }

    public PomSerde withIndentSchemaLocation(Boolean indentSchemaLocation) {
        if (indentSchemaLocation != null) {
            this.indentSchemaLocation = indentSchemaLocation;
        }
        return this;
    }

    public Document deserialize(File pomFile) throws Exception {
        return createSaxBuilder().build(pomFile);
    }

    public Document deserialize(Reader pomReader) throws Exception {
        return createSaxBuilder().build(pomReader);
    }

    private SAXBuilder createSaxBuilder() {
        SAXBuilder builder = new SAXBuilder();
        builder.setIgnoringBoundaryWhitespace(!keepBlankLines);
        builder.setIgnoringElementContentWhitespace(!keepBlankLines);
        return builder;
    }

    public byte[] serialize(Document document) {
        try {
            Format format = Format.getPrettyFormat()
                    .setIndent(" ".repeat(indentSpacesNumber))
                    .setLineSeparator(lineSeparator)
                    .setEncoding(encoding);
            format.setSpecifiedAttributesOnly(formatSchemaLocation);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            XMLOutputter outputter = new XMLOutputter(new PomOutputProcessor(indentSchemaLocation));
            outputter.setFormat(format);
            outputter.output(document, output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error during XML serialization", e);
        }
    }
}
