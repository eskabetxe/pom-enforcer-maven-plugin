package pro.boto.maven.plugin.pom.enforcer.format;

import pro.boto.maven.plugin.pom.enforcer.serde.PomOutputProcessor;

import org.jdom2.output.Format;

import java.util.Objects;

/**
 * Immutable configuration for POM serialization formatting.
 *
 * <p>Contains builder methods that produce the JDOM2 objects
 * this config describes, so {@code PomSerde} stays free of mapping logic.
 */
public class FormattingConfig {

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final int DEFAULT_INDENT_SIZE = 4;
    public static final boolean DEFAULT_KEEP_BLANK_LINES = false;
    public static final String DEFAULT_LINE_SEPARATOR = "\n";
    public static final SchemaLocationPolicy DEFAULT_SCHEMA_LOCATION = SchemaLocationPolicy.INDENTED;

    private String encoding = DEFAULT_ENCODING;
    private int indentSize = DEFAULT_INDENT_SIZE;
    private boolean keepBlankLines = DEFAULT_KEEP_BLANK_LINES;
    private String lineSeparator = DEFAULT_LINE_SEPARATOR;
    private SchemaLocationPolicy schemaLocation = DEFAULT_SCHEMA_LOCATION;

    /** No-arg constructor with sensible defaults. Required for Maven injection. */
    public FormattingConfig() {}

    public FormattingConfig(
            String encoding,
            int indentSize,
            boolean keepBlankLines,
            String lineSeparator,
            SchemaLocationPolicy schemaLocation) {
        this.encoding = Objects.requireNonNull(encoding, "encoding must not be null");
        this.indentSize = indentSize;
        this.keepBlankLines = keepBlankLines;
        this.lineSeparator = Objects.requireNonNull(lineSeparator, "lineSeparator must not be null");
        this.schemaLocation = Objects.requireNonNull(schemaLocation, "schemaLocation must not be null");
    }

    // ---- Getters ----

    public String encoding() {
        return encoding;
    }

    public int indentSize() {
        return indentSize;
    }

    public boolean keepBlankLines() {
        return keepBlankLines;
    }

    public String lineSeparator() {
        return lineSeparator;
    }

    public SchemaLocationPolicy schemaLocation() {
        return schemaLocation;
    }

    // ---- Setters (for Maven injection) ----

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    public void setKeepBlankLines(boolean keepBlankLines) {
        this.keepBlankLines = keepBlankLines;
    }

    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = resolveEscapes(lineSeparator);
    }

    public void setSchemaLocation(SchemaLocationPolicy schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    // ---- Builder methods: produce JDOM2 objects ----

    /**
     * Builds the JDOM2 {@link Format} configured by this policy.
     */
    public Format buildFormat() {
        Format format = Format.getPrettyFormat()
                .setIndent(" ".repeat(indentSize))
                .setLineSeparator(lineSeparator)
                .setEncoding(encoding);
        format.setSpecifiedAttributesOnly(schemaLocation != SchemaLocationPolicy.KEEP);
        return format;
    }

    /**
     * Builds the {@link PomOutputProcessor} configured by this policy.
     */
    public PomOutputProcessor buildOutputProcessor() {
        return new PomOutputProcessor(schemaLocation == SchemaLocationPolicy.INDENTED);
    }

    private static String resolveEscapes(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
    }

    public static FormattingConfig defaultConfig() {
        return new FormattingConfig();
    }
}
