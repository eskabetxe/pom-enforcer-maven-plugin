package pro.boto.maven.plugin.pom.enforcer.rules;

import static org.assertj.core.api.Assertions.assertThat;

import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;
import pro.boto.maven.plugin.pom.enforcer.serde.PomSerde;

import org.jdom2.Document;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

class RuleRegistryTest {

    private final PomSerde pomSerde = PomSerde.defaultConfig();

    @Test
    void rulesShouldBeSortedByPriority() {
        DependencyOrderRule depRule = new DependencyOrderRule();
        TemplateOrderRule templateRule = new TemplateOrderRule();

        // Insert in wrong order — registry should sort by priority
        RuleRegistry registry = new RuleRegistry(Arrays.asList(depRule, templateRule));

        assertThat(registry.getRules())
                .extracting(PomRule::getName)
                .containsExactly("template-order", "dependency-order");
    }

    @Test
    void analyzeAllShouldCollectViolationsFromAllRules() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencies>"
                + "    <dependency><groupId>Z</groupId>"
                + "<artifactId>Z</artifactId></dependency>"
                + "    <dependency><groupId>A</groupId>"
                + "<artifactId>A</artifactId></dependency>"
                + "  </dependencies>"
                + "  <modelVersion>4.0.0</modelVersion>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        RuleRegistry registry = new RuleRegistry(Arrays.asList(new TemplateOrderRule(), new DependencyOrderRule()));

        List<RuleViolation> violations = registry.analyzeAll(doc);

        assertThat(violations).extracting(RuleViolation::ruleName).contains("template-order", "dependency-order");
    }

    @Test
    void applyAllThenAnalyzeAllShouldReturnEmpty() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencies>"
                + "    <dependency><groupId>Z</groupId>"
                + "<artifactId>Z</artifactId></dependency>"
                + "    <dependency><groupId>A</groupId>"
                + "<artifactId>A</artifactId></dependency>"
                + "  </dependencies>"
                + "  <modelVersion>4.0.0</modelVersion>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        RuleRegistry registry = new RuleRegistry(Arrays.asList(new TemplateOrderRule(), new DependencyOrderRule()));

        registry.applyAll(doc);

        assertThat(registry.analyzeAll(doc)).isEmpty();
    }
}
