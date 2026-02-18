package pro.boto.maven.plugin.pom.enforcer;

import pro.boto.maven.plugin.pom.enforcer.parsers.Parser;
import pro.boto.maven.plugin.pom.enforcer.xml.PomSerde;

import org.jdom2.Document;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class PomEnforcerProcessor {
    private final PomSerde pomSerde;
    private final List<Parser> parsers;

    public PomEnforcerProcessor(PomSerde pomSerde, List<Parser> parsers) {
        this.pomSerde = pomSerde;
        this.parsers = parsers;
    }

    public boolean process(File pomFile, boolean applyChanges) throws Exception {
        Document doc = pomSerde.deserialize(pomFile);

        // Apply all parsers/modifiers
        parsers.forEach(parser -> parser.accept(doc));

        byte[] currentContent = Files.readAllBytes(pomFile.toPath());
        byte[] formattedContent = pomSerde.serialize(doc);

        boolean isDifferent = !Arrays.equals(currentContent, formattedContent);

        if (isDifferent && applyChanges) {
            Files.write(pomFile.toPath(), formattedContent);
        }

        return isDifferent;
    }
}
