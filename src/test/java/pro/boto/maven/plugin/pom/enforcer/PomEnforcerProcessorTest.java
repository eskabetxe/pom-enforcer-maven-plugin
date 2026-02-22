package pro.boto.maven.plugin.pom.enforcer;

import static org.assertj.core.api.Assertions.*;

import pro.boto.maven.plugin.pom.enforcer.format.FormattingConfig;
import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;
import pro.boto.maven.plugin.pom.enforcer.rules.PomRule;
import pro.boto.maven.plugin.pom.enforcer.rules.TemplateOrderRule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

class PomEnforcerProcessorTest {

    @TempDir
    Path tempDir;

    private PomEnforcerProcessor enforcer;

    @BeforeEach
    void setUp() throws Exception {
        // Use the default template parser
        List<PomRule> parsers = Collections.singletonList(new TemplateOrderRule());
        this.enforcer = new PomEnforcerProcessor(FormattingConfig.defaultConfig(), parsers);
    }

    @Test
    void shouldReturnTrueWhenPomIsMalformed() throws Exception {
        String messyXml = "<project><dependencies></dependencies><modelVersion>4.0.0</modelVersion></project>";
        File pomFile = tempDir.resolve("pom.xml").toFile();
        Files.write(pomFile.toPath(), messyXml.getBytes());

        List<RuleViolation> violations = enforcer.process(pomFile, false);
        // Assert violations
        assertThat(violations).isNotEmpty();
        // Ensure file was NOT modified in check mode
        assertThat(Files.readString(pomFile.toPath())).isEqualTo(messyXml);
    }

    @Test
    void shouldFixPomWhenApplyChangesIsTrue() throws Exception {
        String messyXml = "<project><dependencies></dependencies><modelVersion>4.0.0</modelVersion></project>";
        File pomFile = tempDir.resolve("pom.xml").toFile();
        Files.write(pomFile.toPath(), messyXml.getBytes());

        List<RuleViolation> violations = enforcer.process(pomFile, true);
        // Assert violations
        assertThat(violations).isNotEmpty();

        String cleanXml = Files.readString(pomFile.toPath());
        assertThat(cleanXml)
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n"
                        + "    <modelVersion>4.0.0</modelVersion>\n"
                        + "    <dependencies/>\n"
                        + "</project>\n");

        // Check order: modelVersion should come before dependencies based on default template
        assertThat(cleanXml.indexOf("modelVersion")).isLessThan(cleanXml.indexOf("dependencies"));
    }

    @Test
    void shouldReturnFalseWhenPomIsAlreadyPerfect() throws Exception {
        String messyXml = "<project><modelVersion>4.0.0</modelVersion></project>";
        File pomFile = tempDir.resolve("pom.xml").toFile();
        Files.write(pomFile.toPath(), messyXml.getBytes());

        // First pass to fix it
        enforcer.process(pomFile, true);

        List<RuleViolation> violations = enforcer.process(pomFile, true);
        // Assert violations
        assertThat(violations).isEmpty();
    }
}
