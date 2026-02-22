package pro.boto.maven.plugin.pom.enforcer;

import pro.boto.maven.plugin.pom.enforcer.format.FormattingConfig;
import pro.boto.maven.plugin.pom.enforcer.format.PomSerde;
import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;
import pro.boto.maven.plugin.pom.enforcer.rules.PomRule;

import org.jdom2.Document;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PomEnforcerProcessor {
    private final PomSerde pomSerde;
    private final List<PomRule> parsers;

    public PomEnforcerProcessor(FormattingConfig formattingConfig, List<PomRule> parsers) {
        this.pomSerde = new PomSerde(formattingConfig);
        this.parsers = parsers;
    }

    public List<RuleViolation> process(File pomFile, boolean applyChanges) throws Exception {
        Document doc = pomSerde.deserialize(pomFile);
        List<RuleViolation> allViolations = new ArrayList<>();
        // Collect violations from each rule
        for (PomRule rule : parsers) {
            allViolations.addAll(rule.apply(doc));
        }
        byte[] currentContent = Files.readAllBytes(pomFile.toPath());
        byte[] formattedContent = pomSerde.serialize(doc);

        if (!Arrays.equals(currentContent, formattedContent)) {
            if (allViolations.isEmpty()) {
                allViolations.add(
                        new RuleViolation("formatting", "The file has inconsistent indentation or whitespace."));
            }
            if (applyChanges) {
                Files.write(pomFile.toPath(), formattedContent);
            }
        }

        return allViolations;
    }
}
