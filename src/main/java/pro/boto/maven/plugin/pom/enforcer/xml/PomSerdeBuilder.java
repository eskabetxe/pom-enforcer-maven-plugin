package pro.boto.maven.plugin.pom.enforcer.xml;

public class PomSerdeBuilder {

    private boolean keepBlankLines;
    private int indentSpacesNumber;
    private String lineSeparator;
    private boolean formatSchemaLocation;
    private String encoding;

    public PomSerdeBuilder() {
        this.keepBlankLines = false;
        this.indentSpacesNumber = 4;
        this.lineSeparator = System.lineSeparator();
        this.formatSchemaLocation = true;
        this.encoding = "UTF-8";
    }

    public PomSerdeBuilder withKeepBlankLines(Boolean keepBlankLines) {
        if (keepBlankLines != null) {
            this.keepBlankLines = keepBlankLines;
        }
        return this;
    }

    public PomSerdeBuilder withIndentSpacesNumber(Integer indentSpacesNumber) {
        if (indentSpacesNumber != null) {
            if (indentSpacesNumber < 0) {
                throw new IllegalArgumentException("Indent spaces number cannot be negative");
            }
            this.indentSpacesNumber = indentSpacesNumber;
        }
        return this;
    }

    public PomSerdeBuilder withLineSeparator(String lineSeparator) {
        if (lineSeparator != null && !lineSeparator.isBlank()) {
            this.lineSeparator = lineSeparator;
        }
        return this;
    }

    public PomSerdeBuilder withFormatSchemaLocation(Boolean formatSchemaLocation) {
        if (formatSchemaLocation != null) {
            this.formatSchemaLocation = formatSchemaLocation;
        }
        return this;
    }

    public PomSerdeBuilder withEncoding(String encoding) {
        if (encoding != null && !encoding.isBlank()) {
            this.encoding = encoding;
        }
        return this;
    }

    public PomSerde build() {
        return new PomSerde(keepBlankLines, indentSpacesNumber, lineSeparator, formatSchemaLocation, encoding);
    }
}
