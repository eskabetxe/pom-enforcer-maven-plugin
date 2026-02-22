package pro.boto.maven.plugin.pom.enforcer.format;

import java.util.Objects;

public class FormattingConfig {
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final boolean DEFAULT_FORMAT_SCHEMA_LOCATION = true;
    public static final boolean DEFAULT_INDENT_SCHEMA_LOCATION = true;
    public static final int DEFAULT_INDENT_SPACES_NUMBER = 4;
    public static final boolean DEFAULT_KEEP_BLANK_LINES = false;
    public static final String DEFAULT_LINE_SEPARATOR = "\n";

    private final String encoding;
    private final boolean formatSchemaLocation;
    private final boolean indentSchemaLocation;
    private final int indentSpacesNumber;
    private final boolean keepBlankLines;
    private final String lineSeparator;

    public FormattingConfig(
            String encoding,
            boolean formatSchemaLocation,
            boolean indentSchemaLocation,
            int indentSpacesNumber,
            boolean keepBlankLines,
            String lineSeparator) {
        this.encoding = Objects.requireNonNull(encoding, "encoding must not be null");
        this.formatSchemaLocation = formatSchemaLocation;
        this.indentSchemaLocation = indentSchemaLocation;
        this.indentSpacesNumber = indentSpacesNumber;
        this.keepBlankLines = keepBlankLines;
        this.lineSeparator = Objects.requireNonNull(lineSeparator, "lineSeparator must not be null");
    }

    public String encoding() {
        return encoding;
    }

    public boolean formatSchemaLocation() {
        return formatSchemaLocation;
    }

    public boolean indentSchemaLocation() {
        return indentSchemaLocation;
    }

    public int indentSpacesNumber() {
        return indentSpacesNumber;
    }

    public boolean keepBlankLines() {
        return keepBlankLines;
    }

    public String lineSeparator() {
        return lineSeparator;
    }

    public static FormattingConfig defaultConfig() {
        return new FormattingConfig(
                DEFAULT_ENCODING,
                DEFAULT_FORMAT_SCHEMA_LOCATION,
                DEFAULT_INDENT_SCHEMA_LOCATION,
                DEFAULT_INDENT_SPACES_NUMBER,
                DEFAULT_KEEP_BLANK_LINES,
                DEFAULT_LINE_SEPARATOR);
    }
}
