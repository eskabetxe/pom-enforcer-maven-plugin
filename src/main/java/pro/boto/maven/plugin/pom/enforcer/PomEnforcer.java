package pro.boto.maven.plugin.pom.enforcer;

import pro.boto.maven.plugin.pom.enforcer.format.FormattingConfig;
import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;
import pro.boto.maven.plugin.pom.enforcer.rules.RuleRegistry;
import pro.boto.maven.plugin.pom.enforcer.serde.PomSerde;

import org.jdom2.Document;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Core engine. Coordinates rules and serialization for both check and apply goals.
 */
public class PomEnforcer {

    private final PomSerde pomSerde;
    private final RuleRegistry registry;

    public PomEnforcer(FormattingConfig formattingConfig, RuleRegistry registry) {
        this.pomSerde = new PomSerde(formattingConfig);
        this.registry = registry;
    }

    /**
     * Check mode: read-only analysis. Document is never mutated on disk.
     * Returns all rule violations + formatting violations.
     */
    public List<RuleViolation> check(File pomFile) throws Exception {
        Document doc = pomSerde.deserialize(pomFile);
        List<RuleViolation> violations = new ArrayList<>(registry.analyzeAll(doc));

        // Detect formatting drift by comparing against what a full apply would produce
        Document clone = doc.clone();
        registry.applyAll(clone);
        byte[] currentContent = Files.readAllBytes(pomFile.toPath());
        byte[] formattedContent = pomSerde.serialize(clone);

        if (!Arrays.equals(currentContent, formattedContent) && violations.isEmpty()) {
            violations.add(new RuleViolation("formatting", "The file has inconsistent indentation or whitespace."));
        }

        return violations;
    }

    /**
     * Apply mode: mutates document, serializes, and writes back to disk.
     * Returns the violations that were found (and fixed).
     */
    public List<RuleViolation> apply(File pomFile) throws Exception {
        Document doc = pomSerde.deserialize(pomFile);
        List<RuleViolation> violations = new ArrayList<>(registry.analyzeAll(doc));

        registry.applyAll(doc);

        byte[] currentContent = Files.readAllBytes(pomFile.toPath());
        byte[] formattedContent = pomSerde.serialize(doc);

        if (!Arrays.equals(currentContent, formattedContent)) {
            if (violations.isEmpty()) {
                violations.add(new RuleViolation("formatting", "The file has inconsistent indentation or whitespace."));
            }
            Files.write(pomFile.toPath(), formattedContent);
        }

        return violations;
    }
}
