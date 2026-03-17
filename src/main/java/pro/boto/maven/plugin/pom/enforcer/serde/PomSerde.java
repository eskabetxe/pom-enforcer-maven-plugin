package pro.boto.maven.plugin.pom.enforcer.serde;

import pro.boto.maven.plugin.pom.enforcer.format.FormattingConfig;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

public class PomSerde {

    private final FormattingConfig config;

    public PomSerde(FormattingConfig config) {
        this.config = config;
    }

    public static PomSerde defaultConfig() {
        return new PomSerde(FormattingConfig.defaultConfig());
    }

    public Document deserialize(File pomFile) throws Exception {
        return createSaxBuilder().build(pomFile);
    }

    public Document deserialize(Reader pomReader) throws Exception {
        return createSaxBuilder().build(pomReader);
    }

    private SAXBuilder createSaxBuilder() {
        SAXBuilder builder = new SAXBuilder();
        builder.setIgnoringBoundaryWhitespace(!config.keepBlankLines());
        builder.setIgnoringElementContentWhitespace(!config.keepBlankLines());
        return builder;
    }

    public byte[] serialize(Document document) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            XMLOutputter outputter = new XMLOutputter(config.buildOutputProcessor());
            outputter.setFormat(config.buildFormat());
            outputter.output(document, output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error during XML serialization", e);
        }
    }
}
