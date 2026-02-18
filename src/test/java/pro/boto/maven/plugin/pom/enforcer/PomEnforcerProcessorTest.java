package pro.boto.maven.plugin.pom.enforcer;

import static org.assertj.core.api.Assertions.*;

import pro.boto.maven.plugin.pom.enforcer.parsers.Parser;
import pro.boto.maven.plugin.pom.enforcer.parsers.TemplateOrderParser;
import pro.boto.maven.plugin.pom.enforcer.xml.PomSerde;

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
        // Using defaults for Serde
        PomSerde serde = new PomSerde();
        // Use the default template parser
        List<Parser> parsers = Collections.singletonList(new TemplateOrderParser());
        this.enforcer = new PomEnforcerProcessor(serde, parsers);
    }

    @Test
    void shouldReturnTrueWhenPomIsMalformed() throws Exception {
        // Arrange: A messy, unordered POM
        String messyXml = "<project><dependencies></dependencies><modelVersion>4.0.0</modelVersion></project>";
        File pomFile = tempDir.resolve("pom.xml").toFile();
        Files.write(pomFile.toPath(), messyXml.getBytes());

        // Act: Run enforcer in "check" mode (applyChanges = false)
        boolean result = enforcer.process(pomFile, false);

        // Assert
        assertThat(result).as("Should detect violation").isTrue();

        // Ensure file was NOT modified in check mode
        assertThat(Files.readString(pomFile.toPath())).isEqualTo(messyXml);
    }

    @Test
    void shouldFixPomWhenApplyChangesIsTrue() throws Exception {
        // Arrange
        String messyXml = "<project><dependencies></dependencies><modelVersion>4.0.0</modelVersion></project>";
        File pomFile = tempDir.resolve("pom.xml").toFile();
        Files.write(pomFile.toPath(), messyXml.getBytes());

        // Act: Run enforcer in "apply" mode
        boolean result = enforcer.process(pomFile, true);

        // Assert
        assertThat(result).isTrue();

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
        // Arrange: Prepare a perfectly formatted POM first
        String messyXml = "<project><modelVersion>4.0.0</modelVersion></project>";
        File pomFile = tempDir.resolve("pom.xml").toFile();
        Files.write(pomFile.toPath(), messyXml.getBytes());

        // First pass to fix it
        enforcer.process(pomFile, true);

        // Act: Second pass
        boolean result = enforcer.process(pomFile, false);

        // Assert
        assertThat(result)
                .as("Already perfect POM should not trigger violation")
                .isFalse();
    }

    @Test
    void shouldFormatProjectAttributesWithPedanticAlignment() throws Exception {
        String messyXml =
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n<modelVersion>4.0.0</modelVersion>\n</project>";
        File pomFile = tempDir.resolve("pom.xml").toFile();
        Files.write(pomFile.toPath(), messyXml.getBytes());

        // First pass to fix it
        enforcer.process(pomFile, true);

        // Act: Second pass
        boolean result = enforcer.process(pomFile, false);

        // Assert
        assertThat(result).isFalse();

        String cleanXml = Files.readString(pomFile.toPath());
        assertThat(cleanXml)
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
                        + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                        + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
                        + "    <modelVersion>4.0.0</modelVersion>\n"
                        + "</project>\n");
    }

    @Test
    void shouldFormatProjectAttributesWithLineAlignment() throws Exception {
        String messyXml =
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n<modelVersion>4.0.0</modelVersion>\n</project>";
        File pomFile = tempDir.resolve("pom.xml").toFile();
        Files.write(pomFile.toPath(), messyXml.getBytes());

        // First pass to fix it
        // Using defaults for Serde
        PomSerde serde = new PomSerde().withIndentSchemaLocation(false);
        // Use the default template parser
        List<Parser> parsers = Collections.singletonList(new TemplateOrderParser());
        PomEnforcerProcessor enforcer = new PomEnforcerProcessor(serde, parsers);
        enforcer.process(pomFile, true);

        // Act: Second pass
        boolean result = enforcer.process(pomFile, false);

        // Assert
        assertThat(result).isFalse();

        String cleanXml = Files.readString(pomFile.toPath());
        assertThat(cleanXml)
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
                        + "    <modelVersion>4.0.0</modelVersion>\n"
                        + "</project>\n");
    }
}
