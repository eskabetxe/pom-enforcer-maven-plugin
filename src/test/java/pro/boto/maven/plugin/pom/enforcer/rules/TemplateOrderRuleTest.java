package pro.boto.maven.plugin.pom.enforcer.rules;

import static org.assertj.core.api.Assertions.assertThat;

import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;
import pro.boto.maven.plugin.pom.enforcer.serde.PomSerde;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

class TemplateOrderRuleTest {

    private final PomSerde pomSerde = PomSerde.defaultConfig();

    @Test
    void analyzeShouldDetectWrongOrderWithoutMutating() throws Exception {
        String content = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencies><dependency><groupId>A</groupId>"
                + "<artifactId>A</artifactId></dependency></dependencies>"
                + "  <parent><groupId>P</groupId><artifactId>P</artifactId>"
                + "<version>1</version></parent>"
                + "  <modelVersion>4.0.0</modelVersion>"
                + "</project>";

        Document doc = pomSerde.deserialize(new StringReader(content));
        List<String> originalOrder = List.of("dependencies", "parent", "modelVersion");

        TemplateOrderRule rule = new TemplateOrderRule();
        List<RuleViolation> violations = rule.analyze(doc);

        assertThat(violations).isNotEmpty();
        assertThat(violations.get(0).details()).isNotEmpty();

        // Document was NOT mutated
        assertThat(doc.getRootElement().getChildren())
                .extracting(Element::getName)
                .containsExactlyElementsOf(originalOrder);
    }

    @Test
    void applyShouldSortRecursively() throws Exception {
        String content = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencies><dependency><groupId>A</groupId>"
                + "<artifactId>A</artifactId></dependency></dependencies>"
                + "  <parent><groupId>P</groupId><artifactId>P</artifactId>"
                + "<version>1</version></parent>"
                + "  <modelVersion>4.0.0</modelVersion>"
                + "</project>";

        Document doc = pomSerde.deserialize(new StringReader(content));
        TemplateOrderRule rule = new TemplateOrderRule();

        rule.apply(doc);

        assertThat(doc.getRootElement().getChildren())
                .extracting(Element::getName)
                .containsExactly("modelVersion", "parent", "dependencies");

        Element parent =
                doc.getRootElement().getChild("parent", doc.getRootElement().getNamespace());
        assertThat(parent.getChildren())
                .extracting(Element::getName)
                .containsExactly("groupId", "artifactId", "version");
    }

    @Test
    void analyzeAfterApplyShouldReturnEmpty() throws Exception {
        String content = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencies/>"
                + "  <modelVersion>4.0.0</modelVersion>"
                + "</project>";

        Document doc = pomSerde.deserialize(new StringReader(content));
        TemplateOrderRule rule = new TemplateOrderRule();

        rule.apply(doc);
        assertThat(rule.analyze(doc)).isEmpty();
    }

    @Test
    void applyShouldBeIdempotent() throws Exception {
        String content = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencies/>"
                + "  <modelVersion>4.0.0</modelVersion>"
                + "</project>";

        Document doc = pomSerde.deserialize(new StringReader(content));
        TemplateOrderRule rule = new TemplateOrderRule();

        rule.apply(doc);
        byte[] firstPass = pomSerde.serialize(doc);

        rule.apply(doc);
        byte[] secondPass = pomSerde.serialize(doc);

        assertThat(secondPass).isEqualTo(firstPass);
    }
}
