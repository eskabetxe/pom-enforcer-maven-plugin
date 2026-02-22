package pro.boto.maven.plugin.pom.enforcer.rules;

import static org.assertj.core.api.Assertions.assertThat;

import pro.boto.maven.plugin.pom.enforcer.format.PomSerde;
import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

class TemplateOrderRuleTest {

    private final PomSerde pomSerde = PomSerde.defaultConfig();

    @Test
    void testApplyGoalSortsRecursively() throws Exception {
        String content = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencies><dependency><groupId>A</groupId><artifactId>A</artifactId></dependency></dependencies>"
                + "  <parent><groupId>P</groupId><artifactId>P</artifactId><version>1</version></parent>"
                + "  <modelVersion>4.0.0</modelVersion>"
                + "</project>";

        Document doc = pomSerde.deserialize(new StringReader(content));

        TemplateOrderRule template = new TemplateOrderRule();
        List<RuleViolation> violations = template.apply(doc);
        // Assert
        assertThat(violations).isNotEmpty();

        // Assert: Extract child names and verify order using AssertJ
        List<Element> children = doc.getRootElement().getChildren();

        assertThat(children)
                .as("Root elements should be sorted according to the template")
                .extracting(Element::getName)
                .containsExactly("modelVersion", "parent", "dependencies");

        // Assert: Verify inner recursion (parent GAV order)
        Element parent =
                doc.getRootElement().getChild("parent", doc.getRootElement().getNamespace());
        assertThat(parent.getChildren())
                .as("Parent inner elements should be sorted (GAV order)")
                .extracting(Element::getName)
                .containsExactly("groupId", "artifactId", "version");
    }
}
