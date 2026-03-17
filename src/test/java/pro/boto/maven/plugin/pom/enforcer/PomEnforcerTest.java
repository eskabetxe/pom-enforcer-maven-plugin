package pro.boto.maven.plugin.pom.enforcer;

import static org.assertj.core.api.Assertions.assertThat;

import pro.boto.maven.plugin.pom.enforcer.format.FormattingConfig;
import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;
import pro.boto.maven.plugin.pom.enforcer.rules.RuleRegistry;
import pro.boto.maven.plugin.pom.enforcer.rules.TemplateOrderRule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

class PomEnforcerTest {

    @TempDir
    Path tempDir;

    private PomEnforcer enforcer;

    @BeforeEach
    void setUp() {
        RuleRegistry registry = new RuleRegistry(Collections.singletonList(new TemplateOrderRule()));
        this.enforcer = new PomEnforcer(FormattingConfig.defaultConfig(), registry);
    }

    @Test
    void checkShouldDetectViolationsWithoutModifyingFile() throws Exception {
        String messyXml = "<project><dependencies></dependencies>" + "<modelVersion>4.0.0</modelVersion></project>";
        File pomFile = tempDir.resolve("pom.xml").toFile();
        Files.write(pomFile.toPath(), messyXml.getBytes());

        List<RuleViolation> violations = enforcer.check(pomFile);

        assertThat(violations).isNotEmpty();
        assertThat(Files.readString(pomFile.toPath())).isEqualTo(messyXml);
    }

    @Test
    void applyShouldFixPomAndWriteToDisk() throws Exception {
        String messyXml = "<project><dependencies></dependencies>" + "<modelVersion>4.0.0</modelVersion></project>";
        File pomFile = tempDir.resolve("pom.xml").toFile();
        Files.write(pomFile.toPath(), messyXml.getBytes());

        List<RuleViolation> violations = enforcer.apply(pomFile);

        assertThat(violations).isNotEmpty();

        String cleanXml = Files.readString(pomFile.toPath());
        assertThat(cleanXml)
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<project>\n"
                        + "    <modelVersion>4.0.0</modelVersion>\n"
                        + "    <dependencies/>\n"
                        + "</project>\n");
    }

    @Test
    void checkShouldReturnEmptyWhenPomIsAlreadyPerfect() throws Exception {
        String messyXml = "<project><modelVersion>4.0.0</modelVersion></project>";
        File pomFile = tempDir.resolve("pom.xml").toFile();
        Files.write(pomFile.toPath(), messyXml.getBytes());

        enforcer.apply(pomFile);

        assertThat(enforcer.check(pomFile)).isEmpty();
    }
}
