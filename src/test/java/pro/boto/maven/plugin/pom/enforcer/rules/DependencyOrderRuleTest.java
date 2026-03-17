package pro.boto.maven.plugin.pom.enforcer.rules;

import static org.assertj.core.api.Assertions.assertThat;

import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;
import pro.boto.maven.plugin.pom.enforcer.serde.PomSerde;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

class DependencyOrderRuleTest {

    private final PomSerde pomSerde = PomSerde.defaultConfig();
    private final Namespace ns = Namespace.getNamespace("http://maven.apache.org/POM/4.0.0");

    @Test
    void analyzeShouldDetectUnsortedDepsWithoutMutating() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencies>"
                + "    <dependency><groupId>org.hibernate</groupId>"
                + "<artifactId>hibernate-core</artifactId></dependency>"
                + "    <dependency><groupId>com.google.guava</groupId>"
                + "<artifactId>guava</artifactId></dependency>"
                + "  </dependencies>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        DependencyOrderRule rule = new DependencyOrderRule();
        List<RuleViolation> violations = rule.analyze(doc);

        assertThat(violations).isNotEmpty();
        assertThat(violations.get(0).details()).isNotEmpty();

        // Document NOT mutated: hibernate still first
        List<Element> deps = doc.getRootElement().getChild("dependencies", ns).getChildren();
        assertThat(deps).extracting(e -> e.getChildText("artifactId", ns)).containsExactly("hibernate-core", "guava");
    }

    @Test
    void applyShouldSortByGroupIdAndArtifactId() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencies>"
                + "    <dependency><groupId>org.hibernate</groupId>"
                + "<artifactId>hibernate-core</artifactId></dependency>"
                + "    <dependency><groupId>com.google.guava</groupId>"
                + "<artifactId>guava</artifactId></dependency>"
                + "    <dependency><groupId>org.hibernate</groupId>"
                + "<artifactId>hibernate-validator</artifactId></dependency>"
                + "  </dependencies>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        new DependencyOrderRule().apply(doc);

        List<Element> deps = doc.getRootElement().getChild("dependencies", ns).getChildren();
        assertThat(deps)
                .extracting(e -> e.getChildText("artifactId", ns))
                .containsExactly("guava", "hibernate-core", "hibernate-validator");
    }

    @Test
    void applyShouldPlaceBomsAtTopInManagedDepsOnly() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencyManagement><dependencies>"
                + "    <dependency><groupId>Z.regular</groupId>"
                + "<artifactId>A</artifactId></dependency>"
                + "    <dependency><groupId>M.bom</groupId>"
                + "<artifactId>B</artifactId><type>pom</type>"
                + "<scope>import</scope></dependency>"
                + "    <dependency><groupId>A.bom</groupId>"
                + "<artifactId>C</artifactId><type>pom</type>"
                + "<scope>import</scope></dependency>"
                + "  </dependencies></dependencyManagement>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        new DependencyOrderRule().withBomPreserveOrder(false).apply(doc);

        List<Element> deps = doc.getRootElement()
                .getChild("dependencyManagement", ns)
                .getChild("dependencies", ns)
                .getChildren();
        assertThat(deps).extracting(e -> e.getChildText("groupId", ns)).containsExactly("A.bom", "M.bom", "Z.regular");
    }

    @Test
    void applyShouldPreserveBomOrderWhenConfigured() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencyManagement><dependencies>"
                + "    <dependency><groupId>Z.regular</groupId>"
                + "<artifactId>A</artifactId></dependency>"
                + "    <dependency><groupId>M.bom</groupId>"
                + "<artifactId>B</artifactId><type>pom</type>"
                + "<scope>import</scope></dependency>"
                + "    <dependency><groupId>A.bom</groupId>"
                + "<artifactId>C</artifactId><type>pom</type>"
                + "<scope>import</scope></dependency>"
                + "  </dependencies></dependencyManagement>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        new DependencyOrderRule().apply(doc);

        List<Element> deps = doc.getRootElement()
                .getChild("dependencyManagement", ns)
                .getChild("dependencies", ns)
                .getChildren();
        assertThat(deps).extracting(e -> e.getChildText("groupId", ns)).containsExactly("M.bom", "A.bom", "Z.regular");
    }

    @Test
    void bomFirstShouldNotAffectRegularDependencies() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencies>"
                + "    <dependency><groupId>Z</groupId><artifactId>Z</artifactId>"
                + "<type>pom</type><scope>import</scope></dependency>"
                + "    <dependency><groupId>A</groupId>"
                + "<artifactId>A</artifactId></dependency>"
                + "  </dependencies>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        // bomFirst is true but this is <dependencies>, not <dependencyManagement>
        new DependencyOrderRule().apply(doc);

        List<Element> deps = doc.getRootElement().getChild("dependencies", ns).getChildren();
        // Sorted purely by sortBy fields, BOM status ignored
        assertThat(deps).extracting(e -> e.getChildText("groupId", ns)).containsExactly("A", "Z");
    }

    @Test
    void analyzeAfterApplyShouldReturnEmpty() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencies>"
                + "    <dependency><groupId>Z</groupId>"
                + "<artifactId>Z</artifactId></dependency>"
                + "    <dependency><groupId>A</groupId>"
                + "<artifactId>A</artifactId></dependency>"
                + "  </dependencies>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        DependencyOrderRule rule = new DependencyOrderRule();
        rule.apply(doc);

        assertThat(rule.analyze(doc)).isEmpty();
    }

    @Test
    void applyShouldBeIdempotent() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                + "  <dependencies>"
                + "    <dependency><groupId>Z</groupId>"
                + "<artifactId>Z</artifactId></dependency>"
                + "    <dependency><groupId>A</groupId>"
                + "<artifactId>A</artifactId></dependency>"
                + "  </dependencies>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        DependencyOrderRule rule = new DependencyOrderRule();
        rule.apply(doc);
        byte[] firstPass = pomSerde.serialize(doc);

        rule.apply(doc);
        byte[] secondPass = pomSerde.serialize(doc);

        assertThat(secondPass).isEqualTo(firstPass);
    }
}
